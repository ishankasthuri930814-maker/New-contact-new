package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.remote.PoliceApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

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

class AuthRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("police_auth_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val mapAdapter = moshi.adapter<Map<String, String>>(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    )

    private val apiService: PoliceApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://sheets.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PoliceApiService::class.java)
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SAVED_USERNAME = "saved_username"
        private const val KEY_SAVED_PASSWORD = "saved_password"
        private const val KEY_CACHED_CREDENTIALS = "cached_credentials_json"
    }

    val isUserLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    val currentUsername: String?
        get() = prefs.getString(KEY_SAVED_USERNAME, null)

    val currentPassword: String?
        get() = prefs.getString(KEY_SAVED_PASSWORD, null)

    suspend fun fetchLiveCredentials(): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSheetValues(PoliceApiService.AUTH_SHEET_URL)
            val rows = response.body()?.values

            if (rows.isNullOrEmpty()) {
                val cached = getCachedCredentials()
                if (cached.isNotEmpty()) {
                    return@withContext Result.success(cached)
                }
                return@withContext Result.failure(Exception("ගූගල් ෂීට් එකෙන් පරිශීලක තොරතුරු ලබා ගැනීමට නොහැකි විය."))
            }

            val credentialsMap = mutableMapOf<String, String>()

            for ((index, row) in rows.withIndex()) {
                if (row.isEmpty()) continue
                val u = row.getOrNull(0)?.trim() ?: ""
                val p = row.getOrNull(1)?.trim() ?: ""

                // Skip header row if it contains labels like "Username" / "Password"
                if (index == 0 && (u.equals("username", ignoreCase = true) || u.equals("user", ignoreCase = true) || u.equals("නම", ignoreCase = true))) {
                    continue
                }

                if (u.isNotBlank()) {
                    credentialsMap[u] = p
                }
            }

            // Cache valid credentials map locally
            saveCachedCredentials(credentialsMap)
            Log.d("AuthRepo", "Successfully fetched ${credentialsMap.size} user credentials from Google Sheet")
            Result.success(credentialsMap)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error fetching credentials from Google Sheet", e)
            val cached = getCachedCredentials()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun login(usernameInput: String, passwordInput: String): AuthResult = withContext(Dispatchers.IO) {
        val uInput = usernameInput.trim()
        val pInput = passwordInput.trim()

        if (uInput.isBlank()) {
            return@withContext AuthResult.Error("කරුණාකර පරිශීලක නාමය ඇතුළත් කරන්න (Enter Username)")
        }
        if (pInput.isBlank()) {
            return@withContext AuthResult.Error("කරුණාකර මුරපදය ඇතුළත් කරන්න (Enter Password)")
        }

        val credentialsResult = fetchLiveCredentials()

        if (credentialsResult.isSuccess) {
            val credentials = credentialsResult.getOrDefault(emptyMap())
            
            // Match username case-insensitively or exactly, match password exactly
            val matchedEntry = credentials.entries.find { it.key.equals(uInput, ignoreCase = true) }

            if (matchedEntry != null && matchedEntry.value == pInput) {
                // Save credentials to device storage
                saveCredentials(matchedEntry.key, pInput)
                AuthResult.Success(matchedEntry.key)
            } else {
                AuthResult.InvalidCredentials
            }
        } else {
            // Check against cached credentials if available
            val cached = getCachedCredentials()
            val matchedEntry = cached.entries.find { it.key.equals(uInput, ignoreCase = true) }
            if (matchedEntry != null && matchedEntry.value == pInput) {
                saveCredentials(matchedEntry.key, pInput)
                AuthResult.Success(matchedEntry.key)
            } else {
                AuthResult.Error("අන්තර්ජාල සම්බන්ධතාවය පරීක්ෂා කර නැවත උත්සාහ කරන්න (Please check internet connection)")
            }
        }
    }

    suspend fun checkAutoLogin(): AutoLoginResult = withContext(Dispatchers.IO) {
        val savedUser = currentUsername
        val savedPass = currentPassword

        if (!isUserLoggedIn || savedUser.isNullOrBlank() || savedPass.isNullOrBlank()) {
            return@withContext AutoLoginResult.NotLoggedIn
        }

        // Live check against Google Sheet
        try {
            val response = apiService.getSheetValues(PoliceApiService.AUTH_SHEET_URL)
            val rows = response.body()?.values

            if (!rows.isNullOrEmpty()) {
                val liveMap = mutableMapOf<String, String>()
                for ((index, row) in rows.withIndex()) {
                    if (row.isEmpty()) continue
                    val u = row.getOrNull(0)?.trim() ?: ""
                    val p = row.getOrNull(1)?.trim() ?: ""
                    if (index == 0 && (u.equals("username", ignoreCase = true) || u.equals("user", ignoreCase = true))) {
                        continue
                    }
                    if (u.isNotBlank()) {
                        liveMap[u] = p
                    }
                }

                saveCachedCredentials(liveMap)

                val match = liveMap.entries.find { it.key.equals(savedUser, ignoreCase = true) }
                if (match != null && match.value == savedPass) {
                    Log.d("AuthRepo", "Auto-login verified with Google Sheet for user: $savedUser")
                    return@withContext AutoLoginResult.Success(match.key)
                } else {
                    Log.w("AuthRepo", "Auto-login failed: Saved credentials are no longer valid in Google Sheet")
                    logout()
                    return@withContext AutoLoginResult.CredentialsRevoked(
                        "ගූගල් ෂීට් දත්ත අනුව ඔබගේ පරිශීලක නාමය හෝ මුරපදය වෙනස් වී ඇත. කරුණාකර නැවත ඇතුළු වන්න."
                    )
                }
            } else {
                // If sheet response was empty or network failed, allow existing logged-in session on device
                return@withContext AutoLoginResult.Success(savedUser)
            }
        } catch (e: Exception) {
            Log.w("AuthRepo", "Network error during auto-login check, falling back to local session", e)
            // If device is offline, keep previous authenticated session
            return@withContext AutoLoginResult.Success(savedUser)
        }
    }

    fun saveCredentials(username: String, password: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_SAVED_USERNAME, username)
            .putString(KEY_SAVED_PASSWORD, password)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_SAVED_USERNAME)
            .remove(KEY_SAVED_PASSWORD)
            .apply()
    }

    private fun saveCachedCredentials(map: Map<String, String>) {
        try {
            val json = mapAdapter.toJson(map)
            prefs.edit().putString(KEY_CACHED_CREDENTIALS, json).apply()
        } catch (e: Exception) {
            Log.e("AuthRepo", "Failed to cache credentials JSON", e)
        }
    }

    private fun getCachedCredentials(): Map<String, String> {
        return try {
            val json = prefs.getString(KEY_CACHED_CREDENTIALS, null) ?: return emptyMap()
            mapAdapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
