package com.example.ui

import android.app.Activity
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.MainActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

private const val GOOGLE_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun BannerAdView(
    adUnitId: String = "ca-app-pub-7472113156561687/7428861005",
    modifier: Modifier = Modifier
) {
    val isEmulator = remember { MainActivity.isEmulator() }

    if (isEmulator) {
        // Safe placeholder for web container preview to prevent OpenGL/DRI headless crashes
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("admob_banner_view"),
            color = Color(0xFFF1F5F9),
            shadowElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📢 AdMob Banner Space (Preview)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        return
    }

    var isAdVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_view"),
        color = Color(0xFFF8FAFC),
        shadowElevation = if (isAdVisible) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isAdVisible) 4.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx)
                    var currentAdView: AdView? = null
                    val retryHandler = Handler(Looper.getMainLooper())
                    var isDestroyed = false
                    var isShowingFallbackTestAd = false

                    val hostActivity: Activity? = generateSequence(ctx) {
                        if (it is ContextWrapper) it.baseContext else null
                    }.filterIsInstance<Activity>().firstOrNull()
                    val activityContext = hostActivity ?: ctx

                    try {
                        MobileAds.initialize(activityContext)
                    } catch (t: Throwable) {
                        Log.e("AdMob", "Error initializing MobileAds", t)
                    }

                    fun requestAd(targetAdUnitId: String, isFallback: Boolean) {
                        if (isDestroyed) return

                        try {
                            val displayMetrics = activityContext.resources.displayMetrics
                            val adWidthPx = displayMetrics.widthPixels
                            val density = displayMetrics.density
                            val adWidth = if (density > 0) (adWidthPx / density).toInt() else 320
                            val adSize = if (adWidth > 0) {
                                try {
                                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activityContext, adWidth)
                                } catch (e: Throwable) {
                                    AdSize.BANNER
                                }
                            } else {
                                AdSize.BANNER
                            }

                            currentAdView?.let { oldView ->
                                try {
                                    oldView.destroy()
                                } catch (e: Throwable) {
                                    Log.w("AdMob", "Error destroying old AdView", e)
                                }
                            }
                            frameLayout.removeAllViews()

                            val adView = AdView(activityContext).apply {
                                setAdSize(adSize)
                                this.adUnitId = targetAdUnitId
                            }
                            currentAdView = adView

                            adView.adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    Log.i("AdMob", "Banner loaded successfully! Unit: $targetAdUnitId (Fallback: $isFallback)")
                                    isAdVisible = true
                                    adView.visibility = View.VISIBLE
                                }

                                override fun onAdFailedToLoad(adError: LoadAdError) {
                                    val errorReason = when (adError.code) {
                                        0 -> "ERROR_CODE_INTERNAL_ERROR (Account verification or AdMob approval pending)"
                                        1 -> "ERROR_CODE_INVALID_REQUEST (Check ad unit configuration)"
                                        2 -> "ERROR_CODE_NETWORK_ERROR (Check internet connectivity)"
                                        3 -> "ERROR_CODE_NO_FILL (No live inventory available yet / New ad unit warming up)"
                                        else -> "Code: ${adError.code}"
                                    }
                                    Log.w("AdMob", "Ad failed to load [$errorReason] for $targetAdUnitId: ${adError.message}")

                                    // If real ad unit fails to load due to NO_FILL or INTERNAL_ERROR (common on new units),
                                    // load the official Google Test Banner so ads are immediately visible to verify integration.
                                    if (!isFallback && !isDestroyed) {
                                        Log.d("AdMob", "Loading official Google Test Banner as fallback while real ad warms up...")
                                        isShowingFallbackTestAd = true
                                        requestAd(GOOGLE_TEST_BANNER_ID, isFallback = true)
                                    } else if (isFallback) {
                                        isAdVisible = false
                                        adView.visibility = View.GONE
                                    }

                                    // Schedule periodic retry for real ad unit (every 30 seconds)
                                    if (!isDestroyed && !isFallback) {
                                        retryHandler.removeCallbacksAndMessages(null)
                                        retryHandler.postDelayed({
                                            if (!isDestroyed) {
                                                Log.d("AdMob", "Retrying real ad unit: $adUnitId")
                                                requestAd(adUnitId, isFallback = false)
                                            }
                                        }, 30000L)
                                    }
                                }

                                override fun onAdOpened() {
                                    Log.d("AdMob", "Ad opened: $targetAdUnitId")
                                }

                                override fun onAdClosed() {
                                    Log.d("AdMob", "Ad closed: $targetAdUnitId")
                                }
                            }

                            frameLayout.addView(adView)

                            val adRequest = AdRequest.Builder().build()
                            adView.loadAd(adRequest)
                        } catch (t: Throwable) {
                            Log.e("AdMob", "Error creating or loading banner ad", t)
                            isAdVisible = false
                        }
                    }

                    try {
                        requestAd(adUnitId, isFallback = false)
                    } catch (t: Throwable) {
                        Log.e("AdMob", "Initial banner request failed", t)
                    }

                    frameLayout.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {}
                        override fun onViewDetachedFromWindow(v: View) {
                            isDestroyed = true
                            retryHandler.removeCallbacksAndMessages(null)
                            try {
                                currentAdView?.destroy()
                            } catch (e: Exception) {
                                Log.w("AdMob", "Error destroying AdView on detach", e)
                            }
                        }
                    })

                    frameLayout
                }
            )
        }
    }
}
