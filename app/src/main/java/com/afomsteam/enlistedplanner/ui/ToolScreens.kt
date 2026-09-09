package com.afomsteam.enlistedplanner.ui

import android.content.Intent
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afomsteam.enlistedplanner.data.*
import com.afomsteam.enlistedplanner.logic.*
import com.afomsteam.enlistedplanner.util.PdfExporter
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun ToolScreen(name: String, vm: PlannerViewModel, modifier: Modifier, onOpen: (String) -> Unit, onBack: () -> Unit) {
    when (name) {
        "Attention Center" -> AttentionTool(vm, modifier)
        "Tasks" -> TasksTool(vm, modifier)
        "Promotion Analyzer" -> PromotionTool(vm, modifier, onOpen)
        "Evaluation & Feedback" -> EvaluationTool(vm, modifier, onOpen)
        "Awards & Recognition" -> AwardsTool(vm, modifier)
        "Quarter Summary" -> QuarterSummaryTool(vm, modifier)
        "Career Roadmap" -> CareerRoadmapTool(vm, modifier)
        "Experience Inventory" -> ExperienceTool(vm, modifier)
        "WAPS Study Plan" -> StudyPlanTool(vm, modifier)
        "Pro Hands / Professional References" -> ProHandsTool(modifier)
        "Fitness" -> FitnessTool(vm, modifier)
        "Whole Airman / CAF" -> WholeAirmanTool(vm, modifier)
        "Upgrade Training" -> UpgradeTrainingTool(vm, modifier, onOpen)
        "Qualifications" -> QualificationsTool(vm, modifier, onOpen)
        "Life Planning" -> LifePlanningTool(vm, modifier)
        "Helping Resources" -> HelpingResourcesTool(modifier)
        "Financial Readiness" -> FinancialReadinessTool(vm, modifier)
        "Programs & Projects" -> ProgramsTool(vm, modifier)
        "Force Management" -> ForceManagementTool(vm, modifier)
        "Leadership Commitments" -> CommitmentsTool(vm, modifier)
        "Recognition Review" -> RecognitionReviewTool(vm, modifier, onOpen)
        "Calendar" -> CalendarTool(vm, modifier)
        "Time Blocks" -> TimeBlocksTool(vm, modifier)
        "Notes" -> NotesTool(vm, modifier)
        "Backup & Restore" -> BackupTool(vm, modifier)
        "Goals" -> GoalsTool(vm, modifier)
        "SWOT Journal" -> SwotTool(vm, modifier)
        "Add Airman" -> AddAirmanTool(vm, modifier, onBack)
        "Add Accomplishment" -> AddAccomplishmentTool(vm, modifier, onBack)
        else -> GenericTool(name, modifier)
    }
}

@Composable
private fun ToolBody(modifier: Modifier, title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().padding(18.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ScreenHeader(title, subtitle)
        content()
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun AttentionTool(vm: PlannerViewModel, modifier: Modifier) {
    val signals = EnlistedBrain.signals(vm.state)
    ToolBody(modifier, "Attention Center", "Everything the enlisted brain thinks deserves review") {
        if (signals.isEmpty()) EmptyState("All clear", "No generated signals from the data currently recorded.")
        signals.forEach { SignalCard(it) }
        PlannerCard { Text("Signal philosophy", fontWeight = FontWeight.Bold); Text("Red = overdue/action required. Amber = due soon or a capability gap. Blue = planning information. Signals are planning aids and do not replace official systems.", color = TextMuted) }
    }
}

@Composable
private fun TasksTool(vm: PlannerViewModel, modifier: Modifier) {
    var title by remember { mutableStateOf("") }
    var due by remember { mutableStateOf(LocalDate.now().toString()) }
    var priority by remember { mutableStateOf(TaskPriority.NORMAL) }
    ToolBody(modifier, "Tasks", "Plan work, then convert meaningful work into career evidence") {
        LabeledField("Task", title, { title = it })
        LabeledField("Due date", due, { due = it })
        DropdownField("Priority", priority, TaskPriority.entries, { it.name.lowercase().replaceFirstChar(Char::uppercase) }, { priority = it })
        Button(enabled = title.isNotBlank(), onClick = { vm.addTask(Task(title = title.trim(), dueDate = due, priority = priority)); title = "" }) { Text("Add task") }
        SectionTitle("Open")
        vm.state.tasks.filterNot { it.completed }.sortedBy { it.dueDate }.forEach { t ->
            PlannerCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(t.completed, { vm.toggleTask(t.id) })
                    Column(Modifier.weight(1f)) { Text(t.title, fontWeight = FontWeight.SemiBold); if (t.dueDate.isNotBlank()) Text(Dates.display(t.dueDate), color = TextMuted) }
                    if (t.priority == TaskPriority.HIGH) StatusChip("HIGH", Severity.WARNING)
                }
            }
        }
        SectionTitle("Completed")
        vm.state.tasks.filter { it.completed }.takeLast(10).forEach { t -> PlannerCard { Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(true, { vm.toggleTask(t.id) }); Text(t.title) } } }
    }
}

@Composable
private fun PromotionTool(vm: PlannerViewModel, modifier: Modifier, onOpen: (String) -> Unit) {
    val p = vm.state.profile
    val self = TeamMember(name = p.name.ifBlank { "You" }, grade = p.grade, component = p.component, dateOfRank = p.dateOfRank, dateEnteredService = p.dateEnteredService, afsc = p.afsc, skillLevel = p.skillLevel, sixYearEnlistee = p.sixYearEnlistee, bmtCompletionDate = p.bmtCompletionDate, technicalTrainingCompletionDate = p.technicalTrainingCompletionDate)
    ToolBody(modifier, "Promotion Analyzer", "Analyze recorded dates and requirements; verify member-specific eligibility officially") {
        SectionTitle("You")
        PromotionAssessmentCard(PromotionRules.assess(self))
        if (p.dateOfRank.isBlank() || p.dateEnteredService.isBlank()) Text("Add your DOR and Date Entered Service in Profile for deeper analysis.", color = Warn)
        if (vm.state.team.isNotEmpty()) SectionTitle("Your Airmen")
        vm.state.team.forEach { m -> PlannerCard(onClick = { onOpen("member:${m.id}") }) { Text("${m.grade.label} ${m.name}", fontWeight = FontWeight.Bold); val a = PromotionRules.assess(m); Text(a.headline, color = SoftBlue); Text(a.details.firstOrNull().orEmpty(), color = TextMuted, style = MaterialTheme.typography.bodySmall) } }
        PlannerCard { Text("Trust boundary", fontWeight = FontWeight.Bold); Text("RegAF junior-enlisted timing and WAPS planning logic are estimates from recorded data. AFR/ANG records intentionally route to component-specific verification rather than pretending RegAF rules apply.", color = TextMuted) }
    }
}

@Composable
private fun PromotionAssessmentCard(a: PromotionAssessment) {
    PlannerCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(a.headline, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); StatusChip(a.status.name, a.status) }
        a.details.forEach { Text("• $it", color = TextMuted, modifier = Modifier.padding(top = 5.dp)) }
        Text(a.verification, color = Warn, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun EvaluationTool(vm: PlannerViewModel, modifier: Modifier, onOpen: (String) -> Unit) {
    val today = LocalDate.now()
    ToolBody(modifier, "Evaluation & Feedback", "SCOD visibility, feedback planning markers and performance evidence") {
        if (vm.state.profile.component == Component.REGAF) {
            val selfScod = EvaluationRules.nextScod(vm.state.profile.grade, today)
            PlannerCard { Text("Your ${vm.state.profile.grade.label} SCOD", fontWeight = FontWeight.Bold); Text(Dates.display(selfScod.toString()), style = MaterialTheme.typography.titleLarge); Text("${java.time.temporal.ChronoUnit.DAYS.between(today, selfScod)} days away", color = TextMuted) }
        } else {
            PlannerCard { Text("ARC evaluation cycle", fontWeight = FontWeight.Bold); Text("Verify status-specific SCOD", style = MaterialTheme.typography.titleMedium); Text("AFR/ANG SCOD cadence depends on status and cycle. This app does not assume RegAF timing for ARC members.", color = TextMuted) }
        }
        SectionTitle("Airmen")
        if (vm.state.team.isEmpty()) EmptyState("No Airmen", "Add supervised Airmen to calculate their next feedback planning marker and SCOD.")
        vm.state.team.forEach { m ->
            val f = EvaluationRules.nextFeedbackPlanningDate(m, today)
            val scod = if (m.component == Component.REGAF) EvaluationRules.nextScod(m.grade, today) else null
            PlannerCard(onClick = { onOpen("member:${m.id}") }) {
                Text("${m.grade.label} ${m.name}", fontWeight = FontWeight.Bold)
                Text("Feedback: ${f.first?.let { Dates.display(it.toString()) } ?: "Need supervision start/last feedback"}", color = TextMuted)
                Text("SCOD: ${scod?.let { Dates.display(it.toString()) } ?: "Verify ARC status-specific cycle"}", color = TextMuted)
            }
        }
        PlannerCard { Text("Feedback timing", fontWeight = FontWeight.Bold); Text("Planning cues use the recorded supervision start, initial 60-day requirement, junior-Airman 180-day cadence when applicable, projected midterm, and end-of-reporting-period window. Verify current requirements, CROs, status-specific ARC cycles, and exceptions in official evaluation guidance.", color = TextMuted) }
    }
}

@Composable
private fun AwardsTool(vm: PlannerViewModel, modifier: Modifier) {
    var memberId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    val options = listOf("" to "Self") + vm.state.team.map { it.id to "${it.grade.label} ${it.name}" }
    ToolBody(modifier, "Awards & Recognition", "Capture formal recognition and review performance evidence before it disappears") {
        val selected = options.firstOrNull { it.first == memberId } ?: options.first()
        DropdownField("For", selected, options, { it.second }, { memberId = it.first })
        LabeledField("Award / recognition", title, { title = it })
        LabeledField("Date", date, { date = it })
        Button(enabled = title.isNotBlank(), onClick = { vm.addRecognition(RecognitionItem(memberId = memberId, title = title.trim(), date = date, status = "Recorded")); title = "" }) { Text("Record recognition") }
        SectionTitle("Recognition history")
        vm.state.recognition.sortedByDescending { it.date }.forEach { r -> PlannerCard { Text(r.title, fontWeight = FontWeight.Bold); Text((vm.state.team.firstOrNull { it.id == r.memberId }?.name ?: "Self") + " • " + Dates.display(r.date), color = TextMuted) } }
    }
}

@Composable
private fun QuarterSummaryTool(vm: PlannerViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val quarter = ((today.monthValue - 1) / 3) + 1
    val start = LocalDate.of(today.year, (quarter - 1) * 3 + 1, 1)
    val items = vm.state.accomplishments.filter { Dates.parse(it.date)?.let { d -> !d.isBefore(start) && !d.isAfter(today) } == true }
    ToolBody(modifier, "Quarter Summary", "Your performance evidence assembled from normal daily capture") {
        PlannerCard {
            Text("Q$quarter ${today.year}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${items.size} accomplishments captured", color = TextMuted)
            Spacer(Modifier.height(8.dp))
            Mga.entries.forEach { mga -> Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(mga.label); Text(items.count { it.mga == mga }.toString(), fontWeight = FontWeight.Bold) } }
        }
        val least = Mga.entries.minByOrNull { mga -> items.count { it.mga == mga } }
        if (items.isNotEmpty() && least != null) PlannerCard { Text("Potential capture gap", fontWeight = FontWeight.Bold); Text("${least.label} has the least captured evidence this quarter. Review whether meaningful work is going uncaptured; do not manufacture accomplishments just to balance categories.", color = TextMuted) }
        Button(onClick = { val file = PdfExporter.createQuarterSummary(context, vm.state); PdfExporter.share(context, file) }, modifier = Modifier.fillMaxWidth()) { Text("Create & share PDF") }
        SectionTitle("Evidence")
        items.sortedByDescending { it.date }.forEach { a -> PlannerCard { Text(a.action, fontWeight = FontWeight.Bold); Text(a.impact, color = TextMuted); Text("${a.mga.label} • ${a.impactLevel}", color = SoftBlue, style = MaterialTheme.typography.labelSmall) } }
    }
}

@Composable
private fun CareerRoadmapTool(vm: PlannerViewModel, modifier: Modifier) {
    val g = vm.state.profile.grade
    val next = g.next()
    val focus = when (g) {
        Grade.AB, Grade.AMN, Grade.A1C -> listOf("Technical proficiency", "Upgrade training", "Fitness/readiness", "Financial foundation", "First-assignment habits")
        Grade.SRA -> listOf("Technical depth", "BTZ/SSgt awareness", "ALS/NCO transition", "Education", "Deliberate mentorship")
        Grade.SSGT -> listOf("Supervisor fundamentals", "7-level progression", "People development", "Program ownership", "Promotion record review")
        Grade.TSGT -> listOf("Section leadership", "Technical/organizational depth", "Succession planning", "Resource management", "MSgt competitiveness")
        Grade.MSGT -> listOf("Flight-level leadership", "Force development", "Manning/capability depth", "Strategic communication", "SMSgt board record")
        Grade.SMSGT -> listOf("Enterprise thinking", "Senior enlisted leadership", "Talent management", "Strategic alignment", "CMSgt board record")
        Grade.CMSGT -> listOf("Enterprise stewardship", "Developing leaders", "Institutional continuity", "Strategic advice", "Legacy and succession")
    }
    ToolBody(modifier, "Career Roadmap", "Guide development without pretending there is one perfect career path") {
        PlannerCard { Text("${g.label}${next?.let { " → ${it.label}" } ?: ""}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Current phase based on rank; tailor it to AFSC, duty position and goals.", color = TextMuted) }
        SectionTitle("Recommended focus")
        focus.forEach { PlannerCard { Text(it, fontWeight = FontWeight.SemiBold) } }
        SectionTitle("Your next actions")
        vm.state.goals.filterNot { it.completed }.take(6).forEach { PlannerCard { Text(it.title, fontWeight = FontWeight.Bold); Text(it.nextAction.ifBlank { "Add a next action" }, color = TextMuted) } }
    }
}

@Composable
private fun ExperienceTool(vm: PlannerViewModel, modifier: Modifier) {
    var title by remember { mutableStateOf("") }; var category by remember { mutableStateOf("Duty") }; var start by remember { mutableStateOf("") }; var end by remember { mutableStateOf("") }; var impact by remember { mutableStateOf("") }
    ToolBody(modifier, "Experience Inventory", "Keep a durable record of assignments, duties, deployments, education and major roles") {
        LabeledField("Experience", title, { title = it }); DropdownField("Category", category, listOf("Duty","Assignment","Deployment/TDY","Education","PME","Program","Certification","Volunteer"), { it }, { category = it }); LabeledField("Start", start, { start=it }); LabeledField("End", end, { end=it }); LabeledField("Impact / what you learned", impact, { impact=it }, singleLine=false)
        Button(enabled=title.isNotBlank(), onClick={ vm.addExperience(ExperienceItem(title=title.trim(),category=category,startDate=start,endDate=end,impact=impact)); title=""; impact="" }){Text("Add experience")}
        SectionTitle("Inventory")
        vm.state.experience.reversed().forEach { e -> PlannerCard { Text(e.title,fontWeight=FontWeight.Bold); Text("${e.category} • ${e.startDate}${e.endDate.takeIf{it.isNotBlank()}?.let{" – $it"}?:""}",color=TextMuted); if(e.impact.isNotBlank()) Text(e.impact,modifier=Modifier.padding(top=4.dp)) } }
    }
}

@Composable
private fun StudyPlanTool(vm: PlannerViewModel, modifier: Modifier) {
    val s = vm.state.studyPlan
    ToolBody(modifier, "WAPS Study Plan", "A study planner — official testing windows and source material still require verification") {
        LabeledField("Cycle", s.cycle, { v -> vm.update { it.copy(studyPlan = it.studyPlan.copy(cycle=v)) } }, "27E5")
        LabeledField("Exam / target date", s.examDate, { v -> vm.update { it.copy(studyPlan = it.studyPlan.copy(examDate=v)) } })
        LabeledField("Weekly study hours", s.weeklyHours.toString(), { v -> v.toDoubleOrNull()?.let { n -> vm.update { it.copy(studyPlan = it.studyPlan.copy(weeklyHours=n)) } } })
        LabeledField("PDG / PFE focus", s.pdgFocus, { v -> vm.update { it.copy(studyPlan = it.studyPlan.copy(pdgFocus=v)) } }, singleLine=false)
        LabeledField("SKT focus", s.sktFocus, { v -> vm.update { it.copy(studyPlan = it.studyPlan.copy(sktFocus=v)) } }, singleLine=false)
        Dates.daysUntil(s.examDate)?.let { days -> PlannerCard { Text(if(days>=0) "$days days to target" else "Target date passed", fontWeight=FontWeight.Bold); Text(if(days>0) "At ${s.weeklyHours} hr/week, approximately ${(days/7.0*s.weeklyHours).roundToInt()} planned study hours remain." else "Update your study cycle.", color=TextMuted) } }
    }
}

@Composable
private fun ProHandsTool(modifier: Modifier) {
    val refs = listOf(
        Triple("Evaluations, EPBs, feedback", "AFI 36-2406", "Evaluation system starting point; verify current publication and myFSS implementation guidance."),
        Triple("Enlisted promotion / BTZ", "AFI 36-2502", "Promotion and demotion starting point; verify current cycle messages, PECD and member eligibility."),
        Triple("Awards and decorations", "DAFI 36-2803", "Recognition program starting point; verify current supplements and award-specific guidance."),
        Triple("Dress and appearance", "DAFI 36-2903", "Uniform and appearance reference; verify current version and local guidance."),
        Triple("Assignments", "DAFI 36-2110", "Assignment system starting point; use MyVector/myFSS for member-specific actions."),
        Triple("Reenlistment / extensions", "DAFI 36-2606", "Retention reference starting point; verify current eligibility and MPF guidance."),
        Triple("Leave", "DAFI 36-3003", "Military leave program reference; verify current version and unit procedures."),
        Triple("Fitness", "Current DAF fitness guidance", "Use current official PFRA publication and scoring charts rather than a stored stale table."),
        Triple("Education / CCAF", "myFSS / Air University / CCAF", "Use current official education and credential guidance for irreversible decisions."),
        Triple("Not sure where to start", "myFSS / CSS / MPF / supervisor", "Use this map to find the likely lane, then verify in the controlling official source.")
    )
    ToolBody(modifier, "Pro Hands", "Find the right professional reference without remembering which publication covers the issue") {
        refs.forEach { (need, ref, note) -> PlannerCard { Text(need, fontWeight = FontWeight.Bold); Text(ref, color = AirBlue); Text(note, color = TextMuted, modifier = Modifier.padding(top = 4.dp)) } }
        PlannerCard { Text("Reference hygiene", fontWeight = FontWeight.Bold); Text("Publication numbers and titles can change. Treat Pro Hands as a routing map, not a frozen policy library. Verify the current e-Publishing/myFSS source before official action.", color = TextMuted) }
    }
}

@Composable
private fun FitnessTool(vm: PlannerViewModel, modifier: Modifier) {
    val f = vm.state.fitness
    val whtr = if (f.heightInches > 0 && f.waistInches > 0) f.waistInches / f.heightInches else 0.0
    val total = f.cardioPoints + f.strengthPoints + f.corePoints + f.bodyCompositionPoints
    ToolBody(modifier, "Fitness", "Track the next PFRA and your current official component points without pretending this replaces the scoring chart") {
        LabeledField("Height (inches)", f.heightInches.toString(), { v -> v.toDoubleOrNull()?.let { n -> vm.update { it.copy(fitness = it.fitness.copy(heightInches=n)) } } })
        LabeledField("Waist (inches)", f.waistInches.toString(), { v -> v.toDoubleOrNull()?.let { n -> vm.update { it.copy(fitness = it.fitness.copy(waistInches=n)) } } })
        if (whtr > 0) PlannerCard { Text("Waist-to-height ratio", fontWeight=FontWeight.Bold); Text(String.format("%.3f", whtr), style=MaterialTheme.typography.headlineSmall); Text("Use current official DAF guidance for interpretation and body-composition scoring.", color=TextMuted) }
        LabeledField("Cardio points", f.cardioPoints.toString(), { v -> v.toDoubleOrNull()?.let { n -> vm.update { it.copy(fitness = it.fitness.copy(cardioPoints=n)) } } })
        LabeledField("Strength points", f.strengthPoints.toString(), { v -> v.toDoubleOrNull()?.let { n -> vm.update { it.copy(fitness = it.fitness.copy(strengthPoints=n)) } } })
        LabeledField("Core points", f.corePoints.toString(), { v -> v.toDoubleOrNull()?.let { n -> vm.update { it.copy(fitness = it.fitness.copy(corePoints=n)) } } })
        LabeledField("Body-composition points", f.bodyCompositionPoints.toString(), { v -> v.toDoubleOrNull()?.let { n -> vm.update { it.copy(fitness = it.fitness.copy(bodyCompositionPoints=n)) } } })
        LabeledField("Next PFRA", f.nextPfraDate, { v -> vm.update { it.copy(fitness = it.fitness.copy(nextPfraDate=v)) } })
        PlannerCard { Text("Recorded total", fontWeight=FontWeight.Bold); Text(String.format("%.1f / 100", total), style=MaterialTheme.typography.headlineMedium); Text("Enter points from the current official scoring chart. This prevents stale age/sex tables from silently producing the wrong score.", color=TextMuted) }
    }
}

@Composable
private fun WholeAirmanTool(vm: PlannerViewModel, modifier: Modifier) {
    var mental by remember { mutableStateOf(3) }; var physical by remember { mutableStateOf(3) }; var social by remember { mutableStateOf(3) }; var spiritual by remember { mutableStateOf(3) }; var action by remember { mutableStateOf("") }
    ToolBody(modifier, "Whole Airman / CAF", "A non-clinical self-check across mental, physical, social and spiritual fitness") {
        PlannerCard { Text("This is reflection, not diagnosis", fontWeight=FontWeight.Bold); Text("Use 1–5 only as your own personal trend marker. Do not store detailed medical or mental-health information here.", color=TextMuted) }
        RatingRow("Mental", mental) { mental=it }; RatingRow("Physical", physical){physical=it}; RatingRow("Social", social){social=it}; RatingRow("Spiritual", spiritual){spiritual=it}
        LabeledField("One action this month", action, { action=it }, singleLine=false)
        Button(onClick={ vm.addWholeAirman(WholeAirmanCheck(date=LocalDate.now().toString(),mental=mental,physical=physical,social=social,spiritual=spiritual,oneAction=action)); action="" }){Text("Save check-in")}
        SectionTitle("History")
        vm.state.wholeAirmanChecks.sortedByDescending{it.date}.take(8).forEach { w -> PlannerCard { Text(Dates.display(w.date),fontWeight=FontWeight.Bold); Text("Mental ${w.mental}/5 • Physical ${w.physical}/5 • Social ${w.social}/5 • Spiritual ${w.spiritual}/5",color=TextMuted); if(w.oneAction.isNotBlank()) Text("Action: ${w.oneAction}",modifier=Modifier.padding(top=4.dp)) } }
    }
}

@Composable
private fun RatingRow(label:String, value:Int, onChange:(Int)->Unit){
    PlannerCard { Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically){ Text(label,Modifier.weight(1f),fontWeight=FontWeight.SemiBold); (1..5).forEach { n -> FilterChip(selected=value==n,onClick={onChange(n)},label={Text(n.toString())},modifier=Modifier.padding(start=4.dp)) } } }
}

@Composable
private fun UpgradeTrainingTool(vm: PlannerViewModel, modifier: Modifier, onOpen: (String) -> Unit) {
    ToolBody(modifier, "Upgrade Training", "Keep progress, target dates and supervisor visibility in one place") {
        SectionTitle("You")
        LabeledField("Your progress %", vm.state.selfUpgradeProgress.toString(), { v -> v.toIntOrNull()?.let { n -> vm.update { it.copy(selfUpgradeProgress=n.coerceIn(0,100)) } } })
        LabeledField("Target date", vm.state.selfUpgradeTargetDate, { v -> vm.update { it.copy(selfUpgradeTargetDate=v) } })
        LinearProgressIndicator(progress={vm.state.selfUpgradeProgress/100f},modifier=Modifier.fillMaxWidth())
        SectionTitle("Airmen")
        vm.state.team.forEach { m -> PlannerCard(onClick={onOpen("member:${m.id}")}) { Text("${m.grade.label} ${m.name}",fontWeight=FontWeight.Bold); Text("${m.upgradeProgress}% • Target ${Dates.display(m.upgradeTargetDate)}",color=TextMuted); LinearProgressIndicator(progress={m.upgradeProgress/100f},modifier=Modifier.fillMaxWidth().padding(top=8.dp)) } }
    }
}

@Composable
private fun QualificationsTool(vm: PlannerViewModel, modifier: Modifier, onOpen: (String) -> Unit) {
    var title by remember { mutableStateOf("") }; var exp by remember { mutableStateOf("") }; var critical by remember { mutableStateOf(false) }
    ToolBody(modifier, "Qualifications", "Track professional qualifications and expirations for yourself and your work center") {
        SectionTitle("You")
        LabeledField("Qualification",title,{title=it}); LabeledField("Expiration",exp,{exp=it}); Row(verticalAlignment=Alignment.CenterVertically){Checkbox(critical,{critical=it});Text("Critical capability")}
        Button(enabled=title.isNotBlank(),onClick={vm.update{it.copy(selfQualifications=it.selfQualifications+Qualification(title=title.trim(),expirationDate=exp,isCritical=critical))};title="";exp="";critical=false}){Text("Add qualification")}
        vm.state.selfQualifications.forEach { q -> PlannerCard { Text(q.title,fontWeight=FontWeight.Bold); Text(if(q.expirationDate.isBlank())"No expiration" else "Expires ${Dates.display(q.expirationDate)}",color=TextMuted) } }
        SectionTitle("Airmen")
        vm.state.team.forEach { m -> PlannerCard(onClick={onOpen("member:${m.id}")}) { Text("${m.grade.label} ${m.name}",fontWeight=FontWeight.Bold); Text("${m.qualifications.size} recorded qualification(s)",color=TextMuted); val expiring=m.qualifications.count{Dates.daysUntil(it.expirationDate)?.let{d->d<=60}==true}; if(expiring>0) StatusChip("$expiring expiring/expired",Severity.WARNING) } }
    }
}

@Composable
private fun LifePlanningTool(vm: PlannerViewModel, modifier: Modifier) {
    var category by remember{mutableStateOf("Personal")}; var title by remember{mutableStateOf("")}; var due by remember{mutableStateOf("")}
    ToolBody(modifier,"Life Planning","Keep personal logistics visible without storing sensitive detail"){
        DropdownField("Category",category,listOf("Personal","Family","PCS","Education","Vehicle","Housing","Legal/Admin","Other"),{it},{category=it});LabeledField("Item",title,{title=it});LabeledField("Due date",due,{due=it});Button(enabled=title.isNotBlank(),onClick={vm.addLifePlan(LifePlan(category=category,title=title.trim(),dueDate=due));title=""}){Text("Add")}
        vm.state.lifePlans.filterNot{it.completed}.forEach { l -> PlannerCard { Row(verticalAlignment=Alignment.CenterVertically){Checkbox(l.completed,{v->vm.update{s->s.copy(lifePlans=s.lifePlans.map{if(it.id==l.id)it.copy(completed=v) else it})}});Column{Text(l.title,fontWeight=FontWeight.Bold);Text("${l.category}${l.dueDate.takeIf{it.isNotBlank()}?.let{" • ${Dates.display(it)}"}?:""}",color=TextMuted)}} } }
    }
}

@Composable
private fun HelpingResourcesTool(modifier: Modifier) {
    val resources = listOf(
        Triple("Emergency","911","Immediate danger or medical emergency."),
        Triple("988 Suicide & Crisis Lifeline","988","Call or text for 24/7 crisis support in the United States."),
        Triple("Military OneSource","800-342-9647","Confidential non-medical counseling, relocation, finances, spouse/family and other support."),
        Triple("American Red Cross Hero Care","877-272-7337","Emergency communications and military family support."),
        Triple("Chaplain","Contact local chapel / Command Post","Confidential spiritual and pastoral support; local numbers vary by installation."),
        Triple("Sexual Assault Support","Contact installation SAPR/SARC","Use the current installation response line or official DAF resources."),
        Triple("Family Advocacy / Helping Agencies","Contact local installation","Use the installation directory for current local services and hours.")
    )
    ToolBody(modifier,"Helping Resources","A routing guide, not a replacement for emergency or official helping systems"){
        resources.forEach { (name,contact,detail)->PlannerCard{Text(name,fontWeight=FontWeight.Bold);Text(contact,color=AirBlue);Text(detail,color=TextMuted,modifier=Modifier.padding(top=4.dp))} }
        PlannerCard { Text("Localize this",fontWeight=FontWeight.Bold);Text("Add installation-specific numbers as ordinary Notes if needed, but avoid documenting an Airman's private situation or clinical details in the planner.",color=TextMuted) }
    }
}

@Composable
private fun FinancialReadinessTool(vm: PlannerViewModel, modifier: Modifier) {
    val f=vm.state.financial
    val free=(f.monthlyTakeHome-f.essentials-f.debtPayments-f.savings).coerceAtLeast(0.0)
    val fixedRatio=if(f.monthlyTakeHome>0)(f.essentials+f.debtPayments)/f.monthlyTakeHome else 0.0
    ToolBody(modifier,"Financial Readiness","A simple readiness baseline — not financial advice"){
        LabeledField("Monthly take-home",f.monthlyTakeHome.toString(),{v->v.toDoubleOrNull()?.let{n->vm.update{it.copy(financial=it.financial.copy(monthlyTakeHome=n))}}});LabeledField("Essential expenses",f.essentials.toString(),{v->v.toDoubleOrNull()?.let{n->vm.update{it.copy(financial=it.financial.copy(essentials=n))}}});LabeledField("Debt payments",f.debtPayments.toString(),{v->v.toDoubleOrNull()?.let{n->vm.update{it.copy(financial=it.financial.copy(debtPayments=n))}}});LabeledField("Monthly savings",f.savings.toString(),{v->v.toDoubleOrNull()?.let{n->vm.update{it.copy(financial=it.financial.copy(savings=n))}}});LabeledField("TSP contribution %",f.tspPercent.toString(),{v->v.toDoubleOrNull()?.let{n->vm.update{it.copy(financial=it.financial.copy(tspPercent=n))}}});LabeledField("Emergency fund (months)",f.emergencyFundMonths.toString(),{v->v.toDoubleOrNull()?.let{n->vm.update{it.copy(financial=it.financial.copy(emergencyFundMonths=n))}}})
        PlannerCard { Text("Monthly picture",fontWeight=FontWeight.Bold);Text("Unallocated after recorded categories: $${String.format("%.0f",free)}",style=MaterialTheme.typography.titleLarge);Text("Essentials + debt: ${(fixedRatio*100).roundToInt()}% of take-home",color=TextMuted);Text("Emergency fund: ${String.format("%.1f",f.emergencyFundMonths)} months • TSP: ${String.format("%.1f",f.tspPercent)}%",color=TextMuted) }
    }
}

@Composable
private fun ProgramsTool(vm: PlannerViewModel, modifier: Modifier) {
    var title by remember{mutableStateOf("")};var due by remember{mutableStateOf("")};var status by remember{mutableStateOf("On Track")};var backup by remember{mutableStateOf("")}
    var projectTitle by remember{mutableStateOf("")};var projectDue by remember{mutableStateOf("")};var projectStatus by remember{mutableStateOf("On Track")};var owner by remember{mutableStateOf("")};var projectNotes by remember{mutableStateOf("")}
    val people=listOf("" to "No backup/owner")+vm.state.team.map{it.id to "${it.grade.label} ${it.name}"}
    ToolBody(modifier,"Programs & Projects","Track recurring programs and one-time projects without losing continuity, ownership or suspenses"){
        SectionTitle("Programs")
        LabeledField("Program",title,{title=it});LabeledField("Next suspense",due,{due=it});DropdownField("Status",status,listOf("On Track","Watch","At Risk","Complete"),{it},{status=it});val sel=people.firstOrNull{it.first==backup}?:people.first();DropdownField("Backup",sel,people,{it.second},{backup=it.first});Button(enabled=title.isNotBlank(),onClick={vm.addProgram(ProgramItem(title=title.trim(),dueDate=due,status=status,backupMemberId=backup));title=""}){Text("Add program")}
        vm.state.programs.forEach { p -> PlannerCard { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(p.title,fontWeight=FontWeight.Bold);StatusChip(p.status,if(p.status=="At Risk")Severity.WARNING else Severity.INFO)};Text(if(p.dueDate.isBlank())"No suspense" else "Next: ${Dates.display(p.dueDate)}",color=TextMuted);Text("Backup: ${vm.state.team.firstOrNull{it.id==p.backupMemberId}?.name?:"None"}",color=TextMuted) } }
        SectionTitle("Projects")
        LabeledField("Project",projectTitle,{projectTitle=it});LabeledField("Due date",projectDue,{projectDue=it});DropdownField("Status",projectStatus,listOf("On Track","Watch","At Risk","Complete"),{it},{projectStatus=it});val osel=people.firstOrNull{it.first==owner}?:people.first();DropdownField("Owner",osel,people,{it.second},{owner=it.first});LabeledField("Objective / notes",projectNotes,{projectNotes=it},singleLine=false)
        Button(enabled=projectTitle.isNotBlank(),onClick={vm.update{it.copy(projects=it.projects+ProjectItem(title=projectTitle.trim(),dueDate=projectDue,status=projectStatus,ownerMemberId=owner,notes=projectNotes))};projectTitle="";projectNotes=""}){Text("Add project")}
        vm.state.projects.forEach { p -> PlannerCard { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(p.title,fontWeight=FontWeight.Bold);StatusChip(p.status,if(p.status=="At Risk")Severity.WARNING else Severity.INFO)};Text(if(p.dueDate.isBlank())"No due date" else "Due ${Dates.display(p.dueDate)}",color=TextMuted);Text("Owner: ${vm.state.team.firstOrNull{it.id==p.ownerMemberId}?.name?:"Self/unassigned"}",color=TextMuted);if(p.notes.isNotBlank())Text(p.notes,modifier=Modifier.padding(top=4.dp)) } }
    }
}

@Composable
private fun ForceManagementTool(vm: PlannerViewModel, modifier: Modifier) {
    var roleTitle by remember{mutableStateOf("")};var primary by remember{mutableStateOf("")};var backup by remember{mutableStateOf("")}
    var groupName by remember{mutableStateOf("")};var groupMember by remember{mutableStateOf("")}
    val people=listOf("" to "None")+vm.state.team.map{it.id to "${it.grade.label} ${it.name}"}
    ToolBody(modifier,"Force Management","Manning, availability, reusable team groups, critical roles, backups and succession depth"){
        PlannerCard { Text("Work-center picture",fontWeight=FontWeight.Bold);Text(EnlistedBrain.teamReadiness(vm.state),style=MaterialTheme.typography.titleMedium);vm.state.team.groupingBy{it.availability.label}.eachCount().forEach{(k,v)->Text("$k: $v",color=TextMuted)} }
        SectionTitle("Reusable team groups")
        LabeledField("Group name",groupName,{groupName=it},"Alpha Shift");val gsel=people.firstOrNull{it.first==groupMember}?:people.first();DropdownField("Initial member",gsel,people,{it.second},{groupMember=it.first});Button(enabled=groupName.isNotBlank(),onClick={vm.update{it.copy(teamGroups=it.teamGroups+TeamGroup(name=groupName.trim(),memberIds=listOfNotNull(groupMember.takeIf{it.isNotBlank()})))};groupName=""}){Text("Create group")}
        vm.state.teamGroups.forEach { g -> PlannerCard { Text(g.name,fontWeight=FontWeight.Bold);val names=g.memberIds.mapNotNull{id->vm.state.team.firstOrNull{it.id==id}?.name};Text("${g.memberIds.size} member(s) • ${names.joinToString().ifBlank{"No members assigned"}}",color=TextMuted) } }
        SectionTitle("Critical role / succession depth")
        LabeledField("Critical role",roleTitle,{roleTitle=it});val psel=people.firstOrNull{it.first==primary}?:people.first();DropdownField("Primary",psel,people,{it.second},{primary=it.first});val bsel=people.firstOrNull{it.first==backup}?:people.first();DropdownField("Backup",bsel,people,{it.second},{backup=it.first});Button(enabled=roleTitle.isNotBlank(),onClick={vm.addCriticalRole(CriticalRole(title=roleTitle.trim(),primaryMemberId=primary,backupMemberIds=listOfNotNull(backup.takeIf{it.isNotBlank()})));roleTitle=""}){Text("Add critical role")}
        vm.state.criticalRoles.forEach { r -> val backups=r.backupMemberIds.mapNotNull{id->vm.state.team.firstOrNull{it.id==id}?.name};PlannerCard{Text(r.title,fontWeight=FontWeight.Bold);Text("Primary: ${vm.state.team.firstOrNull{it.id==r.primaryMemberId}?.name?:"None"}",color=TextMuted);Text("Backups: ${backups.ifEmpty{listOf("None")}.joinToString()}",color=TextMuted);if(backups.size<r.minimumBackups)StatusChip("SINGLE-POINT GAP",Severity.WARNING)} }
    }
}

@Composable
private fun CommitmentsTool(vm: PlannerViewModel, modifier: Modifier) {
    var memberId by remember{mutableStateOf("")};var text by remember{mutableStateOf("")};var due by remember{mutableStateOf("")}
    val people=vm.state.team.map{it.id to "${it.grade.label} ${it.name}"}
    ToolBody(modifier,"Leadership Commitments","Track what you promised your Airmen — not private counseling details"){
        if(people.isEmpty()) EmptyState("Add an Airman first","Commitments are linked to a supervised member.") else {
            val sel=people.firstOrNull{it.first==memberId}?:people.first().also{memberId=it.first}
            DropdownField("Airman",sel,people,{it.second},{memberId=it.first});LabeledField("Commitment",text,{text=it},"Review CCAF plan Friday");LabeledField("Due date",due,{due=it});Button(enabled=text.isNotBlank(),onClick={vm.addCommitment(LeadershipCommitment(memberId=memberId,text=text.trim(),dueDate=due));text=""}){Text("Add commitment")}
        }
        vm.state.commitments.filterNot{it.completed}.forEach { c -> PlannerCard { Row(verticalAlignment=Alignment.CenterVertically){Checkbox(c.completed,{vm.toggleCommitment(c.id)});Column{Text(c.text,fontWeight=FontWeight.Bold);Text("${vm.state.team.firstOrNull{it.id==c.memberId}?.name?:"Airman"}${c.dueDate.takeIf{it.isNotBlank()}?.let{" • ${Dates.display(it)}"}?:""}",color=TextMuted)}} } }
    }
}

@Composable
private fun RecognitionReviewTool(vm: PlannerViewModel, modifier: Modifier, onOpen:(String)->Unit) {
    val today=LocalDate.now();val quarter=((today.monthValue-1)/3)+1;val start=LocalDate.of(today.year,(quarter-1)*3+1,1)
    ToolBody(modifier,"Recognition Review","Surface Airmen whose recorded impact deserves a deliberate review"){
        if(vm.state.team.isEmpty()) EmptyState("No Airmen","Add Airmen and link accomplishments to them when appropriate.")
        vm.state.team.forEach { m ->
            val evidence=vm.state.accomplishments.filter{it.linkedMemberId==m.id && Dates.parse(it.date)?.let{d->!d.isBefore(start)}==true}
            val recentRecognition=vm.state.recognition.any{it.memberId==m.id && Dates.parse(it.date)?.let{d->java.time.temporal.ChronoUnit.DAYS.between(d,today)<=120}==true}
            PlannerCard(onClick={onOpen("member:${m.id}")}){Text("${m.grade.label} ${m.name}",fontWeight=FontWeight.Bold);Text("${evidence.size} linked accomplishment(s) this quarter",color=TextMuted);when{evidence.size>=3&&!recentRecognition->StatusChip("REVIEW FOR RECOGNITION",Severity.WARNING);recentRecognition->StatusChip("RECENT RECOGNITION RECORDED",Severity.GOOD);else->StatusChip("CONTINUE CAPTURING",Severity.INFO)}}
        }
        PlannerCard { Text("Recognition principle",fontWeight=FontWeight.Bold);Text("The app never decides that someone deserves an award. It surfaces evidence and time gaps so a supervisor remembers to review the Airman's contributions.",color=TextMuted) }
    }
}

@Composable
private fun CalendarTool(vm: PlannerViewModel, modifier: Modifier) {
    val context=LocalContext.current;var title by remember{mutableStateOf("")};var date by remember{mutableStateOf(LocalDate.now().toString())};var time by remember{mutableStateOf("")};var category by remember{mutableStateOf("Personal")}
    ToolBody(modifier,"Calendar","Internal events plus enlisted-relevant planning dates from the embedded 2026 dataset"){
        LabeledField("Event",title,{title=it});LabeledField("Date",date,{date=it});LabeledField("Time",time,{time=it},"0900");DropdownField("Category",category,listOf("Personal","Duty","Career","Readiness","Family"),{it},{category=it});Button(enabled=title.isNotBlank(),onClick={vm.addEvent(PlannerEvent(title=title.trim(),date=date,time=time,category=category));title=""}){Text("Add event")}
        SectionTitle("Your upcoming events")
        vm.state.events.filter{Dates.parse(it.date)?.isBefore(LocalDate.now())==false}.sortedBy{it.date}.take(12).forEach{e->PlannerCard{Text(e.title,fontWeight=FontWeight.Bold);Text("${Dates.display(e.date)} ${e.time}",color=TextMuted);TextButton(onClick=addToDeviceCalendar(context,e)){Text("Add to device calendar")}}}
        SectionTitle("Official / planning calendar","Verify current guidance before acting")
        vm.upcomingOfficialEvents(180).take(16).forEach{e->PlannerCard{Text(e.title,fontWeight=FontWeight.Bold);Text(Dates.display(e.date),color=SoftBlue);if(e.description.isNotBlank())Text(e.description,color=TextMuted,style=MaterialTheme.typography.bodySmall)}}
    }
}

private fun addToDeviceCalendar(context:android.content.Context,event:PlannerEvent):()->Unit = {
    val d=Dates.parse(event.date)
    if(d!=null){
        val start=d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent=Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).putExtra(CalendarContract.Events.TITLE,event.title).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,start).putExtra(CalendarContract.Events.DESCRIPTION,event.description)
        runCatching{context.startActivity(intent)}
    }
}

@Composable
private fun TimeBlocksTool(vm: PlannerViewModel, modifier: Modifier) {
    var date by remember{mutableStateOf(LocalDate.now().toString())};var start by remember{mutableStateOf("0800")};var end by remember{mutableStateOf("0900")};var title by remember{mutableStateOf("")}
    ToolBody(modifier,"Time Blocks","Protect time for work, development and life priorities"){
        LabeledField("Date",date,{date=it});LabeledField("Start",start,{start=it});LabeledField("End",end,{end=it});LabeledField("Block",title,{title=it});Button(enabled=title.isNotBlank(),onClick={vm.addTimeBlock(TimeBlock(date=date,startTime=start,endTime=end,title=title.trim()));title=""}){Text("Add block")}
        vm.state.timeBlocks.sortedWith(compareBy<TimeBlock>{it.date}.thenBy{it.startTime}).takeLast(30).forEach{b->PlannerCard{Text(b.title,fontWeight=FontWeight.Bold);Text("${Dates.display(b.date)} • ${b.startTime}–${b.endTime}",color=TextMuted)}}
    }
}

@Composable
private fun NotesTool(vm: PlannerViewModel, modifier: Modifier) {
    var title by remember{mutableStateOf("")};var body by remember{mutableStateOf("")}
    ToolBody(modifier,"Notes","Personal planning notes only — never use as a sensitive counseling or medical record"){
        LabeledField("Title",title,{title=it});LabeledField("Note",body,{body=it},singleLine=false);Button(enabled=title.isNotBlank(),onClick={vm.addNote(NoteItem(title=title.trim(),body=body.trim(),date=LocalDate.now().toString()));title="";body=""}){Text("Save note")}
        vm.state.notes.sortedByDescending{it.date}.forEach{n->PlannerCard{Text(n.title,fontWeight=FontWeight.Bold);Text(n.body,color=TextMuted);Text(Dates.display(n.date),color=SoftBlue,style=MaterialTheme.typography.labelSmall)}}
    }
}

@Composable
private fun BackupTool(vm: PlannerViewModel, modifier: Modifier) {
    val context=LocalContext.current
    var status by remember{mutableStateOf("")}
    val create=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)runCatching{context.contentResolver.openOutputStream(uri)?.use{it.write(vm.exportJson().toByteArray())}}.onSuccess{status="Backup saved."}.onFailure{status="Backup failed: ${it.message}"}}
    val open=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)runCatching{context.contentResolver.openInputStream(uri)?.bufferedReader()?.use{it.readText()}?:error("Could not read file")}.mapCatching{vm.importJson(it).getOrThrow()}.onSuccess{status="Backup imported."}.onFailure{status="Import failed: ${it.message}"}}
    ToolBody(modifier,"Backup & Restore","Explicit user-controlled JSON export; Android cloud backup is disabled"){
        Button(onClick={create.launch("Enlisted-Planner-backup-${LocalDate.now()}.json")},modifier=Modifier.fillMaxWidth()){Text("Export JSON backup")}
        OutlinedButton(onClick={open.launch(arrayOf("application/json","text/plain"))},modifier=Modifier.fillMaxWidth()){Text("Import JSON backup")}
        if(status.isNotBlank())PlannerCard{Text(status)}
        PlannerCard{Text("Protect the file",fontWeight=FontWeight.Bold);Text("A JSON export can contain names, career data, tasks and financial planning amounts. Store/share it deliberately. Do not put CUI or highly sensitive information in the app.",color=TextMuted)}
    }
}

@Composable
private fun GoalsTool(vm: PlannerViewModel, modifier: Modifier) {
    var title by remember{mutableStateOf("")};var area by remember{mutableStateOf(DevelopmentArea.PROFESSIONAL)};var horizon by remember{mutableStateOf("90 Days")};var next by remember{mutableStateOf("")};var date by remember{mutableStateOf("")}
    ToolBody(modifier,"Goals","Long-term direction → this year → next action"){
        LabeledField("Goal",title,{title=it});DropdownField("Area",area,DevelopmentArea.entries,{it.label},{area=it});DropdownField("Horizon",horizon,listOf("30 Days","90 Days","This Year","Long Term"),{it},{horizon=it});LabeledField("Next action",next,{next=it});LabeledField("Target date",date,{date=it});Button(enabled=title.isNotBlank(),onClick={vm.addGoal(Goal(title=title.trim(),area=area,horizon=horizon,nextAction=next,targetDate=date));title="";next=""}){Text("Add goal")}
        vm.state.goals.forEach{g->PlannerCard{Row(verticalAlignment=Alignment.CenterVertically){Checkbox(g.completed,{vm.toggleGoal(g.id)});Column{Text(g.title,fontWeight=FontWeight.Bold);Text("${g.area.label} • ${g.horizon}",color=TextMuted);if(g.nextAction.isNotBlank())Text("Next: ${g.nextAction}")}}}}
    }
}

@Composable
private fun SwotTool(vm: PlannerViewModel, modifier: Modifier) {
    var strengths by remember{mutableStateOf("")};var weaknesses by remember{mutableStateOf("")};var opportunities by remember{mutableStateOf("")};var threats by remember{mutableStateOf("")};var nextAction by remember{mutableStateOf("")}
    ToolBody(modifier,"SWOT Journal","A dated development exercise, not a permanent label"){
        LabeledField("Strengths",strengths,{strengths=it},singleLine=false);LabeledField("Weaknesses / growth areas",weaknesses,{weaknesses=it},singleLine=false);LabeledField("Opportunities",opportunities,{opportunities=it},singleLine=false);LabeledField("Threats / barriers",threats,{threats=it},singleLine=false);LabeledField("Next action",nextAction,{nextAction=it})
        Button(onClick={vm.update{it.copy(swotEntries=it.swotEntries+SwotEntry(date=LocalDate.now().toString(),strengths=strengths,weaknesses=weaknesses,opportunities=opportunities,threats=threats,nextAction=nextAction))};strengths="";weaknesses="";opportunities="";threats="";nextAction=""}){Text("Save SWOT")}
        SectionTitle("Past SWOT entries")
        vm.state.swotEntries.sortedByDescending{it.date}.forEach{e->PlannerCard{Text(Dates.display(e.date),fontWeight=FontWeight.Bold);if(e.strengths.isNotBlank())Text("Strengths: ${e.strengths}",color=TextMuted);if(e.weaknesses.isNotBlank())Text("Growth: ${e.weaknesses}",color=TextMuted);if(e.opportunities.isNotBlank())Text("Opportunities: ${e.opportunities}",color=TextMuted);if(e.threats.isNotBlank())Text("Barriers: ${e.threats}",color=TextMuted);if(e.nextAction.isNotBlank())Text("Next: ${e.nextAction}",modifier=Modifier.padding(top=4.dp))}}
    }
}

@Composable
private fun AddAirmanTool(vm:PlannerViewModel,modifier:Modifier,onBack:()->Unit){
    var name by remember{mutableStateOf("")};var grade by remember{mutableStateOf(Grade.A1C)};var component by remember{mutableStateOf(Component.REGAF)};var dor by remember{mutableStateOf("")};var dis by remember{mutableStateOf("")};var afsc by remember{mutableStateOf("")};var duty by remember{mutableStateOf("")};var workCenter by remember{mutableStateOf("")};var supervision by remember{mutableStateOf(LocalDate.now().toString())}
    ToolBody(modifier,"Add Airman","Minimum data needed to generate promotion, feedback, training and readiness cues"){
        LabeledField("Name",name,{name=it});DropdownField("Grade",grade,Grade.entries,{it.label},{grade=it});DropdownField("Component",component,Component.entries,{it.label},{component=it});LabeledField("Date of Rank",dor,{dor=it});LabeledField("Date Entered Service",dis,{dis=it});LabeledField("AFSC",afsc,{afsc=it});LabeledField("Duty title",duty,{duty=it});LabeledField("Work center",workCenter,{workCenter=it});LabeledField("Supervision start",supervision,{supervision=it});Button(enabled=name.isNotBlank(),onClick={vm.addMember(TeamMember(name=name.trim(),grade=grade,component=component,dateOfRank=dor,dateEnteredService=dis,afsc=afsc,dutyTitle=duty,workCenter=workCenter,supervisionStartDate=supervision));onBack()},modifier=Modifier.fillMaxWidth()){Text("Add Airman")}
        PlannerCard{Text("Do not create an unofficial personnel dossier",fontWeight=FontWeight.Bold);Text("Keep this profile intentionally minimal: career dates, professional development, qualifications, tasks and commitments. Do not store detailed medical, disciplinary or private-family information.",color=TextMuted)}
    }
}

@Composable
private fun AddAccomplishmentTool(vm:PlannerViewModel,modifier:Modifier,onBack:()->Unit){
    var action by remember{mutableStateOf("")};var impact by remember{mutableStateOf("")};var mga by remember{mutableStateOf(Mga.EXECUTING_MISSION)};var alq by remember{mutableStateOf("")};var level by remember{mutableStateOf("Work Center")};var date by remember{mutableStateOf(LocalDate.now().toString())};var memberId by remember{mutableStateOf("")}
    val people=listOf("" to "Self")+vm.state.team.map{it.id to "${it.grade.label} ${it.name}"}
    ToolBody(modifier,"Add Accomplishment","Capture what happened, why it mattered, and where the impact landed"){
        val sel=people.firstOrNull{it.first==memberId}?:people.first();DropdownField("For",sel,people,{it.second},{memberId=it.first});LabeledField("What did you/they do?",action,{action=it});LabeledField("Impact / result",impact,{impact=it},singleLine=false);DropdownField("MGA",mga,Mga.entries,{it.label},{mga=it});LabeledField("ALQ (optional)",alq,{alq=it});DropdownField("Impact level",level,listOf("Individual","Work Center","Flight","Squadron","Group","Wing","MAJCOM","DAF/Joint"),{it},{level=it});LabeledField("Date",date,{date=it});Button(enabled=action.isNotBlank(),onClick={vm.addAccomplishment(Accomplishment(date=date,action=action.trim(),impact=impact.trim(),mga=mga,alq=alq,impactLevel=level,linkedMemberId=memberId));onBack()},modifier=Modifier.fillMaxWidth()){Text("Save accomplishment")}
    }
}

@Composable
private fun GenericTool(name:String,modifier:Modifier){ToolBody(modifier,name,"Capability placeholder") { EmptyState("Not yet wired","This destination exists in the V2 Full shell but does not yet have a dedicated workflow in this source package.") }}
