package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.AppConfig
import com.example.data.model.AppFeaturesConfig
import com.example.data.model.AppHeaderConfig
import com.example.data.model.AppThemeConfig
import com.example.data.model.QuickActionConfig
import com.example.data.model.defaultQuickActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AppConfigManager {
    private const val TAG = "AppConfigManager"
    private const val PREFS_NAME = "app_remote_config_prefs"
    private const val KEY_CACHED_CONFIG = "cached_app_config_json"
    private const val CONFIG_FILE_NAME = "app_config.json"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _appConfig = MutableStateFlow(AppConfig())
    val appConfig: StateFlow<AppConfig> = _appConfig.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun loadConfig(context: Context) = withContext(Dispatchers.IO) {
        // 1. First load from cached preferences for instantaneous UI render
        val cachedJson = getPrefs(context).getString(KEY_CACHED_CONFIG, null)
        if (!cachedJson.isNullOrBlank()) {
            val parsed = parseConfigJson(cachedJson)
            if (parsed != null) {
                _appConfig.value = parsed
                Log.d(TAG, "Loaded cached AppConfig successfully")
            }
        }

        // 2. Fetch live config from GitHub Raw
        try {
            val repo = AppUpdateManager.getGitHubRepo(context)
            val branches = listOf("main", "master")
            var liveJson: String? = null

            for (branch in branches) {
                val url = "https://raw.githubusercontent.com/$repo/$branch/$CONFIG_FILE_NAME"
                val request = Request.Builder()
                    .url(url)
                    .header("Cache-Control", "no-cache")
                    .build()

                try {
                    httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            if (!body.isNullOrBlank()) {
                                liveJson = body
                                return@use
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Config fetch from branch $branch failed: ${e.message}")
                }
                if (liveJson != null) break
            }

            if (liveJson != null) {
                val parsed = parseConfigJson(liveJson!!)
                if (parsed != null) {
                    _appConfig.value = parsed
                    getPrefs(context).edit().putString(KEY_CACHED_CONFIG, liveJson).apply()
                    Log.d(TAG, "Live AppConfig fetched and saved from GitHub")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking remote app_config.json", e)
        }
    }

    private fun parseConfigJson(jsonString: String): AppConfig? {
        return try {
            val root = JSONObject(jsonString)

            // Theme parsing
            val themeObj = root.optJSONObject("theme")
            val theme = if (themeObj != null) {
                AppThemeConfig(
                    primaryColorHex = themeObj.optString("primaryColor", "#0D2240"),
                    accentColorHex = themeObj.optString("accentColor", "#F59E0B"),
                    emergencyRedHex = themeObj.optString("emergencyRed", "#DC2626"),
                    headerTextColorHex = themeObj.optString("headerTextColor", "#FFFFFF")
                )
            } else {
                AppThemeConfig()
            }

            // Header parsing
            val headerObj = root.optJSONObject("header")
            val header = if (headerObj != null) {
                AppHeaderConfig(
                    badgeText = headerObj.optString("badgeText", "OFFICIAL DIRECTORY"),
                    title = headerObj.optString("title", "Sri Lanka Police Directory"),
                    subtitle = headerObj.optString("subtitle", "ශ්‍රී ලංකා පොලිස් නිල ඇමතුම් සහ විද්‍යුත් තැපැල් නාමාවලිය"),
                    emergencyBadgeText = headerObj.optString("emergencyBadgeText", "119 EMERGENCY"),
                    emergencyBadgeNumber = headerObj.optString("emergencyBadgeNumber", "119"),
                    bannerImageUrl = headerObj.optString("bannerImageUrl", "")
                )
            } else {
                AppHeaderConfig()
            }

            val quickActionsTitle = root.optString("quickActionsTitle", "⚡ Quick Emergency Hotlines / හදිසි ඇමතුම්")

            // Quick Actions parsing
            val actionsArray = root.optJSONArray("quickActions")
            val quickActions = if (actionsArray != null && actionsArray.length() > 0) {
                val list = mutableListOf<QuickActionConfig>()
                for (i in 0 until actionsArray.length()) {
                    val a = actionsArray.optJSONObject(i) ?: continue
                    val enabled = a.optBoolean("enabled", true)
                    if (!enabled) continue

                    list.add(
                        QuickActionConfig(
                            id = a.optString("id", "btn_$i"),
                            title = a.optString("title", "Hotline"),
                            subtitle = a.optString("subtitle", ""),
                            phoneNumber = a.optString("phoneNumber", a.optString("phone", "119")),
                            iconType = a.optString("icon", "call"),
                            badgeColorHex = a.optString("badgeColor", "#0D2240"),
                            enabled = true
                        )
                    )
                }
                if (list.isNotEmpty()) list else defaultQuickActions
            } else {
                defaultQuickActions
            }

            // Features parsing
            val featuresObj = root.optJSONObject("features")
            val features = if (featuresObj != null) {
                AppFeaturesConfig(
                    showEmergencyBanner = featuresObj.optBoolean("showEmergencyBanner", true),
                    showQuickActions = featuresObj.optBoolean("showQuickActions", true),
                    showCategoryTabs = featuresObj.optBoolean("showCategoryTabs", true),
                    showGpsMap = featuresObj.optBoolean("showGpsMap", true),
                    showFloatingSos = featuresObj.optBoolean("showFloatingSos", true),
                    showAnnouncementTicker = featuresObj.optBoolean("showAnnouncementTicker", false),
                    announcementTickerText = featuresObj.optString("announcementTickerText", "")
                )
            } else {
                AppFeaturesConfig()
            }

            AppConfig(
                theme = theme,
                header = header,
                quickActionsTitle = quickActionsTitle,
                quickActions = quickActions,
                features = features
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse app_config.json", e)
            null
        }
    }
}
