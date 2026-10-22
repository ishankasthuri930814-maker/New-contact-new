package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.MyApplicationTheme

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

    var currentScreen by remember { mutableStateOf<AppDestination>(AppDestination.MAIN_DIRECTORY) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val policeRepository = remember(context) { PoliceRepository(context.applicationContext) }
    val policeViewModel: PoliceViewModel = viewModel(
        factory = PoliceViewModel.Factory(policeRepository)
    )

    when (currentScreen) {
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
        AppDestination.LOGIN -> {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    currentScreen = AppDestination.MAIN_DIRECTORY
                }
            )
        }
        AppDestination.PHONE_AUTH -> {
            val phoneAuthViewModel: PhoneAuthViewModel = viewModel()
            PhoneAuthScreen(
                viewModel = phoneAuthViewModel,
                onNavigateBack = {
                    currentScreen = AppDestination.MAIN_DIRECTORY
                }
            )
        }
        AppDestination.ADMIN_PANEL -> {
            AdminPanelScreen(
                viewModel = policeViewModel,
                onNavigateBack = {
                    currentScreen = AppDestination.MAIN_DIRECTORY
                }
            )
        }
    }
}
