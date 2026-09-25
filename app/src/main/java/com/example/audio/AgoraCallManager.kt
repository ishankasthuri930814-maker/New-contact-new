package com.example.audio

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.CallSession
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.abs

class AgoraCallManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "AgoraCallManager"
        private const val PREFS_NAME = "agora_voice_prefs"
        private const val KEY_APP_ID = "agora_app_id"
        const val DEFAULT_AGORA_APP_ID = "2d581abb45724140b8ad89b56d23fe2d"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _agoraAppId = MutableStateFlow(loadCachedAppId())
    val agoraAppId: StateFlow<String> = _agoraAppId.asStateFlow()

    private val _isAgoraActive = MutableStateFlow(false)
    val isAgoraActive: StateFlow<Boolean> = _isAgoraActive.asStateFlow()

    private val _isAudioConnected = MutableStateFlow(false)
    val isAudioConnected: StateFlow<Boolean> = _isAudioConnected.asStateFlow()

    private val _micAmplitude = MutableStateFlow(0f)
    val micAmplitude: StateFlow<Float> = _micAmplitude.asStateFlow()

    private val _speakerAmplitude = MutableStateFlow(0f)
    val speakerAmplitude: StateFlow<Float> = _speakerAmplitude.asStateFlow()

    var onFallbackNeeded: (() -> Unit)? = null

    private var rtcEngine: RtcEngine? = null
    private var currentChannel: String? = null
    private var isMutedLocal = false
    private var isSpeakerLocal = true

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    init {
        if (!prefs.contains(KEY_APP_ID)) {
            prefs.edit().putString(KEY_APP_ID, DEFAULT_AGORA_APP_ID).apply()
        }
        listenForRemoteAgoraConfig()
    }

    private fun loadCachedAppId(): String {
        val stored = prefs.getString(KEY_APP_ID, DEFAULT_AGORA_APP_ID)?.trim()
        return if (stored.isNullOrBlank()) DEFAULT_AGORA_APP_ID else stored
    }

    fun isConfigured(): Boolean {
        return _agoraAppId.value.isNotBlank()
    }

    fun saveAppId(appId: String, syncToCloud: Boolean = true) {
        val clean = appId.trim()
        prefs.edit().putString(KEY_APP_ID, clean).apply()
        _agoraAppId.value = clean
        Log.d(TAG, "Agora App ID saved locally: ${clean.take(6)}...")

        if (syncToCloud && clean.isNotBlank()) {
            coroutineScope.launch {
                try {
                    firestore?.collection("app_settings")
                        ?.document("voice_config")
                        ?.set(
                            mapOf(
                                "agoraAppId" to clean,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )?.await()
                    Log.d(TAG, "Agora App ID synced to Firestore for all users")
                } catch (t: Throwable) {
                    Log.w(TAG, "Failed to sync Agora App ID to Firestore", t)
                }
            }
        }
    }

    private fun listenForRemoteAgoraConfig() {
        try {
            val db = firestore ?: return
            db.collection("app_settings").document("voice_config")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val remoteAppId = snapshot.getString("agoraAppId")?.trim() ?: ""
                    if (remoteAppId.isNotBlank() && remoteAppId != _agoraAppId.value) {
                        prefs.edit().putString(KEY_APP_ID, remoteAppId).apply()
                        _agoraAppId.value = remoteAppId
                        Log.d(TAG, "Updated Agora App ID from Firestore: ${remoteAppId.take(6)}...")
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error attaching Firestore voice config listener", e)
        }
    }

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "Successfully joined Agora voice channel: $channel (uid: $uid)")
            _isAudioConnected.value = true
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "Remote peer joined Agora voice call: $uid")
            _isAudioConnected.value = true
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "Remote peer left Agora call: $uid (reason: $reason)")
        }

        override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
            if (speakers == null) return
            var localVol = 0
            var remoteVol = 0

            for (speaker in speakers) {
                if (speaker.uid == 0) {
                    localVol = speaker.volume
                } else {
                    remoteVol = maxOf(remoteVol, speaker.volume)
                }
            }

            // Agora volume is 0..255. Normalize to 0f..1f for UI waveform visualizer
            val localNorm = if (!isMutedLocal) (localVol / 255f).coerceIn(0f, 1f) else 0f
            val remoteNorm = (remoteVol / 255f).coerceIn(0f, 1f)

            _micAmplitude.value = localNorm
            _speakerAmplitude.value = remoteNorm
        }

        override fun onError(err: Int) {
            Log.w(TAG, "Agora RTC Engine error: $err")
            if (err == 109 || err == 110 || err == 119 || err == 116 || err == 17) {
                Log.w(TAG, "Agora token or channel error code: $err. Invoking native voice fallback.")
                onFallbackNeeded?.invoke()
            }
        }
    }

    fun joinVoiceCall(
        callSession: CallSession,
        userIdentifier: String,
        amCaller: Boolean
    ): Boolean {
        val appId = _agoraAppId.value
        if (appId.isBlank()) {
            Log.d(TAG, "Agora App ID is not configured, will use native engine")
            return false
        }

        try {
            leaveVoiceCall()

            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = appId
                mEventHandler = rtcEventHandler
                mChannelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            }

            val engine = RtcEngine.create(config)
            rtcEngine = engine

            engine.enableAudio()
            engine.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_GAME_STREAMING)
            engine.enableAudioVolumeIndication(200, 3, true)
            engine.setEnableSpeakerphone(isSpeakerLocal)
            engine.muteLocalAudioStream(isMutedLocal)

            val channelName = callSession.agoraChannel.ifBlank {
                callSession.callId.replace("-", "").take(60).ifBlank { "call_default" }
            }
            val numericUid = abs((userIdentifier.hashCode() and 0x7FFFFFFF) % 1000000) + 1

            val joinRes = engine.joinChannel(null, channelName, "police_call", numericUid)
            if (joinRes == 0) {
                currentChannel = channelName
                _isAgoraActive.value = true
                Log.d(TAG, "Agora joinChannel initiated successfully for $channelName with UID $numericUid")
                coroutineScope.launch {
                    kotlinx.coroutines.delay(4500L)
                    if (!_isAudioConnected.value && _isAgoraActive.value) {
                        Log.i(TAG, "Agora connecting timeout, activating parallel fallback voice stream")
                        onFallbackNeeded?.invoke()
                    }
                }
                return true
            } else {
                Log.w(TAG, "Agora joinChannel returned code: $joinRes")
                onFallbackNeeded?.invoke()
                leaveVoiceCall()
                return false
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception initializing Agora RTC Engine", t)
            onFallbackNeeded?.invoke()
            leaveVoiceCall()
            return false
        }
    }

    fun setMuted(muted: Boolean) {
        isMutedLocal = muted
        rtcEngine?.muteLocalAudioStream(muted)
    }

    fun setSpeakerphone(speaker: Boolean) {
        isSpeakerLocal = speaker
        rtcEngine?.setEnableSpeakerphone(speaker)
    }

    fun leaveVoiceCall() {
        _isAgoraActive.value = false
        _isAudioConnected.value = false
        _micAmplitude.value = 0f
        _speakerAmplitude.value = 0f

        try {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
            currentChannel = null
            Log.d(TAG, "Agora voice call left and engine destroyed")
        } catch (e: Exception) {
            Log.w(TAG, "Error leaving Agora call: ${e.message}")
        }
    }
}
