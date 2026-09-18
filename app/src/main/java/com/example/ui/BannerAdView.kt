package com.example.ui

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

@Composable
fun BannerAdView(
    adUnitId: String = "ca-app-pub-7472113156561687/7428861005",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEmulator = remember { MainActivity.isEmulator() }

    if (isEmulator) {
        // Safe placeholder for emulator/test preview environment to avoid DRI issues
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
                    text = "📢 AdMob Banner Space",
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

    var isAdLoaded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_view"),
        color = Color(0xFFF1F5F9),
        shadowElevation = if (isAdLoaded) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isAdLoaded) 4.dp else 0.dp),
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

                    try {
                        MobileAds.initialize(ctx)
                    } catch (t: Throwable) {
                        Log.e("AdMob", "Error initializing MobileAds", t)
                    }

                    fun loadRealBannerAd() {
                        if (isDestroyed) return

                        val displayMetrics = ctx.resources.displayMetrics
                        val adWidthPx = displayMetrics.widthPixels
                        val density = displayMetrics.density
                        val adWidth = (adWidthPx / density).toInt()
                        val adSize = try {
                            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth)
                        } catch (e: Exception) {
                            AdSize.BANNER
                        }

                        // Destroy previous AdView if any
                        currentAdView?.let { oldView ->
                            try {
                                oldView.destroy()
                            } catch (e: Exception) {
                                Log.w("AdMob", "Error destroying old AdView", e)
                            }
                        }
                        frameLayout.removeAllViews()

                        val adView = AdView(ctx).apply {
                            setAdSize(adSize)
                            this.adUnitId = adUnitId
                        }
                        currentAdView = adView

                        adView.adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.i("AdMob", "Real AdMob banner loaded successfully! Unit: $adUnitId")
                                isAdLoaded = true
                                adView.visibility = View.VISIBLE
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                val errorReason = when (adError.code) {
                                    0 -> "ERROR_CODE_INTERNAL_ERROR (Account or AdMob verification pending)"
                                    1 -> "ERROR_CODE_INVALID_REQUEST (Check ad unit configuration)"
                                    2 -> "ERROR_CODE_NETWORK_ERROR (Check internet connectivity)"
                                    3 -> "ERROR_CODE_NO_FILL (No ad inventory available yet / New ad unit warming up)"
                                    else -> "Code: ${adError.code}"
                                }
                                Log.w("AdMob", "Real banner failed to load [$errorReason]: ${adError.message}")
                                isAdLoaded = false
                                adView.visibility = View.GONE

                                // Retry requesting real ad after 30 seconds if still active
                                if (!isDestroyed) {
                                    retryHandler.removeCallbacksAndMessages(null)
                                    retryHandler.postDelayed({
                                        if (!isDestroyed) {
                                            Log.d("AdMob", "Retrying real ad request for unit: $adUnitId")
                                            loadRealBannerAd()
                                        }
                                    }, 30000L)
                                }
                            }

                            override fun onAdOpened() {
                                Log.d("AdMob", "Real Ad opened")
                            }

                            override fun onAdClosed() {
                                Log.d("AdMob", "Real Ad closed")
                            }
                        }

                        frameLayout.addView(adView)

                        try {
                            val adRequest = AdRequest.Builder().build()
                            adView.loadAd(adRequest)
                        } catch (t: Throwable) {
                            Log.e("AdMob", "Error calling loadAd for real ad unit: $adUnitId", t)
                        }
                    }

                    loadRealBannerAd()

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
