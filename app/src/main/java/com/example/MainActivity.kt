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
import com.example.data.repository.AuthRepository
import com.example.data.repository.PoliceRepository
import com.example.service.MyFirebaseMessagingService
import com.example.ui.AuthViewModel
import com.example.ui.LoginScreen
import com.example.ui.PoliceScreen
import com.example.ui.AdminPanelScreen
import com.example.ui.PoliceViewModel
import com.example.ui.components.AdminPasswordDialog
import com.example.ui.theme.PoliceDirectoryTheme
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var policeViewModel: PoliceViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Pre-create WebView and Chromium cache directories to prevent Chromium opendir & index errors
        try {
            val webViewCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!webViewCacheDir.exists()) {
                webViewCacheDir.mkdirs()
            }
        } catch (t: Throwable) {
            Log.w("MainActivity", "WebView cache dir preparation", t)
        }

        // 2. Initialize Firebase safely
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (t: Throwable) {
            Log.e("MainActivity", "FirebaseApp initialization error", t)
        }

        // 3. Initialize Google Mobile Ads SDK safely (only when not running on headless emulator without DRI)
        try {
            if (!isEmulator()) {
                com.google.android.gms.ads.MobileAds.initialize(this) { status ->
                    Log.d("MainActivity", "AdMob MobileAds initialized: ${status.adapterStatusMap}")
                }
            } else {
                Log.i("MainActivity", "Virtual/emulator environment detected without DRI; suppressed AdMob GPU initialization.")
            }
        } catch (t: Throwable) {
            Log.e("MainActivity", "MobileAds initialization error", t)
        }

        // 4. Initialize Notification Channel (FCM auto-init kept false to prevent registration failures)
        try {
            MyFirebaseMessagingService.createNotificationChannel(applicationContext)
        } catch (t: Throwable) {
            Log.e("MainActivity", "NotificationChannel creation error", t)
        }

        val authRepository = AuthRepository(applicationContext)
        val authFactory = AuthViewModel.Factory(authRepository)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        val repository = PoliceRepository(applicationContext)
        val factory = PoliceViewModel.Factory(repository)
        policeViewModel = ViewModelProvider(this, factory)[PoliceViewModel::class.java]

        setContent {
            PoliceDirectoryTheme {
                val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

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

                var showAdminPanel by remember { mutableStateOf(false) }
                var showAdminPasswordDialog by remember { mutableStateOf(false) }

                if (authUiState.isAuthenticated) {
                    if (showAdminPanel) {
                        AdminPanelScreen(
                            viewModel = policeViewModel,
                            currentUser = authUiState.loggedInUsername ?: "Admin Ishan",
                            onNavigateBack = { showAdminPanel = false }
                        )
                    } else {
                        PoliceScreen(
                            viewModel = policeViewModel,
                            currentUser = authUiState.loggedInUsername,
                            onLogout = { authViewModel.logout() },
                            onOpenAdminPanel = { showAdminPasswordDialog = true }
                        )
                    }

                    if (showAdminPasswordDialog) {
                        AdminPasswordDialog(
                            onDismiss = { showAdminPasswordDialog = false },
                            onSuccess = {
                                showAdminPasswordDialog = false
                                showAdminPanel = true
                            }
                        )
                    }
                } else {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = { /* Automatically navigates due to auth state */ }
                    )
                }
            }
        }
    }

    companion object {
        fun isEmulator(): Boolean {
            val fingerprint = Build.FINGERPRINT.lowercase()
            val model = Build.MODEL.lowercase()
            val manufacturer = Build.MANUFACTURER.lowercase()
            val hardware = Build.HARDWARE.lowercase()
            val product = Build.PRODUCT.lowercase()
            val hasDri = File("/dev/dri").exists()

            return !hasDri ||
                    fingerprint.startsWith("generic") ||
                    fingerprint.startsWith("unknown") ||
                    model.contains("google_sdk") ||
                    model.contains("emulator") ||
                    model.contains("android sdk built for") ||
                    manufacturer.contains("genymotion") ||
                    hardware.contains("goldfish") ||
                    hardware.contains("ranchu") ||
                    hardware.contains("cutf") ||
                    product.contains("sdk") ||
                    product.contains("google_sdk") ||
                    product.contains("emulator")
        }
    }
}
