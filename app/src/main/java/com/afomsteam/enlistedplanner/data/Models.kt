package com.afomsteam.enlistedplanner.data

import java.util.UUID

enum class Component(val label: String) {
    REGAF("Active Duty"), AFR("Air Force Reserve"), ANG("Air National Guard")
}

enum class Grade(val label: String, val payGrade: String, val order: Int) {
    AB("AB", "E-1", 1),
    AMN("Amn", "E-2", 2),
    A1C("A1C", "E-3", 3),
    SRA("SrA", "E-4", 4),
    SSGT("SSgt", "E-5", 5),
    TSGT("TSgt", "E-6", 6),
    MSGT("MSgt", "E-7", 7),
    SMSGT("SMSgt", "E-8", 8),
    CMSGT("CMSgt", "E-9", 9);

    fun next(): Grade? = entries.firstOrNull { it.order == order + 1 }
}

enum class Severity { CRITICAL, WARNING, INFO, GOOD }
enum class TaskPriority { LOW, NORMAL, HIGH }
enum class Availability(val label: String) {
    AVAILABLE("Available"), LEAVE("Leave"), TDY("TDY"), DEPLOYED("Deployed"), PME("PME/Training"), OTHER("Unavailable/Other")
}

enum class Mga(val label: String) {
    EXECUTING_MISSION("Executing the Mission"),
    LEADING_PEOPLE("Leading People"),
    MANAGING_RESOURCES("Managing Resources"),
    IMPROVING_UNIT("Improving the Unit")
}

enum class DevelopmentArea(val label: String) {
    PROFESSIONAL("Professional"), LEADERSHIP("Leadership"), EDUCATION("Education"),
    QUALIFICATIONS("Qualifications"), FITNESS("Fitness/Readiness"), PERSONAL("Personal")
}

data class Profile(
    val name: String = "",
    val component: Component = Component.REGAF,
    val grade: Grade = Grade.SSGT,
    val dateOfRank: String = "",
    val dateEnteredService: String = "",
    val afsc: String = "",
    val skillLevel: Int = 5,
    val dutyTitle: String = "",
    val workCenter: String = "",
    val supervisesAirmen: Boolean = false,
    val managesProgram: Boolean = false,
    val sixYearEnlistee: Boolean = false,
    val bmtCompletionDate: String = "",
    val technicalTrainingCompletionDate: String = "",
    val nextPfraDate: String = "",
    val lastPfraScore: Double? = null
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dueDate: String = "",
    val completed: Boolean = false,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val linkedMemberId: String = "",
    val notes: String = ""
)

data class Accomplishment(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val action: String,
    val impact: String = "",
    val mga: Mga = Mga.EXECUTING_MISSION,
    val alq: String = "",
    val impactLevel: String = "Work Center",
    val linkedMemberId: String = ""
)

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val area: DevelopmentArea = DevelopmentArea.PROFESSIONAL,
    val horizon: String = "90 Days",
    val nextAction: String = "",
    val targetDate: String = "",
    val completed: Boolean = false
)

data class Qualification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val expirationDate: String = "",
    val completedDate: String = "",
    val isCritical: Boolean = false
)

data class TeamMember(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val grade: Grade = Grade.A1C,
    val component: Component = Component.REGAF,
    val dateOfRank: String = "",
    val dateEnteredService: String = "",
    val afsc: String = "",
    val skillLevel: Int = 3,
    val dutyTitle: String = "",
    val workCenter: String = "",
    val availability: Availability = Availability.AVAILABLE,
    val supervisionStartDate: String = "",
    val lastFeedbackDate: String = "",
    val upgradeProgress: Int = 0,
    val upgradeTargetDate: String = "",
    val selectedForPromotion: Boolean = false,
    val selectedGrade: Grade? = null,
    val lineNumber: String = "",
    val projectedPromotionDate: String = "",
    val sixYearEnlistee: Boolean = false,
    val bmtCompletionDate: String = "",
    val technicalTrainingCompletionDate: String = "",
    val qualifications: List<Qualification> = emptyList()
)

data class ProgramItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dueDate: String = "",
    val status: String = "On Track",
    val backupMemberId: String = "",
    val notes: String = ""
)

data class LeadershipCommitment(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val text: String,
    val dueDate: String = "",
    val completed: Boolean = false
)

data class RecognitionItem(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val title: String,
    val date: String = "",
    val status: String = "Consider",
    val notes: String = ""
)

data class CriticalRole(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val primaryMemberId: String = "",
    val backupMemberIds: List<String> = emptyList(),
    val minimumBackups: Int = 1
)

data class WholeAirmanCheck(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val mental: Int = 3,
    val physical: Int = 3,
    val social: Int = 3,
    val spiritual: Int = 3,
    val oneAction: String = ""
)

data class FinancialReadiness(
    val monthlyTakeHome: Double = 0.0,
    val essentials: Double = 0.0,
    val debtPayments: Double = 0.0,
    val savings: Double = 0.0,
    val tspPercent: Double = 0.0,
    val emergencyFundMonths: Double = 0.0
)

data class FitnessReadiness(
    val heightInches: Double = 0.0,
    val waistInches: Double = 0.0,
    val cardioPoints: Double = 0.0,
    val strengthPoints: Double = 0.0,
    val corePoints: Double = 0.0,
    val bodyCompositionPoints: Double = 0.0,
    val nextPfraDate: String = ""
)

data class LifePlan(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val title: String,
    val dueDate: String = "",
    val completed: Boolean = false
)

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String = "",
    val date: String = ""
)

data class PlannerEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val time: String = "",
    val category: String = "Personal",
    val description: String = ""
)

data class TimeBlock(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val startTime: String,
    val endTime: String,
    val title: String
)

data class LaunchpadItem(
    val id: String,
    val phase: String,
    val title: String,
    val completed: Boolean = false
)

data class StudyPlan(
    val cycle: String = "",
    val examDate: String = "",
    val weeklyHours: Double = 0.0,
    val pdgFocus: String = "",
    val sktFocus: String = ""
)

data class ExperienceItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String = "Duty",
    val startDate: String = "",
    val endDate: String = "",
    val impact: String = ""
)


data class SwotEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val strengths: String = "",
    val weaknesses: String = "",
    val opportunities: String = "",
    val threats: String = "",
    val nextAction: String = ""
)

data class ProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dueDate: String = "",
    val status: String = "On Track",
    val ownerMemberId: String = "",
    val notes: String = ""
)

data class TeamGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val memberIds: List<String> = emptyList()
)

data class AppSettings(
    val disclosureAccepted: Boolean = false,
    val dailyNotifications: Boolean = true,
    val notificationHour: Int = 7,
    val favoriteTools: List<String> = listOf("Promotion Analyzer", "Evaluation & Feedback", "Financial Readiness", "Fitness")
)

data class PlannerState(
    val profile: Profile = Profile(),
    val tasks: List<Task> = emptyList(),
    val accomplishments: List<Accomplishment> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val selfUpgradeProgress: Int = 0,
    val selfUpgradeTargetDate: String = "",
    val selfQualifications: List<Qualification> = emptyList(),
    val team: List<TeamMember> = emptyList(),
    val programs: List<ProgramItem> = emptyList(),
    val commitments: List<LeadershipCommitment> = emptyList(),
    val recognition: List<RecognitionItem> = emptyList(),
    val criticalRoles: List<CriticalRole> = emptyList(),
    val wholeAirmanChecks: List<WholeAirmanCheck> = emptyList(),
    val financial: FinancialReadiness = FinancialReadiness(),
    val fitness: FitnessReadiness = FitnessReadiness(),
    val lifePlans: List<LifePlan> = emptyList(),
    val notes: List<NoteItem> = emptyList(),
    val events: List<PlannerEvent> = emptyList(),
    val timeBlocks: List<TimeBlock> = emptyList(),
    val launchpad: List<LaunchpadItem> = defaultLaunchpad(),
    val studyPlan: StudyPlan = StudyPlan(),
    val experience: List<ExperienceItem> = emptyList(),
    val swotEntries: List<SwotEntry> = emptyList(),
    val projects: List<ProjectItem> = emptyList(),
    val teamGroups: List<TeamGroup> = emptyList(),
    val settings: AppSettings = AppSettings()
)

fun defaultLaunchpad(): List<LaunchpadItem> = listOf(
    LaunchpadItem("lp01", "First 30 Days", "Meet your supervisor and understand duty expectations"),
    LaunchpadItem("lp02", "First 30 Days", "Verify personnel and pay records"),
    LaunchpadItem("lp03", "First 30 Days", "Understand your LES, BRS and TSP"),
    LaunchpadItem("lp04", "First 30 Days", "Learn local helping and emergency resources"),
    LaunchpadItem("lp05", "First 30 Days", "Understand fitness expectations"),
    LaunchpadItem("lp06", "Days 31–60", "Review upgrade training and qualification requirements"),
    LaunchpadItem("lp07", "Days 31–60", "Discuss education and CCAF goals"),
    LaunchpadItem("lp08", "Days 31–60", "Understand evaluations, ALQs and recognition"),
    LaunchpadItem("lp09", "Days 61–90", "Build a 90-day development plan"),
    LaunchpadItem("lp10", "Days 61–90", "Review career roadmap and promotion basics"),
    LaunchpadItem("lp11", "Days 61–90", "Complete a financial-readiness baseline"),
    LaunchpadItem("lp12", "Days 61–90", "Identify a mentor and schedule a career conversation")
)

data class BrainSignal(
    val id: String,
    val severity: Severity,
    val title: String,
    val detail: String,
    val dueDate: String = "",
    val category: String = "General",
    val memberId: String = ""
)
