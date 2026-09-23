package com.example

import android.app.Application
import android.util.Log
import com.example.service.MyFirebaseMessagingService
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.File

class PoliceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Setup global uncaught exception handler to prevent hard crashes on background threads
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = Log.getStackTraceString(throwable)
            Log.e("PoliceApplication", "Uncaught exception on thread ${thread.name}: $stackTrace", throwable)

            // Check if exception originates from background service, floating action mode or optional SDKs
            val isBackgroundException = thread.name != "main"
            val isRecoverableSdkException = stackTrace.contains("com.google.firebase.messaging") ||
                    stackTrace.contains("com.google.android.gms.ads") ||
                    stackTrace.contains("WebViewFactory") ||
                    stackTrace.contains("MissingWebViewPackageException") ||
                    stackTrace.contains("FloatingActionMode") ||
                    stackTrace.contains("DecorView")

            if ((isBackgroundException && isRecoverableSdkException) || stackTrace.contains("FloatingActionMode")) {
                Log.w("PoliceApplication", "Suppressed non-fatal system framework exception: ${throwable.message}")
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

        // 3. Initialize Firebase safely and explicitly
        try {
            val apps = FirebaseApp.getApps(this)
            if (apps.isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:880156476376:android:e14409f7b16d90abf3a8ad")
                    .setApiKey("AIzaSyDSJ2osq4TdvY4CAdUeoQP7miO8i4R4ZFk")
                    .setProjectId("policecontact")
                    .setStorageBucket("policecontact.firebasestorage.app")
                    .setGcmSenderId("880156476376")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("PoliceApplication", "FirebaseApp initialized with explicit options")
            } else {
                Log.d("PoliceApplication", "FirebaseApp already initialized (${apps.size} apps)")
            }
        } catch (t: Throwable) {
            Log.e("PoliceApplication", "FirebaseApp initialization failed in Application", t)
        }

        // 4. Initialize Facebook SDK safely
        try {
            com.facebook.FacebookSdk.sdkInitialize(this)
            com.facebook.appevents.AppEventsLogger.activateApp(this)
            Log.d("PoliceApplication", "FacebookSdk initialized")
        } catch (t: Throwable) {
            Log.w("PoliceApplication", "FacebookSdk initialization skipped or failed", t)
        }

        // 5. Create Notification Channels
        try {
            MyFirebaseMessagingService.createNotificationChannel(this)
            Log.d("PoliceApplication", "Notification channels created")
        } catch (t: Throwable) {
            Log.e("PoliceApplication", "Notification channel creation failed", t)
        }

        // 5. Initialize AdMob Manager for App Open Ads & Interstitial Ads
        try {
            com.example.ads.AdMobManager.initialize(this)
            Log.d("PoliceApplication", "AdMobManager initialized")
        } catch (t: Throwable) {
            Log.e("PoliceApplication", "AdMobManager initialization failed", t)
        }
    }
}
