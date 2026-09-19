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
 * 1. App Open Ads (on cold start and background-to-foreground resume)
 * 2. Interstitial Ads (on contact search back / dismissal / app exit)
 * 3. Graceful fallback to official Google test IDs if newly created units have NO_FILL
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
    var isShowingAd = false
        private set

    // Interstitial Ad State
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var lastInterstitialShownTime: Long = 0
    private const val INTERSTITIAL_INTERVAL_MS = 5000L // 5 seconds cooldown

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
                    Log.i(TAG, "App Open Ad loaded successfully! Unit: $targetUnitId")
                    appOpenAd = ad
                    isLoadingAppOpenAd = false
                    appOpenLoadTime = Date().time

                    // If activity is already alive and we haven't shown opening ad yet, show it
                    currentActivity?.let { act ->
                        if (!act.isFinishing && !act.isDestroyed && !isShowingAd) {
                            showAppOpenAdIfAvailable(act)
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open Ad failed to load [Code ${loadAdError.code}]: ${loadAdError.message}")
                    isLoadingAppOpenAd = false
                    appOpenAd = null

                    // If real ad unit fails due to NO_FILL (3) or INTERNAL_ERROR (0), load test unit as fallback
                    if (!forceFallback) {
                        Log.d(TAG, "Falling back to Google Test App Open Ad...")
                        mainHandler.postDelayed({
                            loadAppOpenAd(context, forceFallback = true)
                        }, 2000L)
                    } else {
                        // Retry real ad after 45 seconds
                        mainHandler.postDelayed({
                            loadAppOpenAd(context, forceFallback = false)
                        }, 45000L)
                    }
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        val wasLoadedRecently = (Date().time - appOpenLoadTime) < (4 * 3600000L) // 4 hours validity
        return appOpenAd != null && wasLoadedRecently
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        if (MainActivity.isEmulator()) {
            onComplete()
            return
        }

        if (!isUserAuthenticated) {
            Log.d(TAG, "User is on login/signup screen; skipping App Open Ad.")
            onComplete()
            return
        }

        if (isShowingAd) {
            Log.d(TAG, "Cannot show App Open Ad: Another ad is already showing.")
            onComplete()
            return
        }

        if (!isAppOpenAdAvailable()) {
            Log.d(TAG, "App Open Ad not ready yet. Triggering load.")
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
                Log.d(TAG, "App Open Ad dismissed")
                appOpenAd = null
                isShowingAd = false
                onComplete()
                loadAppOpenAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                onComplete()
                loadAppOpenAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open Ad showing on screen")
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
                    Log.i(TAG, "Interstitial Ad loaded successfully! Unit: $targetUnitId")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial Ad failed to load [Code ${loadAdError.code}]: ${loadAdError.message}")
                    isLoadingInterstitial = false
                    interstitialAd = null

                    // If real ad unit fails due to NO_FILL (3) or INTERNAL_ERROR (0), load test unit as fallback
                    if (!forceFallback) {
                        Log.d(TAG, "Falling back to Google Test Interstitial Ad...")
                        mainHandler.postDelayed({
                            loadInterstitialAd(context, forceFallback = true)
                        }, 2000L)
                    } else {
                        // Retry real ad after 45 seconds
                        mainHandler.postDelayed({
                            loadInterstitialAd(context, forceFallback = false)
                        }, 45000L)
                    }
                }
            }
        )
    }

    fun hasInterstitialAd(): Boolean {
        return interstitialAd != null && !isShowingAd
    }

    /**
     * Shows the Interstitial Ad on back navigation from search or when backing out of a contact.
     * Includes a short cooldown check to keep user experience pleasant while reliably showing ads.
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
            Log.d(TAG, "User is on login/signup screen; skipping Interstitial Ad.")
            onAdDismissed()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (!ignoreCooldown && (currentTime - lastInterstitialShownTime < INTERSTITIAL_INTERVAL_MS)) {
            Log.d(TAG, "Interstitial ad skipped due to frequency cooldown.")
            onAdDismissed()
            return
        }

        if (isShowingAd) {
            Log.d(TAG, "Another ad is currently showing; skipping interstitial.")
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not ready yet; loading for next time.")
            onAdDismissed()
            loadInterstitialAd(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad dismissed")
                interstitialAd = null
                isShowingAd = false
                lastInterstitialShownTime = System.currentTimeMillis()
                onAdDismissed()
                loadInterstitialAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                interstitialAd = null
                isShowingAd = false
                onAdDismissed()
                loadInterstitialAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad showed full screen")
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
        // Triggered when app moves to foreground from background
        if (!isUserAuthenticated) {
            Log.d(TAG, "App resumed but user is on login/signup screen; skipping App Open Ad.")
            return
        }
        currentActivity?.let { act ->
            if (!act.isFinishing && !act.isDestroyed && !isShowingAd) {
                Log.d(TAG, "App moved to foreground; presenting App Open Ad if available")
                showAppOpenAdIfAvailable(act)
            }
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
