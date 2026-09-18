package com.example

import android.app.Application
import android.util.Log
import com.example.service.MyFirebaseMessagingService
import com.google.firebase.FirebaseApp
import java.io.File

class PoliceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Setup global uncaught exception handler to prevent hard crashes on background threads
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = Log.getStackTraceString(throwable)
            Log.e("PoliceApplication", "Uncaught exception on thread ${thread.name}: $stackTrace", throwable)

            // Check if exception originates from background service or optional SDKs
            val isBackgroundException = thread.name != "main"
            val isRecoverableSdkException = stackTrace.contains("com.google.firebase.messaging") ||
                    stackTrace.contains("com.google.android.gms.ads") ||
                    stackTrace.contains("WebViewFactory") ||
                    stackTrace.contains("MissingWebViewPackageException")

            if (isBackgroundException && isRecoverableSdkException) {
                Log.w("PoliceApplication", "Suppressed non-fatal background SDK exception to keep app running.")
            } else {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        // 2. Prepare WebView cache dir for modern Android versions
        try {
            val webViewCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!webViewCacheDir.exists()) {
                webViewCacheDir.mkdirs()
            }
        } catch (t: Throwable) {
            Log.w("PoliceApplication", "Failed to create WebView cache dir", t)
        }

        // 3. Initialize Firebase safely
        try {
            FirebaseApp.initializeApp(this)
            Log.d("PoliceApplication", "FirebaseApp initialized in Application")
        } catch (t: Throwable) {
            Log.e("PoliceApplication", "FirebaseApp initialization failed in Application", t)
        }

        // 4. Create Notification Channels
        try {
            MyFirebaseMessagingService.createNotificationChannel(this)
            Log.d("PoliceApplication", "Notification channels created")
        } catch (t: Throwable) {
            Log.e("PoliceApplication", "Notification channel creation failed", t)
        }
    }
}
