package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.aistudio.policedirectory.zxklm.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class GitHubRelease(
    val tagName: String,
    val title: String,
    val releaseNotes: String,
    val publishedAt: String,
    val htmlUrl: String,
    val apkDownloadUrl: String?,
    val apkFileName: String?,
    val apkSize: Long
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class NoUpdate(val checkedVersion: String) : UpdateStatus()
    data class UpdateAvailable(val release: GitHubRelease) : UpdateStatus()
    data class Downloading(val progressPercent: Int, val bytesRead: Long, val totalBytes: Long) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File, val release: GitHubRelease) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_GITHUB_REPO = "github_repo"
    private const val DEFAULT_REPO = "ishankasthuri930814-maker/New-contact-new"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    private var downloadedApkFile: File? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getGitHubRepo(context: Context): String {
        val saved = getPrefs(context).getString(KEY_GITHUB_REPO, null)
        return if (!saved.isNullOrBlank()) saved.trim() else DEFAULT_REPO
    }

    fun setGitHubRepo(context: Context, repo: String) {
        val cleanRepo = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
        getPrefs(context).edit().putString(KEY_GITHUB_REPO, cleanRepo).apply()
    }

    fun getCurrentVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    fun getCurrentVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    fun resetStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    /**
     * Checks GitHub Releases API for the configured repository:
     * https://api.github.com/repos/{owner}/{repo}/releases/latest
     */
    suspend fun checkForUpdates(context: Context, isManualCheck: Boolean = false): UpdateStatus {
        _updateStatus.value = UpdateStatus.Checking
        return withContext(Dispatchers.IO) {
            try {
                val repo = getGitHubRepo(context)
                if (repo.isBlank() || !repo.contains("/")) {
                    val err = "වලංගු GitHub Repository නාමයක් නොමැත (උදා: owner/repo)"
                    _updateStatus.value = UpdateStatus.Error(err)
                    return@withContext UpdateStatus.Error(err)
                }

                val apiUrl = "https://api.github.com/repos/$repo/releases/latest"
                Log.d(TAG, "Checking GitHub releases at: $apiUrl")

                val request = Request.Builder()
                    .url(apiUrl)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "PoliceDirectory-AndroidApp")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.code == 404) {
                        val msg = "GitHub හි '$repo' සඳහා Releases තවමත් පළ කර නොමැත."
                        _updateStatus.value = UpdateStatus.NoUpdate(getCurrentVersionName())
                        return@withContext UpdateStatus.NoUpdate(getCurrentVersionName())
                    }

                    if (!response.isSuccessful) {
                        val msg = "GitHub API දෝෂයකි (${response.code})"
                        _updateStatus.value = UpdateStatus.Error(msg)
                        return@withContext UpdateStatus.Error(msg)
                    }

                    val responseBody = response.body?.string() ?: ""
                    if (responseBody.isBlank()) {
                        val msg = "හිස් ප්‍රතිචාරයක් ලැබිණි"
                        _updateStatus.value = UpdateStatus.Error(msg)
                        return@withContext UpdateStatus.Error(msg)
                    }

                    val json = JSONObject(responseBody)
                    val tagName = json.optString("tag_name", "")
                    val releaseTitle = json.optString("name", tagName)
                    val bodyRaw = if (json.has("body") && !json.isNull("body")) json.optString("body", "") else ""
                    val releaseNotes = if (bodyRaw.isNotBlank()) bodyRaw.trim() else "නව විශේෂාංග සහ වැඩිදියුණු කිරීම් ඇතුළත් කර ඇත."
                    val publishedAt = json.optString("published_at", "")
                    val htmlUrl = json.optString("html_url", "https://github.com/$repo/releases")

                    var apkDownloadUrl: String? = null
                    var apkFileName: String? = null
                    var apkSize: Long = 0L

                    // Search assets for an APK file
                    val assetsArray: JSONArray? = json.optJSONArray("assets")
                    if (assetsArray != null) {
                        for (i in 0 until assetsArray.length()) {
                            val asset = assetsArray.getJSONObject(i)
                            val name = asset.optString("name", "")
                            val downloadUrl = asset.optString("browser_download_url", "")
                            val size = asset.optLong("size", 0L)
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkDownloadUrl = downloadUrl
                                apkFileName = name
                                apkSize = size
                                break
                            }
                        }
                    }

                    // Fallback to html_url if no direct apk asset was attached
                    if (apkDownloadUrl == null) {
                        apkDownloadUrl = htmlUrl
                    }

                    val release = GitHubRelease(
                        tagName = tagName,
                        title = if (releaseTitle.isNotBlank()) releaseTitle else tagName,
                        releaseNotes = releaseNotes,
                        publishedAt = publishedAt,
                        htmlUrl = htmlUrl,
                        apkDownloadUrl = apkDownloadUrl,
                        apkFileName = apkFileName,
                        apkSize = apkSize
                    )

                    val isNewer = isNewerVersion(remoteTag = tagName, currentVersion = getCurrentVersionName())
                    Log.d(TAG, "Remote tag: $tagName, Current: ${getCurrentVersionName()}, isNewer: $isNewer")

                    if (isNewer) {
                        val status = UpdateStatus.UpdateAvailable(release)
                        _updateStatus.value = status
                        status
                    } else {
                        val status = UpdateStatus.NoUpdate(getCurrentVersionName())
                        _updateStatus.value = status
                        status
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed", e)
                val errMsg = "යාවත්කාලීන පරීක්ෂා කිරීම අසාර්ථක විය: ${e.localizedMessage ?: "Network error"}"
                val status = UpdateStatus.Error(errMsg)
                _updateStatus.value = status
                status
            }
        }
    }

    /**
     * Compares remote version tag (e.g. "v3.6", "3.6.0") with current app version (e.g. "3.5").
     * Returns true if remote is strictly greater than current.
     */
    fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
        try {
            val cleanRemote = remoteTag.trim().removePrefix("v").removePrefix("V")
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")

            if (cleanRemote == cleanCurrent) return false

            val remoteParts = cleanRemote.split(".", "-").mapNotNull { it.toIntOrNull() }
            val currentParts = cleanCurrent.split(".", "-").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }

            // Fallback string compare
            return cleanRemote > cleanCurrent
        } catch (e: Exception) {
            return remoteTag.trim() != currentVersion.trim()
        }
    }

    /**
     * Downloads the APK file from GitHub with real-time download progress tracking.
     */
    suspend fun downloadApk(context: Context, release: GitHubRelease) {
        val downloadUrl = release.apkDownloadUrl
        if (downloadUrl.isNullOrBlank() || !downloadUrl.endsWith(".apk", ignoreCase = true)) {
            // Open in browser if direct apk is not available
            openInBrowser(context, release.htmlUrl)
            return
        }

        _updateStatus.value = UpdateStatus.Downloading(0, 0, release.apkSize)

        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(downloadUrl)
                    .header("User-Agent", "PoliceDirectory-AndroidApp")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        _updateStatus.value = UpdateStatus.Error("APK බාගත කිරීම අසාර්ථක විය (${response.code})")
                        return@withContext
                    }

                    val body = response.body ?: run {
                        _updateStatus.value = UpdateStatus.Error("APK දත්ත නොලැබිණි.")
                        return@withContext
                    }

                    val totalBytes = if (release.apkSize > 0) release.apkSize else body.contentLength()
                    val fileName = release.apkFileName ?: "police_directory_${release.tagName}.apk"

                    val cacheDir = File(context.cacheDir, "updates").apply { mkdirs() }
                    val apkFile = File(cacheDir, fileName)

                    body.byteStream().use { input ->
                        FileOutputStream(apkFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead = 0L
                            var read: Int
                            var lastPercent = 0

                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                bytesRead += read
                                val percent = if (totalBytes > 0) {
                                    ((bytesRead * 100) / totalBytes).toInt()
                                } else 0

                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    _updateStatus.value = UpdateStatus.Downloading(percent, bytesRead, totalBytes)
                                }
                            }
                            output.flush()
                        }
                    }

                    // Also make a copy in public Downloads folder so if user uninstalls, the APK is still in Downloads/
                    try {
                        val publicDownloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (publicDownloadsDir != null && publicDownloadsDir.exists()) {
                            val publicApk = File(publicDownloadsDir, fileName)
                            apkFile.copyTo(publicApk, overwrite = true)
                            Log.d(TAG, "Copied APK to public Downloads folder: ${publicApk.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not copy to public Downloads folder: ${e.message}")
                    }

                    downloadedApkFile = apkFile
                    _updateStatus.value = UpdateStatus.ReadyToInstall(apkFile, release)
                }
            } catch (e: Exception) {
                Log.e(TAG, "APK download failed", e)
                _updateStatus.value = UpdateStatus.Error("බාගත කිරීමේ දෝෂයකි: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Triggers Android Package Installer for the downloaded APK via FileProvider.
     */
    fun installApk(activity: Activity, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Log.e(TAG, "APK file does not exist: ${apkFile.absolutePath}")
                return
            }

            // Android 8+ (Oreo): Check for Unknown App Installation permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.packageManager.canRequestPackageInstalls()) {
                    val permissionIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${activity.packageName}")
                    )
                    activity.startActivity(permissionIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            activity.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            // Fallback: Open browser
            openInBrowser(activity, "https://github.com/${getGitHubRepo(activity)}/releases")
        }
    }

    fun openInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open browser", e)
        }
    }
}
