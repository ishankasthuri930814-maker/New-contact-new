package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.AppNotice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AppNoticeManager {
    private const val TAG = "AppNoticeManager"
    private const val PREFS_NAME = "app_notice_prefs"
    private const val KEY_DISMISSED_NOTICE_ID = "dismissed_notice_id"
    private const val NOTICE_FILE_NAME = "notice.json"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _currentNotice = MutableStateFlow<AppNotice?>(null)
    val currentNotice: StateFlow<AppNotice?> = _currentNotice.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun checkForNotices(context: Context, forceShow: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            val repo = AppUpdateManager.getGitHubRepo(context)
            
            // Try fetching raw notice.json from main and master branches
            val branches = listOf("main", "master")
            var jsonString: String? = null

            for (branch in branches) {
                val url = "https://raw.githubusercontent.com/$repo/$branch/$NOTICE_FILE_NAME"
                val request = Request.Builder()
                    .url(url)
                    .header("Cache-Control", "no-cache")
                    .build()

                try {
                    httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            if (!body.isNullOrBlank()) {
                                jsonString = body
                                return@use
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Notice fetch from branch $branch failed: ${e.message}")
                }
                if (jsonString != null) break
            }

            if (jsonString == null) {
                Log.d(TAG, "No notice.json found in repository $repo")
                return@withContext
            }

            val json = JSONObject(jsonString)
            val active = json.optBoolean("active", false)
            if (!active && !forceShow) {
                _currentNotice.value = null
                return@withContext
            }

            val id = json.optString("id", "")
            val title = json.optString("title", "විශේෂ නිවේදනයයි")
            val message = json.optString("message", "")
            val type = json.optString("type", "info")
            val date = json.optString("date", "")
            val dismissible = json.optBoolean("dismissible", true)

            if (message.isBlank()) {
                _currentNotice.value = null
                return@withContext
            }

            // Check if user already dismissed this notice ID
            val dismissedId = getPrefs(context).getString(KEY_DISMISSED_NOTICE_ID, "")
            if (!forceShow && dismissedId == id && id.isNotBlank()) {
                Log.d(TAG, "Notice $id has already been dismissed by user")
                _currentNotice.value = null
                return@withContext
            }

            val notice = AppNotice(
                id = id,
                title = title,
                message = message,
                type = type,
                date = date,
                active = active,
                dismissible = dismissible
            )

            _currentNotice.value = notice
            Log.d(TAG, "Active notice loaded: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notice.json", e)
        }
    }

    fun dismissNotice(context: Context, noticeId: String) {
        if (noticeId.isNotBlank()) {
            getPrefs(context).edit()
                .putString(KEY_DISMISSED_NOTICE_ID, noticeId)
                .apply()
        }
        _currentNotice.value = null
    }

    fun clearDismissedNotice(context: Context) {
        getPrefs(context).edit().remove(KEY_DISMISSED_NOTICE_ID).apply()
    }
}
