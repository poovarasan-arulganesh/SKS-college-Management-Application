package com.example.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter fun fromUserRole(v: UserRole): String = v.name
    @TypeConverter fun toUserRole(v: String): UserRole = runCatching { UserRole.valueOf(v) }.getOrDefault(UserRole.STUDENT)

    @TypeConverter fun fromFeeStatus(v: FeeStatus): String = v.name
    @TypeConverter fun toFeeStatus(v: String): FeeStatus = runCatching { FeeStatus.valueOf(v) }.getOrDefault(FeeStatus.PENDING)

    @TypeConverter fun fromPaymentMode(v: PaymentMode): String = v.name
    @TypeConverter fun toPaymentMode(v: String): PaymentMode = runCatching { PaymentMode.valueOf(v) }.getOrDefault(PaymentMode.UPI)

    @TypeConverter fun fromPaymentStatus(v: PaymentStatus): String = v.name
    @TypeConverter fun toPaymentStatus(v: String): PaymentStatus = runCatching { PaymentStatus.valueOf(v) }.getOrDefault(PaymentStatus.SUCCESS)

    @TypeConverter fun fromAttendanceStatus(v: AttendanceStatus): String = v.name
    @TypeConverter fun toAttendanceStatus(v: String): AttendanceStatus = runCatching { AttendanceStatus.valueOf(v) }.getOrDefault(AttendanceStatus.PRESENT)

    @TypeConverter fun fromExamType(v: ExamType): String = v.name
    @TypeConverter fun toExamType(v: String): ExamType = runCatching { ExamType.valueOf(v) }.getOrDefault(ExamType.INTERNAL_1)

    @TypeConverter fun fromRequestType(v: RequestType): String = v.name
    @TypeConverter fun toRequestType(v: String): RequestType = runCatching { RequestType.valueOf(v) }.getOrDefault(RequestType.BONAFIDE)

    @TypeConverter fun fromRequestStatus(v: RequestStatus): String = v.name
    @TypeConverter fun toRequestStatus(v: String): RequestStatus = runCatching { RequestStatus.valueOf(v) }.getOrDefault(RequestStatus.PENDING)

    @TypeConverter fun fromNoticeAudience(v: NoticeAudience): String = v.name
    @TypeConverter fun toNoticeAudience(v: String): NoticeAudience = runCatching { NoticeAudience.valueOf(v) }.getOrDefault(NoticeAudience.ALL)

    @TypeConverter fun fromNoticePriority(v: NoticePriority): String = v.name
    @TypeConverter fun toNoticePriority(v: String): NoticePriority = runCatching { NoticePriority.valueOf(v) }.getOrDefault(NoticePriority.NORMAL)
}

@Database(
    entities = [
        User::class,
        Student::class,
        Staff::class,
        Department::class,
        Course::class,
        Subject::class,
        Attendance::class,
        Mark::class,
        Timetable::class,
        Fee::class,
        Payment::class,
        Notice::class,
        StudentRequest::class,
        Assignment::class,
        Feedback::class,
        LibraryBook::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CollegeDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun staffDao(): StaffDao
    abstract fun academicDao(): AcademicDao
    abstract fun financeDao(): FinanceDao
    abstract fun communicationDao(): CommunicationDao
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: CollegeDatabase? = null

        fun getInstance(context: Context): CollegeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CollegeDatabase::class.java,
                    "college_erp.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default dataset on first database creation
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { seedDatabase(it) }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Double check seed data on background in case table was created previously without seed
                CoroutineScope(Dispatchers.IO).launch {
                    if (instance.userDao().getUserCount() == 0) {
                        seedDatabase(instance)
                    }
                }

                instance
            }
        }

        private suspend fun seedDatabase(db: CollegeDatabase) {
            db.userDao().insertUsers(SeedData.getDefaultUsers())
            db.staffDao().insertStaffList(SeedData.getDefaultStaff())
            db.academicDao().insertDepartments(SeedData.getDefaultDepartments())
            db.academicDao().insertCourses(SeedData.getDefaultCourses())
            db.academicDao().insertSubjects(SeedData.getDefaultSubjects())
            db.studentDao().insertStudents(SeedData.getDefaultStudents())
            db.academicDao().insertAttendanceList(SeedData.getDefaultAttendance())
            db.academicDao().insertMarks(SeedData.getDefaultMarks())
            db.academicDao().insertTimetable(SeedData.getDefaultTimetable())
            db.financeDao().insertFees(SeedData.getDefaultFees())
            db.financeDao().insertPayments(SeedData.getDefaultPayments())
            db.communicationDao().insertNotices(SeedData.getDefaultNotices())
            db.communicationDao().insertRequests(SeedData.getDefaultRequests())
            db.communicationDao().insertLibraryBooks(SeedData.getDefaultLibrary())
            db.auditDao().insertLog(SeedData.getDefaultAuditLogs().first())
        }
    }
}
