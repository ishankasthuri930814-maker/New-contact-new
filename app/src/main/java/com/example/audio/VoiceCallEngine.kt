package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Base64
import android.util.Log
import com.example.data.model.CallSession
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import kotlin.math.abs
import kotlin.math.max

class VoiceCallEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "VoiceCallEngine"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val FRAME_SIZE_BYTES = 640 // 20ms of 16kHz 16-bit mono
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var gainControl: AutomaticGainControl? = null

    private var udpSocket: DatagramSocket? = null
    private var localPort: Int = 0
    private var remoteHost: String? = null
    private var remotePort: Int = 0

    private var isRecording = false
    private var isPlaying = false
    private var isMuted = false

    private var recordingJob: Job? = null
    private var playbackJob: Job? = null
    private var cloudRelayJob: Job? = null
    private var cloudRelayListener: ListenerRegistration? = null

    private val _isAudioConnected = MutableStateFlow(false)
    val isAudioConnected: StateFlow<Boolean> = _isAudioConnected.asStateFlow()

    private val _micAmplitude = MutableStateFlow(0f)
    val micAmplitude: StateFlow<Float> = _micAmplitude.asStateFlow()

    private val _speakerAmplitude = MutableStateFlow(0f)
    val speakerAmplitude: StateFlow<Float> = _speakerAmplitude.asStateFlow()

    private var currentCallId: String? = null
    private var isCaller: Boolean = false

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Prepares UDP socket and returns local host/port for exchange via CallSession
     */
    fun prepareEndpoint(): Pair<String, Int> {
        return try {
            if (udpSocket == null || udpSocket?.isClosed == true) {
                udpSocket = DatagramSocket()
                udpSocket?.soTimeout = 1000
            }
            localPort = udpSocket?.localPort ?: 0
            val localIp = getDeviceIpAddress()
            Log.d(TAG, "Local endpoint prepared: $localIp:$localPort")
            Pair(localIp, localPort)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare UDP socket", e)
            Pair("127.0.0.1", 0)
        }
    }

    /**
     * Start two-way voice streaming for the active call
     */
    @SuppressLint("MissingPermission")
    fun startVoice(
        callSession: CallSession,
        amCaller: Boolean
    ) {
        stopVoice()
        currentCallId = callSession.callId
        isCaller = amCaller

        // Configure remote endpoint from CallSession
        if (amCaller) {
            remoteHost = callSession.receiverHost.takeIf { it.isNotBlank() }
            remotePort = callSession.receiverPort
        } else {
            remoteHost = callSession.callerHost.takeIf { it.isNotBlank() }
            remotePort = callSession.callerPort
        }

        Log.d(TAG, "Starting voice streaming. AmCaller: $amCaller, Remote: $remoteHost:$remotePort")

        // 1. Configure Hardware Audio Mode
        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isSpeakerphoneOn = true
        } catch (e: Exception) {
            Log.w(TAG, "Audio mode setup failed: ${e.message}")
        }

        // 2. Start AudioTrack for incoming voice playback
        initAudioTrack()

        // 3. Start AudioRecord for microphone input
        initAudioRecord()

        _isAudioConnected.value = true

        // 4. Start Network Threads (UDP streaming & Cloud relay fallback)
        startUdpReceiver()
        startAudioRecorder()
        startCloudVoiceRelay(callSession.callId, amCaller)
    }

    @SuppressLint("MissingPermission")
    private fun initAudioRecord() {
        try {
            val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, AUDIO_ENCODING)
            val bufferSize = max(minBuf, FRAME_SIZE_BYTES * 4)

            val record = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                CHANNEL_IN,
                AUDIO_ENCODING,
                bufferSize
            )

            if (record.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord = record

                // Attach Hardware Audio Effects for crystal-clear WhatsApp-quality audio
                val audioSessionId = record.audioSessionId
                if (AcousticEchoCanceler.isAvailable()) {
                    echoCanceler = AcousticEchoCanceler.create(audioSessionId)?.apply {
                        enabled = true
                        Log.d(TAG, "Hardware AcousticEchoCanceler enabled")
                    }
                }
                if (NoiseSuppressor.isAvailable()) {
                    noiseSuppressor = NoiseSuppressor.create(audioSessionId)?.apply {
                        enabled = true
                        Log.d(TAG, "Hardware NoiseSuppressor enabled")
                    }
                }
                if (AutomaticGainControl.isAvailable()) {
                    gainControl = AutomaticGainControl.create(audioSessionId)?.apply {
                        enabled = true
                        Log.d(TAG, "Hardware AutomaticGainControl enabled")
                    }
                }
            } else {
                Log.e(TAG, "AudioRecord failed to initialize")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AudioRecord", e)
        }
    }

    private fun initAudioTrack() {
        try {
            val minBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT, AUDIO_ENCODING)
            val bufferSize = max(minBuf, FRAME_SIZE_BYTES * 4)

            val track = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_OUT)
                    .setEncoding(AUDIO_ENCODING)
                    .build(),
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            if (track.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack = track
                track.play()
                isPlaying = true
                Log.d(TAG, "AudioTrack initialized and playing")
            } else {
                Log.e(TAG, "AudioTrack failed to initialize")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AudioTrack", e)
        }
    }

    private fun startAudioRecorder() {
        val record = audioRecord ?: return
        if (record.state != AudioRecord.STATE_INITIALIZED) return

        try {
            record.startRecording()
            isRecording = true
        } catch (e: Exception) {
            Log.e(TAG, "Cannot start recording", e)
            return
        }

        recordingJob = coroutineScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(FRAME_SIZE_BYTES)
            var remoteAddress: InetAddress? = null

            while (isActive && isRecording) {
                val bytesRead = record.read(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    // Compute amplitude for UI visualizer
                    val amp = computeRmsAmplitude(buffer, bytesRead)
                    _micAmplitude.value = if (!isMuted) amp else 0f

                    // Send audio packets via UDP if target is resolved and not muted
                    if (!isMuted) {
                        try {
                            if (remoteAddress == null && !remoteHost.isNullOrBlank() && remotePort > 0) {
                                remoteAddress = InetAddress.getByName(remoteHost)
                            }

                            val socket = udpSocket
                            if (socket != null && !socket.isClosed && remoteAddress != null && remotePort > 0) {
                                val packet = DatagramPacket(buffer, bytesRead, remoteAddress, remotePort)
                                socket.send(packet)
                            }
                        } catch (e: Exception) {
                            // UDP send transient error
                        }
                    }
                }
            }
        }
    }

    private fun startUdpReceiver() {
        val socket = udpSocket ?: return
        val track = audioTrack ?: return

        playbackJob = coroutineScope.launch(Dispatchers.IO) {
            val receiveBuffer = ByteArray(FRAME_SIZE_BYTES * 2)
            val packet = DatagramPacket(receiveBuffer, receiveBuffer.size)

            while (isActive && isPlaying) {
                try {
                    socket.receive(packet)
                    val len = packet.length
                    if (len > 0) {
                        track.write(packet.data, 0, len)
                        val amp = computeRmsAmplitude(packet.data, len)
                        _speakerAmplitude.value = amp
                        _isAudioConnected.value = true
                    }
                } catch (e: Exception) {
                    // Timeout or socket close is expected when idle
                }
            }
        }
    }

    /**
     * Resilient Cloud Audio Relay using Firestore subcollection:
     * Guarantees audio transmission even if UDP hole-punching is hindered by strict carrier CGNAT
     */
    private fun startCloudVoiceRelay(callId: String, amCaller: Boolean) {
        val db = firestore ?: return
        val outgoingChannel = if (amCaller) "caller_stream" else "receiver_stream"
        val incomingChannel = if (amCaller) "receiver_stream" else "caller_stream"

        // 1. Listen for incoming voice frames from the other party
        cloudRelayListener?.remove()
        cloudRelayListener = db.collection("active_calls")
            .document(callId)
            .collection("voice_frames")
            .document(incomingChannel)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val base64Data = snapshot.getString("audio") ?: return@addSnapshotListener
                val seq = snapshot.getLong("seq") ?: 0L
                try {
                    val pcmBytes = Base64.decode(base64Data, Base64.NO_WRAP)
                    if (pcmBytes != null && pcmBytes.isNotEmpty()) {
                        audioTrack?.write(pcmBytes, 0, pcmBytes.size)
                        _speakerAmplitude.value = computeRmsAmplitude(pcmBytes, pcmBytes.size)
                        _isAudioConnected.value = true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Cloud audio relay playback error", e)
                }
            }

        // 2. Transmit compressed audio packets to Firestore if needed as fallback (periodically 400ms chunks)
        cloudRelayJob = coroutineScope.launch(Dispatchers.IO) {
            val record = audioRecord ?: return@launch
            val chunkBuffer = ByteArray(SAMPLE_RATE / 2) // ~250ms of audio
            var seq = 0L

            while (isActive && isRecording) {
                if (isMuted) {
                    kotlinx.coroutines.delay(200)
                    continue
                }

                val read = record.read(chunkBuffer, 0, chunkBuffer.size)
                if (read > 0 && isRecording) {
                    val encoded = Base64.encodeToString(chunkBuffer, 0, read, Base64.NO_WRAP)
                    try {
                        seq++
                        db.collection("active_calls")
                            .document(callId)
                            .collection("voice_frames")
                            .document(outgoingChannel)
                            .set(
                                mapOf(
                                    "audio" to encoded,
                                    "seq" to seq,
                                    "ts" to System.currentTimeMillis()
                                ),
                                SetOptions.merge()
                            )
                    } catch (t: Throwable) {
                        // Suppress transient Firestore write error
                    }
                }
                kotlinx.coroutines.delay(250)
            }
        }
    }

    fun updateRemoteEndpoint(host: String, port: Int) {
        if (host.isNotBlank() && port > 0) {
            remoteHost = host
            remotePort = port
            Log.d(TAG, "Updated remote voice endpoint: $host:$port")
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        try {
            audioManager?.isMicrophoneMute = muted
        } catch (e: Exception) {
            Log.w(TAG, "Failed to toggle mic mute: ${e.message}")
        }
    }

    fun setSpeakerOn(speaker: Boolean) {
        try {
            audioManager?.isSpeakerphoneOn = speaker
        } catch (e: Exception) {
            Log.w(TAG, "Failed to toggle speaker: ${e.message}")
        }
    }

    fun stopVoice() {
        isRecording = false
        isPlaying = false
        _isAudioConnected.value = false
        _micAmplitude.value = 0f
        _speakerAmplitude.value = 0f

        recordingJob?.cancel()
        recordingJob = null
        playbackJob?.cancel()
        playbackJob = null
        cloudRelayJob?.cancel()
        cloudRelayJob = null

        cloudRelayListener?.remove()
        cloudRelayListener = null

        try {
            echoCanceler?.release()
            echoCanceler = null
            noiseSuppressor?.release()
            noiseSuppressor = null
            gainControl?.release()
            gainControl = null
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing audio effects", e)
        }

        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
            audioRecord = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioRecord", e)
        }

        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
            audioTrack = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioTrack", e)
        }

        try {
            udpSocket?.close()
            udpSocket = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing UDP socket", e)
        }

        try {
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isMicrophoneMute = false
        } catch (e: Exception) {
            Log.w(TAG, "Error restoring AudioManager mode", e)
        }

        Log.d(TAG, "Voice engine stopped and hardware released")
    }

    private fun computeRmsAmplitude(buffer: ByteArray, bytesRead: Int): Float {
        var sum = 0.0
        val numSamples = bytesRead / 2
        if (numSamples == 0) return 0f

        for (i in 0 until bytesRead - 1 step 2) {
            val sample = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)
            sum += (sample * sample).toDouble()
        }
        val rms = Math.sqrt(sum / numSamples)
        // Normalize 0..32767 to 0f..1f
        return (rms / 32767.0).toFloat().coerceIn(0f, 1f)
    }

    private fun getDeviceIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val hostAddress = addr.hostAddress ?: continue
                        val isIPv4 = hostAddress.indexOf(':') < 0
                        if (isIPv4) return hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            Log.w(TAG, "IP lookup error", ex)
        }
        return "127.0.0.1"
    }
}
