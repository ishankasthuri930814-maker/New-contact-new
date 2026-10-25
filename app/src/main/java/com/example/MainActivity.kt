package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.data.repository.PoliceRepository
import com.example.ui.AdminPanelScreen
import com.example.ui.AuthState
import com.example.ui.AuthViewModel
import com.example.ui.LoginScreen
import com.example.ui.PhoneAuthScreen
import com.example.ui.PhoneAuthViewModel
import com.example.ui.PoliceScreen
import com.example.ui.PoliceViewModel
import com.aistudio.policedirectory.zxklm.R
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PoliceGold

enum class AppDestination {
    LOGIN,
    MAIN_DIRECTORY,
    PHONE_AUTH,
    ADMIN_PANEL
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            AdMobManager.initialize(application)
        } catch (_: Exception) { }

        setContent {
            MyApplicationTheme {
                MainAppNav()
            }
        }
    }

    companion object {
        fun isEmulator(): Boolean {
            return (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || "google_sdk" == Build.PRODUCT)
        }
    }
}

@Composable
fun MainAppNav() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<AppDestination?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val policeRepository = remember(context) { PoliceRepository(context.applicationContext) }
    val policeViewModel: PoliceViewModel = viewModel(
        factory = PoliceViewModel.Factory(policeRepository)
    )

    // Check auto login on start: route to MAIN_DIRECTORY if logged in, otherwise show LOGIN screen
    androidx.compose.runtime.LaunchedEffect(authUiState.isCheckingAutoLogin, authUiState.isAuthenticated) {
        if (!authUiState.isCheckingAutoLogin && currentScreen == null) {
            currentScreen = if (authUiState.isAuthenticated) {
                AppDestination.MAIN_DIRECTORY
            } else {
                AppDestination.LOGIN
            }
        }
    }

    when (val dest = currentScreen) {
        null -> {
            // Splash / Initializing Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D2240)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.police_launcher_icon),
                        contentDescription = "Sri Lanka Police Directory Emblem",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sri Lanka Police Directory",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ශ්‍රී ලංකා පොලිස් නාමාවලිය",
                        color = PoliceGold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(
                        color = PoliceGold,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }
        AppDestination.LOGIN -> {
            androidx.activity.compose.BackHandler {
                (context as? android.app.Activity)?.finish()
            }
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    currentScreen = AppDestination.MAIN_DIRECTORY
                },
                onSkipLogin = {
                    currentScreen = AppDestination.MAIN_DIRECTORY
                },
                onOpenPhoneAuth = {
                    currentScreen = AppDestination.PHONE_AUTH
                }
            )
        }
        AppDestination.MAIN_DIRECTORY -> {
            val loggedInUser = (authState as? AuthState.Success)?.user?.email
                ?: (authState as? AuthState.Success)?.user?.displayName
                ?: authViewModel.uiState.collectAsStateWithLifecycle().value.loggedInUsername
            PoliceScreen(
                viewModel = policeViewModel,
                currentUser = loggedInUser,
                onLogout = {
                    authViewModel.logout()
                    currentScreen = AppDestination.LOGIN
                },
                onOpenPhoneAuth = {
                    currentScreen = AppDestination.PHONE_AUTH
                },
                onOpenAdminPanel = {
                    currentScreen = AppDestination.ADMIN_PANEL
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        AppDestination.PHONE_AUTH -> {
            androidx.activity.compose.BackHandler {
                currentScreen = AppDestination.MAIN_DIRECTORY
            }
            val phoneAuthViewModel: PhoneAuthViewModel = viewModel()
            PhoneAuthScreen(
                viewModel = phoneAuthViewModel,
                onNavigateBack = {
                    currentScreen = AppDestination.MAIN_DIRECTORY
                }
            )
        }
        AppDestination.ADMIN_PANEL -> {
            androidx.activity.compose.BackHandler {
                currentScreen = AppDestination.MAIN_DIRECTORY
            }
            AdminPanelScreen(
                viewModel = policeViewModel,
                onNavigateBack = {
                    currentScreen = AppDestination.MAIN_DIRECTORY
                }
            )
        }
    }
}
