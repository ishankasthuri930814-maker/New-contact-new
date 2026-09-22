package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object InvalidCredentials : AuthResult()
}

sealed class AutoLoginResult {
    object Idle : AutoLoginResult()
    object Checking : AutoLoginResult()
    data class Success(val username: String) : AutoLoginResult()
    data class CredentialsRevoked(val reason: String) : AutoLoginResult()
    object NotLoggedIn : AutoLoginResult()
}

sealed class UserVerificationResult {
    object Active : UserVerificationResult()
    data class Blocked(val reason: String) : UserVerificationResult()
    object NotApplicable : UserVerificationResult()
}

class AuthRepository(
    private val context: Context? = null,
    private val injectedFirebaseAuth: FirebaseAuth? = null
) {

    constructor(firebaseAuth: FirebaseAuth) : this(context = null, injectedFirebaseAuth = firebaseAuth)

    private fun getOrInitFirebaseApp(): FirebaseApp? {
        return try {
            FirebaseApp.getInstance()
        } catch (e: Throwable) {
            try {
                val ctx = context
                if (ctx != null) {
                    val apps = FirebaseApp.getApps(ctx)
                    if (apps.isNotEmpty()) {
                        apps[0]
                    } else {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:880156476376:android:e14409f7b16d90abf3a8ad")
                            .setApiKey("AIzaSyDSJ2osq4TdvY4CAdUeoQP7miO8i4R4ZFk")
                            .setProjectId("policecontact")
                            .setStorageBucket("policecontact.firebasestorage.app")
                            .setGcmSenderId("880156476376")
                            .build()
                        FirebaseApp.initializeApp(ctx, options)
                    }
                } else null
            } catch (t: Throwable) {
                Log.e("AuthRepo", "Explicit FirebaseApp initialization failed", t)
                null
            }
        }
    }

    private val firebaseAuth: FirebaseAuth?
        get() {
            if (injectedFirebaseAuth != null) return injectedFirebaseAuth
            return try {
                FirebaseAuth.getInstance()
            } catch (t: Throwable) {
                try {
                    val app = getOrInitFirebaseApp()
                    if (app != null) {
                        FirebaseAuth.getInstance(app)
                    } else null
                } catch (t2: Throwable) {
                    Log.e("AuthRepo", "Failed to get FirebaseAuth instance", t2)
                    null
                }
            }
        }

    val currentUser: FirebaseUser? get() = try { firebaseAuth?.currentUser } catch (t: Throwable) { null }

    // Email සහ Password මගින් නව පරිශීලකයෙකු ලියාපදිංචි කිරීම (Firebase Auth + Local Fallback)
    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val auth = firebaseAuth

        if (auth != null) {
            return try {
                val result = auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                val user = result.user
                if (user?.email != null) {
                    saveCredentials(user.email!!, trimmedPass)
                }
                Result.success(user)
            } catch (e: Exception) {
                Log.e("AuthRepo", "Firebase createUserWithEmailAndPassword error", e)
                val friendlyError = mapFirebaseException(e)
                if (e is FirebaseAuthUserCollisionException ||
                    e is FirebaseAuthWeakPasswordException ||
                    e is FirebaseAuthInvalidCredentialsException) {
                    Result.failure(Exception(friendlyError))
                } else {
                    // If Firebase service is disabled, blocked or unavailable, fall back to local credentials
                    Log.w("AuthRepo", "Falling back to local registration: ${e.message}")
                    saveCredentials(trimmedEmail, trimmedPass)
                    Result.success(null)
                }
            }
        } else {
            // Local fallback when Firebase is not active on device
            saveCredentials(trimmedEmail, trimmedPass)
            return Result.success(null)
        }
    }

    // Email සහ Password මගින් ඇතුළු වීම (Firebase Auth + Local Fallback)
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val auth = firebaseAuth

        if (auth != null) {
            return try {
                val result = auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                val user = result.user
                if (user?.email != null) {
                    saveCredentials(user.email!!, trimmedPass)
                }
                Result.success(user)
            } catch (e: Exception) {
                Log.e("AuthRepo", "Firebase signInWithEmailAndPassword error", e)
                val savedUser = currentUsername
                val savedPass = currentPassword
                if (savedUser != null && savedPass != null &&
                    savedUser.equals(trimmedEmail, ignoreCase = true) && savedPass == trimmedPass) {
                    saveCredentials(trimmedEmail, trimmedPass)
                    return Result.success(null)
                }
                Result.failure(Exception(mapFirebaseException(e)))
            }
        } else {
            val savedUser = currentUsername
            val savedPass = currentPassword
            return if (savedUser != null && savedPass != null &&
                savedUser.equals(trimmedEmail, ignoreCase = true) && savedPass == trimmedPass) {
                saveCredentials(trimmedEmail, trimmedPass)
                Result.success(null)
            } else {
                Result.failure(Exception("ඊමේල් ලිපිනය හෝ මුරපදය වැරදියි (Invalid Email or Password)"))
            }
        }
    }

    // Google මගින් ඇතුළු වීම / Sign Up වීම (Firebase Auth Google Credential)
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Google සත්‍යාපනය සඳහා සේවාව සූදානම් නැත"))
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user?.email != null) {
                saveCredentials(user.email!!, "GOOGLE_AUTH")
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Google මගින් ඇතුළු වීම (Firebase OAuthProvider Fallback)
    suspend fun signInWithGoogleProvider(activity: Activity): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Google සත්‍යාපනය සඳහා සේවාව සූදානම් නැත"))
        return try {
            val provider = OAuthProvider.newBuilder("google.com")
            provider.addCustomParameter("prompt", "select_account")
            val scopes = listOf("profile", "email")
            provider.setScopes(scopes)

            val pendingResultTask = auth.pendingAuthResult
            val result = if (pendingResultTask != null) {
                pendingResultTask.await()
            } else {
                auth.startActivityForSignInWithProvider(activity, provider.build()).await()
            }
            val user = result.user
            if (user != null) {
                val emailOrName = user.email ?: user.displayName ?: "Google User"
                saveCredentials(emailOrName, "GOOGLE_AUTH")
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Firebase Google OAuthProvider sign-in failed", e)
            Result.failure(e)
        }
    }

    // Facebook මගින් ඇතුළු වීම / Sign In (Firebase OAuthProvider)
    suspend fun signInWithFacebook(activity: Activity): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Facebook සත්‍යාපනය සඳහා සේවාව සූදානම් නැත"))
        return try {
            val provider = OAuthProvider.newBuilder("facebook.com")
            provider.addCustomParameter("display", "touch")
            val scopes = listOf("email", "public_profile")
            provider.setScopes(scopes)

            val pendingResultTask = auth.pendingAuthResult
            val result = if (pendingResultTask != null) {
                pendingResultTask.await()
            } else {
                auth.startActivityForSignInWithProvider(activity, provider.build()).await()
            }
            val user = result.user
            if (user != null) {
                val emailOrName = user.email ?: user.displayName ?: "Facebook User"
                saveCredentials(emailOrName, "FACEBOOK_AUTH")
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Facebook sign-in failed", e)
            Result.failure(e)
        }
    }

    // Facebook Access Token මගින් ඇතුළු වීම (Facebook Android SDK Credential)
    suspend fun signInWithFacebookToken(accessToken: String): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Facebook සත්‍යාපනය සඳහා සේවාව සූදානම් නැත"))
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                val emailOrName = user.email ?: user.displayName ?: "Facebook User"
                saveCredentials(emailOrName, "FACEBOOK_AUTH")
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Firebase Facebook sign-in with credential failed", e)
            Result.failure(e)
        }
    }

    // Sign Out වීම
    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e("AuthRepo", "Firebase signOut error", e)
        }
        logout()
    }

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("police_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SAVED_USERNAME = "saved_username"
        private const val KEY_SAVED_PASSWORD = "saved_password"
    }

    val isUserLoggedIn: Boolean
        get() = (prefs?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false) || (currentUser != null)

    val currentUsername: String?
        get() = currentUser?.email ?: prefs?.getString(KEY_SAVED_USERNAME, null)

    val currentPassword: String?
        get() = prefs?.getString(KEY_SAVED_PASSWORD, null)

    // සත්‍යාපනය සිදු කිරීම
    suspend fun login(usernameInput: String, passwordInput: String): AuthResult = withContext(Dispatchers.IO) {
        val uInput = usernameInput.trim()
        val pInput = passwordInput.trim()

        if (uInput.isBlank()) {
            return@withContext AuthResult.Error("කරුණාකර ඊමේල් ලිපිනය ඇතුළත් කරන්න (Enter Email)")
        }
        if (pInput.isBlank()) {
            return@withContext AuthResult.Error("කරුණාකර මුරපදය ඇතුළත් කරන්න (Enter Password)")
        }

        val auth = firebaseAuth
        if (auth != null) {
            try {
                val result = auth.signInWithEmailAndPassword(uInput, pInput).await()
                val user = result.user
                val email = user?.email ?: uInput
                saveCredentials(email, pInput)
                AuthResult.Success(email)
            } catch (e: Exception) {
                Log.e("AuthRepo", "Firebase login error", e)
                val savedUser = currentUsername
                val savedPass = currentPassword
                if (savedUser != null && savedPass != null &&
                    savedUser.equals(uInput, ignoreCase = true) && savedPass == pInput) {
                    saveCredentials(uInput, pInput)
                    return@withContext AuthResult.Success(uInput)
                }
                AuthResult.Error(mapFirebaseException(e))
            }
        } else {
            val savedUser = currentUsername
            val savedPass = currentPassword
            if (savedUser != null && savedPass != null &&
                savedUser.equals(uInput, ignoreCase = true) && savedPass == pInput) {
                saveCredentials(uInput, pInput)
                AuthResult.Success(uInput)
            } else {
                AuthResult.Error("ඊමේල් ලිපිනය හෝ මුරපදය වැරදියි (Invalid Email or Password)")
            }
        }
    }

    private fun mapFirebaseException(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            e is FirebaseAuthUserCollisionException ||
                    msg.contains("already in use", ignoreCase = true) ->
                "මෙම ඊමේල් ලිපිනය දැනටමත් ලියාපදිංචි කර ඇත (Email already registered). කරුණාකර 'ඇතුළු වන්න' (Sign In) තෝරන්න."
            e is FirebaseAuthWeakPasswordException ||
                    msg.contains("weak password", ignoreCase = true) ->
                "මුරපදය ඉතා කෙටි හෝ සරල වැඩියි. කරුණාකර අවම වශයෙන් අක්ෂර 6ක් යොදන්න."
            e is FirebaseAuthInvalidCredentialsException ||
                    msg.contains("invalid credential", ignoreCase = true) ||
                    msg.contains("wrong password", ignoreCase = true) ||
                    msg.contains("password is invalid", ignoreCase = true) ->
                "ඊමේල් ලිපිනය හෝ මුරපදය වැරදියි (Incorrect Email or Password)."
            e is FirebaseAuthInvalidUserException ||
                    msg.contains("user not found", ignoreCase = true) ->
                "මෙම ඊමේල් ලිපිනයට අදාළ ගිණුමක් හමු නොවීය. කරුණාකර ලියාපදිංචි වන්න (Sign Up)."
            e is FirebaseNetworkException ||
                    msg.contains("network", ignoreCase = true) ->
                "අන්තර්ජාල සම්බන්ධතාවය පරීක්ෂා කරන්න (Network error)."
            msg.contains("disabled", ignoreCase = true) ->
                "Firebase Console හි මෙම සේවාව අක්‍රීය කර ඇත."
            else -> e.localizedMessage ?: "සත්‍යාපන දෝෂයක් සිදු විය (Authentication Error)"
        }
    }

    suspend fun verifyCurrentUserStatus(): UserVerificationResult = withContext(Dispatchers.IO) {
        val user = currentUser
        if (user != null) {
            try {
                user.reload().await()
                // If user is deleted or disabled
                val refreshedUser = currentUser
                if (refreshedUser == null) {
                    return@withContext UserVerificationResult.Blocked("මෙම පරිශීලක ගිණුම Firebase මගින් අවලංගු කර හෝ ඉවත් කර ඇත (User Account Disabled)")
                }
                return@withContext UserVerificationResult.Active
            } catch (e: Exception) {
                Log.w("AuthRepo", "User verification check exception: ${e.message}")
                if (e is FirebaseAuthInvalidUserException ||
                    e.message?.contains("user-disabled", ignoreCase = true) == true ||
                    e.message?.contains("user-not-found", ignoreCase = true) == true ||
                    e.message?.contains("USER_NOT_FOUND", ignoreCase = true) == true ||
                    e.message?.contains("USER_DISABLED", ignoreCase = true) == true ||
                    e.message?.contains("has been disabled", ignoreCase = true) == true ||
                    e.message?.contains("has been deleted", ignoreCase = true) == true
                ) {
                    return@withContext UserVerificationResult.Blocked("ඔබගේ පරිශීලක ගිණුම Firebase පද්ධතිය මගින් අවහිර කර ඇත (User Blocked or Disabled)")
                }
                // For network timeouts or transient errors, avoid disrupting the user
                return@withContext UserVerificationResult.Active
            }
        }

        // If not a Firebase user session, check saved credentials against Firebase if possible
        val savedEmail = currentUsername
        val savedPass = currentPassword
        if (!savedEmail.isNullOrBlank() && !savedPass.isNullOrBlank() && savedPass != "GOOGLE_AUTH" && savedPass != "FACEBOOK_AUTH") {
            val auth = firebaseAuth
            if (auth != null) {
                try {
                    val result = auth.signInWithEmailAndPassword(savedEmail.trim(), savedPass.trim()).await()
                    if (result.user == null) {
                        return@withContext UserVerificationResult.Blocked("ගිණුම් විස්තර වලංගු නොවේ (Invalid Credentials)")
                    }
                    return@withContext UserVerificationResult.Active
                } catch (e: Exception) {
                    if (e is FirebaseAuthInvalidUserException ||
                        e.message?.contains("user-disabled", ignoreCase = true) == true ||
                        e.message?.contains("user-not-found", ignoreCase = true) == true ||
                        e.message?.contains("has been disabled", ignoreCase = true) == true
                    ) {
                        return@withContext UserVerificationResult.Blocked("ඔබගේ පරිශීලක ගිණුම Firebase පද්ධතිය මගින් අවහිර කර ඇත (Account Blocked)")
                    }
                }
            }
        }

        UserVerificationResult.NotApplicable
    }

    suspend fun checkAutoLogin(): AutoLoginResult = withContext(Dispatchers.IO) {
        val verification = verifyCurrentUserStatus()
        if (verification is UserVerificationResult.Blocked) {
            logout()
            return@withContext AutoLoginResult.CredentialsRevoked(verification.reason)
        }

        val firebaseUser = currentUser
        if (firebaseUser != null) {
            val email = firebaseUser.email ?: "User"
            return@withContext AutoLoginResult.Success(email)
        }

        val savedUser = currentUsername
        if (isUserLoggedIn && !savedUser.isNullOrBlank()) {
            return@withContext AutoLoginResult.Success(savedUser)
        }

        AutoLoginResult.NotLoggedIn
    }

    fun saveCredentials(username: String, password: String) {
        prefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, true)
            ?.putString(KEY_SAVED_USERNAME, username)
            ?.putString(KEY_SAVED_PASSWORD, password)
            ?.apply()
    }

    fun logout() {
        prefs?.edit()
            ?.putBoolean(KEY_IS_LOGGED_IN, false)
            ?.remove(KEY_SAVED_USERNAME)
            ?.remove(KEY_SAVED_PASSWORD)
            ?.apply()
    }
}
