package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aistudio.policedirectory.zxklm.R
import com.example.ui.theme.PoliceBlueAccent
import com.example.ui.theme.PoliceGold
import com.example.ui.theme.PoliceGoldLight
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.PoliceNavyLight

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    // Gradient background matching official Sri Lanka Police theme
    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF071426),
            Color(0xFF0F2A4A),
            Color(0xFF1E3A5F)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(headerGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Brand Header: Police Emblem & Official Title
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(12.dp, CircleShape)
                    .border(2.5.dp, Brush.linearGradient(listOf(PoliceGoldLight, PoliceGold)), CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.police_app_icon_1785650919319),
                    contentDescription = "Sri Lanka Police Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ශ්‍රී ලංකා පොලිස් නාමාවලිය",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sri Lanka Police Official Directory",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = PoliceGoldLight.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Main Authentication Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Segmented Mode Selector: Sign In vs Sign Up
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            // Sign In Tab
                            val isSignIn = authMode == AuthMode.SIGN_IN
                            val signInBg by animateColorAsState(
                                targetValue = if (isSignIn) PoliceNavy else Color.Transparent,
                                animationSpec = tween(250),
                                label = "signInBg"
                            )
                            val signInTextColor by animateColorAsState(
                                targetValue = if (isSignIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = tween(250),
                                label = "signInTextColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(signInBg)
                                    .clickable(enabled = !isLoading) {
                                        authMode = AuthMode.SIGN_IN
                                        localValidationError = null
                                        authViewModel.resetAuthState()
                                    }
                                    .testTag("tab_sign_in"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ඇතුළු වන්න",
                                    fontWeight = if (isSignIn) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = signInTextColor
                                )
                            }

                            // Sign Up Tab
                            val isSignUp = authMode == AuthMode.SIGN_UP
                            val signUpBg by animateColorAsState(
                                targetValue = if (isSignUp) PoliceNavy else Color.Transparent,
                                animationSpec = tween(250),
                                label = "signUpBg"
                            )
                            val signUpTextColor by animateColorAsState(
                                targetValue = if (isSignUp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = tween(250),
                                label = "signUpTextColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(signUpBg)
                                    .clickable(enabled = !isLoading) {
                                        authMode = AuthMode.SIGN_UP
                                        localValidationError = null
                                        authViewModel.resetAuthState()
                                    }
                                    .testTag("tab_sign_up"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ලියාපදිංචි වන්න",
                                    fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = signUpTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localValidationError = null
                        },
                        label = { Text("ඊමේල් ලිපිනය (Email)") },
                        placeholder = { Text("ඔබගේ ඊමේල් ලිපිනය ඇතුළත් කරන්න") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = "Email Icon",
                                tint = PoliceNavyLight
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PoliceNavy,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = PoliceNavy
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localValidationError = null
                        },
                        label = { Text("මුරපදය (Password)") },
                        placeholder = { Text("අවම අකුරු 6 ක්") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password Icon",
                                tint = PoliceNavyLight
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.testTag("toggle_password_visibility")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (authMode == AuthMode.SIGN_UP) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = {
                                focusManager.clearFocus()
                                submitForm(authMode, email, password, confirmPassword, authViewModel) {
                                    localValidationError = it
                                }
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PoliceNavy,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = PoliceNavy
                        )
                    )

                    // Confirm Password Field (Sign Up Only)
                    AnimatedVisibility(
                        visible = authMode == AuthMode.SIGN_UP,
                        enter = fadeIn() + androidx.compose.animation.expandVertically(),
                        exit = fadeOut() + androidx.compose.animation.shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    localValidationError = null
                                },
                                label = { Text("මුරපදය තහවුරු කරන්න (Confirm Password)") },
                                placeholder = { Text("මුරපදය නැවත ඇතුළත් කරන්න") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Confirm Password Icon",
                                        tint = PoliceNavyLight
                                    )
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_password_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        submitForm(authMode, email, password, confirmPassword, authViewModel) {
                                            localValidationError = it
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PoliceNavy,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = PoliceNavy
                                )
                            )
                        }
                    }

                    // Error Message Banner (Local validation or Firebase Auth Error)
                    val activeError = localValidationError ?: (authState as? AuthState.Error)?.message
                    AnimatedVisibility(
                        visible = !activeError.isNullOrBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (!activeError.isNullOrBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button (Sign In / Sign Up)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            submitForm(authMode, email, password, confirmPassword, authViewModel) {
                                localValidationError = it
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag(if (authMode == AuthMode.SIGN_IN) "sign_in_button" else "sign_up_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PoliceNavy,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (authMode == AuthMode.SIGN_IN) Icons.Filled.Login else Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (authMode == AuthMode.SIGN_IN) "ඇතුළු වන්න (Sign In)" else "ගිණුම සාදන්න (Sign Up)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // "OR" Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "හෝ (OR)",
                            modifier = Modifier.padding(horizontal = 14.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Google Sign In / Sign Up Button
                    OutlinedButton(
                        onClick = {
                            localValidationError = null
                            authViewModel.signInWithGoogle(context)
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_signup_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(22.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Google සමඟ සම්බන්ධ වන්න",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Official Trust & Security Badge at Bottom
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Shield",
                    tint = PoliceGoldLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ශ්‍රී ලංකා පොලිස් නිල නාමාවලිය • 119 හදිසි ඇමතුම්",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun submitForm(
    mode: AuthMode,
    email: String,
    pass: String,
    confirmPass: String,
    viewModel: AuthViewModel,
    onError: (String?) -> Unit
) {
    val trimmedEmail = email.trim()
    val trimmedPass = pass.trim()

    if (trimmedEmail.isBlank()) {
        onError("කරුණාකර ඔබගේ ඊමේල් ලිපිනය ඇතුළත් කරන්න")
        return
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
        onError("වලංගු ඊමේල් ලිපිනයක් ඇතුළත් කරන්න (උදා: name@gmail.com)")
        return
    }
    if (trimmedPass.isBlank()) {
        onError("කරුණාකර මුරපදය ඇතුළත් කරන්න")
        return
    }
    if (trimmedPass.length < 6) {
        onError("මුරපදයේ අවම වශයෙන් අක්ෂර 6ක් තිබිය යුතුය")
        return
    }

    if (mode == AuthMode.SIGN_UP) {
        if (confirmPass.trim() != trimmedPass) {
            onError("මුරපද දෙක එකිනෙකට නොගැළපේ (Passwords do not match)")
            return
        }
        onError(null)
        viewModel.registerUser(trimmedEmail, trimmedPass)
    } else {
        onError(null)
        viewModel.loginUser(trimmedEmail, trimmedPass)
    }
}
