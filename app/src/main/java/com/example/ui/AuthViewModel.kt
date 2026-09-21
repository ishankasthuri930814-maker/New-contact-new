package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.AutoLoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class AuthUiState(
    val usernameInput: String = "",
    val passwordInput: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isCheckingAutoLogin: Boolean = true,
    val isAuthenticated: Boolean = false,
    val loggedInUsername: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAutoLogin()
    }

    // Sign Up ක්‍රියාවලිය
    fun registerUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("කරුණාකර Email සහ Password ඇතුළත් කරන්න")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signUpWithEmail(email, pass)
            result.onSuccess { user ->
                _authState.value = AuthState.Success(user)
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        loggedInUsername = user?.email ?: email,
                        successMessage = "සාර්ථකව ලියාපදිංචි විය (Registered Successfully)"
                    )
                }
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "ලියාපදිංචි වීමේ දෝෂයක් සිදු විය")
            }
        }
    }

    // Sign In ක්‍රියාවලිය
    fun loginUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("කරුණාකර Email සහ Password ඇතුළත් කරන්න")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signInWithEmail(email, pass)
            result.onSuccess { user ->
                _authState.value = AuthState.Success(user)
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        loggedInUsername = user?.email ?: email,
                        successMessage = "සාර්ථකව ඇතුළු විය (Login Successful)"
                    )
                }
                startActiveSessionMonitoring()
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "ඇතුළු වීමේ දෝෂයක් සිදු විය")
            }
        }
    }

    // Google මගින් ඇතුළු වීම / Sign Up වීම (Credential Manager)
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val hostActivity: Activity? = generateSequence(context) {
                    if (it is ContextWrapper) it.baseContext else null
                }.filterIsInstance<Activity>().firstOrNull()

                val credentialContext = hostActivity ?: context
                val credentialManager = CredentialManager.create(credentialContext)

                val serverClientId = try {
                    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                    if (resId != 0) context.getString(resId) else "880156476376-m6jm67tkk7u5h5d4ikmvmclh0mmbdgj9.apps.googleusercontent.com"
                } catch (e: Exception) {
                    "880156476376-m6jm67tkk7u5h5d4ikmvmclh0mmbdgj9.apps.googleusercontent.com"
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(credentialContext, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdToken.idToken

                    val authResult = repository.signInWithGoogle(idToken)
                    authResult.onSuccess { user ->
                        _authState.value = AuthState.Success(user)
                        _uiState.update {
                            it.copy(
                                isAuthenticated = true,
                                loggedInUsername = user?.email ?: user?.displayName ?: "Google User",
                                successMessage = "Google මගින් සාර්ථකව ඇතුළු විය (Google Sign-In Successful)"
                            )
                        }
                    }.onFailure { ex ->
                        _authState.value = AuthState.Error(ex.localizedMessage ?: "Google මගින් ඇතුළු වීමේ දෝෂයක් සිදු විය")
                    }
                } else {
                    _authState.value = AuthState.Error("Google ගිණුම් විස්තර ලබාගත නොහැකි විය")
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                val msg = if (errorMsg.contains("cancel", ignoreCase = true) || errorMsg.contains("USER_CANCELED", ignoreCase = true)) {
                    "Google පිවිසුම අවලංගු කරන ලදී (Cancelled)"
                } else if (errorMsg.contains("No credentials", ignoreCase = true)) {
                    "දුරකථනයේ Google ගිණුමක් සොයාගත නොහැකි විය. කරුණාකර ඔබගේ Google ගිණුම තහවුරු කර නැවත උත්සාහ කරන්න."
                } else {
                    e.localizedMessage ?: "Google මගින් ඇතුළු වීමේ දෝෂයක් සිදු විය"
                }
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signInWithGoogle(idToken)
            result.onSuccess { user ->
                _authState.value = AuthState.Success(user)
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        loggedInUsername = user?.email ?: user?.displayName,
                        successMessage = "Google මගින් සාර්ථකව ඇතුළු විය"
                    )
                }
            }.onFailure { ex ->
                _authState.value = AuthState.Error(ex.localizedMessage ?: "Google මගින් ඇතුළු වීමේ දෝෂයක් සිදු විය")
            }
        }
    }

    fun checkAutoLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAutoLogin = true, errorMessage = null) }
            val result = repository.checkAutoLogin()
            when (result) {
                is AutoLoginResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isCheckingAutoLogin = false,
                            isAuthenticated = true,
                            loggedInUsername = result.username,
                            errorMessage = null
                        )
                    }
                    startActiveSessionMonitoring()
                }
                is AutoLoginResult.CredentialsRevoked -> {
                    _uiState.update {
                        it.copy(
                            isCheckingAutoLogin = false,
                            isAuthenticated = false,
                            loggedInUsername = null,
                            errorMessage = result.reason
                        )
                    }
                }
                is AutoLoginResult.NotLoggedIn, AutoLoginResult.Idle -> {
                    _uiState.update {
                        it.copy(
                            isCheckingAutoLogin = false,
                            isAuthenticated = false,
                            loggedInUsername = null
                        )
                    }
                }
                is AutoLoginResult.Checking -> {}
            }
        }
    }

    private var activeSessionJob: kotlinx.coroutines.Job? = null

    fun startActiveSessionMonitoring() {
        activeSessionJob?.cancel()
        activeSessionJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000L)
                if (!_uiState.value.isAuthenticated) break
                val status = repository.verifyCurrentUserStatus()
                if (status is com.example.data.repository.UserVerificationResult.Blocked) {
                    val reasonMsg = status.reason
                    logout()
                    _uiState.update {
                        it.copy(
                            errorMessage = reasonMsg
                        )
                    }
                    _authState.value = AuthState.Error(reasonMsg)
                    break
                }
            }
        }
    }

    fun verifyActiveUserImmediately() {
        viewModelScope.launch {
            if (!_uiState.value.isAuthenticated) return@launch
            val status = repository.verifyCurrentUserStatus()
            if (status is com.example.data.repository.UserVerificationResult.Blocked) {
                val reasonMsg = status.reason
                logout()
                _uiState.update {
                    it.copy(
                        errorMessage = reasonMsg
                    )
                }
                _authState.value = AuthState.Error(reasonMsg)
            }
        }
    }

    fun onUsernameChange(newUsername: String) {
        _uiState.update { it.copy(usernameInput = newUsername, errorMessage = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(passwordInput = newPassword, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val username = _uiState.value.usernameInput
        val password = _uiState.value.passwordInput

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.login(username, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            loggedInUsername = result.username,
                            errorMessage = null,
                            successMessage = "සාර්ථකව ඇතුළු විය (Login Successful)"
                        )
                    }
                }
                is AuthResult.InvalidCredentials -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "පරිශීලක නාමය හෝ මුරපදය වැරදියි (Incorrect Username or Password)"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        activeSessionJob?.cancel()
        activeSessionJob = null
        repository.signOut()
        _authState.value = AuthState.Idle
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                loggedInUsername = null,
                usernameInput = "",
                passwordInput = "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun getSavedCredentials(): Pair<String?, String?> {
        return Pair(repository.currentUsername, repository.currentPassword)
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
