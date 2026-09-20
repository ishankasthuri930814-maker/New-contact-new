package com.example.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.MainActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date

/**
 * Centralized AdMob Manager handling:
 * 1. App Open Ads: Only shown on initial cold start or when returning from background after > 30s.
 * 2. Interstitial Ads: Only shown on explicit user triggers (Search Back, Contact Details Dismiss, App Exit).
 * 3. Strict cooldowns and guards to prevent ads from looping or showing unexpectedly.
 */
object AdMobManager : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private const val TAG = "AdMobManager"

    // Ad Unit IDs
    const val REAL_APP_OPEN_AD_ID = "ca-app-pub-7472113156561687/9188630812"
    const val TEST_APP_OPEN_AD_ID = "ca-app-pub-3940256099942544/9257395921"

    const val REAL_INTERSTITIAL_AD_ID = "ca-app-pub-7472113156561687/5740340391"
    const val TEST_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"

    private var currentActivity: Activity? = null
    private var isInitialized = false

    // App Open Ad State
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAppOpenAd = false
    private var appOpenLoadTime: Long = 0
    private var hasShownColdStartAd = false
    private var appBackgroundTimestamp: Long = 0

    // Interstitial Ad State
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false

    // Global Ad Cooldown State
    var isShowingAd = false
        private set
    private var lastAdDismissedTime: Long = 0
    private const val INTERSTITIAL_MIN_INTERVAL_MS = 8000L // 8s cooldown between interstitial actions
    private const val APP_OPEN_MIN_BACKGROUND_MS = 30000L // Must be in background for at least 30s
    private const val APP_OPEN_COOLDOWN_MS = 60000L // Minimum 60s between App Open ads

    // Authentication screen awareness: strictly NO ads on Login/Signup
    var isUserAuthenticated: Boolean = false

    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize(application: Application) {
        if (isInitialized) return
        isInitialized = true

        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        if (!MainActivity.isEmulator()) {
            try {
                MobileAds.initialize(application) { status ->
                    Log.d(TAG, "MobileAds initialized: ${status.adapterStatusMap}")
                    loadAppOpenAd(application)
                    loadInterstitialAd(application)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to initialize MobileAds in AdMobManager", t)
            }
        } else {
            Log.i(TAG, "Virtual/web emulator detected; bypassing AdMob pre-initialization.")
        }
    }

    // ==========================================
    // APP OPEN AD LOGIC
    // ==========================================

    fun loadAppOpenAd(context: Context, forceFallback: Boolean = false) {
        if (MainActivity.isEmulator() || isLoadingAppOpenAd || isAppOpenAdAvailable()) {
            return
        }

        isLoadingAppOpenAd = true
        val targetUnitId = if (forceFallback) TEST_APP_OPEN_AD_ID else REAL_APP_OPEN_AD_ID
        Log.d(TAG, "Requesting App Open Ad: $targetUnitId (Fallback: $forceFallback)")

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context.applicationContext,
            targetUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.i(TAG, "App Open Ad cached successfully. Unit: $targetUnitId")
                    appOpenAd = ad
                    isLoadingAppOpenAd = false
                    appOpenLoadTime = Date().time
                    // DO NOT automatically show ad here! It will only show on verified cold start or background resume.
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open Ad failed to load [Code ${loadAdError.code}]: ${loadAdError.message}")
                    isLoadingAppOpenAd = false
                    appOpenAd = null

                    if (!forceFallback) {
                        Log.d(TAG, "Falling back to Google Test App Open Ad...")
                        mainHandler.postDelayed({
                            loadAppOpenAd(context, forceFallback = true)
                        }, 2000L)
                    } else {
                        // Retry real ad after 60 seconds
                        mainHandler.postDelayed({
                            loadAppOpenAd(context, forceFallback = false)
                        }, 60000L)
                    }
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        val wasLoadedRecently = (Date().time - appOpenLoadTime) < (4 * 3600000L) // 4 hours validity
        return appOpenAd != null && wasLoadedRecently
    }

    fun showAppOpenAdIfAvailable(activity: Activity, isColdStart: Boolean = false, onComplete: () -> Unit = {}) {
        if (MainActivity.isEmulator()) {
            onComplete()
            return
        }

        if (!isUserAuthenticated) {
            Log.d(TAG, "User not authenticated; skipping App Open Ad.")
            onComplete()
            return
        }

        if (isShowingAd) {
            Log.d(TAG, "Ad already visible; skipping App Open Ad.")
            onComplete()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAdDismissedTime < APP_OPEN_COOLDOWN_MS) {
            Log.d(TAG, "App Open Ad skipped: cooldown active.")
            onComplete()
            return
        }

        if (isColdStart && hasShownColdStartAd) {
            Log.d(TAG, "Cold start ad already displayed; skipping.")
            onComplete()
            return
        }

        if (!isAppOpenAdAvailable()) {
            Log.d(TAG, "App Open Ad not ready. Preloading.")
            onComplete()
            loadAppOpenAd(activity)
            return
        }

        val ad = appOpenAd ?: run {
            onComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open Ad dismissed.")
                appOpenAd = null
                isShowingAd = false
                lastAdDismissedTime = System.currentTimeMillis()
                if (isColdStart) hasShownColdStartAd = true
                onComplete()
                // Preload silently for future background resume
                mainHandler.postDelayed({
                    loadAppOpenAd(activity)
                }, 10000L)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                lastAdDismissedTime = System.currentTimeMillis()
                onComplete()
                loadAppOpenAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open Ad displayed on screen.")
                isShowingAd = true
            }
        }

        isShowingAd = true
        try {
            ad.show(activity)
        } catch (t: Throwable) {
            Log.e(TAG, "Exception showing App Open Ad", t)
            isShowingAd = false
            appOpenAd = null
            onComplete()
        }
    }

    // ==========================================
    // INTERSTITIAL AD LOGIC
    // ==========================================

    fun loadInterstitialAd(context: Context, forceFallback: Boolean = false) {
        if (MainActivity.isEmulator() || isLoadingInterstitial || interstitialAd != null) {
            return
        }

        isLoadingInterstitial = true
        val targetUnitId = if (forceFallback) TEST_INTERSTITIAL_AD_ID else REAL_INTERSTITIAL_AD_ID
        Log.d(TAG, "Requesting Interstitial Ad: $targetUnitId (Fallback: $forceFallback)")

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            targetUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.i(TAG, "Interstitial Ad cached successfully. Unit: $targetUnitId")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial Ad failed to load [Code ${loadAdError.code}]: ${loadAdError.message}")
                    isLoadingInterstitial = false
                    interstitialAd = null

                    if (!forceFallback) {
                        Log.d(TAG, "Falling back to Google Test Interstitial Ad...")
                        mainHandler.postDelayed({
                            loadInterstitialAd(context, forceFallback = true)
                        }, 2000L)
                    } else {
                        // Retry real ad after 60 seconds
                        mainHandler.postDelayed({
                            loadInterstitialAd(context, forceFallback = false)
                        }, 60000L)
                    }
                }
            }
        )
    }

    fun hasInterstitialAd(): Boolean {
        return interstitialAd != null && !isShowingAd
    }

    /**
     * Shows Interstitial Ad strictly on explicit user actions (Search Back, Contact Details Dismiss, Exit).
     */
    fun showInterstitialAd(
        activity: Activity,
        ignoreCooldown: Boolean = false,
        onAdDismissed: () -> Unit = {}
    ) {
        if (MainActivity.isEmulator()) {
            onAdDismissed()
            return
        }

        if (!isUserAuthenticated) {
            Log.d(TAG, "User not authenticated; skipping Interstitial Ad.")
            onAdDismissed()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (!ignoreCooldown && (currentTime - lastAdDismissedTime < INTERSTITIAL_MIN_INTERVAL_MS)) {
            Log.d(TAG, "Interstitial ad skipped due to interval cooldown.")
            onAdDismissed()
            return
        }

        if (isShowingAd) {
            Log.d(TAG, "Another ad is currently showing; skipping Interstitial.")
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not cached yet; requesting load.")
            onAdDismissed()
            loadInterstitialAd(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad dismissed by user.")
                interstitialAd = null
                isShowingAd = false
                lastAdDismissedTime = System.currentTimeMillis()
                onAdDismissed()
                // Preload silently for the next user action
                mainHandler.postDelayed({
                    loadInterstitialAd(activity)
                }, 5000L)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                interstitialAd = null
                isShowingAd = false
                lastAdDismissedTime = System.currentTimeMillis()
                onAdDismissed()
                loadInterstitialAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad displaying full screen.")
                isShowingAd = true
            }
        }

        isShowingAd = true
        try {
            ad.show(activity)
        } catch (t: Throwable) {
            Log.e(TAG, "Exception showing Interstitial Ad", t)
            isShowingAd = false
            interstitialAd = null
            onAdDismissed()
        }
    }

    // ==========================================
    // LIFECYCLE CALLBACKS
    // ==========================================

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // Only consider showing App Open Ad if returning from background after at least 30 seconds
        val timeInBackground = System.currentTimeMillis() - appBackgroundTimestamp
        if (!isUserAuthenticated || isShowingAd || appBackgroundTimestamp == 0L || timeInBackground < APP_OPEN_MIN_BACKGROUND_MS) {
            Log.d(TAG, "App foregrounded without qualifying background duration (${timeInBackground}ms); skipping App Open Ad.")
            return
        }

        // Reset background timestamp so it doesn't trigger again
        appBackgroundTimestamp = 0L

        currentActivity?.let { act ->
            if (!act.isFinishing && !act.isDestroyed) {
                Log.d(TAG, "User returned to app after ${timeInBackground}ms; checking App Open Ad.")
                showAppOpenAdIfAvailable(act, isColdStart = false)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Record timestamp only when the app itself goes to the background (and not when an ad was covering it)
        if (!isShowingAd) {
            appBackgroundTimestamp = System.currentTimeMillis()
            Log.d(TAG, "App moved to background at $appBackgroundTimestamp")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
