package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(identifier) = LOWER(:identifier) LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): User?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET failedAttempts = failedAttempts + 1 WHERE id = :userId")
    suspend fun incrementFailedAttempts(userId: Long)

    @Query("UPDATE users SET isLocked = 1 WHERE id = :userId")
    suspend fun lockAccount(userId: Long)

    @Query("UPDATE users SET isLocked = 0, failedAttempts = 0 WHERE id = :userId")
    suspend fun unlockAccount(userId: Long)

    @Query("UPDATE users SET failedAttempts = 0, lastLoginAt = :timestamp WHERE id = :userId")
    suspend fun recordSuccessfulLogin(userId: Long, timestamp: Long)

    @Query("UPDATE users SET passwordHash = :newHash WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newHash: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    fun getStudentByUserId(userId: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    suspend fun getStudentByUserIdSync(userId: Long): Student?

    @Query("SELECT * FROM students WHERE LOWER(regNo) = LOWER(:regNo) LIMIT 1")
    suspend fun getStudentByRegNo(regNo: String): Student?

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students ORDER BY regNo ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE departmentId = :deptId")
    fun getStudentsByDepartment(deptId: Long): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE userId = :userId LIMIT 1")
    fun getStaffByUserId(userId: Long): Flow<Staff?>

    @Query("SELECT * FROM staff WHERE userId = :userId LIMIT 1")
    suspend fun getStaffByUserIdSync(userId: Long): Staff?

    @Query("SELECT * FROM staff WHERE LOWER(empId) = LOWER(:empId) LIMIT 1")
    suspend fun getStaffByEmpId(empId: String): Staff?

    @Query("SELECT * FROM staff WHERE id = :id LIMIT 1")
    suspend fun getStaffById(id: Long): Staff?

    @Query("SELECT * FROM staff ORDER BY empId ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffList(staffList: List<Staff>)

    @Update
    suspend fun updateStaff(staff: Staff)
}

@Dao
interface AcademicDao {
    @Query("SELECT * FROM departments ORDER BY code ASC")
    fun getAllDepartments(): Flow<List<Department>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departments: List<Department>)

    @Query("SELECT * FROM courses WHERE departmentId = :deptId")
    fun getCoursesForDept(deptId: Long): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Query("SELECT * FROM subjects WHERE courseId = :courseId AND semester = :semester")
    fun getSubjectsForCourse(courseId: Long, semester: Int): Flow<List<Subject>>

    @Query("SELECT * FROM subjects ORDER BY code ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Query("SELECT * FROM timetable WHERE courseId = :courseId AND section = :section AND semester = :semester")
    fun getTimetableForClass(courseId: Long, section: String, semester: Int): Flow<List<Timetable>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(entries: List<Timetable>)

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<Attendance>)

    @Query("SELECT * FROM marks WHERE studentId = :studentId")
    fun getMarksForStudent(studentId: Long): Flow<List<Mark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<Mark>)
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM fees WHERE studentId = :studentId ORDER BY dueDate DESC")
    fun getFeesForStudent(studentId: Long): Flow<List<Fee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFees(fees: List<Fee>)

    @Update
    suspend fun updateFee(fee: Fee)

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY paidAt DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY paidAt DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<Payment>)

    @Query("SELECT COUNT(*) FROM payments")
    suspend fun getPaymentCount(): Int
}

@Dao
interface CommunicationDao {
    @Query("SELECT * FROM notices ORDER BY postedAt DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<Notice>)

    @Query("SELECT * FROM requests WHERE studentId = :studentId ORDER BY requestedAt DESC")
    fun getRequestsForStudent(studentId: Long): Flow<List<StudentRequest>>

    @Query("SELECT * FROM requests ORDER BY requestedAt DESC")
    fun getAllRequests(): Flow<List<StudentRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: StudentRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<StudentRequest>)

    @Query("UPDATE requests SET status = :status, processedByStaffId = :staffId, remarks = :remarks WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Long, status: RequestStatus, staffId: Long, remarks: String)

    @Query("SELECT * FROM library WHERE studentId = :studentId")
    fun getLibraryBooksForStudent(studentId: Long): Flow<List<LibraryBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryBooks(books: List<LibraryBook>)
}

@Dao
interface AuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog): Long

    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLogsForUser(userId: Long): Flow<List<AuditLog>>
}
