package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CollegeDatabase
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.CollegeRepository
import com.example.ui.auth.*
import com.example.ui.portal.StaffPortalHome
import com.example.ui.portal.StudentPortalHome

enum class AppScreen {
    UNIFIED_LOGIN,
    STUDENT_PORTAL,
    STAFF_PORTAL
}

@Composable
fun CollegeApp() {
    val context = LocalContext.current
    val database = remember { CollegeDatabase.getInstance(context) }
    val authRepository = remember {
        AuthRepository(
            userDao = database.userDao(),
            studentDao = database.studentDao(),
            staffDao = database.staffDao(),
            auditDao = database.auditDao()
        )
    }
    val collegeRepository = remember {
        CollegeRepository(
            studentDao = database.studentDao(),
            staffDao = database.staffDao(),
            academicDao = database.academicDao(),
            financeDao = database.financeDao(),
            communicationDao = database.communicationDao(),
            auditDao = database.auditDao()
        )
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )

    val uiState by authViewModel.uiState.collectAsState()
    val currentSession by authViewModel.currentSession.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.UNIFIED_LOGIN) }

    // Synchronize screen state with authenticated session
    LaunchedEffect(currentSession) {
        val session = currentSession
        if (session != null) {
            currentScreen = if (session.user.role == UserRole.STUDENT) {
                AppScreen.STUDENT_PORTAL
            } else {
                AppScreen.STAFF_PORTAL
            }
        } else {
            // When logged out, stay on unified login screen
            currentScreen = AppScreen.UNIFIED_LOGIN
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.UNIFIED_LOGIN -> {
                UnifiedLoginScreen(
                    uiState = uiState,
                    initialRole = PortalRole.STUDENT,
                    onStudentIdentifierChanged = authViewModel::onStudentIdentifierChanged,
                    onStudentPasswordChanged = authViewModel::onStudentPasswordChanged,
                    onStudentOtpChanged = authViewModel::onStudentOtpChanged,
                    onStaffEmpIdChanged = authViewModel::onStaffEmpIdChanged,
                    onStaffPasswordChanged = authViewModel::onStaffPasswordChanged,
                    onLoginStudent = authViewModel::loginStudent,
                    onLoginStaff = authViewModel::loginStaff,
                    onForgotPasswordClick = { authViewModel.setForgotPasswordDialogVisible(true) },
                    onForgotPasswordDismiss = { authViewModel.setForgotPasswordDialogVisible(false) },
                    onForgotPasswordInputChanged = authViewModel::onForgotPasswordInputChanged,
                    onForgotPasswordSubmit = authViewModel::submitForgotPassword,
                    onFillDemoCredentials = authViewModel::fillDemoCredentials
                )
            }

            AppScreen.STUDENT_PORTAL -> {
                currentSession?.let { session ->
                    StudentPortalHome(
                        session = session,
                        collegeRepository = collegeRepository,
                        onLogoutClick = {
                            authViewModel.logout()
                        }
                    )
                }
            }

            AppScreen.STAFF_PORTAL -> {
                currentSession?.let { session ->
                    StaffPortalHome(
                        session = session,
                        collegeRepository = collegeRepository,
                        onLogoutClick = {
                            authViewModel.logout()
                        }
                    )
                }
            }
        }
    }
}
