package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Role-Based Access Control (RBAC) definitions.
 * Staff role is strictly resolved from the database, never chosen on login.
 */
enum class UserRole {
    STUDENT,
    ADMIN,
    FACULTY,
    ACCOUNTS
}

enum class FeeStatus {
    PENDING,
    PARTIALLY_PAID,
    PAID
}

enum class PaymentMode {
    UPI,
    CARD,
    CASH,
    NET_BANKING
}

enum class PaymentStatus {
    SUCCESS,
    REFUNDED,
    CANCELLED
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    ON_DUTY,
    MEDICAL_LEAVE
}

enum class ExamType {
    INTERNAL_1,
    INTERNAL_2,
    MODEL,
    SEMESTER
}

enum class RequestType {
    BONAFIDE,
    TC,
    LEAVE,
    REFUND
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class NoticeAudience {
    ALL,
    DEPARTMENT,
    CLASS
}

enum class NoticePriority {
    NORMAL,
    HIGH,
    URGENT
}

// 1. Users table for authentication and RBAC
@Entity(
    tableName = "users",
    indices = [Index(value = ["identifier"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val identifier: String, // Register number for student OR Employee ID for staff OR email
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val isLocked: Boolean = false,
    val failedAttempts: Int = 0,
    val isFirstLogin: Boolean = false,
    val otpSecret: String? = null,
    val lastLoginAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

// 2. Students profile table
@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index(value = ["regNo"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val regNo: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val photoUrl: String = "",
    val dob: String = "2004-05-15",
    val gender: String = "Male",
    val bloodGroup: String = "O+",
    val guardianName: String,
    val guardianPhone: String,
    val guardianRelation: String = "Father",
    val departmentId: Long,
    val courseId: Long,
    val section: String = "A",
    val batch: String = "2024-2028",
    val admissionDate: String = "2024-08-01",
    val currentSemester: Int = 4,
    val cgpa: Double = 8.65,
    val isActive: Boolean = true
)

// 3. Staff profile table
@Entity(
    tableName = "staff",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index(value = ["empId"], unique = true)]
)
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val empId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: UserRole, // ADMIN, FACULTY, ACCOUNTS
    val departmentId: Long,
    val designation: String,
    val qualification: String,
    val dateOfJoining: String = "2020-06-15",
    val isActive: Boolean = true
)

// 4. Academic Structure: Departments
@Entity(tableName = "departments")
data class Department(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String, // CSE, ECE, MECH, MBA
    val name: String,
    val hodName: String
)

// 5. Academic Structure: Courses
@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("departmentId")]
)
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val departmentId: Long,
    val code: String,
    val name: String,
    val durationYears: Int = 4,
    val totalSemesters: Int = 8
)

// 6. Academic Structure: Subjects
@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val semester: Int,
    val code: String,
    val name: String,
    val credits: Int = 4,
    val type: String = "THEORY",
    val staffId: Long
)

// 7. Attendance Table
@Entity(
    tableName = "attendance",
    indices = [Index("studentId"), Index("subjectId"), Index("date")]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subjectId: Long,
    val date: String, // YYYY-MM-DD
    val status: AttendanceStatus,
    val period: Int,
    val markedByStaffId: Long,
    val remarks: String = ""
)

// 8. Marks & Assessment Table
@Entity(
    tableName = "marks",
    indices = [Index("studentId"), Index("subjectId")]
)
data class Mark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subjectId: Long,
    val examType: ExamType,
    val marksObtained: Double,
    val maxMarks: Double = 100.0,
    val remarks: String = "",
    val enteredByStaffId: Long
)

// 9. Timetable Table
@Entity(
    tableName = "timetable",
    indices = [Index("courseId"), Index("subjectId")]
)
data class Timetable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val section: String,
    val semester: Int,
    val dayOfWeek: String, // Monday, Tuesday, ...
    val periodNo: Int,
    val startTime: String,
    val endTime: String,
    val subjectId: Long,
    val staffId: Long,
    val roomNo: String
)

// 10. Fees Structure / Dues Table
@Entity(
    tableName = "fees",
    indices = [Index("studentId")]
)
data class Fee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val academicYear: String,
    val semester: Int,
    val feeType: String, // Tuition, Exam, Bus, Hostel
    val totalAmount: Double,
    val paidAmount: Double,
    val dueDate: String,
    val status: FeeStatus
)

// 11. Payments & Receipts Table
@Entity(
    tableName = "payments",
    indices = [Index("studentId"), Index("feeId"), Index(value = ["receiptNo"], unique = true)]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val feeId: Long,
    val receiptNo: String, // e.g. RCP/2026-27/000123
    val amount: Double,
    val mode: PaymentMode,
    val transactionId: String,
    val status: PaymentStatus,
    val paidAt: Long = System.currentTimeMillis(),
    val receivedByStaffId: Long? = null,
    val notes: String = "",
    val receiptUrl: String = ""
)

// 12. Notices & Announcements Table
@Entity(
    tableName = "notices",
    indices = [Index("departmentId"), Index("postedByStaffId")]
)
data class Notice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val targetAudience: NoticeAudience,
    val departmentId: Long? = null,
    val courseId: Long? = null,
    val semester: Int? = null,
    val postedByStaffId: Long,
    val postedAt: Long = System.currentTimeMillis(),
    val priority: NoticePriority = NoticePriority.NORMAL
)

// 13. Student Requests Table (Leave, Bonafide, TC, Refunds)
@Entity(
    tableName = "requests",
    indices = [Index("studentId")]
)
data class StudentRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val type: RequestType,
    val subject: String,
    val reason: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val processedByStaffId: Long? = null,
    val remarks: String = ""
)

// 14. Assignments Table
@Entity(
    tableName = "assignments",
    indices = [Index("subjectId")]
)
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val description: String,
    val dueDate: String,
    val postedByStaffId: Long
)

// 15. Feedback & Grievance Box Table
@Entity(
    tableName = "feedback",
    indices = [Index("studentId")]
)
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val category: String, // Academic, Hostel, Canteen, Infrastructure
    val message: String,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Open"
)

// 16. Library Table
@Entity(
    tableName = "library",
    indices = [Index("studentId")]
)
data class LibraryBook(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val bookTitle: String,
    val bookCode: String,
    val issueDate: String,
    val dueDate: String,
    val returnDate: String? = null,
    val fineAmount: Double = 0.0,
    val status: String = "Issued"
)

// 17. Security & Admin Audit Trail Table
@Entity(
    tableName = "audit_logs",
    indices = [Index("userId"), Index("targetStudentId")]
)
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val action: String, // LOGIN, LOGIN_FAILED, GENERATE_PDF, REPRINT_RECEIPT, MARK_ATTENDANCE
    val targetStudentId: Long? = null,
    val details: String,
    val ipOrClient: String = "Android Mobile App",
    val createdAt: Long = System.currentTimeMillis()
)
