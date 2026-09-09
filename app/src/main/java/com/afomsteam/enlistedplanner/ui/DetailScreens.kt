package com.afomsteam.enlistedplanner.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afomsteam.enlistedplanner.R
import com.afomsteam.enlistedplanner.data.*
import com.afomsteam.enlistedplanner.logic.Dates
import com.afomsteam.enlistedplanner.logic.EvaluationRules
import com.afomsteam.enlistedplanner.logic.PromotionRules
import java.time.LocalDate

@Composable
fun DisclosureScreen(onAccept: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.enlisted_planner_background), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = .28f)
        Column(
            Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Text("ENLISTED PLANNER", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Text("V2 FULL • YOUR ENLISTED BRAIN", color = AirBlue, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            PlannerCard {
                Text("Personal planning aid", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("This app is not an official Department of the Air Force personnel system and does not replace myFSS, MPF/FSS, commanders, supervisors, current publications, or official records.", color = TextMuted, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(14.dp))
                Text("Do not enter:", fontWeight = FontWeight.Bold)
                Text("• SSNs or DoD ID numbers\n• CUI or classified information\n• detailed medical or mental-health information\n• disciplinary/counseling narratives\n• passwords or operationally sensitive information", color = TextMuted, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(14.dp))
                Text("Local-first privacy", fontWeight = FontWeight.Bold)
                Text("Planner data is stored locally on this device. Android cloud backup is disabled. You control explicit JSON exports.", color = TextMuted, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(14.dp))
                Text("Contact: John Garcia", color = SoftBlue)
            }
            Button(onClick = onAccept, modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) { Text("I understand — enter planner") }
        }
    }
}

@Composable
fun QuickCaptureDialog(vm: PlannerViewModel, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf("Task") }
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var mga by remember { mutableStateOf(Mga.EXECUTING_MISSION) }
    var impactLevel by remember { mutableStateOf("Work Center") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Capture") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownField("Capture", type, listOf("Task", "Accomplishment", "Event", "Note"), { it }, { type = it })
                LabeledField(if (type == "Accomplishment") "What did you do?" else "Title", title, { title = it })
                if (type != "Task") LabeledField(if (type == "Accomplishment") "Impact / result" else "Details", detail, { detail = it }, singleLine = false)
                LabeledField(if (type == "Task") "Due date (YYYY-MM-DD)" else "Date (YYYY-MM-DD)", date, { date = it })
                if (type == "Accomplishment") {
                    DropdownField("MGA", mga, Mga.entries, { it.label }, { mga = it })
                    DropdownField("Impact level", impactLevel, listOf("Individual", "Work Center", "Flight", "Squadron", "Group", "Wing", "MAJCOM", "DAF/Joint"), { it }, { impactLevel = it })
                }
            }
        },
        confirmButton = {
            Button(enabled = title.isNotBlank(), onClick = {
                when (type) {
                    "Task" -> vm.addTask(Task(title = title.trim(), dueDate = date))
                    "Accomplishment" -> vm.addAccomplishment(Accomplishment(date = date, action = title.trim(), impact = detail.trim(), mga = mga, impactLevel = impactLevel))
                    "Event" -> vm.addEvent(PlannerEvent(title = title.trim(), date = date, description = detail.trim()))
                    "Note" -> vm.addNote(NoteItem(title = title.trim(), body = detail.trim(), date = date))
                }
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DetailRouter(route: String, vm: PlannerViewModel, onBack: () -> Unit, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    when {
        route == "search" -> SearchScreen(vm, modifier, onOpen)
        route == "profile" -> ProfileScreen(vm, modifier)
        route == "launchpad" -> LaunchpadScreen(vm, modifier)
        route.startsWith("member:") -> MemberDetailScreen(vm, route.removePrefix("member:"), modifier, onBack)
        route.startsWith("tool:") -> ToolScreen(route.removePrefix("tool:"), vm, modifier, onOpen, onBack)
        else -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Unknown destination") }
    }
}

@Composable
private fun SearchScreen(vm: PlannerViewModel, modifier: Modifier, onOpen: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()
    val toolNames = listOf("Promotion Analyzer","Evaluation & Feedback","Awards & Recognition","Quarter Summary","Career Roadmap","Experience Inventory","WAPS Study Plan","Pro Hands / Professional References","Fitness","Whole Airman / CAF","Upgrade Training","Qualifications","Life Planning","Helping Resources","Financial Readiness","Programs & Projects","Force Management","Leadership Commitments","Recognition Review","Tasks","Calendar","Time Blocks","Notes","Attention Center","Airman Launchpad","Backup & Restore")

    val results = buildList<Pair<String,String>> {
        if (q.isNotBlank()) {
            vm.state.team.filter { (it.name + " " + it.afsc + " " + it.dutyTitle).lowercase().contains(q) }.forEach { add("${it.grade.label} ${it.name} — Airman" to "member:${it.id}") }
            toolNames.filter { it.lowercase().contains(q) }.forEach { add("$it — Tool" to if (it == "Airman Launchpad") "launchpad" else "tool:$it") }
            vm.state.tasks.filter { it.title.lowercase().contains(q) }.forEach { add("${it.title} — Task" to "tool:Tasks") }
            vm.state.accomplishments.filter { (it.action + " " + it.impact + " " + it.result + " " + it.evidence + " " + it.challenge + " " + it.alq + " " + it.mileFocus).lowercase().contains(q) }.forEach { add("${it.action} — Accomplishment" to "tool:Quarter Summary") }
            vm.state.notes.filter { (it.title + it.body).lowercase().contains(q) }.forEach { add("${it.title} — Note" to "tool:Notes") }
        }
    }.take(30)

    Column(modifier.fillMaxSize().padding(18.dp)) {
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("Search Airmen, tools, tasks, accomplishments...") }, singleLine = true)
        Spacer(Modifier.height(14.dp))
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (query.isBlank()) EmptyState("Universal search", "Try “EPB”, “fitness”, a troop name, “promotion”, “feedback”, or a task.")
            else if (results.isEmpty()) EmptyState("No matches", "Try a broader term.")
            results.forEach { (label, dest) -> PlannerCard(onClick = { onOpen(dest) }) { Text(label, fontWeight = FontWeight.SemiBold) } }
        }
    }
}

@Composable
private fun ProfileScreen(vm: PlannerViewModel, modifier: Modifier) {
    val p = vm.state.profile
    Column(modifier.fillMaxSize().padding(18.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ScreenHeader("Profile", "Rank + component + responsibilities + goals drive the enlisted brain")
        LabeledField("Name", p.name, { v -> vm.update { it.copy(profile = it.profile.copy(name = v)) } })
        DropdownField("Component", p.component, Component.entries, { it.label }, { v -> vm.update { it.copy(profile = it.profile.copy(component = v)) } })
        DropdownField("Grade", p.grade, Grade.entries, { "${it.label} (${it.payGrade})" }, { v -> vm.update { it.copy(profile = it.profile.copy(grade = v)) } })
        LabeledField("Date of Rank (YYYY-MM-DD)", p.dateOfRank, { v -> vm.update { it.copy(profile = it.profile.copy(dateOfRank = v)) } })
        LabeledField("Date Entered Service (YYYY-MM-DD)", p.dateEnteredService, { v -> vm.update { it.copy(profile = it.profile.copy(dateEnteredService = v)) } })
        LabeledField("AFSC", p.afsc, { v -> vm.update { it.copy(profile = it.profile.copy(afsc = v)) } })
        LabeledField("Skill level", p.skillLevel.toString(), { v -> v.toIntOrNull()?.let { n -> vm.update { it.copy(profile = it.profile.copy(skillLevel = n.coerceIn(1,9))) } } })
        LabeledField("Duty title", p.dutyTitle, { v -> vm.update { it.copy(profile = it.profile.copy(dutyTitle = v)) } })
        LabeledField("Work center", p.workCenter, { v -> vm.update { it.copy(profile = it.profile.copy(workCenter = v)) } })
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(p.supervisesAirmen, { v -> vm.update { it.copy(profile = it.profile.copy(supervisesAirmen = v)) } }); Text("I currently supervise Airmen") }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(p.managesProgram, { v -> vm.update { it.copy(profile = it.profile.copy(managesProgram = v)) } }); Text("I manage a program/project") }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(p.sixYearEnlistee, { v -> vm.update { it.copy(profile = it.profile.copy(sixYearEnlistee = v)) } }); Text("Initial six-year enlistee") }
        if (p.sixYearEnlistee) {
            LabeledField("BMT completion (YYYY-MM-DD)", p.bmtCompletionDate, { v -> vm.update { it.copy(profile = it.profile.copy(bmtCompletionDate = v)) } })
            LabeledField("Technical training completion (YYYY-MM-DD)", p.technicalTrainingCompletionDate, { v -> vm.update { it.copy(profile = it.profile.copy(technicalTrainingCompletionDate = v)) } })
        }
        LabeledField("Next PFRA (YYYY-MM-DD)", p.nextPfraDate, { v -> vm.update { it.copy(profile = it.profile.copy(nextPfraDate = v)) } })
        SectionTitle("Notifications")
        Row(verticalAlignment = Alignment.CenterVertically) { Switch(vm.state.settings.dailyNotifications, { v -> vm.update { it.copy(settings = it.settings.copy(dailyNotifications = v)) } }); Spacer(Modifier.width(10.dp)); Text("Daily local attention summary") }
        PlannerCard { Text("Contact: John Garcia", color = SoftBlue); Text("App package: ${com.afomsteam.enlistedplanner.BuildConfig.VERSION_NAME}", color = TextMuted, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun MemberDetailScreen(vm: PlannerViewModel, memberId: String, modifier: Modifier, onBack: () -> Unit) {
    val member = vm.state.team.firstOrNull { it.id == memberId }
    if (member == null) { Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Airman not found") }; return }
    val promo = PromotionRules.assess(member)
    val feedback = EvaluationRules.nextFeedbackPlanningDate(member)
    val scod = if (member.component == Component.REGAF) EvaluationRules.nextScod(member.grade) else null
    var showQualDialog by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize().padding(18.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("${member.grade.label} ${member.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(member.dutyTitle.ifBlank { "Duty title not set" }, color = TextMuted) }
            StatusChip(member.availability.label, if (member.availability == Availability.AVAILABLE) Severity.GOOD else Severity.INFO)
        }
        PlannerCard {
            Text("Enlisted brain", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Promotion: ${promo.headline}")
            Text("Feedback: ${feedback.first?.let { Dates.display(it.toString()) } ?: "Need supervision/feedback date"}", color = TextMuted)
            Text("Next SCOD: ${scod?.let { Dates.display(it.toString()) } ?: "Verify ARC status-specific cycle"}", color = TextMuted)
            Text("Upgrade training: ${member.upgradeProgress}%", color = TextMuted)
            Text("Qualifications: ${member.qualifications.size}", color = TextMuted)
        }
        SectionTitle("Core record", "Minimum planning data — not an official personnel record")
        LabeledField("Name", member.name, { v -> vm.updateMember(member.copy(name = v)) })
        DropdownField("Grade", member.grade, Grade.entries, { it.label }, { v -> vm.updateMember(member.copy(grade = v)) })
        DropdownField("Component", member.component, Component.entries, { it.label }, { v -> vm.updateMember(member.copy(component = v)) })
        LabeledField("Date of Rank", member.dateOfRank, { v -> vm.updateMember(member.copy(dateOfRank = v)) })
        LabeledField("Date Entered Service", member.dateEnteredService, { v -> vm.updateMember(member.copy(dateEnteredService = v)) })
        LabeledField("AFSC", member.afsc, { v -> vm.updateMember(member.copy(afsc = v)) })
        LabeledField("Skill level", member.skillLevel.toString(), { v -> v.toIntOrNull()?.let { vm.updateMember(member.copy(skillLevel = it.coerceIn(1,9))) } })
        LabeledField("Duty title", member.dutyTitle, { v -> vm.updateMember(member.copy(dutyTitle = v)) })
        LabeledField("Work center", member.workCenter, { v -> vm.updateMember(member.copy(workCenter = v)) })
        DropdownField("Availability", member.availability, Availability.entries, { it.label }, { v -> vm.updateMember(member.copy(availability = v)) })

        SectionTitle("Feedback & evaluation")
        LabeledField("Supervision start", member.supervisionStartDate, { v -> vm.updateMember(member.copy(supervisionStartDate = v)) })
        LabeledField("Last feedback", member.lastFeedbackDate, { v -> vm.updateMember(member.copy(lastFeedbackDate = v)) })
        Button(onClick = { vm.updateMember(member.copy(lastFeedbackDate = LocalDate.now().toString())) }) { Text("Record feedback today") }
        PlannerCard { Text("Planning marker: ${feedback.second}", fontWeight = FontWeight.SemiBold); Text(feedback.first?.let { Dates.display(it.toString()) } ?: "Enter a date above", color = TextMuted); Text("Verify current feedback/evaluation requirements in official guidance.", color = TextMuted, style = MaterialTheme.typography.labelSmall) }

        SectionTitle("Promotion")
        PlannerCard { Text(promo.headline, fontWeight = FontWeight.Bold); promo.details.forEach { Text("• $it", color = TextMuted, modifier = Modifier.padding(top = 4.dp)) }; Text(promo.verification, color = Warn, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp)) }
        Row(verticalAlignment = Alignment.CenterVertically) { Switch(member.selectedForPromotion, { v -> vm.updateMember(member.copy(selectedForPromotion = v, selectedGrade = if (v) member.grade.next() else null)) }); Spacer(Modifier.width(10.dp)); Text("Selected for promotion") }
        if (member.selectedForPromotion) {
            DropdownField("Selected grade", member.selectedGrade ?: member.grade.next() ?: member.grade, Grade.entries.filter { it.order >= member.grade.order }, { it.label }, { v -> vm.updateMember(member.copy(selectedGrade = v)) })
            LabeledField("Line number", member.lineNumber, { v -> vm.updateMember(member.copy(lineNumber = v)) })
            LabeledField("Projected promotion date", member.projectedPromotionDate, { v -> vm.updateMember(member.copy(projectedPromotionDate = v)) })
        }
        if (member.grade == Grade.AMN || member.grade == Grade.AB) {
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(member.sixYearEnlistee, { v -> vm.updateMember(member.copy(sixYearEnlistee = v)) }); Text("Initial six-year enlistee") }
            if (member.sixYearEnlistee) {
                LabeledField("BMT completion", member.bmtCompletionDate, { v -> vm.updateMember(member.copy(bmtCompletionDate = v)) })
                LabeledField("Technical training completion", member.technicalTrainingCompletionDate, { v -> vm.updateMember(member.copy(technicalTrainingCompletionDate = v)) })
            }
        }

        SectionTitle("Upgrade training")
        LabeledField("Progress %", member.upgradeProgress.toString(), { v -> v.toIntOrNull()?.let { vm.updateMember(member.copy(upgradeProgress = it.coerceIn(0,100))) } })
        LabeledField("Target date", member.upgradeTargetDate, { v -> vm.updateMember(member.copy(upgradeTargetDate = v)) })
        LinearProgressIndicator(progress = { member.upgradeProgress / 100f }, modifier = Modifier.fillMaxWidth())

        SectionTitle("Qualifications", action = "Add") { showQualDialog = true }
        if (member.qualifications.isEmpty()) EmptyState("No qualifications", "Add only professional qualification names and expiration dates needed for planning.")
        member.qualifications.forEach { q -> PlannerCard { Text(q.title, fontWeight = FontWeight.SemiBold); Text(if (q.expirationDate.isBlank()) "No expiration" else "Expires ${Dates.display(q.expirationDate)}", color = TextMuted) } }

        SectionTitle("Supervisor commitments")
        vm.state.commitments.filter { it.memberId == member.id }.forEach { c -> PlannerCard { Text(c.text); if (c.dueDate.isNotBlank()) Text("Due ${Dates.display(c.dueDate)}", color = TextMuted) } }

        OutlinedButton(onClick = { vm.deleteMember(member.id); onBack() }, modifier = Modifier.fillMaxWidth()) { Text("Remove Airman from planner", color = Critical) }
        Spacer(Modifier.height(30.dp))
    }

    if (showQualDialog) AddQualificationDialog(member, onDismiss = { showQualDialog = false }) { q -> vm.updateMember(member.copy(qualifications = member.qualifications + q)); showQualDialog = false }
}

@Composable
private fun AddQualificationDialog(member: TeamMember, onDismiss: () -> Unit, onAdd: (Qualification) -> Unit) {
    var title by remember { mutableStateOf("") }
    var expiration by remember { mutableStateOf("") }
    var critical by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add qualification") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledField("Qualification", title, { title = it })
            LabeledField("Expiration (YYYY-MM-DD)", expiration, { expiration = it })
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(critical, { critical = it }); Text("Critical work-center capability") }
        }
    }, confirmButton = { Button(enabled = title.isNotBlank(), onClick = { onAdd(Qualification(title = title.trim(), expirationDate = expiration, isCritical = critical)) }) { Text("Add") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun LaunchpadScreen(vm: PlannerViewModel, modifier: Modifier) {
    val items = vm.state.launchpad
    val done = items.count { it.completed }
    Column(modifier.fillMaxSize().padding(18.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ScreenHeader("Airman Launchpad", "A first-assignment transition guide that eventually gets out of your way")
        PlannerCard { Text("$done / ${items.size} complete", fontWeight = FontWeight.Bold); LinearProgressIndicator(progress = { if (items.isEmpty()) 0f else done.toFloat()/items.size }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) }
        items.groupBy { it.phase }.forEach { (phase, phaseItems) ->
            SectionTitle(phase)
            phaseItems.forEach { item ->
                PlannerCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(item.completed, { vm.toggleLaunchpad(item.id) })
                        Text(item.title, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (done == items.size && items.isNotEmpty()) PlannerCard { Text("Launchpad complete", fontWeight = FontWeight.Bold, color = Good); Text("You can keep this guide in Tools and let the daily/career system take over.", color = TextMuted) }
        Spacer(Modifier.height(30.dp))
    }
}
