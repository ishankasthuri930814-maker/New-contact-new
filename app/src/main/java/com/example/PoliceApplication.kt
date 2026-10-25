package com.example

import android.app.Application
import android.os.Build
import android.util.Log
import java.io.File

class PoliceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Safe handler for non-fatal system framework or Chromium background logs
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stack = Log.getStackTraceString(throwable)
            if (stack.contains("variations_seed") ||
                stack.contains("chromium") ||
                stack.contains("WebViewFactory") ||
                stack.contains("MissingWebViewPackageException") ||
                stack.contains("FloatingActionMode")
            ) {
                Log.w("PoliceApplication", "Handled non-fatal framework exception: ${throwable.message}")
            } else {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        // 2. Clean up any corrupted or unsigned Chromium variations seed files
        try {
            val dataDirectory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dataDir
            } else {
                filesDir.parentFile
            }
            if (dataDirectory != null) {
                val appWebviewDir = File(dataDirectory, "app_webview")
                if (appWebviewDir.exists()) {
                    val seed = File(appWebviewDir, "variations_seed")
                    val seedNew = File(appWebviewDir, "variations_seed_new")
                    val seedStamp = File(appWebviewDir, "variations_stamp")
                    if (seed.exists() && seed.length() < 256) {
                        seed.delete()
                    }
                    if (seedNew.exists() && seedNew.length() < 256) {
                        seedNew.delete()
                    }
                    if (seedStamp.exists() && seedStamp.length() < 32) {
                        seedStamp.delete()
                    }
                }
            }

            // Ensure WebView HTTP cache directory exists cleanly
            val webViewCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!webViewCacheDir.exists()) {
                webViewCacheDir.mkdirs()
            }
        } catch (t: Throwable) {
            Log.w("PoliceApplication", "Safe Chromium variations cleanup", t)
        }
    }
}
