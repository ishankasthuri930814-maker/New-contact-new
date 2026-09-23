package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserProfileRepository(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)

    private val _currentProfile = MutableStateFlow(UserProfile())
    val currentProfile: StateFlow<UserProfile> = _currentProfile.asStateFlow()

    private val _allRegisteredUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allRegisteredUsers: StateFlow<List<UserProfile>> = _allRegisteredUsers.asStateFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (t: Throwable) {
            Log.w("UserProfileRepo", "Firestore not initialized", t)
            null
        }
    }

    private var usersListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        loadCachedProfile()
        startRealtimeUsersListener()
        setupAuthListener()
    }

    private fun setupAuthListener() {
        try {
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                val fbUser = auth.currentUser
                if (fbUser != null) {
                    val email = fbUser.email ?: ""
                    val name = fbUser.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    externalScope.launch {
                        getOrCreateProfileForEmail(email, name, fbUser.uid)
                    }
                }
            }
        } catch (t: Throwable) {
            Log.w("UserProfileRepo", "Could not attach FirebaseAuth listener", t)
        }
    }

    fun startRealtimeUsersListener() {
        try {
            val db = firestore ?: return
            usersListener?.remove()
            usersListener = db.collection("users").limit(200).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("UserProfileRepo", "Real-time users listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        UserProfile.fromMap(data)
                    }
                    _allRegisteredUsers.value = list
                    Log.d("UserProfileRepo", "Realtime updated registered users count: ${list.size}")
                }
            }
        } catch (t: Throwable) {
            Log.w("UserProfileRepo", "Could not start realtime users listener", t)
        }
    }

    private fun getDocIdForEmail(email: String): String {
        return email.trim().lowercase().replace(".", "_").replace("@", "_at_")
    }

    fun loadCachedProfile(): UserProfile {
        var email = prefs.getString("email", "") ?: ""
        var userId = prefs.getString("userId", "") ?: ""
        var displayName = prefs.getString("displayName", "") ?: ""

        // Safe Firebase Auth user fallback
        try {
            val fbUser = FirebaseAuth.getInstance().currentUser
            if (fbUser != null) {
                if (email.isBlank()) {
                    email = fbUser.email ?: ""
                }
                if (userId.isBlank()) {
                    userId = fbUser.uid
                }
                if (displayName.isBlank() || displayName.equals("Citizen", ignoreCase = true)) {
                    displayName = fbUser.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                }
            }
        } catch (t: Throwable) {
            Log.w("UserProfileRepo", "FirebaseAuth check in loadCachedProfile skipped", t)
        }

        // Safe auth prefs fallback if email still blank
        if (email.isBlank()) {
            try {
                val authPrefs = context.getSharedPreferences("police_auth_prefs", Context.MODE_PRIVATE)
                val savedAuthUser = authPrefs.getString("saved_username", "") ?: ""
                if (savedAuthUser.isNotBlank() && savedAuthUser.contains("@")) {
                    email = savedAuthUser
                } else if (savedAuthUser.isNotBlank() && userId.isBlank()) {
                    userId = savedAuthUser
                }
            } catch (e: Exception) {
                Log.w("UserProfileRepo", "Error reading auth prefs fallback", e)
            }
        }

        if (displayName.isBlank() || displayName.equals("Citizen", ignoreCase = true)) {
            displayName = if (email.isNotBlank()) {
                email.substringBefore("@").replaceFirstChar { it.uppercase() }
            } else {
                "User_${(1000..9999).random()}"
            }
        }

        val profile = UserProfile(
            userId = userId.ifBlank { getDocIdForEmail(email) },
            email = email,
            displayName = displayName,
            phoneNumber = prefs.getString("phoneNumber", "") ?: "",
            district = prefs.getString("district", "Colombo") ?: "Colombo",
            emergencyNote = prefs.getString("emergencyNote", "") ?: "",
            avatarIndex = prefs.getInt("avatarIndex", 0),
            badge = prefs.getString("badge", "සත්‍යාපිත සාමාජික (Verified Citizen)") ?: "සත්‍යාපිත සාමාජික (Verified Citizen)",
            joinedTimestamp = prefs.getLong("joinedTimestamp", System.currentTimeMillis()),
            isVerified = prefs.getBoolean("isVerified", true),
            isPhonePublic = prefs.getBoolean("isPhonePublic", false),
            allowDirectMessages = prefs.getBoolean("allowDirectMessages", true),
            allowDirectCalls = prefs.getBoolean("allowDirectCalls", true)
        )
        _currentProfile.value = profile
        return profile
    }

    private fun saveProfileToCache(profile: UserProfile) {
        prefs.edit().apply {
            putString("userId", profile.userId)
            putString("email", profile.email)
            putString("displayName", profile.displayName)
            putString("phoneNumber", profile.phoneNumber)
            putString("district", profile.district)
            putString("emergencyNote", profile.emergencyNote)
            putInt("avatarIndex", profile.avatarIndex)
            putString("badge", profile.badge)
            putLong("joinedTimestamp", profile.joinedTimestamp)
            putBoolean("isVerified", profile.isVerified)
            putBoolean("isPhonePublic", profile.isPhonePublic)
            putBoolean("allowDirectMessages", profile.allowDirectMessages)
            putBoolean("allowDirectCalls", profile.allowDirectCalls)
            apply()
        }
        _currentProfile.value = profile
    }

    /**
     * Initializes or loads the profile for the authenticated user.
     */
    suspend fun getOrCreateProfileForEmail(email: String, fallbackName: String? = null, userId: String? = null): UserProfile = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            return@withContext loadCachedProfile()
        }

        val docId = getDocIdForEmail(cleanEmail)
        val defaultName = fallbackName?.ifBlank { null } ?: cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

        var profile = loadCachedProfile()
        if (profile.email != cleanEmail) {
            profile = profile.copy(
                userId = userId ?: docId,
                email = cleanEmail,
                displayName = defaultName
            )
            saveProfileToCache(profile)
        }

        try {
            val db = firestore
            if (db != null) {
                val docRef = db.collection("users").document(docId)
                val snapshot = docRef.get().await()

                if (snapshot.exists()) {
                    val data = snapshot.data
                    if (data != null) {
                        profile = UserProfile.fromMap(data)
                        saveProfileToCache(profile)
                        Log.d("UserProfileRepo", "Loaded existing profile for $cleanEmail from Firestore")
                    }
                } else {
                    val newProfile = profile.copy(
                        userId = userId ?: docId,
                        email = cleanEmail,
                        displayName = defaultName
                    )
                    docRef.set(newProfile.toMap(), SetOptions.merge()).await()
                    saveProfileToCache(newProfile)
                    profile = newProfile
                    Log.d("UserProfileRepo", "Created new single profile document for $cleanEmail in Firestore")
                }

                // Also fetch all registered users for 1-on-1 directory
                fetchAllRegisteredUsers()
            }
        } catch (t: Throwable) {
            Log.w("UserProfileRepo", "Firestore sync failed, using local profile for $cleanEmail", t)
        }

        return@withContext profile
    }

    suspend fun fetchAllRegisteredUsers() = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            val snapshot = db.collection("users").limit(100).get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                UserProfile.fromMap(data)
            }
            _allRegisteredUsers.value = list
        } catch (t: Throwable) {
            Log.w("UserProfileRepo", "Failed to fetch registered users list", t)
        }
    }

    /**
     * Updates profile in local cache and Firestore.
     */
    suspend fun updateProfile(
        displayName: String,
        phoneNumber: String,
        district: String,
        emergencyNote: String,
        avatarIndex: Int,
        isPhonePublic: Boolean,
        allowDirectMessages: Boolean,
        allowDirectCalls: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val current = _currentProfile.value
        val updated = current.copy(
            displayName = displayName.trim(),
            phoneNumber = phoneNumber.trim(),
            district = district.trim(),
            emergencyNote = emergencyNote.trim(),
            avatarIndex = avatarIndex,
            isPhonePublic = isPhonePublic,
            allowDirectMessages = allowDirectMessages,
            allowDirectCalls = allowDirectCalls
        )
        saveProfileToCache(updated)

        if (updated.email.isNotBlank()) {
            val docId = getDocIdForEmail(updated.email)
            try {
                firestore?.collection("users")?.document(docId)?.set(updated.toMap(), SetOptions.merge())?.await()
                Log.d("UserProfileRepo", "Successfully updated profile in Firestore for ${updated.email}")
                fetchAllRegisteredUsers()
                return@withContext true
            } catch (t: Throwable) {
                Log.w("UserProfileRepo", "Could not sync profile to Firestore", t)
            }
        }
        return@withContext true
    }
}
