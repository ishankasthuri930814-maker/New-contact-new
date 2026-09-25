package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.UserProfile
import com.example.data.model.DirectChatMessage
import com.example.data.model.DeviceContactMatch
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
                    val phone = fbUser.phoneNumber ?: ""
                    val name = fbUser.displayName?.takeIf { it.isNotBlank() }
                        ?: phone.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "User" }
                    externalScope.launch {
                        getOrCreateProfileForEmail(email, name, fbUser.uid, phone)
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
            profilePhotoUrl = prefs.getString("profilePhotoUrl", "") ?: "",
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
            putString("profilePhotoUrl", profile.profilePhotoUrl)
            putString("customAlias", profile.customAlias)
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
     * User custom alias (local contact renaming) support.
     */
    fun setUserAlias(userKey: String, customAlias: String) {
        val cleanKey = DirectChatMessage.normalizeUserKey(userKey)
        val aliasPrefs = context.getSharedPreferences("user_aliases_prefs", Context.MODE_PRIVATE)
        if (customAlias.isBlank()) {
            aliasPrefs.edit().remove(cleanKey).apply()
        } else {
            aliasPrefs.edit().putString(cleanKey, customAlias.trim()).apply()
        }
    }

    fun getUserAlias(userKey: String): String? {
        val cleanKey = DirectChatMessage.normalizeUserKey(userKey)
        val aliasPrefs = context.getSharedPreferences("user_aliases_prefs", Context.MODE_PRIVATE)
        return aliasPrefs.getString(cleanKey, null)
    }

    fun getAllUserAliases(): Map<String, String> {
        val aliasPrefs = context.getSharedPreferences("user_aliases_prefs", Context.MODE_PRIVATE)
        val result = mutableMapOf<String, String>()
        for ((k, v) in aliasPrefs.all) {
            if (v is String && v.isNotBlank()) {
                result[k] = v
            }
        }
        return result
    }

    /**
     * Hide / Remove user from local directory list.
     */
    fun hideUser(userKey: String) {
        val cleanKey = DirectChatMessage.normalizeUserKey(userKey)
        val hiddenPrefs = context.getSharedPreferences("hidden_users_prefs", Context.MODE_PRIVATE)
        hiddenPrefs.edit().putBoolean(cleanKey, true).apply()
    }

    fun unhideUser(userKey: String) {
        val cleanKey = DirectChatMessage.normalizeUserKey(userKey)
        val hiddenPrefs = context.getSharedPreferences("hidden_users_prefs", Context.MODE_PRIVATE)
        hiddenPrefs.edit().remove(cleanKey).apply()
    }

    fun getHiddenUsers(): Set<String> {
        val hiddenPrefs = context.getSharedPreferences("hidden_users_prefs", Context.MODE_PRIVATE)
        return hiddenPrefs.all.keys
    }

    /**
     * Read device phone contacts and match against registered app users.
     */
    fun readAndMatchDeviceContacts(context: Context): List<DeviceContactMatch> {
        val matches = mutableListOf<DeviceContactMatch>()
        val registered = _allRegisteredUsers.value

        try {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return emptyList<DeviceContactMatch>()

            val cursor = context.contentResolver.query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            val seenNumbers = mutableSetOf<String>()

            cursor?.use {
                val nameIdx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val rawName = if (nameIdx >= 0) it.getString(nameIdx) ?: "Contact" else "Contact"
                    val rawNum = if (numIdx >= 0) it.getString(numIdx) ?: "" else ""
                    val cleanNum = rawNum.replace(Regex("[^0-9+]"), "").trim()

                    if (cleanNum.isNotBlank() && !seenNumbers.contains(cleanNum)) {
                        seenNumbers.add(cleanNum)
                        val last9 = cleanNum.takeLast(9)

                        // Check if registered in app
                        val matchedUser = registered.firstOrNull { user ->
                            val userPhone = user.phoneNumber.replace(Regex("[^0-9+]"), "").trim()
                            userPhone.isNotBlank() && (userPhone == cleanNum || (last9.length == 9 && userPhone.endsWith(last9)))
                        }

                        matches.add(
                            DeviceContactMatch(
                                name = rawName,
                                phoneNumber = cleanNum,
                                isAppUser = matchedUser != null,
                                registeredProfile = matchedUser
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("UserProfileRepo", "Error reading device contacts", e)
        }

        return matches
    }

    /**
     * Converts a selected profile image to an optimized Base64 JPEG string and syncs with Firebase.
     */
    suspend fun uploadProfilePhotoBase64(photoBase64: String): Boolean = withContext(Dispatchers.IO) {
        val current = _currentProfile.value
        val updated = current.copy(profilePhotoUrl = photoBase64)
        saveProfileToCache(updated)

        val docId = when {
            updated.email.isNotBlank() -> getDocIdForEmail(updated.email)
            updated.userId.isNotBlank() -> DirectChatMessage.normalizeUserKey(updated.userId)
            updated.phoneNumber.isNotBlank() -> DirectChatMessage.normalizeUserKey(updated.phoneNumber)
            else -> "user_${System.currentTimeMillis()}"
        }

        try {
            firestore?.collection("users")?.document(docId)?.update("profilePhotoUrl", photoBase64)?.await()
            Log.d("UserProfileRepo", "Uploaded profile photo to Firestore for $docId")
            fetchAllRegisteredUsers()
            return@withContext true
        } catch (e: Exception) {
            Log.e("UserProfileRepo", "Failed to upload photo to Firestore", e)
            return@withContext false
        }
    }

    /**
     * Initializes or loads the profile for the authenticated user.
     */
    suspend fun getOrCreateProfileForEmail(
        email: String,
        fallbackName: String? = null,
        userId: String? = null,
        phone: String? = null
    ): UserProfile = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanUserId = userId?.trim() ?: ""
        val cleanPhone = phone?.trim() ?: ""

        val docId = when {
            cleanEmail.isNotBlank() -> getDocIdForEmail(cleanEmail)
            cleanUserId.isNotBlank() -> DirectChatMessage.normalizeUserKey(cleanUserId)
            cleanPhone.isNotBlank() -> DirectChatMessage.normalizeUserKey(cleanPhone)
            else -> "user_${System.currentTimeMillis()}"
        }

        val defaultName = fallbackName?.ifBlank { null }
            ?: cleanPhone.takeIf { it.isNotBlank() }
            ?: cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "User" }

        var profile = loadCachedProfile()
        val updatedProfile = profile.copy(
            userId = cleanUserId.ifBlank { profile.userId.ifBlank { docId } },
            email = cleanEmail.ifBlank { profile.email },
            displayName = profile.displayName.takeIf { it.isNotBlank() && !it.equals("Citizen", ignoreCase = true) } ?: defaultName,
            phoneNumber = cleanPhone.ifBlank { profile.phoneNumber }
        )
        saveProfileToCache(updatedProfile)
        profile = updatedProfile

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
                        Log.d("UserProfileRepo", "Loaded existing profile for $docId from Firestore")
                    }
                } else {
                    docRef.set(profile.toMap(), SetOptions.merge()).await()
                    Log.d("UserProfileRepo", "Created new profile document for $docId in Firestore")
                }

                // Also fetch all registered users for 1-on-1 directory
                fetchAllRegisteredUsers()
            }
        } catch (t: Throwable) {
            Log.w("UserProfileRepo", "Firestore sync failed, using local profile for $docId", t)
        }

        // Start background incoming call & message monitoring service
        com.example.service.CallBackgroundService.start(context, profile)

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
        allowDirectCalls: Boolean,
        profilePhotoUrl: String? = null
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
            allowDirectCalls = allowDirectCalls,
            profilePhotoUrl = profilePhotoUrl ?: current.profilePhotoUrl
        )
        saveProfileToCache(updated)

        val docId = when {
            updated.email.isNotBlank() -> getDocIdForEmail(updated.email)
            updated.userId.isNotBlank() -> DirectChatMessage.normalizeUserKey(updated.userId)
            updated.phoneNumber.isNotBlank() -> DirectChatMessage.normalizeUserKey(updated.phoneNumber)
            else -> "user_${System.currentTimeMillis()}"
        }

        try {
            firestore?.collection("users")?.document(docId)?.set(updated.toMap(), SetOptions.merge())?.await()
            Log.d("UserProfileRepo", "Successfully updated profile in Firestore for $docId")
            fetchAllRegisteredUsers()
            com.example.service.CallBackgroundService.start(context, updated)
            return@withContext true
        } catch (t: Throwable) {
            Log.w("UserProfileRepo", "Could not sync profile to Firestore", t)
        }
        return@withContext true
    }
}
