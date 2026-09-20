package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

enum class PortalRole(val label: String, val badge: String) {
    STUDENT("Student", "Student Portal"),
    FACULTY("Faculty", "Teaching & Clinical"),
    MANAGEMENT("Management", "Admin & Dean"),
    ACCOUNTS("Accounts", "Finance & Billing")
}

@Composable
fun UnifiedLoginScreen(
    uiState: AuthUiState,
    initialRole: PortalRole = PortalRole.STUDENT,
    onStudentIdentifierChanged: (String) -> Unit,
    onStudentPasswordChanged: (String) -> Unit,
    onStudentOtpChanged: (String) -> Unit,
    onStaffEmpIdChanged: (String) -> Unit,
    onStaffPasswordChanged: (String) -> Unit,
    onLoginStudent: () -> Unit,
    onLoginStaff: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onForgotPasswordDismiss: () -> Unit,
    onForgotPasswordInputChanged: (String) -> Unit,
    onForgotPasswordSubmit: () -> Unit,
    onFillDemoCredentials: ((com.example.data.model.UserRole) -> Unit)? = null
) {
    var selectedRole by remember { mutableStateOf(initialRole) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleBackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // SKS Official Emblem Seal
            SksCollegeEmblem(
                size = 96.dp,
                showMotto = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // College Header and Tagline
            Text(
                text = "SKS COLLEGE OF NURSING",
                style = Typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalPrimary,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Learn with Compassion. Serve with Care.",
                style = Typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = NursingTealDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Harur - 636 903 • Dharmapuri Dt., Tamil Nadu",
                style = Typography.bodySmall,
                color = SlateMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 24.dp)
            )

            // Apple Segmented Control for Role Switching
            Text(
                text = "SELECT PORTAL",
                style = Typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SlateSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 4.dp, bottom = 8.dp)
            )

            AppleSegmentedControl(
                items = PortalRole.values().toList(),
                selectedItem = selectedRole,
                onItemSelected = { role ->
                    selectedRole = role
                    focusManager.clearFocus()
                },
                itemLabel = { it.label },
                testTagPrefix = "role_selector"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Login Card according to Selected Role
            AppleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(22.dp)
            ) {
                // Role header pill & description
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = when (selectedRole) {
                                PortalRole.STUDENT -> "Student Sign In"
                                PortalRole.FACULTY -> "Faculty Portal"
                                PortalRole.MANAGEMENT -> "Management Sign In"
                                PortalRole.ACCOUNTS -> "Accounts Portal"
                            },
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalPrimary
                        )
                        Text(
                            text = when (selectedRole) {
                                PortalRole.STUDENT -> "Enter institutional credentials"
                                PortalRole.FACULTY -> "Clinical & teaching faculty access"
                                PortalRole.MANAGEMENT -> "Administrative & director access"
                                PortalRole.ACCOUNTS -> "Finance, fee receipts & billing"
                            },
                            style = Typography.bodySmall,
                            color = SlateSecondary
                        )
                    }

                    AppleBadge(
                        text = selectedRole.badge,
                        containerColor = when (selectedRole) {
                            PortalRole.STUDENT -> NursingTealSubtle
                            PortalRole.FACULTY -> MedicalBlueSubtle
                            PortalRole.MANAGEMENT -> AmberWarningSubtle
                            PortalRole.ACCOUNTS -> EmeraldSuccessSubtle
                        },
                        textColor = when (selectedRole) {
                            PortalRole.STUDENT -> NursingTealDark
                            PortalRole.FACULTY -> MedicalBlue
                            PortalRole.MANAGEMENT -> AmberWarning
                            PortalRole.ACCOUNTS -> EmeraldSuccess
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error / Lock / Warning Alert Banners
                if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CrimsonErrorSubtle)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = CrimsonError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage,
                                style = Typography.bodySmall,
                                color = CrimsonError,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (uiState.attemptsLeft != null && uiState.attemptsLeft in 1..3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberWarningSubtle)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Security notice: ${uiState.attemptsLeft} attempt(s) remaining before account lockout.",
                                style = Typography.bodySmall,
                                color = AmberWarning,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (uiState.infoMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NursingTealSubtle)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.infoMessage,
                            style = Typography.bodySmall,
                            color = NursingTealDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Dynamic Form Fields
                if (selectedRole == PortalRole.STUDENT) {
                    AppleTextField(
                        value = uiState.studentIdentifierInput,
                        onValueChange = onStudentIdentifierChanged,
                        label = "Register Number or Email",
                        placeholder = "e.g. NUR2024001 or student@skscon.edu.in",
                        leadingIcon = Icons.Default.Badge,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_student_identifier"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppleTextField(
                        value = uiState.studentPasswordInput,
                        onValueChange = onStudentPasswordChanged,
                        label = "Password",
                        placeholder = "Enter your password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (uiState.isOtpPromptVisible) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLoginStudent()
                            }
                        ),
                        testTag = "input_student_password"
                    )

                    AnimatedVisibility(visible = uiState.isOtpPromptVisible) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            AppleTextField(
                                value = uiState.studentOtpInput,
                                onValueChange = onStudentOtpChanged,
                                label = "Verification OTP (Sent to Email)",
                                placeholder = "Enter 6-digit OTP",
                                leadingIcon = Icons.Default.VpnKey,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onLoginStudent()
                                    }
                                ),
                                testTag = "input_student_otp"
                            )
                        }
                    }
                } else {
                    // Staff / Faculty / Management / Accounts Form Fields
                    val idLabel = when (selectedRole) {
                        PortalRole.FACULTY -> "Faculty Employee ID"
                        PortalRole.MANAGEMENT -> "Administrator ID"
                        PortalRole.ACCOUNTS -> "Accounts Staff ID"
                        else -> "Employee ID"
                    }
                    val idPlaceholder = when (selectedRole) {
                        PortalRole.FACULTY -> "e.g. FAC001"
                        PortalRole.MANAGEMENT -> "e.g. ADM001"
                        PortalRole.ACCOUNTS -> "e.g. ACC001"
                        else -> "e.g. EMP001"
                    }

                    AppleTextField(
                        value = uiState.staffEmpIdInput,
                        onValueChange = onStaffEmpIdChanged,
                        label = idLabel,
                        placeholder = idPlaceholder,
                        leadingIcon = Icons.Default.Badge,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "input_staff_empid"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppleTextField(
                        value = uiState.staffPasswordInput,
                        onValueChange = onStaffPasswordChanged,
                        label = "Password",
                        placeholder = "Enter staff password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLoginStaff()
                            }
                        ),
                        testTag = "input_staff_password"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Forgot Password text link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onForgotPasswordClick,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Forgot password?",
                            style = Typography.labelMedium,
                            color = NursingTealDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Login Button
                AppleButton(
                    text = when (selectedRole) {
                        PortalRole.STUDENT -> "Sign In to Student Portal"
                        PortalRole.FACULTY -> "Sign In as Faculty"
                        PortalRole.MANAGEMENT -> "Sign In to Admin Portal"
                        PortalRole.ACCOUNTS -> "Sign In to Accounts Portal"
                    },
                    onClick = {
                        focusManager.clearFocus()
                        if (selectedRole == PortalRole.STUDENT) {
                            onLoginStudent()
                        } else {
                            onLoginStaff()
                        }
                    },
                    isLoading = uiState.isLoading,
                    enabled = !uiState.isAccountLocked,
                    icon = Icons.Default.LockOpen,
                    containerColor = NursingTealDark,
                    testTag = "submit_login_button"
                )

                if (onFillDemoCredentials != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            val targetRole = when (selectedRole) {
                                PortalRole.STUDENT -> com.example.data.model.UserRole.STUDENT
                                PortalRole.FACULTY -> com.example.data.model.UserRole.FACULTY
                                PortalRole.MANAGEMENT -> com.example.data.model.UserRole.ADMIN
                                PortalRole.ACCOUNTS -> com.example.data.model.UserRole.ACCOUNTS
                            }
                            onFillDemoCredentials(targetRole)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = NursingTealDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Auto-fill ${selectedRole.label} Demo Credentials",
                            style = Typography.labelMedium,
                            color = NursingTealDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Footer Information
            Text(
                text = "Affiliated to The Tamil Nadu Dr. M.G.R. Medical University\nApproved by Indian Nursing Council (INC) & Tamil Nadu Nurses Council",
                style = Typography.bodySmall,
                color = SlateSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Forgot Password Modal Dialog
        if (uiState.showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = onForgotPasswordDismiss,
                title = {
                    Text(
                        text = "Reset Portal Access",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter your registered email, Register Number (for students), or Employee ID (for staff). A password recovery link will be dispatched securely.",
                            style = Typography.bodyMedium,
                            color = SlateSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AppleTextField(
                            value = uiState.forgotPasswordInput,
                            onValueChange = onForgotPasswordInputChanged,
                            label = "Identifier / Email",
                            placeholder = "e.g. NUR2024001 or user@skscon.edu.in",
                            leadingIcon = Icons.Default.Email,
                            testTag = "input_forgot_password"
                        )

                        if (uiState.forgotPasswordSuccessMsg != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldSuccessSubtle)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = uiState.forgotPasswordSuccessMsg,
                                    style = Typography.bodySmall,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onForgotPasswordSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = NursingTealDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send Recovery Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onForgotPasswordDismiss) {
                        Text("Cancel", color = SlateSecondary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = PureWhite
            )
        }
    }
}
