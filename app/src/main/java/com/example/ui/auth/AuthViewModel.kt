package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.LoggedInUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val studentIdentifierInput: String = "",
    val studentPasswordInput: String = "",
    val studentOtpInput: String = "",
    val isOtpPromptVisible: Boolean = false,
    val otpTargetEmail: String = "",
    val staffEmpIdInput: String = "",
    val staffPasswordInput: String = "",
    val errorMessage: String? = null,
    val attemptsLeft: Int? = null,
    val isAccountLocked: Boolean = false,
    val lockMessage: String? = null,
    val infoMessage: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordInput: String = "",
    val forgotPasswordSuccessMsg: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentSession: StateFlow<LoggedInUser?> = authRepository.currentSession

    fun onStudentIdentifierChanged(value: String) {
        _uiState.update { it.copy(studentIdentifierInput = value, errorMessage = null) }
    }

    fun onStudentPasswordChanged(value: String) {
        _uiState.update { it.copy(studentPasswordInput = value, errorMessage = null) }
    }

    fun onStudentOtpChanged(value: String) {
        _uiState.update { it.copy(studentOtpInput = value, errorMessage = null) }
    }

    fun onStaffEmpIdChanged(value: String) {
        _uiState.update { it.copy(staffEmpIdInput = value, errorMessage = null) }
    }

    fun onStaffPasswordChanged(value: String) {
        _uiState.update { it.copy(staffPasswordInput = value, errorMessage = null) }
    }

    fun setForgotPasswordDialogVisible(visible: Boolean) {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = visible,
                forgotPasswordSuccessMsg = null,
                errorMessage = null
            )
        }
    }

    fun onForgotPasswordInputChanged(value: String) {
        _uiState.update { it.copy(forgotPasswordInput = value) }
    }

    fun submitForgotPassword() {
        val input = _uiState.value.forgotPasswordInput.trim()
        if (input.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your Register Number, Employee ID, or Email") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.forgotPassword(input)
            result.onSuccess { msg ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        forgotPasswordSuccessMsg = msg
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Could not process password reset."
                    )
                }
            }
        }
    }

    fun loginStudent() {
        val identifier = _uiState.value.studentIdentifierInput.trim()
        val password = _uiState.value.studentPasswordInput
        val otp = if (_uiState.value.isOtpPromptVisible) _uiState.value.studentOtpInput else null

        if (identifier.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your Register Number or Email.") }
            return
        }
        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.loginStudent(identifier, password, otp)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            isOtpPromptVisible = false,
                            studentPasswordInput = "",
                            studentOtpInput = ""
                        )
                    }
                }
                is AuthResult.OtpRequired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOtpPromptVisible = true,
                            otpTargetEmail = result.registeredEmail,
                            infoMessage = "First login detected! An OTP has been sent to ${result.registeredEmail}. Enter OTP (Demo: 123456)."
                        )
                    }
                }
                is AuthResult.AccountLocked -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAccountLocked = true,
                            lockMessage = result.message,
                            errorMessage = result.message
                        )
                    }
                }
                is AuthResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error,
                            attemptsLeft = result.attemptsLeft
                        )
                    }
                }
            }
        }
    }

    fun loginStaff() {
        val empId = _uiState.value.staffEmpIdInput.trim()
        val password = _uiState.value.staffPasswordInput

        if (empId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your Employee ID.") }
            return
        }
        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your staff password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.loginStaff(empId, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            staffPasswordInput = ""
                        )
                    }
                }
                is AuthResult.AccountLocked -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAccountLocked = true,
                            lockMessage = result.message,
                            errorMessage = result.message
                        )
                    }
                }
                is AuthResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error,
                            attemptsLeft = result.attemptsLeft
                        )
                    }
                }
                is AuthResult.OtpRequired -> {
                    // Not expected for staff
                }
            }
        }
    }

    fun fillDemoCredentials(role: UserRole) {
        when (role) {
            UserRole.STUDENT -> {
                _uiState.update {
                    it.copy(
                        studentIdentifierInput = "REG2024001",
                        studentPasswordInput = "Student@123",
                        isOtpPromptVisible = false,
                        errorMessage = null
                    )
                }
            }
            UserRole.ADMIN -> {
                _uiState.update {
                    it.copy(
                        staffEmpIdInput = "ADM001",
                        staffPasswordInput = "Admin@123",
                        errorMessage = null
                    )
                }
            }
            UserRole.FACULTY -> {
                _uiState.update {
                    it.copy(
                        staffEmpIdInput = "FAC001",
                        staffPasswordInput = "Faculty@123",
                        errorMessage = null
                    )
                }
            }
            UserRole.ACCOUNTS -> {
                _uiState.update {
                    it.copy(
                        staffEmpIdInput = "ACC001",
                        staffPasswordInput = "Staff@123",
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    studentPasswordInput = "",
                    staffPasswordInput = "",
                    errorMessage = null,
                    isOtpPromptVisible = false
                )
            }
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
