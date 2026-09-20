package com.example.ui.portal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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

enum class StaffTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    STUDENTS("Students", Icons.Default.People),
    ACADEMIC("Clinical & Marks", Icons.Default.CheckCircle),
    FINANCE("Fees & Finance", Icons.Default.AccountBalance),
    SYSTEM("Audit & Notices", Icons.Default.Security)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffPortalHome(
    session: LoggedInUser,
    collegeRepository: CollegeRepository,
    onLogoutClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val staff = session.staffProfile
    val userRole = session.user.role

    var selectedTab by remember { mutableStateOf(StaffTab.OVERVIEW) }
    var searchQuery by remember { mutableStateOf("") }

    val students by collegeRepository.getAllStudents()
        .collectAsState(initial = emptyList())
    val departments by collegeRepository.getAllDepartments()
        .collectAsState(initial = emptyList())
    val allStaff by collegeRepository.getAllStaff()
        .collectAsState(initial = emptyList())
    val allPayments by collegeRepository.getAllPayments()
        .collectAsState(initial = emptyList())
    val notices by collegeRepository.getAllNotices()
        .collectAsState(initial = emptyList())
    val auditLogs by collegeRepository.getRecentAuditLogs(30)
        .collectAsState(initial = emptyList())

    val totalCollected = allPayments.sumOf { it.amount }

    // Dialog states for Quick Actions
    var showMarkAttendanceDialog by remember { mutableStateOf(false) }
    var selectedStudentForAttendance by remember { mutableStateOf<Student?>(null) }
    var attendanceStatusToMark by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var attendancePeriodInput by remember { mutableStateOf("1") }
    var attendanceRemarksInput by remember { mutableStateOf("") }

    var showEnterMarksDialog by remember { mutableStateOf(false) }
    var selectedStudentForMarks by remember { mutableStateOf<Student?>(null) }
    var marksObtainedInput by remember { mutableStateOf("45") }
    var maxMarksInput by remember { mutableStateOf("50") }
    var examTypeInput by remember { mutableStateOf(ExamType.INTERNAL_1) }
    var marksRemarksInput by remember { mutableStateOf("Clinical performance verified") }

    var showBroadcastNoticeDialog by remember { mutableStateOf(false) }
    var noticeTitleInput by remember { mutableStateOf("") }
    var noticeContentInput by remember { mutableStateOf("") }
    var noticePriorityInput by remember { mutableStateOf(NoticePriority.NORMAL) }

    var showRecordFeeDialog by remember { mutableStateOf(false) }
    var feeStudentRegInput by remember { mutableStateOf("NUR2024001") }
    var feeAmountInput by remember { mutableStateOf("16000") }
    var feeModeInput by remember { mutableStateOf(PaymentMode.CASH) }

    var actionSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Role display badges
    val roleBadgeColor = when (userRole) {
        UserRole.ADMIN -> AmberWarningSubtle
        UserRole.FACULTY -> NursingTealSubtle
        UserRole.ACCOUNTS -> EmeraldSuccessSubtle
        else -> SlateBorderSubtle
    }
    val roleTextColor = when (userRole) {
        UserRole.ADMIN -> AmberWarning
        UserRole.FACULTY -> NursingTealDark
        UserRole.ACCOUNTS -> EmeraldSuccess
        else -> SlateSecondary
    }

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
                                text = "${staff?.firstName ?: "Staff"} • ID: ${staff?.empId ?: session.user.identifier} (${userRole.name})",
                                style = Typography.bodySmall,
                                color = SlateSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.testTag("staff_logout_button")
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
                StaffTab.values().forEach { tab ->
                    // Filter tabs if specific role: e.g. hide audit from faculty
                    if (tab == StaffTab.SYSTEM && userRole == UserRole.FACULTY) {
                        // Skip system tab for faculty
                        return@forEach
                    }
                    if (tab == StaffTab.FINANCE && userRole == UserRole.FACULTY) {
                        // Skip finance tab for faculty
                        return@forEach
                    }

                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
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
                        modifier = Modifier.testTag("staff_tab_${tab.name.lowercase()}")
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
                    .widthIn(max = 760.dp)
                    .testTag("staff_dashboard_scroll"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success feedback banner
                if (actionSuccessMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldSuccessSubtle)
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = actionSuccessMessage!!,
                                    style = Typography.bodyMedium,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { actionSuccessMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                when (selectedTab) {
                    StaffTab.OVERVIEW -> {
                        // Staff Profile Banner
                        item {
                            AppleCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("staff_profile_card"),
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
                                            imageVector = when (userRole) {
                                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                                UserRole.FACULTY -> Icons.Default.School
                                                UserRole.ACCOUNTS -> Icons.Default.AccountBalanceWallet
                                                else -> Icons.Default.Person
                                            },
                                            contentDescription = null,
                                            tint = NursingTealDark,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${staff?.firstName ?: "Staff"} ${staff?.lastName ?: ""}".trim(),
                                            style = Typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalPrimary
                                        )
                                        Text(
                                            text = staff?.designation ?: "Institutional Member",
                                            style = Typography.bodySmall,
                                            color = SlateSecondary
                                        )
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AppleBadge(
                                                text = userRole.name,
                                                containerColor = roleBadgeColor,
                                                textColor = roleTextColor
                                            )
                                            AppleBadge(
                                                text = staff?.qualification ?: "Nursing Faculty",
                                                containerColor = SlateBorderSubtle,
                                                textColor = SlateSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Role-specific Metrics Row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppleStatCard(
                                    title = "Enrolled Students",
                                    value = "${students.size}",
                                    subtitle = "B.Sc. Nursing cohorts",
                                    modifier = Modifier.weight(1f),
                                    accentColor = NursingTealDark,
                                    badgeText = "Active",
                                    badgeColor = NursingTealSubtle,
                                    badgeTextColor = NursingTealDark
                                )

                                when (userRole) {
                                    UserRole.ACCOUNTS -> {
                                        AppleStatCard(
                                            title = "Fee Collections",
                                            value = "₹${totalCollected.toInt()}",
                                            subtitle = "${allPayments.size} verified transactions",
                                            modifier = Modifier.weight(1f),
                                            accentColor = EmeraldSuccess,
                                            badgeText = "Audited",
                                            badgeColor = EmeraldSuccessSubtle,
                                            badgeTextColor = EmeraldSuccess
                                        )
                                    }
                                    UserRole.ADMIN -> {
                                        AppleStatCard(
                                            title = "Faculty & Staff",
                                            value = "${allStaff.size + 1}",
                                            subtitle = "${departments.size} Nursing Depts",
                                            modifier = Modifier.weight(1f),
                                            accentColor = MedicalBlue,
                                            badgeText = "Verified",
                                            badgeColor = MedicalBlueSubtle,
                                            badgeTextColor = MedicalBlue
                                        )
                                    }
                                    else -> {
                                        AppleStatCard(
                                            title = "Clinical Postings",
                                            value = "Hospital OT/ICU",
                                            subtitle = "District Hospital Harur",
                                            modifier = Modifier.weight(1f),
                                            accentColor = NursingTealDark,
                                            badgeText = "In Rotation",
                                            badgeColor = NursingTealSubtle,
                                            badgeTextColor = NursingTealDark
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Action Buttons Grid
                        item {
                            AppleSectionHeader(title = "Clinical & Administrative Actions")
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (userRole == UserRole.FACULTY || userRole == UserRole.ADMIN) {
                                    AppleButton(
                                        text = "Mark Student Attendance & Postings",
                                        onClick = {
                                            selectedStudentForAttendance = students.firstOrNull()
                                            showMarkAttendanceDialog = true
                                        },
                                        icon = Icons.Default.HowToReg,
                                        testTag = "quick_action_attendance"
                                    )

                                    AppleSecondaryButton(
                                        text = "Enter Internal Assessment Marks",
                                        onClick = {
                                            selectedStudentForMarks = students.firstOrNull()
                                            showEnterMarksDialog = true
                                        },
                                        icon = Icons.Default.Grade,
                                        testTag = "quick_action_marks"
                                    )
                                }

                                if (userRole == UserRole.ACCOUNTS || userRole == UserRole.ADMIN) {
                                    AppleSecondaryButton(
                                        text = "Record Counter Fee Payment",
                                        onClick = { showRecordFeeDialog = true },
                                        icon = Icons.Default.PointOfSale,
                                        containerColor = EmeraldSuccessSubtle,
                                        contentColor = EmeraldSuccess,
                                        testTag = "quick_action_record_fee"
                                    )
                                }

                                if (userRole == UserRole.ADMIN) {
                                    AppleSecondaryButton(
                                        text = "Broadcast Official College Notice",
                                        onClick = { showBroadcastNoticeDialog = true },
                                        icon = Icons.Default.Campaign,
                                        containerColor = AmberWarningSubtle,
                                        contentColor = AmberWarning,
                                        testTag = "quick_action_notice"
                                    )
                                }
                            }
                        }

                        // Academic Departments Card
                        item {
                            AppleSectionHeader(title = "SKS Nursing Academic Departments")
                        }

                        items(departments) { dept ->
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
                                        Text(text = dept.name, style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                        Text(text = "HOD: ${dept.hodName}", style = Typography.bodySmall, color = SlateSecondary)
                                    }
                                    AppleBadge(text = dept.code, containerColor = NursingTealSubtle, textColor = NursingTealDark)
                                }
                            }
                        }
                    }

                    StaffTab.STUDENTS -> {
                        // Students Tab: Searchable Nursing Student Directory
                        item {
                            AppleTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = "Search Student Directory",
                                placeholder = "Search by Name or Register Number (e.g. Ananya, NUR2024001)",
                                leadingIcon = Icons.Default.Search,
                                testTag = "search_student_input"
                            )
                        }

                        val filteredStudents = students.filter {
                            searchQuery.isBlank() ||
                                    it.firstName.contains(searchQuery, ignoreCase = true) ||
                                    it.lastName.contains(searchQuery, ignoreCase = true) ||
                                    it.regNo.contains(searchQuery, ignoreCase = true)
                        }

                        item {
                            AppleSectionHeader(title = "Nursing Students (${filteredStudents.size})")
                        }

                        if (filteredStudents.isEmpty()) {
                            item {
                                AppleCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("No matching students found.", style = Typography.bodyMedium, color = SlateMuted)
                                }
                            }
                        } else {
                            items(filteredStudents) { st ->
                                AppleCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(NursingTealSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${st.firstName.first()}${st.lastName.firstOrNull() ?: ""}",
                                                style = Typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = NursingTealDark
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${st.firstName} ${st.lastName}",
                                                style = Typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalPrimary
                                            )
                                            Text(
                                                text = "Reg: ${st.regNo} • Sem ${st.currentSemester} (Batch ${st.batch})",
                                                style = Typography.bodySmall,
                                                color = SlateSecondary
                                            )
                                            Text(
                                                text = "Guardian: ${st.guardianName} (${st.guardianPhone})",
                                                style = Typography.bodySmall,
                                                color = SlateMuted
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            AppleBadge(
                                                text = "CGPA: ${st.cgpa}",
                                                containerColor = NursingTealSubtle,
                                                textColor = NursingTealDark
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            AppleBadge(
                                                text = st.bloodGroup,
                                                containerColor = CrimsonErrorSubtle,
                                                textColor = CrimsonError
                                            )
                                        }
                                    }

                                    // Direct action buttons on student card
                                    if (userRole == UserRole.FACULTY || userRole == UserRole.ADMIN) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    selectedStudentForAttendance = st
                                                    showMarkAttendanceDialog = true
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = NursingTealSurface, contentColor = NursingTealDark),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text("Mark Attendance", style = Typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                            }

                                            Button(
                                                onClick = {
                                                    selectedStudentForMarks = st
                                                    showEnterMarksDialog = true
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlueSubtle, contentColor = MedicalBlue),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text("Enter Marks", style = Typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    StaffTab.ACADEMIC -> {
                        // Academic & Clinical Records
                        item {
                            AppleCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = NursingTealSurface,
                                borderColor = NursingTealSubtle
                            ) {
                                Text(
                                    text = "Clinical Practical & Theory Management",
                                    style = Typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalPrimary
                                )
                                Text(
                                    text = "Recorded under Indian Nursing Council (INC) & The Tamil Nadu Dr. M.G.R. Medical University norms.",
                                    style = Typography.bodySmall,
                                    color = SlateSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        item {
                            AppleSectionHeader(title = "Clinical Posting Centers")
                        }

                        item {
                            AppleCard(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "1. SKS Hospital & Research Center", style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                Text(text = "Specialties: Medical ICU, Surgical OT, Emergency Casualty, Dialysis", style = Typography.bodySmall, color = SlateSecondary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "2. Government District Headquarters Hospital", style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                Text(text = "Specialties: Pediatric Ward, Labor Ward & OBG, NICU", style = Typography.bodySmall, color = SlateSecondary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "3. Community Health Center (CHC) Harur", style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                Text(text = "Focus: Rural outreach, Immunization surveys, Family health nursing", style = Typography.bodySmall, color = SlateSecondary)
                            }
                        }

                        item {
                            AppleSectionHeader(title = "Quick Attendance Recording")
                        }

                        items(students) { st ->
                            AppleCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "${st.firstName} ${st.lastName}", style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                        Text(text = "Reg: ${st.regNo}", style = Typography.bodySmall, color = SlateSecondary)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    collegeRepository.markAttendance(
                                                        studentId = st.id,
                                                        subjectId = 1L,
                                                        date = "2026-09-20",
                                                        status = AttendanceStatus.PRESENT,
                                                        period = 1,
                                                        markedByStaffId = staff?.id ?: 2L,
                                                        remarks = "Marked Present"
                                                    )
                                                    actionSuccessMessage = "Marked Present for ${st.firstName}"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Present", style = Typography.labelSmall)
                                        }

                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    collegeRepository.markAttendance(
                                                        studentId = st.id,
                                                        subjectId = 1L,
                                                        date = "2026-09-20",
                                                        status = AttendanceStatus.ON_DUTY,
                                                        period = 1,
                                                        markedByStaffId = staff?.id ?: 2L,
                                                        remarks = "Hospital Clinical Duty"
                                                    )
                                                    actionSuccessMessage = "Marked On-Duty for ${st.firstName}"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("OD", style = Typography.labelSmall)
                                        }

                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    collegeRepository.markAttendance(
                                                        studentId = st.id,
                                                        subjectId = 1L,
                                                        date = "2026-09-20",
                                                        status = AttendanceStatus.ABSENT,
                                                        period = 1,
                                                        markedByStaffId = staff?.id ?: 2L,
                                                        remarks = "Uninformed Absence"
                                                    )
                                                    actionSuccessMessage = "Marked Absent for ${st.firstName}"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Absent", style = Typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    StaffTab.FINANCE -> {
                        // Finance Tab for Accounts and Admin
                        item {
                            AppleCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = EmeraldSuccessSubtle,
                                borderColor = EmeraldSuccess.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Total Institutional Collections", style = Typography.bodySmall, color = EmeraldSuccess)
                                        Text(text = "₹${totalCollected.toInt()}", style = Typography.headlineMedium, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                    }
                                    AppleButton(
                                        text = "+ Collect Fee",
                                        onClick = { showRecordFeeDialog = true },
                                        containerColor = EmeraldSuccess,
                                        modifier = Modifier.width(130.dp),
                                        testTag = "finance_collect_fee_btn"
                                    )
                                }
                            }
                        }

                        item {
                            AppleSectionHeader(title = "Payment Transactions & Receipts")
                        }

                        if (allPayments.isEmpty()) {
                            item {
                                AppleCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("No payment transactions recorded.", style = Typography.bodyMedium, color = SlateMuted)
                                }
                            }
                        } else {
                            items(allPayments) { p ->
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
                                            Text(text = p.receiptNo, style = Typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                            Text(text = "${p.mode.name} • Student ID: ${p.studentId}", style = Typography.bodySmall, color = SlateSecondary)
                                            Text(text = p.notes ?: "Verified", style = Typography.labelSmall, color = SlateMuted)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "₹${p.amount.toInt()}", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                            AppleBadge(text = p.status.name, containerColor = EmeraldSuccessSubtle, textColor = EmeraldSuccess)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    StaffTab.SYSTEM -> {
                        // Audit Logs & Notices Broadcaster
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Security Audit Logs", style = Typography.titleMedium, fontWeight = FontWeight.Bold, color = CharcoalPrimary)
                                AppleBadge(text = "RBAC Enforced", containerColor = AmberWarningSubtle, textColor = AmberWarning)
                            }
                        }

                        items(auditLogs) { log ->
                            AppleCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    AppleBadge(text = log.action, containerColor = SlateBorderSubtle, textColor = CharcoalPrimary)
                                    Text(text = "User #${log.userId}", style = Typography.labelSmall, color = SlateMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = log.details, style = Typography.bodySmall, color = SlateSecondary)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Dialog: Mark Attendance
        if (showMarkAttendanceDialog) {
            AlertDialog(
                onDismissRequest = { showMarkAttendanceDialog = false },
                title = { Text("Mark Clinical / Theory Attendance", style = Typography.titleLarge, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Student: ${selectedStudentForAttendance?.firstName} (${selectedStudentForAttendance?.regNo})",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AppleSegmentedControl(
                            items = listOf(AttendanceStatus.PRESENT, AttendanceStatus.ON_DUTY, AttendanceStatus.ABSENT),
                            selectedItem = attendanceStatusToMark,
                            onItemSelected = { attendanceStatusToMark = it },
                            itemLabel = { it.name }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AppleTextField(
                            value = attendancePeriodInput,
                            onValueChange = { attendancePeriodInput = it },
                            label = "Period / Shift Number",
                            placeholder = "e.g. 1"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppleTextField(
                            value = attendanceRemarksInput,
                            onValueChange = { attendanceRemarksInput = it },
                            label = "Clinical Posting Center / Remarks",
                            placeholder = "e.g. District Hospital OT Posting"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val st = selectedStudentForAttendance
                            if (st != null) {
                                coroutineScope.launch {
                                    collegeRepository.markAttendance(
                                        studentId = st.id,
                                        subjectId = 1L,
                                        date = "2026-09-20",
                                        status = attendanceStatusToMark,
                                        period = attendancePeriodInput.toIntOrNull() ?: 1,
                                        markedByStaffId = staff?.id ?: 2L,
                                        remarks = attendanceRemarksInput.ifBlank { "Marked by Faculty" }
                                    )
                                    showMarkAttendanceDialog = false
                                    actionSuccessMessage = "Attendance recorded for ${st.firstName}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NursingTealDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Attendance")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMarkAttendanceDialog = false }) {
                        Text("Cancel", color = SlateSecondary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = PureWhite
            )
        }

        // Dialog: Enter Internal Assessment Marks
        if (showEnterMarksDialog) {
            AlertDialog(
                onDismissRequest = { showEnterMarksDialog = false },
                title = { Text("Enter Internal Assessment Marks", style = Typography.titleLarge, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Student: ${selectedStudentForMarks?.firstName} (${selectedStudentForMarks?.regNo})",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppleTextField(
                                value = marksObtainedInput,
                                onValueChange = { marksObtainedInput = it },
                                label = "Marks Obtained",
                                placeholder = "45",
                                modifier = Modifier.weight(1f)
                            )
                            AppleTextField(
                                value = maxMarksInput,
                                onValueChange = { maxMarksInput = it },
                                label = "Max Marks",
                                placeholder = "50",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AppleTextField(
                            value = marksRemarksInput,
                            onValueChange = { marksRemarksInput = it },
                            label = "Remarks / Observations",
                            placeholder = "e.g. Flawless clinical demonstration"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val st = selectedStudentForMarks
                            val marksObtained = marksObtainedInput.toDoubleOrNull() ?: 45.0
                            val maxMarks = maxMarksInput.toDoubleOrNull() ?: 50.0

                            if (st != null) {
                                coroutineScope.launch {
                                    collegeRepository.enterMarks(
                                        studentId = st.id,
                                        subjectId = 1L,
                                        examType = examTypeInput,
                                        marksObtained = marksObtained,
                                        maxMarks = maxMarks,
                                        enteredByStaffId = staff?.id ?: 2L,
                                        remarks = marksRemarksInput
                                    )
                                    showEnterMarksDialog = false
                                    actionSuccessMessage = "Marks recorded for ${st.firstName}: $marksObtained / $maxMarks"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NursingTealDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Record Marks")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEnterMarksDialog = false }) {
                        Text("Cancel", color = SlateSecondary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = PureWhite
            )
        }

        // Dialog: Broadcast Notice
        if (showBroadcastNoticeDialog) {
            AlertDialog(
                onDismissRequest = { showBroadcastNoticeDialog = false },
                title = { Text("Broadcast Institutional Circular", style = Typography.titleLarge, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        AppleTextField(
                            value = noticeTitleInput,
                            onValueChange = { noticeTitleInput = it },
                            label = "Circular Title",
                            placeholder = "e.g. Clinical Posting Uniform Inspection"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppleTextField(
                            value = noticeContentInput,
                            onValueChange = { noticeContentInput = it },
                            label = "Circular Body Content",
                            placeholder = "Enter instructions for students and faculty"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (noticeTitleInput.isNotBlank()) {
                                coroutineScope.launch {
                                    collegeRepository.postNotice(
                                        title = noticeTitleInput,
                                        content = noticeContentInput,
                                        targetAudience = NoticeAudience.ALL,
                                        postedByStaffId = staff?.id ?: 1L,
                                        priority = noticePriorityInput
                                    )
                                    showBroadcastNoticeDialog = false
                                    noticeTitleInput = ""
                                    noticeContentInput = ""
                                    actionSuccessMessage = "Circular published to institutional board."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NursingTealDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Broadcast Notice")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBroadcastNoticeDialog = false }) {
                        Text("Cancel", color = SlateSecondary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = PureWhite
            )
        }

        // Dialog: Record Counter Fee Payment
        if (showRecordFeeDialog) {
            AlertDialog(
                onDismissRequest = { showRecordFeeDialog = false },
                title = { Text("Record Accounts Counter Fee Payment", style = Typography.titleLarge, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        AppleTextField(
                            value = feeStudentRegInput,
                            onValueChange = { feeStudentRegInput = it },
                            label = "Student Register Number",
                            placeholder = "NUR2024001"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppleTextField(
                            value = feeAmountInput,
                            onValueChange = { feeAmountInput = it },
                            label = "Amount Paid (₹)",
                            placeholder = "16000"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppleSegmentedControl(
                            items = listOf(PaymentMode.CASH, PaymentMode.UPI, PaymentMode.CARD),
                            selectedItem = feeModeInput,
                            onItemSelected = { feeModeInput = it },
                            itemLabel = { it.name }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = feeAmountInput.toDoubleOrNull() ?: 16000.0
                            val st = students.find { it.regNo.equals(feeStudentRegInput.trim(), ignoreCase = true) } ?: students.firstOrNull()
                            if (st != null) {
                                coroutineScope.launch {
                                    collegeRepository.recordOfflinePayment(
                                        studentId = st.id,
                                        feeId = 3L,
                                        amount = amount,
                                        mode = feeModeInput,
                                        receivedByStaffId = staff?.id ?: 3L,
                                        notes = "Collected by ${staff?.firstName ?: "Accounts"} at Counter"
                                    )
                                    showRecordFeeDialog = false
                                    actionSuccessMessage = "Receipt generated for ₹${amount.toInt()} (Student: ${st.regNo})"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Generate Receipt")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRecordFeeDialog = false }) {
                        Text("Cancel", color = SlateSecondary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = PureWhite
            )
        }
    }
}
