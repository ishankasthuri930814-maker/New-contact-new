package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.AutoLoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAutoLogin()
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
        repository.logout()
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

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
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
