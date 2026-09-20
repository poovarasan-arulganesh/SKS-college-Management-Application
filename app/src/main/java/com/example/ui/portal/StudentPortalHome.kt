package com.example.ui.portal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.CollegeRepository
import com.example.data.repository.LoggedInUser
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class StudentTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    ACADEMIC("Clinical & Academics", Icons.Default.School),
    FEES("Fees & Receipts", Icons.Default.ReceiptLong),
    REQUESTS("Services & Library", Icons.AutoMirrored.Filled.LibraryBooks)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPortalHome(
    session: LoggedInUser,
    collegeRepository: CollegeRepository,
    onLogoutClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val student = session.studentProfile
    val studentId = student?.id ?: 1L

    var selectedTab by remember { mutableStateOf(StudentTab.OVERVIEW) }

    val attendanceList by collegeRepository.getAttendanceForStudent(studentId)
        .collectAsState(initial = emptyList())
    val marks by collegeRepository.getMarksForStudent(studentId)
        .collectAsState(initial = emptyList())
    val notices by collegeRepository.getAllNotices()
        .collectAsState(initial = emptyList())
    val fees by collegeRepository.getFeesForStudent(studentId)
        .collectAsState(initial = emptyList())
    val payments by collegeRepository.getPaymentsForStudent(studentId)
        .collectAsState(initial = emptyList())
    val timetable by collegeRepository.getTimetableForClass(student?.courseId ?: 1L, student?.section ?: "A", student?.currentSemester ?: 4)
        .collectAsState(initial = emptyList())
    val requests by collegeRepository.getStudentRequests(studentId)
        .collectAsState(initial = emptyList())
    val libraryBooks by collegeRepository.getLibraryBooksForStudent(studentId)
        .collectAsState(initial = emptyList())

    // Calculations
    val totalClasses = attendanceList.size
    val presentCount = attendanceList.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.ON_DUTY }
    val attendancePct = if (totalClasses > 0) (presentCount.toDouble() / totalClasses * 100.0) else 92.5
    val pendingFeeSum = fees.filter { it.status != FeeStatus.PAID }.sumOf { it.totalAmount - it.paidAmount }

    // Dialog state for Razorpay payment simulation
    var feeToPay by remember { mutableStateOf<Fee?>(null) }
    var paymentProcessing by remember { mutableStateOf(false) }
    var paymentSuccessDialog by remember { mutableStateOf(false) }

    // Dialog state for New Service Request
    var showNewRequestDialog by remember { mutableStateOf(false) }
    var newRequestType by remember { mutableStateOf(RequestType.BONAFIDE) }
    var newRequestSubject by remember { mutableStateOf("") }
    var newRequestReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SksCollegeEmblem(size = 38.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SKS College of Nursing",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalPrimary
                            )
                            Text(
                                text = "${student?.firstName ?: "Student"} • Reg: ${student?.regNo ?: session.user.identifier}",
                                style = Typography.bodySmall,
                                color = SlateSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.testTag("student_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = CrimsonError
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite,
                    titleContentColor = CharcoalPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 6.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                StudentTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = Typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NursingTealDark,
                            selectedTextColor = NursingTealDark,
                            indicatorColor = NursingTealSubtle,
                            unselectedIconColor = SlateMuted,
                            unselectedTextColor = SlateSecondary
                        ),
                        modifier = Modifier.testTag("student_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = AppleBackgroundLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .testTag("student_dashboard_scroll"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    StudentTab.OVERVIEW -> {
                        // Overview Tab: Profile Card, Key Metrics, Attendance Bar, Recent Notices
                        item {
                            // Student Profile Banner
                            AppleCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("student_profile_card"),
                                backgroundColor = PureWhite,
                                cornerRadius = 24.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(NursingTealSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = NursingTealDark,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${student?.firstName ?: "Ananya"} ${student?.lastName ?: "Krishnan"}",
                                            style = Typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalPrimary
                                        )
                                        Text(
                                            text = "B.Sc. Nursing • Semester ${student?.currentSemester ?: 4} (Batch ${student?.batch ?: "2024-2028"})",
                                            style = Typography.bodySmall,
                                            color = SlateSecondary
                                        )
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AppleBadge(
                                                text = "CGPA: ${student?.cgpa ?: 8.92}",
                                                containerColor = NursingTealSubtle,
                                                textColor = NursingTealDark
                                            )
                                            AppleBadge(
                                                text = "Blood: ${student?.bloodGroup ?: "O+"}",
                                                containerColor = CrimsonErrorSubtle,
                                                textColor = CrimsonError
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Metric Stat Cards Row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppleStatCard(
                                    title = "Clinical Attendance",
                                    value = "%.1f%%".format(attendancePct),
                                    subtitle = if (attendancePct >= 80.0) "Meets INC standard (>80%)" else "Below required minimum",
                                    modifier = Modifier.weight(1f),
                                    accentColor = if (attendancePct >= 80.0) EmeraldSuccess else CrimsonError,
                                    badgeText = if (attendancePct >= 80.0) "Eligible" else "Shortage",
                                    badgeColor = if (attendancePct >= 80.0) EmeraldSuccessSubtle else CrimsonErrorSubtle,
                                    badgeTextColor = if (attendancePct >= 80.0) EmeraldSuccess else CrimsonError
                                )

                                AppleStatCard(
                                    title = "Pending Fees",
                                    value = "₹${pendingFeeSum.toInt()}",
                                    subtitle = if (pendingFeeSum == 0.0) "All dues cleared" else "2 installments pending",
                                    modifier = Modifier.weight(1f),
                                    accentColor = if (pendingFeeSum == 0.0) EmeraldSuccess else AmberWarning,
                                    badgeText = if (pendingFeeSum == 0.0) "Settled" else "Due Soon",
                                    badgeColor = if (pendingFeeSum == 0.0) EmeraldSuccessSubtle else AmberWarningSubtle,
                                    badgeTextColor = if (pendingFeeSum == 0.0) EmeraldSuccess else AmberWarning
                                )
                            }
                        }

                        // Clinical Posting & Attendance Progress Card
                        item {
                            AppleCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 20.dp
                            ) {
                                Text(
                                    text = "Clinical Posting & Theory Attendance",
                                    style = Typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalPrimary
                                )
                                Text(
                                    text = "Indian Nursing Council (INC) requires 80% theory & 100% clinical postings for University Examination Hall Ticket.",
                                    style = Typography.bodySmall,
                                    color = SlateSecondary,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                                )

                                AppleProgressBar(
                                    progress = (attendancePct / 100.0).toFloat(),
                                    label = "Overall Attendance Ratio",
                                    valueText = "%.1f%%".format(attendancePct),
                                    progressColor = if (attendancePct >= 80.0) NursingTealDark else CrimsonError
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Total Recorded Periods", style = Typography.bodySmall, color = SlateSecondary)
                                        Text(text = "$totalClasses Classes", style = Typography.titleSmall, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
                                    }
                                    Column {
                                        Text(text = "Present / On-Duty", style = Typography.bodySmall, color = SlateSecondary)
                                        Text(text = "$presentCount Days", style = Typography.titleSmall, fontWeight = FontWeight.SemiBold, color = EmeraldSuccess)
                                    }
                                    Column {
                                        Text(text = "Clinical Rotations", style = Typography.bodySmall, color = SlateSecondary)
                                        Text(text = "OT, ICU & Ward", style = Typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MedicalBlue)
                                    }
                                }
                            }
                        }

                        // Institutional Circulars & Notices
                        item {
                            AppleSectionHeader(title = "College Circulars & Notices")
                        }

                        if (notices.isEmpty()) {
                            item {
                                AppleCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "No active notices at this time.",
                                        style = Typography.bodyMedium,
                                        color = SlateMuted
                                    )
                                }
                            }
                        } else {
                            items(notices) { notice ->
                                AppleCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        AppleBadge(
                                            text = notice.priority.name,
                                            containerColor = if (notice.priority == NoticePriority.URGENT || notice.priority == NoticePriority.HIGH) CrimsonErrorSubtle else NursingTealSubtle,
                                            textColor = if (notice.priority == NoticePriority.URGENT || notice.priority == NoticePriority.HIGH) CrimsonError else NursingTealDark
                                        )
                                        Text(
                                            text = "Official Notice",
                                            style = Typography.labelSmall,
                                            color = SlateMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = notice.title,
                                        style = Typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalPrimary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = notice.content,
                                        style = Typography.bodyMedium,
                                        color = SlateSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    StudentTab.ACADEMIC -> {
                        // Academic & Clinical Tab: Timetable, Internal Assessment Marks, Subjects
                        item {
                            AppleSectionHeader(title = "Today's Clinical & Lecture Schedule")
                        }

                        if (timetable.isEmpty()) {
                            item {
                                AppleCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("No timetable entries for today.", style = Typography.bodyMedium, color = SlateMuted)
                                }
                            }
                        } else {
                            items(timetable) { slot ->
                                AppleCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(NursingTealSurface)
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${slot.startTime}\n${slot.endTime}",
                                                style = Typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = NursingTealDark,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Period ${slot.periodNo} • ${slot.roomNo}",
                                                style = Typography.labelSmall,
                                                color = SlateSecondary
                                            )
                                            Text(
                                                text = when (slot.subjectId) {
                                                    1L -> "Adult Health (Med-Surg) Nursing II"
                                                    2L -> "Pharmacology, Pathology & Genetics"
                                                    3L -> "Community Health Nursing I"
                                                    4L -> "Hospital Clinical Practical Posting & OSCE"
                                                    else -> "Nursing Ethics & Practice"
                                                },
                                                style = Typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CharcoalPrimary
                                            )
                                        }

                                        AppleBadge(
                                            text = if (slot.subjectId == 4L) "Clinical Lab" else "Theory",
                                            containerColor = if (slot.subjectId == 4L) MedicalBlueSubtle else SlateBorderSubtle,
                                            textColor = if (slot.subjectId == 4L) MedicalBlue else SlateSecondary
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            AppleSectionHeader(title = "Internal Assessment Examination Marks")
                        }

                        if (marks.isEmpty()) {
                            item {
                                AppleCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("No internal marks published yet.", style = Typography.bodyMedium, color = SlateMuted)
                                }
                            }
                        } else {
                            items(marks) { mark ->
                                AppleCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = when (mark.subjectId) {
                                                    1L -> "Adult Health Nursing II (NUR-401)"
                                                    2L -> "Pharmacology & Genetics (NUR-402)"
                                                    3L -> "Community Health Nursing (NUR-403)"
                                                    4L -> "Clinical OSCE Practical (NUR-404)"
                                                    else -> "Nursing Ethics (NUR-405)"
                                                },
                                                style = Typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalPrimary
                                            )
                                            Text(
                                                text = "${mark.examType.name.replace("_", " ")} • ${mark.remarks ?: "Verified by Faculty"}",
                                                style = Typography.bodySmall,
                                                color = SlateSecondary
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${mark.marksObtained.toInt()} / ${mark.maxMarks.toInt()}",
                                                style = Typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = NursingTealDark
                                            )
                                            val pct = (mark.marksObtained / mark.maxMarks * 100.0)
                                            Text(
                                                text = "%.0f%%".format(pct),
                                                style = Typography.labelSmall,
                                                color = if (pct >= 75) EmeraldSuccess else SlateSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    StudentTab.FEES -> {
                        // Fees Tab: Invoices, Razorpay online checkout simulation, receipts
                        item {
                            AppleCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = NursingTealSurface,
                                borderColor = NursingTealSubtle
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Total Outstanding Dues", style = Typography.bodySmall, color = NursingTealDark)
                                        Text(text = "₹${pendingFeeSum.toInt()}", style = Typography.headlineMedium, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                    }
                                    AppleBadge(
                                        text = if (pendingFeeSum == 0.0) "All Fees Paid" else "Action Required",
                                        containerColor = if (pendingFeeSum == 0.0) EmeraldSuccessSubtle else AmberWarningSubtle,
                                        textColor = if (pendingFeeSum == 0.0) EmeraldSuccess else AmberWarning
                                    )
                                }
                            }
                        }

                        item {
                            AppleSectionHeader(title = "Institutional Fee Invoices")
                        }

                        items(fees) { fee ->
                            val isPaid = fee.status == FeeStatus.PAID
                            val due = fee.totalAmount - fee.paidAmount

                            AppleCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = fee.feeType, style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                        Text(text = "Semester ${fee.semester} • Due Date: ${fee.dueDate}", style = Typography.bodySmall, color = SlateSecondary)
                                    }
                                    AppleBadge(
                                        text = fee.status.name,
                                        containerColor = if (isPaid) EmeraldSuccessSubtle else AmberWarningSubtle,
                                        textColor = if (isPaid) EmeraldSuccess else AmberWarning
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Total Amount", style = Typography.bodySmall, color = SlateSecondary)
                                        Text(text = "₹${fee.totalAmount.toInt()}", style = Typography.titleSmall, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
                                    }
                                    Column {
                                        Text(text = "Paid Amount", style = Typography.bodySmall, color = SlateSecondary)
                                        Text(text = "₹${fee.paidAmount.toInt()}", style = Typography.titleSmall, fontWeight = FontWeight.SemiBold, color = EmeraldSuccess)
                                    }
                                    Column {
                                        Text(text = "Balance Due", style = Typography.bodySmall, color = SlateSecondary)
                                        Text(text = "₹${due.toInt()}", style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = if (due > 0) CrimsonError else CharcoalPrimary)
                                    }
                                }

                                if (!isPaid) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    AppleButton(
                                        text = "Pay ₹${due.toInt()} via Razorpay",
                                        onClick = { feeToPay = fee },
                                        icon = Icons.Default.Payment,
                                        containerColor = NursingTealDark,
                                        testTag = "pay_fee_button_${fee.id}"
                                    )
                                }
                            }
                        }

                        // Payment Receipts Section
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            AppleSectionHeader(title = "Settled Payment Receipts")
                        }

                        if (payments.isEmpty()) {
                            item {
                                AppleCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("No previous transactions recorded.", style = Typography.bodyMedium, color = SlateMuted)
                                }
                            }
                        } else {
                            items(payments) { payment ->
                                AppleCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = payment.receiptNo, style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                            Text(text = "${payment.mode.name} • Ref: ${payment.transactionId}", style = Typography.bodySmall, color = SlateSecondary)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "₹${payment.amount.toInt()}", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                            AppleBadge(text = "Verified", containerColor = EmeraldSuccessSubtle, textColor = EmeraldSuccess)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    StudentTab.REQUESTS -> {
                        // Requests & Library Tab: Bonafide / OD Certificate, Library Books
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Student Service Requests",
                                    style = Typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalPrimary
                                )
                                Button(
                                    onClick = { showNewRequestDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NursingTealDark),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("create_request_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Request", style = Typography.labelMedium)
                                }
                            }
                        }

                        if (requests.isEmpty()) {
                            item {
                                AppleCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("No service requests submitted.", style = Typography.bodyMedium, color = SlateMuted)
                                }
                            }
                        } else {
                            items(requests) { req ->
                                AppleCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        AppleBadge(
                                            text = req.type.name,
                                            containerColor = NursingTealSubtle,
                                            textColor = NursingTealDark
                                        )
                                        AppleBadge(
                                            text = req.status.name,
                                            containerColor = when (req.status) {
                                                RequestStatus.APPROVED -> EmeraldSuccessSubtle
                                                RequestStatus.REJECTED -> CrimsonErrorSubtle
                                                else -> AmberWarningSubtle
                                            },
                                            textColor = when (req.status) {
                                                RequestStatus.APPROVED -> EmeraldSuccess
                                                RequestStatus.REJECTED -> CrimsonError
                                                else -> AmberWarning
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(text = req.subject, style = Typography.titleSmall, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
                                    Text(text = req.reason, style = Typography.bodySmall, color = SlateSecondary)

                                    if (req.remarks != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Office note: ${req.remarks}", style = Typography.labelSmall, color = NursingTealDark)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            AppleSectionHeader(title = "Institutional Library Issued Books")
                        }

                        if (libraryBooks.isEmpty()) {
                            item {
                                AppleCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("No books currently issued.", style = Typography.bodyMedium, color = SlateMuted)
                                }
                            }
                        } else {
                            items(libraryBooks) { book ->
                                AppleCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(NursingTealSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, tint = NursingTealDark, modifier = Modifier.size(20.dp))
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = book.bookTitle, style = Typography.titleSmall, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
                                            Text(text = "Code: ${book.bookCode} • Due: ${book.dueDate}", style = Typography.bodySmall, color = SlateSecondary)
                                        }

                                        AppleBadge(text = book.status, containerColor = EmeraldSuccessSubtle, textColor = EmeraldSuccess)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Razorpay Payment Simulation Sheet
        if (feeToPay != null) {
            val f = feeToPay!!
            val balance = f.totalAmount - f.paidAmount

            AlertDialog(
                onDismissRequest = { if (!paymentProcessing) feeToPay = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = NursingTealDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Razorpay Online Checkout", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "SKS College of Nursing ERP - Payment Gateway",
                            style = Typography.labelSmall,
                            color = SlateSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppleCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = AppleCardElevated
                        ) {
                            Text(text = f.feeType, style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                            Text(text = "Due Amount: ₹${balance.toInt()}", style = Typography.headlineSmall, fontWeight = FontWeight.Bold, color = NursingTealDark)
                            Text(text = "Student Reg: ${student?.regNo ?: "NUR2024001"}", style = Typography.bodySmall, color = SlateSecondary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Supports UPI (GPay, PhonePe, Paytm), NetBanking & Debit/Credit Cards. Instant verified digital receipt will be generated.",
                            style = Typography.bodySmall,
                            color = SlateSecondary
                        )

                        if (paymentProcessing) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NursingTealDark)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Processing secure transaction...", style = Typography.bodySmall, color = NursingTealDark)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            paymentProcessing = true
                            coroutineScope.launch {
                                val txnId = "TXN_RZP_${System.currentTimeMillis()}"
                                collegeRepository.payFeeOnline(
                                    studentId = studentId,
                                    feeId = f.id,
                                    amount = balance,
                                    mode = PaymentMode.UPI,
                                    transactionId = txnId
                                )
                                paymentProcessing = false
                                feeToPay = null
                                paymentSuccessDialog = true
                            }
                        },
                        enabled = !paymentProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = NursingTealDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm & Pay ₹${balance.toInt()}")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { feeToPay = null },
                        enabled = !paymentProcessing
                    ) {
                        Text("Cancel", color = SlateSecondary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = PureWhite
            )
        }

        // Payment Success Dialog
        if (paymentSuccessDialog) {
            AlertDialog(
                onDismissRequest = { paymentSuccessDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payment Successful", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        text = "Your fee payment has been successfully recorded in the accounts ledger. A digital receipt with university serial number has been added to your Fees & Receipts tab.",
                        style = Typography.bodyMedium,
                        color = SlateSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { paymentSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = PureWhite
            )
        }

        // New Service Request Dialog
        if (showNewRequestDialog) {
            AlertDialog(
                onDismissRequest = { showNewRequestDialog = false },
                title = {
                    Text("Submit Certificate / Leave Request", style = Typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        AppleSegmentedControl(
                            items = listOf(RequestType.BONAFIDE, RequestType.LEAVE, RequestType.TC),
                            selectedItem = newRequestType,
                            onItemSelected = { newRequestType = it },
                            itemLabel = { it.name }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AppleTextField(
                            value = newRequestSubject,
                            onValueChange = { newRequestSubject = it },
                            label = "Request Subject",
                            placeholder = "e.g. Bonafide for State Nursing Scholarship"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppleTextField(
                            value = newRequestReason,
                            onValueChange = { newRequestReason = it },
                            label = "Reason / Remarks",
                            placeholder = "Describe details for the Dean / Principal"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newRequestSubject.isNotBlank()) {
                                coroutineScope.launch {
                                    collegeRepository.submitStudentRequest(
                                        studentId = studentId,
                                        type = newRequestType,
                                        subject = newRequestSubject,
                                        reason = newRequestReason
                                    )
                                    showNewRequestDialog = false
                                    newRequestSubject = ""
                                    newRequestReason = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NursingTealDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Request")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewRequestDialog = false }) {
                        Text("Cancel", color = SlateSecondary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = PureWhite
            )
        }
    }
}
