package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class SubjectAttendanceSummary(
    val subject: Subject,
    val totalClasses: Int,
    val presentClasses: Int,
    val percentage: Double,
    val isLowAttendance: Boolean
)

data class StudentAcademicOverview(
    val student: Student,
    val department: Department?,
    val course: Course?,
    val overallAttendancePercentage: Double,
    val subjectSummaries: List<SubjectAttendanceSummary>,
    val totalPendingFee: Double
)

class CollegeRepository(
    private val studentDao: StudentDao,
    private val staffDao: StaffDao,
    private val academicDao: AcademicDao,
    private val financeDao: FinanceDao,
    private val communicationDao: CommunicationDao,
    private val auditDao: AuditDao
) {
    fun getStudentProfile(userId: Long): Flow<Student?> = studentDao.getStudentByUserId(userId)

    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()

    fun getStaffProfile(userId: Long): Flow<Staff?> = staffDao.getStaffByUserId(userId)

    fun getAllStaff(): Flow<List<Staff>> = staffDao.getAllStaff()

    fun getAllDepartments(): Flow<List<Department>> = academicDao.getAllDepartments()

    fun getSubjectsForCourse(courseId: Long, semester: Int): Flow<List<Subject>> =
        academicDao.getSubjectsForCourse(courseId, semester)

    fun getAllSubjects(): Flow<List<Subject>> = academicDao.getAllSubjects()

    fun getTimetableForClass(courseId: Long, section: String, semester: Int): Flow<List<Timetable>> =
        academicDao.getTimetableForClass(courseId, section, semester)

    fun getAttendanceForStudent(studentId: Long): Flow<List<Attendance>> =
        academicDao.getAttendanceForStudent(studentId)

    fun getMarksForStudent(studentId: Long): Flow<List<Mark>> =
        academicDao.getMarksForStudent(studentId)

    fun getFeesForStudent(studentId: Long): Flow<List<Fee>> =
        financeDao.getFeesForStudent(studentId)

    fun getPaymentsForStudent(studentId: Long): Flow<List<Payment>> =
        financeDao.getPaymentsForStudent(studentId)

    fun getAllPayments(): Flow<List<Payment>> = financeDao.getAllPayments()

    fun getAllNotices(): Flow<List<Notice>> =
        communicationDao.getAllNotices()

    fun getStudentRequests(studentId: Long): Flow<List<StudentRequest>> =
        communicationDao.getRequestsForStudent(studentId)

    fun getAllRequests(): Flow<List<StudentRequest>> =
        communicationDao.getAllRequests()

    fun getLibraryBooksForStudent(studentId: Long): Flow<List<LibraryBook>> =
        communicationDao.getLibraryBooksForStudent(studentId)

    fun getRecentAuditLogs(limit: Int = 30): Flow<List<AuditLog>> =
        auditDao.getRecentLogs(limit)

    suspend fun submitStudentRequest(
        studentId: Long,
        type: RequestType,
        subject: String,
        reason: String
    ): Long = withContext(Dispatchers.IO) {
        val req = StudentRequest(
            studentId = studentId,
            type = type,
            subject = subject,
            reason = reason,
            status = RequestStatus.PENDING,
            requestedAt = System.currentTimeMillis()
        )
        val id = communicationDao.insertRequest(req)
        auditDao.insertLog(
            AuditLog(
                userId = studentId,
                targetStudentId = studentId,
                action = "REQUEST_SUBMITTED",
                details = "Student submitted $type request: '$subject'"
            )
        )
        id
    }

    suspend fun markAttendance(
        studentId: Long,
        subjectId: Long,
        date: String,
        status: AttendanceStatus,
        period: Int,
        markedByStaffId: Long,
        remarks: String? = null
    ) = withContext(Dispatchers.IO) {
        val entry = Attendance(
            studentId = studentId,
            subjectId = subjectId,
            date = date,
            status = status,
            period = period,
            markedByStaffId = markedByStaffId,
            remarks = remarks ?: ""
        )
        academicDao.insertAttendance(entry)
        auditDao.insertLog(
            AuditLog(
                userId = markedByStaffId,
                targetStudentId = studentId,
                action = "ATTENDANCE_MARKED",
                details = "Marked $status for Student ID $studentId on $date (Period $period)"
            )
        )
    }

    suspend fun enterMarks(
        studentId: Long,
        subjectId: Long,
        examType: ExamType,
        marksObtained: Double,
        maxMarks: Double,
        enteredByStaffId: Long,
        remarks: String? = null
    ) = withContext(Dispatchers.IO) {
        val mark = Mark(
            studentId = studentId,
            subjectId = subjectId,
            examType = examType,
            marksObtained = marksObtained,
            maxMarks = maxMarks,
            enteredByStaffId = enteredByStaffId,
            remarks = remarks ?: ""
        )
        academicDao.insertMarks(listOf(mark))
        auditDao.insertLog(
            AuditLog(
                userId = enteredByStaffId,
                targetStudentId = studentId,
                action = "MARK_ENTERED",
                details = "Entered marks for Student ID $studentId: $marksObtained / $maxMarks ($examType)"
            )
        )
    }

    suspend fun postNotice(
        title: String,
        content: String,
        targetAudience: NoticeAudience,
        postedByStaffId: Long,
        priority: NoticePriority = NoticePriority.NORMAL,
        departmentId: Long? = null
    ) = withContext(Dispatchers.IO) {
        val notice = Notice(
            title = title,
            content = content,
            targetAudience = targetAudience,
            departmentId = departmentId,
            postedByStaffId = postedByStaffId,
            postedAt = System.currentTimeMillis(),
            priority = priority
        )
        communicationDao.insertNotices(listOf(notice))
        auditDao.insertLog(
            AuditLog(
                userId = postedByStaffId,
                action = "NOTICE_PUBLISHED",
                details = "Published circular: '$title' [Audience: $targetAudience]"
            )
        )
    }

    suspend fun payFeeOnline(
        studentId: Long,
        feeId: Long,
        amount: Double,
        mode: PaymentMode,
        transactionId: String
    ): Long = withContext(Dispatchers.IO) {
        val studentFees = financeDao.getFeesForStudent(studentId).first()
        val fee = studentFees.find { it.id == feeId }

        if (fee != null) {
            val updatedPaid = fee.paidAmount + amount
            val updatedStatus = if (updatedPaid >= fee.totalAmount) FeeStatus.PAID else FeeStatus.PARTIALLY_PAID
            val updatedFee = fee.copy(paidAmount = updatedPaid, status = updatedStatus)
            financeDao.updateFee(updatedFee)
        }

        val receiptNumber = "RCP/SKS-NUR/${System.currentTimeMillis().toString().takeLast(6)}"
        val payment = Payment(
            studentId = studentId,
            feeId = feeId,
            receiptNo = receiptNumber,
            amount = amount,
            mode = mode,
            transactionId = transactionId,
            status = PaymentStatus.SUCCESS,
            paidAt = System.currentTimeMillis(),
            receivedByStaffId = null,
            notes = "Settled online via Razorpay - Verified"
        )
        val paymentId = financeDao.insertPayment(payment)

        auditDao.insertLog(
            AuditLog(
                userId = studentId,
                targetStudentId = studentId,
                action = "FEE_PAID_ONLINE",
                details = "Paid fee of ₹${amount.toInt()} (Ref: $transactionId, Receipt: $receiptNumber)"
            )
        )
        paymentId
    }

    suspend fun recordOfflinePayment(
        studentId: Long,
        feeId: Long,
        amount: Double,
        mode: PaymentMode,
        receivedByStaffId: Long,
        notes: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val studentFees = financeDao.getFeesForStudent(studentId).first()
        val fee = studentFees.find { it.id == feeId }

        if (fee != null) {
            val updatedPaid = fee.paidAmount + amount
            val updatedStatus = if (updatedPaid >= fee.totalAmount) FeeStatus.PAID else FeeStatus.PARTIALLY_PAID
            val updatedFee = fee.copy(paidAmount = updatedPaid, status = updatedStatus)
            financeDao.updateFee(updatedFee)
        }

        val receiptNumber = "RCP/SKS-OFFICE/${System.currentTimeMillis().toString().takeLast(6)}"
        val payment = Payment(
            studentId = studentId,
            feeId = feeId,
            receiptNo = receiptNumber,
            amount = amount,
            mode = mode,
            transactionId = "CASH_${System.currentTimeMillis().toString().takeLast(8)}",
            status = PaymentStatus.SUCCESS,
            paidAt = System.currentTimeMillis(),
            receivedByStaffId = receivedByStaffId,
            notes = notes ?: "Received at SKS College Accounts Counter"
        )
        val paymentId = financeDao.insertPayment(payment)

        auditDao.insertLog(
            AuditLog(
                userId = receivedByStaffId,
                targetStudentId = studentId,
                action = "OFFLINE_FEE_COLLECTED",
                details = "Collected ₹${amount.toInt()} for feeId: $feeId (Receipt: $receiptNumber)"
            )
        )
        paymentId
    }

    suspend fun updateRequestStatus(
        requestId: Long,
        status: RequestStatus,
        staffId: Long,
        remarks: String
    ) = withContext(Dispatchers.IO) {
        communicationDao.updateRequestStatus(requestId, status, staffId, remarks)
        auditDao.insertLog(
            AuditLog(
                userId = staffId,
                action = "REQUEST_STATUS_UPDATED",
                details = "Updated request $requestId to $status with remark: '$remarks'"
            )
        )
    }

    suspend fun logAudit(userId: Long, action: String, details: String, targetStudentId: Long? = null) = withContext(Dispatchers.IO) {
        auditDao.insertLog(
            AuditLog(
                userId = userId,
                action = action,
                targetStudentId = targetStudentId,
                details = details
            )
        )
    }
}
