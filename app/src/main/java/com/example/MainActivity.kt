package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.PoliceRepository
import com.example.service.MyFirebaseMessagingService
import com.example.ui.PhoneAuthScreen
import com.example.ui.PhoneAuthViewModel
import com.example.ui.PoliceScreen
import com.example.ui.PoliceViewModel
import com.example.ui.theme.PoliceDirectoryTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private lateinit var policeViewModel: PoliceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase safely
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (t: Throwable) {
            Log.e("MainActivity", "FirebaseApp initialization error", t)
        }

        // Initialize Google Mobile Ads SDK safely
        try {
            com.google.android.gms.ads.MobileAds.initialize(this) { status ->
                Log.d("MainActivity", "AdMob MobileAds initialized: ${status.adapterStatusMap}")
            }
        } catch (t: Throwable) {
            Log.e("MainActivity", "MobileAds initialization error", t)
        }

        // Initialize Notification Channel
        try {
            MyFirebaseMessagingService.createNotificationChannel(applicationContext)
        } catch (t: Throwable) {
            Log.e("MainActivity", "NotificationChannel creation error", t)
        }

        // Retrieve FCM Token for debugging/push notification targeting
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("MainActivity", "FCM Registration Token: $token")
                } else {
                    Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                }
            }
        } catch (t: Throwable) {
            Log.e("MainActivity", "FirebaseMessaging error", t)
        }

        val repository = PoliceRepository(applicationContext)
        val factory = PoliceViewModel.Factory(repository)
        policeViewModel = ViewModelProvider(this, factory)[PoliceViewModel::class.java]

        setContent {
            PoliceDirectoryTheme {
                // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Log.d("MainActivity", "Notification permission granted")
                    } else {
                        Log.w("MainActivity", "Notification permission denied")
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                PoliceScreen(
                    viewModel = policeViewModel
                )
            }
        }
    }
}
