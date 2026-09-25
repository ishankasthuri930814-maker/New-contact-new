package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.view.ActionMode
import android.view.Window
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
import com.example.service.InAppNotification
import com.example.service.InAppNotificationBus
import com.example.ui.AuthViewModel
import com.example.ui.LoginScreen
import com.example.ui.PoliceScreen
import com.example.ui.AdminPanelScreen
import com.example.ui.PoliceViewModel
import com.example.ui.components.AdminPasswordDialog
import com.example.ui.theme.PoliceDirectoryTheme
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var policeViewModel: PoliceViewModel
    private lateinit var authViewModel: AuthViewModel
    val facebookCallbackManager: com.facebook.CallbackManager? by lazy {
        try {
            if (!com.facebook.FacebookSdk.isInitialized()) {
                com.facebook.FacebookSdk.sdkInitialize(applicationContext)
            }
            com.facebook.CallbackManager.Factory.create()
        } catch (t: Throwable) {
            Log.w("MainActivity", "Facebook CallbackManager safe init fallback", t)
            null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            facebookCallbackManager?.onActivityResult(requestCode, resultCode, data)
        } catch (t: Throwable) {
            Log.w("MainActivity", "Facebook callback error", t)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

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
            FirebaseApp.initializeApp(this)
            Log.d("MainActivity", "FirebaseApp initialized successfully")
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

        // 4. Initialize Notification Channel and Firebase Cloud Messaging safely
        try {
            MyFirebaseMessagingService.createNotificationChannel(applicationContext)
            com.example.util.AppNotificationManager.initializeChannels(applicationContext)

            val gmsAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            val isGooglePlayServicesAvailable = (gmsAvailability == ConnectionResult.SUCCESS)
            val isVirtualEnvironment = isEmulator()

            if (isGooglePlayServicesAvailable && !isVirtualEnvironment) {
                try {
                    FirebaseMessaging.getInstance().isAutoInitEnabled = true
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        try {
                            if (task.isSuccessful) {
                                val token = try { task.result } catch (e: Exception) { null }
                                Log.d("MainActivity", "FCM Device Token: $token")
                                try {
                                    FirebaseMessaging.getInstance().subscribeToTopic("all")
                                    FirebaseMessaging.getInstance().subscribeToTopic("police_alerts")
                                    FirebaseMessaging.getInstance().subscribeToTopic("news")
                                    FirebaseMessaging.getInstance().subscribeToTopic("general")
                                } catch (t: Throwable) {
                                    Log.w("MainActivity", "FCM subscribeToTopic error", t)
                                }
                            } else {
                                Log.w("MainActivity", "FCM token retrieval failed: ${task.exception?.message}")
                            }
                        } catch (t: Throwable) {
                            Log.w("MainActivity", "FCM onCompleteListener error", t)
                        }
                    }
                } catch (t: Throwable) {
                    Log.w("MainActivity", "FirebaseMessaging getInstance or setup error", t)
                }
            } else {
                Log.i(
                    "MainActivity",
                    "FCM registration deferred (GMS Status: $gmsAvailability, isEmulator: $isVirtualEnvironment)"
                )
            }
        } catch (t: Throwable) {
            Log.e("MainActivity", "Notification / FCM initialization error", t)
        }

        try {
            val authRepository = AuthRepository(applicationContext)
            val authFactory = AuthViewModel.Factory(authRepository)
            authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]
        } catch (t: Throwable) {
            Log.e("MainActivity", "AuthViewModel initialization fallback", t)
            authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        }

        try {
            val repository = PoliceRepository(applicationContext)
            val factory = PoliceViewModel.Factory(repository)
            policeViewModel = ViewModelProvider(this, factory)[PoliceViewModel::class.java]
        } catch (t: Throwable) {
            Log.e("MainActivity", "PoliceViewModel initialization fallback", t)
            val fallbackRepo = PoliceRepository(applicationContext)
            policeViewModel = PoliceViewModel(fallbackRepo)
        }

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

                    val notifTitle = intent?.getStringExtra("extra_notification_title")
                    val notifBody = intent?.getStringExtra("extra_notification_body")
                    if (!notifTitle.isNullOrBlank() || !notifBody.isNullOrBlank()) {
                        val safeTitle = notifTitle ?: "Police Directory Notification"
                        val safeBody = notifBody ?: ""
                        val displayMsg = listOfNotNull(notifTitle, notifBody).joinToString(": ")
                        Toast.makeText(this@MainActivity, displayMsg, Toast.LENGTH_LONG).show()
                        InAppNotificationBus.postNotification(
                            InAppNotification(
                                title = safeTitle,
                                body = safeBody
                            )
                        )
                    }

                    // Attempt showing App Open Ad on cold launch once view is active ONLY if authenticated
                    if (authUiState.isAuthenticated) {
                        com.example.ads.AdMobManager.showAppOpenAdIfAvailable(this@MainActivity, isColdStart = true)
                        val cachedProfile = com.example.data.repository.UserProfileRepository(applicationContext).loadCachedProfile()
                        if (cachedProfile.email.isNotBlank() || cachedProfile.userId.isNotBlank()) {
                            com.example.service.CallBackgroundService.start(applicationContext, cachedProfile)
                        }
                    }
                }

                LaunchedEffect(authUiState.isAuthenticated) {
                    com.example.ads.AdMobManager.isUserAuthenticated = authUiState.isAuthenticated
                    if (authUiState.isAuthenticated) {
                        val cachedProfile = com.example.data.repository.UserProfileRepository(applicationContext).loadCachedProfile()
                        if (cachedProfile.email.isNotBlank() || cachedProfile.userId.isNotBlank()) {
                            com.example.service.CallBackgroundService.start(applicationContext, cachedProfile)
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
                            onLogout = {
                                com.example.ads.AdMobManager.isUserAuthenticated = false
                                authViewModel.logout()
                            },
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
                        authViewModel = authViewModel,
                        facebookCallbackManager = facebookCallbackManager,
                        onLoginSuccess = {
                            Toast.makeText(this@MainActivity, "සාර්ථකව ඇතුළු විය!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::authViewModel.isInitialized) {
            authViewModel.verifyActiveUserImmediately()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val notifTitle = intent.getStringExtra("extra_notification_title")
        val notifBody = intent.getStringExtra("extra_notification_body")
        if (!notifTitle.isNullOrBlank() || !notifBody.isNullOrBlank()) {
            val safeTitle = notifTitle ?: "Police Directory Notification"
            val safeBody = notifBody ?: ""
            val displayMsg = listOfNotNull(notifTitle, notifBody).joinToString(": ")
            Toast.makeText(this, displayMsg, Toast.LENGTH_LONG).show()
            InAppNotificationBus.postNotification(
                InAppNotification(
                    title = safeTitle,
                    body = safeBody
                )
            )
        }
    }

    private var activeActionMode: ActionMode? = null

    override fun onActionModeStarted(mode: ActionMode?) {
        try {
            activeActionMode = mode
            super.onActionModeStarted(mode)
        } catch (t: Throwable) {
            Log.w("MainActivity", "ActionMode started exception caught safely: ${t.message}")
        }
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        try {
            if (activeActionMode == mode) {
                activeActionMode = null
            }
            super.onActionModeFinished(mode)
        } catch (t: Throwable) {
            Log.w("MainActivity", "ActionMode finished exception caught safely: ${t.message}")
        }
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        return try {
            super.onWindowStartingActionMode(callback, type)
        } catch (t: Throwable) {
            Log.w("MainActivity", "Window starting action mode exception handled: ${t.message}")
            null
        }
    }

    companion object {
        fun isEmulator(): Boolean {
            val fingerprint = Build.FINGERPRINT.lowercase()
            val model = Build.MODEL.lowercase()
            val manufacturer = Build.MANUFACTURER.lowercase()
            val hardware = Build.HARDWARE.lowercase()
            val product = Build.PRODUCT.lowercase()
            val brand = Build.BRAND.lowercase()
            val device = Build.DEVICE.lowercase()

            return (fingerprint.startsWith("generic") && brand.startsWith("generic")) ||
                    model.contains("google_sdk") ||
                    model.contains("emulator") ||
                    model.contains("android sdk built for") ||
                    manufacturer.contains("genymotion") ||
                    hardware.contains("goldfish") ||
                    hardware.contains("ranchu") ||
                    hardware.contains("cutf") ||
                    product.contains("sdk_gphone") ||
                    product.contains("google_sdk") ||
                    (brand.startsWith("generic") && device.startsWith("generic"))
        }
    }
}
