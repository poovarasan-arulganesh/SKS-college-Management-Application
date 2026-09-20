package com.example.data.local

import com.example.data.model.*

/**
 * Seed data for SKS College of Nursing, Harur - 636 903.
 * Affiliated to The Tamil Nadu Dr. M.G.R. Medical University.
 * Approved by INC (Indian Nursing Council) & TNC.
 */
object SeedData {

    fun getDefaultUsers(): List<User> {
        return listOf(
            // 1. Nursing Student (Ananya Krishnan - NUR2024001 / REG2024001)
            User(
                id = 1L,
                identifier = "NUR2024001",
                email = "ananya.nursing@skscon.edu.in",
                passwordHash = "Student@123",
                role = UserRole.STUDENT,
                isLocked = false,
                failedAttempts = 0,
                isFirstLogin = false,
                lastLoginAt = System.currentTimeMillis() - 86400000L
            ),
            // Student alias for REG2024001
            User(
                id = 6L,
                identifier = "REG2024001",
                email = "student@skscon.edu.in",
                passwordHash = "Student@123",
                role = UserRole.STUDENT,
                isLocked = false,
                failedAttempts = 0,
                isFirstLogin = false
            ),
            // 2. Nursing Student (Priya Selvam - First login OTP test)
            User(
                id = 2L,
                identifier = "NUR2024002",
                email = "priya.nursing@skscon.edu.in",
                passwordHash = "Student@123",
                role = UserRole.STUDENT,
                isLocked = false,
                failedAttempts = 0,
                isFirstLogin = true,
                otpSecret = "123456"
            ),
            // 3. Management / Administrator (Prof. Dr. S. Malarvizhi - Principal & Director)
            User(
                id = 3L,
                identifier = "ADM001",
                email = "principal@skscon.edu.in",
                passwordHash = "Admin@123",
                role = UserRole.ADMIN,
                isLocked = false,
                failedAttempts = 0,
                isFirstLogin = false
            ),
            // 4. Nursing Faculty (Assoc. Prof. K. Jayanthi - Medical Surgical Nursing)
            User(
                id = 4L,
                identifier = "FAC001",
                email = "jayanthi.msn@skscon.edu.in",
                passwordHash = "Faculty@123",
                role = UserRole.FACULTY,
                isLocked = false,
                failedAttempts = 0,
                isFirstLogin = false
            ),
            // 5. Office / Accounts Staff (S. Meenakshi - Senior Accounts Officer)
            User(
                id = 5L,
                identifier = "ACC001",
                email = "accounts@skscon.edu.in",
                passwordHash = "Staff@123",
                role = UserRole.ACCOUNTS,
                isLocked = false,
                failedAttempts = 0,
                isFirstLogin = false
            )
        )
    }

    fun getDefaultDepartments(): List<Department> {
        return listOf(
            Department(id = 1L, code = "MSN", name = "Medical Surgical Nursing", hodName = "Prof. K. Jayanthi, M.Sc(N)"),
            Department(id = 2L, code = "CHN", name = "Community Health Nursing", hodName = "Dr. M. Revathi, Ph.D(N)"),
            Department(id = 3L, code = "PED", name = "Child Health (Pediatric) Nursing", hodName = "Prof. S. Geetha, M.Sc(N)"),
            Department(id = 4L, code = "OBG", name = "Obstetrics & Gynaecological Nursing", hodName = "Prof. R. Kavitha, M.Sc(N)"),
            Department(id = 5L, code = "MHN", name = "Mental Health (Psychiatric) Nursing", hodName = "Dr. A. Sudha, Ph.D(N)")
        )
    }

    fun getDefaultCourses(): List<Course> {
        return listOf(
            Course(id = 1L, departmentId = 1L, code = "B.Sc (N)", name = "Bachelor of Science in Nursing", durationYears = 4, totalSemesters = 8),
            Course(id = 2L, departmentId = 1L, code = "P.B.B.Sc (N)", name = "Post Basic B.Sc. Nursing", durationYears = 2, totalSemesters = 4),
            Course(id = 3L, departmentId = 1L, code = "M.Sc (N)", name = "Master of Science in Nursing", durationYears = 2, totalSemesters = 4)
        )
    }

    fun getDefaultStaff(): List<Staff> {
        return listOf(
            Staff(
                id = 1L,
                userId = 3L,
                empId = "ADM001",
                firstName = "S.",
                lastName = "Malarvizhi",
                email = "principal@skscon.edu.in",
                phone = "+91 94432 10001",
                role = UserRole.ADMIN,
                departmentId = 1L,
                designation = "Principal & Professor",
                qualification = "M.Sc(N), Ph.D., RN, RM",
                dateOfJoining = "2012-06-01"
            ),
            Staff(
                id = 2L,
                userId = 4L,
                empId = "FAC001",
                firstName = "K.",
                lastName = "Jayanthi",
                email = "jayanthi.msn@skscon.edu.in",
                phone = "+91 98421 20002",
                role = UserRole.FACULTY,
                departmentId = 1L,
                designation = "Associate Professor & HOD",
                qualification = "M.Sc. Medical Surgical Nursing, RN, RM",
                dateOfJoining = "2016-08-15"
            ),
            Staff(
                id = 3L,
                userId = 5L,
                empId = "ACC001",
                firstName = "S.",
                lastName = "Meenakshi",
                email = "accounts@skscon.edu.in",
                phone = "+91 97890 54321",
                role = UserRole.ACCOUNTS,
                departmentId = 1L,
                designation = "Senior Accounts Officer",
                qualification = "M.Com., MBA (Finance)",
                dateOfJoining = "2018-02-10"
            )
        )
    }

    fun getDefaultStudents(): List<Student> {
        return listOf(
            Student(
                id = 1L,
                userId = 1L,
                regNo = "NUR2024001",
                firstName = "Ananya",
                lastName = "Krishnan",
                email = "ananya.nursing@skscon.edu.in",
                phone = "+91 98765 43210",
                photoUrl = "",
                dob = "2004-06-18",
                gender = "Female",
                bloodGroup = "O+",
                guardianName = "K. Krishnan",
                guardianPhone = "+91 98765 00001",
                guardianRelation = "Father",
                departmentId = 1L,
                courseId = 1L,
                section = "A",
                batch = "2024-2028",
                admissionDate = "2024-08-01",
                currentSemester = 4,
                cgpa = 8.92,
                isActive = true
            ),
            Student(
                id = 2L,
                userId = 2L,
                regNo = "NUR2024002",
                firstName = "Priya",
                lastName = "Selvam",
                email = "priya.nursing@skscon.edu.in",
                phone = "+91 98765 43211",
                photoUrl = "",
                dob = "2005-03-24",
                gender = "Female",
                bloodGroup = "B+",
                guardianName = "M. Selvam",
                guardianPhone = "+91 98765 00002",
                guardianRelation = "Father",
                departmentId = 1L,
                courseId = 1L,
                section = "A",
                batch = "2024-2028",
                admissionDate = "2024-08-01",
                currentSemester = 4,
                cgpa = 9.15,
                isActive = true
            )
        )
    }

    fun getDefaultSubjects(): List<Subject> {
        return listOf(
            Subject(id = 1L, courseId = 1L, semester = 4, code = "NUR-401", name = "Adult Health (Medical Surgical) Nursing II", credits = 4, type = "THEORY", staffId = 2L),
            Subject(id = 2L, courseId = 1L, semester = 4, code = "NUR-402", name = "Pharmacology, Pathology & Genetics", credits = 3, type = "THEORY", staffId = 2L),
            Subject(id = 3L, courseId = 1L, semester = 4, code = "NUR-403", name = "Community Health Nursing I", credits = 4, type = "THEORY", staffId = 2L),
            Subject(id = 4L, courseId = 1L, semester = 4, code = "NUR-404", name = "Hospital Clinical Practical Posting & OSCE", credits = 4, type = "LAB", staffId = 2L),
            Subject(id = 5L, courseId = 1L, semester = 4, code = "NUR-405", name = "Professional Nursing Ethics & Values", credits = 2, type = "THEORY", staffId = 2L)
        )
    }

    fun getDefaultAttendance(): List<Attendance> {
        return listOf(
            Attendance(id = 1L, studentId = 1L, subjectId = 1L, date = "2026-09-18", status = AttendanceStatus.PRESENT, period = 1, markedByStaffId = 2L),
            Attendance(id = 2L, studentId = 1L, subjectId = 2L, date = "2026-09-18", status = AttendanceStatus.PRESENT, period = 2, markedByStaffId = 2L),
            Attendance(id = 3L, studentId = 1L, subjectId = 3L, date = "2026-09-18", status = AttendanceStatus.PRESENT, period = 3, markedByStaffId = 2L),
            Attendance(id = 4L, studentId = 1L, subjectId = 4L, date = "2026-09-19", status = AttendanceStatus.PRESENT, period = 4, markedByStaffId = 2L, remarks = "District Hospital OT Posting"),
            Attendance(id = 5L, studentId = 1L, subjectId = 5L, date = "2026-09-19", status = AttendanceStatus.ON_DUTY, period = 5, markedByStaffId = 2L, remarks = "Harur CHC Pulse Polio Drive")
        )
    }

    fun getDefaultMarks(): List<Mark> {
        return listOf(
            Mark(id = 1L, studentId = 1L, subjectId = 1L, examType = ExamType.INTERNAL_1, marksObtained = 47.0, maxMarks = 50.0, remarks = "Outstanding clinical case diagnosis", enteredByStaffId = 2L),
            Mark(id = 2L, studentId = 1L, subjectId = 2L, examType = ExamType.INTERNAL_1, marksObtained = 46.5, maxMarks = 50.0, remarks = "Excellent pharmacology concepts", enteredByStaffId = 2L),
            Mark(id = 3L, studentId = 1L, subjectId = 3L, examType = ExamType.INTERNAL_1, marksObtained = 48.0, maxMarks = 50.0, remarks = "Comprehensive community survey", enteredByStaffId = 2L),
            Mark(id = 4L, studentId = 1L, subjectId = 4L, examType = ExamType.INTERNAL_1, marksObtained = 50.0, maxMarks = 50.0, remarks = "Flawless aseptic procedure demonstration", enteredByStaffId = 2L),
            Mark(id = 5L, studentId = 1L, subjectId = 5L, examType = ExamType.INTERNAL_1, marksObtained = 44.0, maxMarks = 50.0, remarks = "Good understanding of INC code of ethics", enteredByStaffId = 2L)
        )
    }

    fun getDefaultTimetable(): List<Timetable> {
        return listOf(
            Timetable(id = 1L, courseId = 1L, section = "A", semester = 4, dayOfWeek = "Monday", periodNo = 1, startTime = "08:30", endTime = "09:30", subjectId = 1L, staffId = 2L, roomNo = "Florence Nightingale Hall"),
            Timetable(id = 2L, courseId = 1L, section = "A", semester = 4, dayOfWeek = "Monday", periodNo = 2, startTime = "09:30", endTime = "10:30", subjectId = 2L, staffId = 2L, roomNo = "Florence Nightingale Hall"),
            Timetable(id = 3L, courseId = 1L, section = "A", semester = 4, dayOfWeek = "Monday", periodNo = 3, startTime = "10:45", endTime = "11:45", subjectId = 3L, staffId = 2L, roomNo = "Community Health Seminar Room"),
            Timetable(id = 4L, courseId = 1L, section = "A", semester = 4, dayOfWeek = "Monday", periodNo = 4, startTime = "11:45", endTime = "12:45", subjectId = 5L, staffId = 2L, roomNo = "Florence Nightingale Hall"),
            Timetable(id = 5L, courseId = 1L, section = "A", semester = 4, dayOfWeek = "Monday", periodNo = 5, startTime = "01:30", endTime = "04:30", subjectId = 4L, staffId = 2L, roomNo = "SKS Hospital Clinical Posting")
        )
    }

    fun getDefaultFees(): List<Fee> {
        return listOf(
            Fee(id = 1L, studentId = 1L, academicYear = "2026-2027", semester = 4, feeType = "Tuition & Clinical Lab Fee", totalAmount = 48000.0, paidAmount = 48000.0, dueDate = "2026-08-30", status = FeeStatus.PAID),
            Fee(id = 2L, studentId = 1L, academicYear = "2026-2027", semester = 4, feeType = "Dr. MGR Medical University Exam Fee", totalAmount = 4200.0, paidAmount = 4200.0, dueDate = "2026-09-15", status = FeeStatus.PAID),
            Fee(id = 3L, studentId = 1L, academicYear = "2026-2027", semester = 4, feeType = "Hospital Clinical Posting & Bus Transport", totalAmount = 16000.0, paidAmount = 0.0, dueDate = "2026-10-15", status = FeeStatus.PENDING),
            Fee(id = 4L, studentId = 1L, academicYear = "2026-2027", semester = 4, feeType = "Hostel & Dietary Mess Fee", totalAmount = 30000.0, paidAmount = 0.0, dueDate = "2026-10-25", status = FeeStatus.PENDING)
        )
    }

    fun getDefaultPayments(): List<Payment> {
        return listOf(
            Payment(
                id = 1L,
                studentId = 1L,
                feeId = 1L,
                receiptNo = "RCP/SKS-NUR/2026/0142",
                amount = 48000.0,
                mode = PaymentMode.UPI,
                transactionId = "TXN_UPI_SKS89124912",
                status = PaymentStatus.SUCCESS,
                paidAt = System.currentTimeMillis() - 1728000000L,
                receivedByStaffId = 3L,
                notes = "Tuition Fee paid via Razorpay NetBanking - Verified by Accounts"
            ),
            Payment(
                id = 2L,
                studentId = 1L,
                feeId = 2L,
                receiptNo = "RCP/SKS-NUR/2026/0143",
                amount = 4200.0,
                mode = PaymentMode.CARD,
                transactionId = "TXN_CRD_SKS48120394",
                status = PaymentStatus.SUCCESS,
                paidAt = System.currentTimeMillis() - 864000000L,
                receivedByStaffId = 3L,
                notes = "Medical University Exam Fee settled"
            )
        )
    }

    fun getDefaultNotices(): List<Notice> {
        return listOf(
            Notice(
                id = 1L,
                title = "Clinical Hospital Posting Schedule: District Hospital Harur",
                content = "B.Sc. Nursing 2nd & 3rd Year clinical posting rotations for Medical Surgical Ward and Intensive Care Unit commence from next Monday. Students must wear full clinical uniform with valid institutional ID and stethoscope.",
                targetAudience = NoticeAudience.ALL,
                postedByStaffId = 1L,
                postedAt = System.currentTimeMillis() - 72000000L,
                priority = NoticePriority.HIGH
            ),
            Notice(
                id = 2L,
                title = "International Nurses Day & Florence Nightingale Lamp Lighting Ceremony",
                content = "Annual Pinning & Lamp Lighting Ceremony for 1st Year Nursing Cadets will be celebrated in SKS Auditorium. Guest of Honour: District Medical Officer.",
                targetAudience = NoticeAudience.ALL,
                postedByStaffId = 1L,
                postedAt = System.currentTimeMillis() - 150000000L,
                priority = NoticePriority.NORMAL
            ),
            Notice(
                id = 3L,
                title = "Community Health Camp & Pulse Polio Drive Volunteer Registration",
                content = "Community Health Department is organizing an outreach health checkup camp in Morappur rural sub-center. Eligible B.Sc. Nursing students will be granted On-Duty (OD) clinical hours.",
                targetAudience = NoticeAudience.DEPARTMENT,
                departmentId = 2L,
                postedByStaffId = 2L,
                postedAt = System.currentTimeMillis() - 250000000L,
                priority = NoticePriority.HIGH
            )
        )
    }

    fun getDefaultRequests(): List<StudentRequest> {
        return listOf(
            StudentRequest(
                id = 1L,
                studentId = 1L,
                type = RequestType.BONAFIDE,
                subject = "Bonafide Certificate for Nursing Council Merit Scholarship",
                reason = "Required for State Nursing Education and Healthcare Fellowship application.",
                status = RequestStatus.APPROVED,
                requestedAt = System.currentTimeMillis() - 400000000L,
                processedByStaffId = 3L,
                remarks = "Issued with seal of Principal, SKS College of Nursing."
            ),
            StudentRequest(
                id = 2L,
                studentId = 1L,
                type = RequestType.LEAVE,
                subject = "On-Duty (OD) Leave for National Pediatric Nursing Conclave",
                reason = "Selected as student delegate for state healthcare nursing conference.",
                status = RequestStatus.PENDING,
                requestedAt = System.currentTimeMillis() - 86400000L
            )
        )
    }

    fun getDefaultLibrary(): List<LibraryBook> {
        return listOf(
            LibraryBook(
                id = 1L,
                studentId = 1L,
                bookTitle = "Brunner & Suddarth's Textbook of Medical-Surgical Nursing",
                bookCode = "NUR-LIB-2041",
                issueDate = "2026-09-05",
                dueDate = "2026-10-05",
                status = "Issued"
            ),
            LibraryBook(
                id = 2L,
                studentId = 1L,
                bookTitle = "Park's Textbook of Preventive and Social Medicine (26th Ed.)",
                bookCode = "NUR-LIB-1180",
                issueDate = "2026-09-12",
                dueDate = "2026-10-12",
                status = "Issued"
            )
        )
    }

    fun getDefaultAuditLogs(): List<AuditLog> {
        return listOf(
            AuditLog(
                id = 1L,
                userId = 3L,
                action = "SYSTEM_INITIALIZED",
                details = "SKS College of Nursing ERP initialized with academic departments, courses, clinical postings, and RBAC.",
                createdAt = System.currentTimeMillis() - 864000000L
            ),
            AuditLog(
                id = 2L,
                userId = 1L,
                action = "LOGIN_SUCCESS",
                targetStudentId = 1L,
                details = "Nursing Student Ananya Krishnan authenticated via Register Number NUR2024001.",
                createdAt = System.currentTimeMillis() - 86400000L
            )
        )
    }
}
