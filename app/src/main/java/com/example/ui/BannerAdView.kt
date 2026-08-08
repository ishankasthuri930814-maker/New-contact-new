package com.example.ui

import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    adUnitId: String = "ca-app-pub-7472113156561687/7428861005",
    modifier: Modifier = Modifier
) {
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
                    .wrapContentHeight(),
                factory = { context ->
                    try {
                        AdView(context).apply {
                            setAdSize(AdSize.BANNER)
                            this.adUnitId = adUnitId
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    Log.d("AdMob", "Banner ad loaded successfully")
                                }

                                override fun onAdFailedToLoad(adError: LoadAdError) {
                                    Log.e("AdMob", "Banner ad failed to load: ${adError.message}")
                                }
                            }
                            try {
                                loadAd(AdRequest.Builder().build())
                            } catch (t: Throwable) {
                                Log.e("AdMob", "Error calling loadAd", t)
                            }
                        }
                    } catch (t: Throwable) {
                        Log.e("AdMob", "Error creating AdView", t)
                        View(context)
                    }
                }
            )
        }
    }
}

