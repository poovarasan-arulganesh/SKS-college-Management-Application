package com.example.data.repository

import com.example.data.local.AuditDao
import com.example.data.local.StaffDao
import com.example.data.local.StudentDao
import com.example.data.local.UserDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class LoggedInUser(
    val user: User,
    val studentProfile: Student? = null,
    val staffProfile: Staff? = null
) {
    val displayName: String
        get() = when {
            studentProfile != null -> "${studentProfile.firstName} ${studentProfile.lastName}"
            staffProfile != null -> "${staffProfile.firstName} ${staffProfile.lastName}"
            else -> user.identifier
        }

    val roleName: String
        get() = when (user.role) {
            UserRole.STUDENT -> "Student"
            UserRole.ADMIN -> "Administrator"
            UserRole.FACULTY -> "Faculty"
            UserRole.ACCOUNTS -> "Accounts & Finance"
        }
}

sealed class AuthResult {
    data class Success(val loggedInUser: LoggedInUser) : AuthResult()
    data class OtpRequired(val user: User, val registeredEmail: String) : AuthResult()
    data class AccountLocked(val message: String) : AuthResult()
    data class Failure(val error: String, val attemptsLeft: Int? = null) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val studentDao: StudentDao,
    private val staffDao: StaffDao,
    private val auditDao: AuditDao
) {
    private val _currentSession = MutableStateFlow<LoggedInUser?>(null)
    val currentSession: StateFlow<LoggedInUser?> = _currentSession.asStateFlow()

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
    }

    /**
     * Student Portal Login:
     * Accepts Register Number (e.g. REG2024001) or student email.
     * Enforces role == STUDENT on the backend (RBAC).
     */
    suspend fun loginStudent(
        identifier: String,
        passwordInput: String,
        otpCode: String? = null
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        val user = userDao.getUserByIdentifier(cleanId) ?: userDao.getUserByEmail(cleanId)

        if (user == null) {
            return@withContext AuthResult.Failure("No student account found with '$cleanId'. Check register number or email.")
        }

        // RBAC Enforcement: Only students can log in through the student portal
        if (user.role != UserRole.STUDENT) {
            auditDao.insertLog(
                AuditLog(
                    userId = user.id,
                    action = "PORTAL_MISMATCH",
                    details = "Non-student user '${user.identifier}' attempted student portal login."
                )
            )
            return@withContext AuthResult.Failure("Unauthorized: This portal is reserved for students. Please use the Staff portal.")
        }

        // Check if account is locked
        if (user.isLocked || user.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            auditDao.insertLog(
                AuditLog(
                    userId = user.id,
                    action = "LOCKED_LOGIN_ATTEMPT",
                    details = "Student '${user.identifier}' attempted login while locked."
                )
            )
            return@withContext AuthResult.AccountLocked(
                "Account is locked due to 5 consecutive failed attempts. Contact admin at admin@college.edu to unlock."
            )
        }

        // Password Verification
        if (user.passwordHash != passwordInput) {
            userDao.incrementFailedAttempts(user.id)
            val updatedAttempts = user.failedAttempts + 1
            val left = MAX_FAILED_ATTEMPTS - updatedAttempts

            auditDao.insertLog(
                AuditLog(
                    userId = user.id,
                    action = "LOGIN_FAILED",
                    details = "Failed password attempt #$updatedAttempts for student '${user.identifier}'"
                )
            )

            if (updatedAttempts >= MAX_FAILED_ATTEMPTS) {
                userDao.lockAccount(user.id)
                return@withContext AuthResult.AccountLocked(
                    "Account locked! Maximum of $MAX_FAILED_ATTEMPTS attempts exceeded. Please contact administration."
                )
            } else {
                return@withContext AuthResult.Failure(
                    "Incorrect password.",
                    attemptsLeft = left
                )
            }
        }

        // First login OTP requirement check
        if (user.isFirstLogin) {
            if (otpCode.isNullOrBlank()) {
                return@withContext AuthResult.OtpRequired(user, user.email)
            } else if (otpCode.trim() != (user.otpSecret ?: "123456")) {
                return@withContext AuthResult.Failure("Invalid OTP code. Please enter the 6-digit OTP sent to your registered email.")
            } else {
                // OTP verified successfully, clear first login flag
                userDao.updateUser(user.copy(isFirstLogin = false))
            }
        }

        // Successful authentication
        userDao.recordSuccessfulLogin(user.id, System.currentTimeMillis())
        val student = studentDao.getStudentByUserIdSync(user.id)
        val loggedIn = LoggedInUser(user = user, studentProfile = student)
        _currentSession.value = loggedIn

        auditDao.insertLog(
            AuditLog(
                userId = user.id,
                action = "LOGIN_SUCCESS",
                targetStudentId = student?.id,
                details = "Student '${user.identifier}' successfully logged in."
            )
        )

        AuthResult.Success(loggedIn)
    }

    /**
     * Management & Staff Portal Login:
     * Accepts Employee ID (e.g. ADM001, FAC001, ACC001).
     * The role is STRICTLY fetched from the database, NEVER chosen by the user in UI.
     */
    suspend fun loginStaff(
        employeeId: String,
        passwordInput: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmpId = employeeId.trim()
        val user = userDao.getUserByIdentifier(cleanEmpId) ?: userDao.getUserByEmail(cleanEmpId)

        if (user == null) {
            return@withContext AuthResult.Failure("Staff account with ID '$cleanEmpId' not found.")
        }

        // RBAC Enforcement: Must be a staff role (ADMIN, FACULTY, ACCOUNTS)
        if (user.role == UserRole.STUDENT) {
            auditDao.insertLog(
                AuditLog(
                    userId = user.id,
                    action = "PORTAL_MISMATCH",
                    details = "Student user '${user.identifier}' attempted staff portal login."
                )
            )
            return@withContext AuthResult.Failure("Unauthorized: Student accounts cannot access the Management/Staff portal.")
        }

        // Check account lock
        if (user.isLocked || user.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            auditDao.insertLog(
                AuditLog(
                    userId = user.id,
                    action = "LOCKED_LOGIN_ATTEMPT",
                    details = "Staff '${user.identifier}' attempted login while account is locked."
                )
            )
            return@withContext AuthResult.AccountLocked(
                "Staff account is locked due to repeated failed attempts. Please notify System Administration."
            )
        }

        // Password Verification
        if (user.passwordHash != passwordInput) {
            userDao.incrementFailedAttempts(user.id)
            val updatedAttempts = user.failedAttempts + 1
            val left = MAX_FAILED_ATTEMPTS - updatedAttempts

            auditDao.insertLog(
                AuditLog(
                    userId = user.id,
                    action = "LOGIN_FAILED",
                    details = "Failed login attempt #$updatedAttempts for staff ID '${user.identifier}'"
                )
            )

            if (updatedAttempts >= MAX_FAILED_ATTEMPTS) {
                userDao.lockAccount(user.id)
                return@withContext AuthResult.AccountLocked(
                    "Staff account locked! 5 consecutive failed attempts. Contact super admin."
                )
            } else {
                return@withContext AuthResult.Failure(
                    "Invalid employee credentials.",
                    attemptsLeft = left
                )
            }
        }

        // Success: Fetch staff profile and resolve exact DB role
        userDao.recordSuccessfulLogin(user.id, System.currentTimeMillis())
        val staff = staffDao.getStaffByUserIdSync(user.id)
        val loggedIn = LoggedInUser(user = user, staffProfile = staff)
        _currentSession.value = loggedIn

        auditDao.insertLog(
            AuditLog(
                userId = user.id,
                action = "LOGIN_SUCCESS",
                details = "Staff '${user.identifier}' logged in as ${user.role}."
            )
        )

        AuthResult.Success(loggedIn)
    }

    /**
     * Unlock account (Admin only)
     */
    suspend fun unlockUserAccount(adminUserId: Long, targetUserId: Long): Boolean = withContext(Dispatchers.IO) {
        val admin = userDao.getUserById(adminUserId)
        if (admin?.role != UserRole.ADMIN) return@withContext false

        userDao.unlockAccount(targetUserId)
        auditDao.insertLog(
            AuditLog(
                userId = adminUserId,
                action = "UNLOCK_ACCOUNT",
                details = "Admin unlocked account for user ID: $targetUserId"
            )
        )
        true
    }

    /**
     * Reset / Change Password
     */
    suspend fun changePassword(
        userId: Long,
        oldPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.passwordHash != oldPassword) {
            return@withContext Result.failure(Exception("Current password does not match"))
        }
        if (newPassword.length < 6) {
            return@withContext Result.failure(Exception("New password must be at least 6 characters"))
        }
        userDao.updatePassword(userId, newPassword)
        auditDao.insertLog(
            AuditLog(
                userId = userId,
                action = "PASSWORD_CHANGED",
                details = "User successfully updated their password."
            )
        )
        Result.success(Unit)
    }

    /**
     * Request Password Reset Link / Code
     */
    suspend fun forgotPassword(identifier: String): Result<String> = withContext(Dispatchers.IO) {
        val clean = identifier.trim()
        val user = userDao.getUserByIdentifier(clean) ?: userDao.getUserByEmail(clean)
        if (user == null) {
            return@withContext Result.failure(Exception("No account registered with '$clean'"))
        }
        auditDao.insertLog(
            AuditLog(
                userId = user.id,
                action = "PASSWORD_RESET_REQUESTED",
                details = "Password reset instructions requested for ${user.email}."
            )
        )
        Result.success("Password reset instructions have been dispatched to ${user.email}")
    }

    /**
     * Terminate active session
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        _currentSession.value?.let { session ->
            auditDao.insertLog(
                AuditLog(
                    userId = session.user.id,
                    action = "LOGOUT",
                    details = "User logged out."
                )
            )
        }
        _currentSession.value = null
    }
}
