package com.example.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.PoliceNavyLight

data class CountryCodeOption(val name: String, val code: String, val flag: String)

val countryCodeList = listOf(
    CountryCodeOption("Sri Lanka", "+94", "🇱🇰"),
    CountryCodeOption("USA / Canada", "+1", "🇺🇸"),
    CountryCodeOption("United Kingdom", "+44", "🇬🇧"),
    CountryCodeOption("India", "+91", "🇮🇳"),
    CountryCodeOption("Australia", "+61", "🇦🇺"),
    CountryCodeOption("UAE", "+971", "🇦🇪")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    viewModel: PhoneAuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    var countryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ගිණුමට ප්‍රවේශ වන්න / Login",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Sri Lanka Police Official Portal Authentication",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("navigate_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PoliceNavy
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = PoliceNavy,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(PoliceGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = PoliceNavy,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "ශ්‍රී ලංකා පොලිස් තොරතුරු පද්ධතිය",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PoliceGold,
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ඔබගේ ගිණුම සත්‍යාපනය කර ප්‍රවේශ වන්න",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "Multi-Option Firebase Authentication",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.isAuthenticated) {
                    // AUTHENTICATED SUCCESS STATE
                    AuthenticatedSuccessCard(
                        displayName = uiState.userDisplayName,
                        email = uiState.userEmail,
                        phoneNumber = uiState.userPhoneNumber,
                        userUid = uiState.userUid ?: "",
                        onContinue = onNavigateBack,
                        onSignOut = { viewModel.signOut() }
                    )
                } else {
                    // TAB SELECTION CONTROLLER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(4.dp)
                    ) {
                        val tabs = listOf("📱 Phone OTP", "✉️ Email", "🌐 Google")
                        tabs.forEachIndexed { index, label ->
                            val isSelected = uiState.selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PoliceNavy else Color.Transparent)
                                    .clickable { viewModel.onTabSelected(index) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    when (uiState.selectedTab) {
                        0 -> {
                            // TAB 0: PHONE OTP
                            if (!uiState.isCodeSent) {
                                // STEP 1: PHONE NUMBER INPUT
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    shadowElevation = 2.dp,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = PoliceNavy,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "දුරකථන අංකය මගින් ප්‍රවේශ වන්න",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PoliceNavy
                                                )
                                            )
                                        }

                                        Text(
                                            text = "Enter your mobile phone number to receive a 6-digit SMS verification code.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Country Code Dropdown selector
                                            Box {
                                                Surface(
                                                    onClick = { countryDropdownExpanded = true },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color(0xFFF1F5F9),
                                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                    modifier = Modifier.testTag("country_code_dropdown")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(
                                                            horizontal = 12.dp,
                                                            vertical = 14.dp
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        val selectedOption = countryCodeList.find { it.code == uiState.countryCode } ?: countryCodeList.first()
                                                        Text(
                                                            text = "${selectedOption.flag} ${selectedOption.code}",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = PoliceNavy
                                                            )
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.ArrowDropDown,
                                                            contentDescription = "Select country code",
                                                            tint = PoliceNavy
                                                        )
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = countryDropdownExpanded,
                                                    onDismissRequest = { countryDropdownExpanded = false }
                                                ) {
                                                    countryCodeList.forEach { option ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text("${option.flag} ${option.name} (${option.code})")
                                                            },
                                                            onClick = {
                                                                viewModel.onCountryCodeChange(option.code)
                                                                countryDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            // Phone Number TextField
                                            OutlinedTextField(
                                                value = uiState.phoneNumberInput,
                                                onValueChange = { viewModel.onPhoneNumberChange(it) },
                                                placeholder = { Text("77 123 4567") },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PoliceNavy,
                                                    fontSize = 16.sp
                                                ),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Phone,
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        keyboardController?.hide()
                                                        activity?.let { viewModel.sendOtp(it) }
                                                    }
                                                ),
                                                trailingIcon = {
                                                    if (uiState.phoneNumberInput.isNotEmpty()) {
                                                        IconButton(onClick = { viewModel.onPhoneNumberChange("") }) {
                                                            Icon(
                                                                imageVector = Icons.Default.Clear,
                                                                contentDescription = "Clear",
                                                                tint = PoliceNavy
                                                            )
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("phone_input"),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = PoliceNavy,
                                                    unfocusedTextColor = PoliceNavy,
                                                    focusedContainerColor = Color(0xFFF8F9FA),
                                                    unfocusedContainerColor = Color(0xFFF8F9FA),
                                                    focusedBorderColor = PoliceNavy,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Button(
                                            onClick = {
                                                keyboardController?.hide()
                                                activity?.let { viewModel.sendOtp(it) }
                                            },
                                            enabled = !uiState.isLoading && uiState.phoneNumberInput.isNotBlank(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("send_otp_button"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PoliceNavy,
                                                disabledContainerColor = PoliceNavy.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            if (uiState.isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(22.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.5.dp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text("කේතය යවමින්... / Sending OTP...")
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Security,
                                                    contentDescription = null,
                                                    tint = PoliceGold,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "OTP කේතය ලබාගන්න (Get OTP)",
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // STEP 2: ENTER OTP CODE
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    shadowElevation = 2.dp,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = PoliceNavy,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "OTP කේතය ඇතුළත් කරන්න",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PoliceNavy
                                                )
                                            )
                                        }

                                        Text(
                                            text = "SMS මගින් ලැබුණු ඉලක්කම් 6ක කේතය පහතින් ඇතුළත් කරන්න",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp, bottom = 12.dp)
                                        )

                                        // Phone number badge
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color(0xFFEFF6FF),
                                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Sent to: ${viewModel.getFormattedFullPhoneNumber()}",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = PoliceNavy
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "වෙනස් කරන්න",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = PoliceNavyLight,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.clickable {
                                                        viewModel.resetToPhoneNumberStep()
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // OTP PIN Visual Display
                                        OtpInputBoxes(
                                            code = uiState.otpCodeInput,
                                            onCodeChange = { viewModel.onOtpCodeChange(it) },
                                            modifier = Modifier.testTag("otp_input")
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = {
                                                keyboardController?.hide()
                                                viewModel.verifyOtpCode()
                                            },
                                            enabled = !uiState.isLoading && uiState.otpCodeInput.length == 6,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("verify_otp_button"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PoliceNavy,
                                                disabledContainerColor = PoliceNavy.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            if (uiState.isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(22.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.5.dp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text("තහවුරු කරමින්... / Verifying...")
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = PoliceGold,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "තහවුරු කරන්න (Verify Code)",
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Resend OTP button with timer
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (uiState.timerSeconds > 0) {
                                                Text(
                                                    text = "නැවත කේතය ලබා ගැනීමට තත්පර ${uiState.timerSeconds} රැඳී සිටින්න (${uiState.timerSeconds}s)",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            } else {
                                                OutlinedButton(
                                                    onClick = {
                                                        activity?.let { viewModel.resendOtp(it) }
                                                    },
                                                    modifier = Modifier.testTag("resend_otp_button"),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, PoliceNavy)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = null,
                                                        tint = PoliceNavy,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "නැවත OTP යවන්න (Resend OTP)",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = PoliceNavy
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // TAB 1: EMAIL & PASSWORD AUTHENTICATION
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                shadowElevation = 2.dp,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = PoliceNavy,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (uiState.isSignUpMode) "නව ගිණුමක් තනන්න (Sign Up)" else "ඊමේල් මගින් ඇතුළු වන්න (Email Sign In)",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PoliceNavy
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Email input
                                    OutlinedTextField(
                                        value = uiState.emailInput,
                                        onValueChange = { viewModel.onEmailChange(it) },
                                        label = { Text("විද්‍යුත් තැපෑල (Email Address)") },
                                        placeholder = { Text("example@gmail.com") },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = null,
                                                tint = PoliceNavy
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Next
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("email_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = PoliceNavy,
                                            unfocusedTextColor = PoliceNavy,
                                            focusedBorderColor = PoliceNavy,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Password input
                                    OutlinedTextField(
                                        value = uiState.passwordInput,
                                        onValueChange = { viewModel.onPasswordChange(it) },
                                        label = { Text("මුරපදය (Password)") },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = PoliceNavy
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                                Icon(
                                                    imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle Password",
                                                    tint = PoliceNavy
                                                )
                                            }
                                        },
                                        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                viewModel.submitEmailPasswordAuth()
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("password_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = PoliceNavy,
                                            unfocusedTextColor = PoliceNavy,
                                            focusedBorderColor = PoliceNavy,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )

                                    if (!uiState.isSignUpMode) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = { viewModel.sendPasswordResetEmail() },
                                                modifier = Modifier.testTag("forgot_password_button")
                                            ) {
                                                Text(
                                                    text = "මුරපදය අමතක වුණාද? (Forgot Password?)",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = PoliceNavyLight,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }

                                    Button(
                                        onClick = {
                                            keyboardController?.hide()
                                            viewModel.submitEmailPasswordAuth()
                                        },
                                        enabled = !uiState.isLoading && uiState.emailInput.isNotBlank() && uiState.passwordInput.isNotBlank(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("submit_email_auth_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy)
                                    ) {
                                        if (uiState.isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = Color.White,
                                                strokeWidth = 2.5.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (uiState.isSignUpMode) Icons.Default.PersonAdd else Icons.Default.AccountCircle,
                                                contentDescription = null,
                                                tint = PoliceGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (uiState.isSignUpMode) "ලියාපදිංචි වන්න (Create Account)" else "ඇතුළු වන්න (Sign In)",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (uiState.isSignUpMode) "දැනටමත් ගිණුමක් තිබේද?" else "තවම ගිණුමක් නැද්ද?",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        TextButton(
                                            onClick = { viewModel.toggleSignUpMode() },
                                            modifier = Modifier.testTag("toggle_sign_up_mode_button")
                                        ) {
                                            Text(
                                                text = if (uiState.isSignUpMode) "ඇතුළු වන්න (Sign In)" else "ලියාපදිංචි වන්න (Sign Up)",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PoliceNavy
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 2: GOOGLE SIGN IN
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                shadowElevation = 2.dp,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF4285F4)
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Google මගින් ප්‍රවේශ වන්න",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PoliceNavy
                                        )
                                    )

                                    Text(
                                        text = "ඔබගේ Google / Gmail ගිණුම භාවිතයෙන් තත්පර ගණනකින් ආරක්ෂිතව සම්බන්ධ වන්න.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.signInWithGoogle(context)
                                        },
                                        enabled = !uiState.isLoading,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("google_sign_in_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1E293B)
                                        )
                                    ) {
                                        if (uiState.isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = Color.White,
                                                strokeWidth = 2.5.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("සම්බන්ධ වෙමින්... / Connecting...")
                                        } else {
                                            Text(
                                                text = "🌐 Google Sign-In මගින් පිවිසෙන්න",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpInputBoxes(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Hidden TextField overlay to catch soft keyboard inputs
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Transparent,
                unfocusedTextColor = Color.Transparent
            )
        )

        // Visible 6 box layout
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                val char = code.getOrNull(i)?.toString() ?: ""
                val isFocused = i == code.length

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isFocused) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = when {
                                isFocused -> PoliceNavy
                                char.isNotEmpty() -> PoliceNavy
                                else -> Color(0xFFCBD5E1)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PoliceNavy,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedSuccessCard(
    displayName: String?,
    email: String?,
    phoneNumber: String?,
    userUid: String,
    onContinue: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "සාර්ථකව තහවුරු විය!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PoliceNavy,
                    fontSize = 18.sp
                )
            )

            Text(
                text = "Authenticated Successfully via Firebase",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (!displayName.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Name:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PoliceNavy
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (!email.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Email:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = email,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PoliceNavy
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (!phoneNumber.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Phone:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = phoneNumber,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PoliceNavy
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "UID:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = userUid.take(12) + "...",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PoliceNavy
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("continue_to_directory_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy)
            ) {
                Text(
                    text = "පොලිස් අංක ලැයිස්තුවට යන්න (Go to Directory)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
                Text(
                    text = "ඉවත් වන්න (Sign Out)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                )
            }
        }
    }
}
