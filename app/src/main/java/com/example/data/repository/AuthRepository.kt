package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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

class AuthRepository(
    private val context: Context? = null,
    private val injectedFirebaseAuth: FirebaseAuth? = null
) {

    constructor(firebaseAuth: FirebaseAuth) : this(context = null, injectedFirebaseAuth = firebaseAuth)

    private val firebaseAuth: FirebaseAuth?
        get() = injectedFirebaseAuth ?: try {
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            Log.e("AuthRepo", "Failed to get FirebaseAuth instance", t)
            null
        }

    val currentUser: FirebaseUser? get() = try { firebaseAuth?.currentUser } catch (t: Throwable) { null }

    // Email සහ Password මගින් නව පරිශීලකයෙකු ලියාපදිංචි කිරීම (Firebase Auth)
    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth සේවාව ක්‍රියාත්මක නොවේ"))
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user?.email != null) {
                saveCredentials(user.email!!, pass)
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Email සහ Password මගින් ඇතුළු වීම (Firebase Auth)
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth සේවාව ක්‍රියාත්මක නොවේ"))
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user?.email != null) {
                saveCredentials(user.email!!, pass)
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Google මගින් ඇතුළු වීම / Sign Up වීම (Firebase Auth Google Credential)
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(IllegalStateException("Firebase Auth සේවාව ක්‍රියාත්මක නොවේ"))
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

    // Google Sheet මගින් credentials විමසීම ඉවත් කර Firebase Auth මගින් සත්‍යාපනය සිදු කෙරේ
    suspend fun login(usernameInput: String, passwordInput: String): AuthResult = withContext(Dispatchers.IO) {
        val uInput = usernameInput.trim()
        val pInput = passwordInput.trim()

        if (uInput.isBlank()) {
            return@withContext AuthResult.Error("කරුණාකර ඊමේල් ලිපිනය ඇතුළත් කරන්න (Enter Email)")
        }
        if (pInput.isBlank()) {
            return@withContext AuthResult.Error("කරුණාකර මුරපදය ඇතුළත් කරන්න (Enter Password)")
        }

        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Firebase Auth සේවාව ක්‍රියාත්මක නොවේ")
        try {
            val result = auth.signInWithEmailAndPassword(uInput, pInput).await()
            val user = result.user
            val email = user?.email ?: uInput
            saveCredentials(email, pInput)
            AuthResult.Success(email)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Firebase login error", e)
            AuthResult.Error(e.localizedMessage ?: "ඊමේල් ලිපිනය හෝ මුරපදය වැරදියි (Authentication Failed)")
        }
    }

    suspend fun checkAutoLogin(): AutoLoginResult = withContext(Dispatchers.IO) {
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
