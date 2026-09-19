package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class PhoneAuthState(
    // Mode selection: 0 = Phone OTP, 1 = Email & Password, 2 = Google
    val selectedTab: Int = 0,

    // Phone OTP fields
    val countryCode: String = "+94",
    val phoneNumberInput: String = "",
    val otpCodeInput: String = "",
    val isCodeSent: Boolean = false,
    val verificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,

    // Email & Password fields
    val emailInput: String = "",
    val passwordInput: String = "",
    val isSignUpMode: Boolean = false,
    val isPasswordVisible: Boolean = false,

    // Auth status & user details
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val userPhoneNumber: String? = null,
    val userPhotoUrl: String? = null,
    val userUid: String? = null,
    val timerSeconds: Int = 0
)

class PhoneAuthViewModel : ViewModel() {

    private fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            Log.e("PhoneAuthViewModel", "FirebaseAuth init error", t)
            null
        }
    }

    private val webClientId = "880156476376-m6jm67tkk7u5h5d4ikmvmclh0mmbdgj9.apps.googleusercontent.com"

    private val _uiState = MutableStateFlow(PhoneAuthState())
    val uiState: StateFlow<PhoneAuthState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        try {
            val authInstance = getFirebaseAuth()
            val currentUser = authInstance?.currentUser
            if (currentUser != null) {
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        userDisplayName = currentUser.displayName,
                        userEmail = currentUser.email,
                        userPhoneNumber = currentUser.phoneNumber,
                        userPhotoUrl = currentUser.photoUrl?.toString(),
                        userUid = currentUser.uid
                    )
                }
            } else {
                _uiState.update { it.copy(isAuthenticated = false) }
            }
        } catch (e: Exception) {
            Log.e("PhoneAuthViewModel", "Error checking current user", e)
            _uiState.update { it.copy(isAuthenticated = false) }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update {
            it.copy(
                selectedTab = tabIndex,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // --- EMAIL & PASSWORD AUTHENTICATION ---
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(emailInput = email.trim(), errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(passwordInput = password, errorMessage = null) }
    }

    fun toggleSignUpMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submitEmailPasswordAuth() {
        val email = _uiState.value.emailInput
        val password = _uiState.value.passwordInput
        val isSignUp = _uiState.value.isSignUpMode

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "කරුණාකර නිවැරදි විද්‍යුත් තැපැල් ලිපිනයක් (Email) ඇතුළත් කරන්න") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "මුරපදය අවම වශයෙන් අක්ෂර 6ක් විය යුතුය (Password must be at least 6 chars)") }
            return
        }

        val authInstance = getFirebaseAuth()
        if (authInstance == null) {
            _uiState.update { it.copy(errorMessage = "Firebase Auth සේවාව සම්බන්ධ කරගත නොහැකි විය.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        if (isSignUp) {
            authInstance.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = authInstance.currentUser
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                userEmail = user?.email ?: email,
                                userUid = user?.uid,
                                successMessage = "ගිණුම සාර්ථකව සෑදිණි! (Account created successfully)",
                                errorMessage = null
                            )
                        }
                    } else {
                        val errorMsg = task.exception?.localizedMessage
                            ?: "ගිණුම සෑදීම අසාර්ථක විය. නැවත උත්සාහ කරන්න."
                        _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                    }
                }
        } else {
            authInstance.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = authInstance.currentUser
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                userEmail = user?.email ?: email,
                                userDisplayName = user?.displayName,
                                userUid = user?.uid,
                                successMessage = "සාර්ථකව ප්‍රවේශ විය! (Signed in successfully)",
                                errorMessage = null
                            )
                        }
                    } else {
                        val errorMsg = task.exception?.localizedMessage
                            ?: "ප්‍රවේශ වීම අසාර්ථක විය. Email හෝ Password පරීක්ෂා කරන්න."
                        _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                    }
                }
        }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.emailInput
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "මුරපදය නැවත සකස් කිරීමට කරුණාකර Email ලිපිනය ඇතුළත් කරන්න") }
            return
        }

        val authInstance = getFirebaseAuth()
        if (authInstance == null) {
            _uiState.update { it.copy(errorMessage = "Firebase Auth සේවාව සම්බන්ධ කරගත නොහැකි විය.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        authInstance.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "මුරපදය සකසන ලිංක් එක $email වෙත යවන ලදී (Password reset email sent)"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = task.exception?.localizedMessage ?: "Email යැවීම අසාර්ථක විය."
                        )
                    }
                }
            }
    }

    // --- GOOGLE SIGN IN VIA CREDENTIAL MANAGER ---
    fun signInWithGoogle(context: Context) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(request = request, context = context)

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                    val authInstance = getFirebaseAuth()
                    if (authInstance == null) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Firebase Auth සේවාව සම්බන්ධ කරගත නොහැකි විය.") }
                        return@launch
                    }

                    authInstance.signInWithCredential(authCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = authInstance.currentUser
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isAuthenticated = true,
                                        userDisplayName = user?.displayName,
                                        userEmail = user?.email,
                                        userPhotoUrl = user?.photoUrl?.toString(),
                                        userUid = user?.uid,
                                        successMessage = "Google මගින් සාර්ථකව සම්බන්ධ විය! (Signed in with Google)",
                                        errorMessage = null
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = task.exception?.localizedMessage ?: "Google Sign-In අසාර්ථක විය."
                                    )
                                }
                            }
                        }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "නොදන්නා Google ගිණුම් තොරතුරක්"
                        )
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d("PhoneAuthViewModel", "User cancelled Google Sign In")
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e("PhoneAuthViewModel", "Google Sign In Error", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Google Sign-In හි දෝෂයක් සිදුවිය."
                    )
                }
            }
        }
    }

    // --- PHONE OTP AUTHENTICATION ---
    fun onCountryCodeChange(code: String) {
        _uiState.update { it.copy(countryCode = code, errorMessage = null) }
    }

    fun onPhoneNumberChange(number: String) {
        val cleanNumber = number.filter { it.isDigit() || it == ' ' }
        _uiState.update { it.copy(phoneNumberInput = cleanNumber, errorMessage = null) }
    }

    fun onOtpCodeChange(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _uiState.update { it.copy(otpCodeInput = code, errorMessage = null) }
            if (code.length == 6) {
                verifyOtpCode(code)
            }
        }
    }

    fun getFormattedFullPhoneNumber(): String {
        val state = _uiState.value
        var raw = state.phoneNumberInput.replace(" ", "").trim()
        if (raw.startsWith("0")) {
            raw = raw.substring(1)
        }
        return "${state.countryCode}$raw"
    }

    fun sendOtp(activity: Activity) {
        val fullPhoneNumber = getFormattedFullPhoneNumber()
        if (fullPhoneNumber.length < 9) {
            _uiState.update { it.copy(errorMessage = "කරුණාකර නිවැරදි දුරකථන අංකයක් ඇතුළත් කරන්න (Please enter a valid phone number)") }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("PhoneAuthViewModel", "onVerificationFailed", e)
                val msg = e.localizedMessage ?: e.message ?: ""
                val friendlyMessage = if (msg.contains("SMS unable to be sent") || msg.contains("not allowed") || msg.contains("17006")) {
                    "දුරකථන අංක සත්‍යාපනය සක්‍රීය කර නොමැත. කරුණාකර Email හෝ Google මගින් Sign in වන්න. (Phone Auth region disabled in Firebase console)"
                } else {
                    msg.ifBlank { "OTP යැවීම අසාර්ථක විය. නැවත උත්සාහ කරන්න." }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = friendlyMessage
                    )
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCodeSent = true,
                        verificationId = verificationId,
                        resendToken = token,
                        successMessage = "OTP කේතය ඔබගේ දුරකථනයට යවන ලදී",
                        errorMessage = null
                    )
                }
                startTimer()
            }
        }

        val authInstance = getFirebaseAuth()
        if (authInstance == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Firebase Auth සේවාව සම්බන්ධ කරගත නොහැකි විය.") }
            return
        }

        val options = PhoneAuthOptions.newBuilder(authInstance)
            .setPhoneNumber(fullPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendOtp(activity: Activity) {
        val state = _uiState.value
        val resendToken = state.resendToken
        val fullPhoneNumber = getFormattedFullPhoneNumber()

        if (resendToken == null) {
            sendOtp(activity)
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "නැවත OTP යැවීම අසාර්ථක විය"
                    )
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verificationId = verificationId,
                        resendToken = token,
                        successMessage = "අලුත් OTP කේතය ඔබගේ දුරකථනයට යවන ලදී",
                        errorMessage = null
                    )
                }
                startTimer()
            }
        }

        val authInstance = getFirebaseAuth()
        if (authInstance == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Firebase Auth සේවාව සම්බන්ධ කරගත නොහැකි විය.") }
            return
        }

        val options = PhoneAuthOptions.newBuilder(authInstance)
            .setPhoneNumber(fullPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtpCode(code: String = _uiState.value.otpCodeInput) {
        val verificationId = _uiState.value.verificationId
        if (verificationId.isNullOrEmpty()) {
            _uiState.update { it.copy(errorMessage = "OTP කේතය නොලැබුණි. නැවත කේතය ලබාගන්න.") }
            return
        }
        if (code.length != 6) {
            _uiState.update { it.copy(errorMessage = "කරුණාකර ඉලක්කම් 6ක OTP කේතය සම්පූර්ණයෙන් ඇතුළත් කරන්න") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val authInstance = getFirebaseAuth()
        if (authInstance == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Firebase Auth සේවාව සම්බන්ධ කරගත නොහැකි විය.") }
            return
        }

        authInstance.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = authInstance.currentUser
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            userPhoneNumber = user?.phoneNumber ?: getFormattedFullPhoneNumber(),
                            userUid = user?.uid,
                            successMessage = "දුරකථන අංකය සාර්ථකව තහවුරු විය! (Verified successfully)",
                            errorMessage = null
                        )
                    }
                } else {
                    val exception = task.exception
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception?.localizedMessage ?: "තහවුරු කිරීම අසාර්ථක විය. OTP කේතය වැරදියි."
                        )
                    }
                }
            }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerSeconds = 60) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
            }
        }
    }

    fun resetToPhoneNumberStep() {
        _uiState.update {
            it.copy(
                isCodeSent = false,
                otpCodeInput = "",
                errorMessage = null,
                successMessage = null,
                timerSeconds = 0
            )
        }
        timerJob?.cancel()
    }

    fun signOut() {
        try {
            getFirebaseAuth()?.signOut()
        } catch (e: Exception) {
            Log.e("PhoneAuthViewModel", "Error signing out", e)
        }
        timerJob?.cancel()
        _uiState.value = PhoneAuthState()
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
