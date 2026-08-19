package com.example.ui

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun BannerAdView(
    adUnitId: String = "ca-app-pub-7472113156561687/7428861005",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_view"),
        color = Color(0xFFF1F5F9),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(min = 50.dp),
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx)
                    try {
                        MobileAds.initialize(ctx)
                    } catch (t: Throwable) {
                        Log.e("AdMob", "Error initializing MobileAds", t)
                    }

                    // Helper function to create and load an AdView
                    fun createAdView(targetUnitId: String, isFallback: Boolean) {
                        val displayMetrics = ctx.resources.displayMetrics
                        val adWidthPx = displayMetrics.widthPixels
                        val density = displayMetrics.density
                        val adWidth = (adWidthPx / density).toInt()
                        val adSize = try {
                            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth)
                        } catch (e: Exception) {
                            AdSize.BANNER
                        }

                        val adView = AdView(ctx).apply {
                            setAdSize(adSize)
                            this.adUnitId = targetUnitId
                        }

                        adView.adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.d("AdMob", "Banner ad loaded successfully with unit ID: $targetUnitId")
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.w("AdMob", "Ad failed to load ($targetUnitId): ${adError.message} (code ${adError.code})")
                                if (!isFallback && targetUnitId != TEST_BANNER_AD_UNIT_ID) {
                                    Log.i("AdMob", "Primary ad unit failed (code ${adError.code}). Loading test banner fallback...")
                                    frameLayout.post {
                                        frameLayout.removeAllViews()
                                        createAdView(TEST_BANNER_AD_UNIT_ID, isFallback = true)
                                    }
                                }
                            }
                        }

                        frameLayout.removeAllViews()
                        frameLayout.addView(adView)

                        try {
                            adView.loadAd(AdRequest.Builder().build())
                        } catch (t: Throwable) {
                            Log.e("AdMob", "Error calling loadAd for $targetUnitId", t)
                        }
                    }

                    createAdView(adUnitId, isFallback = false)
                    frameLayout
                }
            )
        }
    }
}