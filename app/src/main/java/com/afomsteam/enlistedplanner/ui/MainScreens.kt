package com.afomsteam.enlistedplanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afomsteam.enlistedplanner.data.*
import com.afomsteam.enlistedplanner.logic.Dates
import com.afomsteam.enlistedplanner.logic.EnlistedBrain
import com.afomsteam.enlistedplanner.logic.EvaluationRules
import com.afomsteam.enlistedplanner.logic.PromotionRules
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(vm: PlannerViewModel, modifier: Modifier = Modifier, onOpen: (String) -> Unit, onCapture: () -> Unit) {
    val state = vm.state
    val today = LocalDate.now()
    val todayStr = today.toString()
    val hour = LocalTime.now().hour
    val greeting = when (hour) { in 0..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
    val signals = EnlistedBrain.signals(state)
    val attention = signals.filter { it.severity == Severity.CRITICAL || it.severity == Severity.WARNING }.take(6)
    val top3 = state.tasks.filterNot { it.completed }.sortedWith(compareByDescending<Task> { it.priority }.thenBy { it.dueDate.ifBlank { "9999-12-31" } }).take(3)
    val agenda = buildList {
        addAll(state.timeBlocks.filter { it.date == todayStr }.map { "${it.startTime} • ${it.title}" })
        addAll(state.events.filter { it.date == todayStr }.map { "${it.time.ifBlank { "All day" }} • ${it.title}" })
    }.sorted()
    val nextOfficial = vm.upcomingOfficialEvents(30).firstOrNull()

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("$greeting${state.profile.name.takeIf { it.isNotBlank() }?.let { ", ${it.substringBefore(' ')}" } ?: ""}", "${today.dayOfWeek.name.lowercase().replaceFirstChar(Char::uppercase)}, ${Dates.display(todayStr)}")
        }

        item {
            SectionTitle("Next up")
            PlannerCard {
                if (agenda.isNotEmpty()) {
                    Text(agenda.first(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Your next scheduled item today", color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                } else if (nextOfficial != null) {
                    Text(nextOfficial.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Official/planning calendar • ${Dates.display(nextOfficial.date)}", color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                } else {
                    Text("No scheduled item", fontWeight = FontWeight.Bold)
                    Text("Use + Capture to add an event, task, accomplishment or note.", color = TextMuted)
                }
            }
        }

        item { SectionTitle("Today's 3", "Keep the duty day focused", "All tasks") { onOpen("tool:Tasks") } }
        if (top3.isEmpty()) item { EmptyState("Clear runway", "No open tasks yet. Capture the three things that matter most today.") }
        items(top3, key = { it.id }) { task ->
            PlannerCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = task.completed, onCheckedChange = { vm.toggleTask(task.id) })
                    Column(Modifier.weight(1f)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        if (task.dueDate.isNotBlank()) Text("Due ${Dates.display(task.dueDate)}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    if (task.priority == TaskPriority.HIGH) StatusChip("HIGH", Severity.WARNING)
                }
            }
        }

        item { SectionTitle("Needs attention", "Generated from your career, troops, readiness and programs", "See all") { onOpen("tool:Attention Center") } }
        if (attention.isEmpty()) item { PlannerCard { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Good); Spacer(Modifier.width(10.dp)); Text("No red or amber signals right now.") } } }
        items(attention, key = { it.id }) { SignalCard(it) }

        item { SectionTitle("Today", "Calendar + time blocks") }
        if (agenda.isEmpty()) item { EmptyState("Open schedule", "No internal events or time blocks recorded for today.") }
        items(agenda) { line -> PlannerCard { Text(line, fontWeight = FontWeight.Medium) } }

        if (hour >= 16) {
            item {
                SectionTitle("Wrap up today")
                val completedToday = state.tasks.count { it.completed }
                PlannerCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric("Tasks completed", "$completedToday")
                        Metric("Accomplishments", "${state.accomplishments.count { it.date == todayStr }}")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Did anything today demonstrate meaningful impact?", fontWeight = FontWeight.SemiBold)
                    Button(onClick = onCapture, modifier = Modifier.padding(top = 8.dp)) { Text("Capture it") }
                }
            }
        }

        item { Spacer(Modifier.height(70.dp)) }
    }
}

@Composable
fun CareerScreen(vm: PlannerViewModel, modifier: Modifier = Modifier, onOpen: (String) -> Unit) {
    val s = vm.state
    val today = LocalDate.now()
    val quarter = ((today.monthValue - 1) / 3) + 1
    val quarterStart = LocalDate.of(today.year, (quarter - 1) * 3 + 1, 1)
    val quarterAccomps = s.accomplishments.filter { Dates.parse(it.date)?.let { d -> !d.isBefore(quarterStart) && !d.isAfter(today) } == true }
    val selfMember = TeamMember(
        name = s.profile.name.ifBlank { "You" }, grade = s.profile.grade, component = s.profile.component,
        dateOfRank = s.profile.dateOfRank, dateEnteredService = s.profile.dateEnteredService, afsc = s.profile.afsc,
        skillLevel = s.profile.skillLevel, dutyTitle = s.profile.dutyTitle, workCenter = s.profile.workCenter,
        sixYearEnlistee = s.profile.sixYearEnlistee, bmtCompletionDate = s.profile.bmtCompletionDate,
        technicalTrainingCompletionDate = s.profile.technicalTrainingCompletionDate
    )
    val promo = PromotionRules.assess(selfMember)
    val scod = if (s.profile.component == Component.REGAF) EvaluationRules.nextScod(s.profile.grade) else null

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Career", "Where you've been, what you've done, and where you currently stand") }
        item {
            SectionTitle("Career snapshot")
            PlannerCard {
                Text("${s.profile.grade.label} • ${s.profile.afsc.ifBlank { "AFSC not set" }}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric("Q$quarter accomplishments", "${quarterAccomps.size}")
                    Metric("Next SCOD", scod?.let { "${it.monthValue}/${it.dayOfMonth}" } ?: "Verify ARC")
                    Metric("Experience", "${s.experience.size}")
                }
            }
        }
        item {
            PlannerCard(onClick = { onOpen("tool:Promotion Analyzer") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(promo.headline, fontWeight = FontWeight.Bold)
                        Text(promo.details.firstOrNull().orEmpty(), color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, null)
                }
            }
        }
        item { SectionTitle("Quarter summary", "Performance evidence by Major Graded Area", "Open") { onOpen("tool:Quarter Summary") } }
        item {
            PlannerCard(onClick = { onOpen("tool:Quarter Summary") }) {
                Mga.entries.forEach { mga ->
                    val count = quarterAccomps.count { it.mga == mga }
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(mga.label, color = SoftBlue)
                        Text("$count", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { SectionTitle("Career tools") }
        item {
            ToolGrid(
                listOf("Promotion Analyzer", "Evaluation & Feedback", "Awards & Recognition", "Experience Inventory"),
                onOpen
            )
        }
        item { SectionTitle("Recent accomplishments", "Quick Capture stays fast; use Detailed when you want MGA/ALQ/AIR depth", "Detailed") { onOpen("tool:Add Accomplishment") } }
        if (s.accomplishments.isEmpty()) item { EmptyState("Nothing captured yet", "Capture meaningful work when it happens so evaluation and award season do not start from a blank page.") }
        items(s.accomplishments.sortedByDescending { it.date }.take(8), key = { it.id }) { a ->
            PlannerCard {
                Text(a.action, fontWeight = FontWeight.SemiBold)
                if (a.impact.isNotBlank()) Text(a.impact, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusChip(a.mga.label)
                    Text(a.impactLevel, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item { Spacer(Modifier.height(70.dp)) }
    }
}

@Composable
fun DevelopScreen(vm: PlannerViewModel, modifier: Modifier = Modifier, onOpen: (String) -> Unit) {
    val s = vm.state
    val openGoals = s.goals.filterNot { it.completed }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Develop", "Where you're going — goals, qualifications, readiness and deliberate growth") }
        item { SectionTitle("Where are you going?", "Turn long-term direction into the next action", "Add goal") { onOpen("tool:Goals") } }
        if (openGoals.isEmpty()) item { EmptyState("No active development goals", "Create a 90-day goal and give it one concrete next action.") }
        items(openGoals.take(5), key = { it.id }) { goal ->
            PlannerCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = goal.completed, onCheckedChange = { vm.toggleGoal(goal.id) })
                    Column(Modifier.weight(1f)) {
                        Text(goal.title, fontWeight = FontWeight.Bold)
                        Text(goal.area.label + " • " + goal.horizon, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        if (goal.nextAction.isNotBlank()) Text("Next: ${goal.nextAction}", modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
        }
        item { SectionTitle("Development areas") }
        item { ToolGrid(listOf("Career Roadmap", "Goals", "Whole Airman / CAF", "Upgrade Training", "Qualifications", "SWOT Journal"), onOpen) }
        item {
            SectionTitle("Whole Airman")
            val last = s.wholeAirmanChecks.maxByOrNull { it.date }
            PlannerCard(onClick = { onOpen("tool:Whole Airman / CAF") }) {
                Text(if (last == null) "No check-in yet" else "Last check-in: ${Dates.display(last.date)}", fontWeight = FontWeight.Bold)
                Text(if (last?.oneAction.isNullOrBlank()) "Review mental, physical, social and spiritual fitness and choose one action." else "Current action: ${last?.oneAction}", color = TextMuted, modifier = Modifier.padding(top = 4.dp))
            }
        }
        item { SectionTitle("Experience & study") }
        item { ToolGrid(listOf("Experience Inventory", "WAPS Study Plan", "Life Planning", "Helping Resources"), onOpen) }
        item { Spacer(Modifier.height(70.dp)) }
    }
}

@Composable
fun LeadershipScreen(vm: PlannerViewModel, modifier: Modifier = Modifier, onOpen: (String) -> Unit) {
    val s = vm.state
    val signals = EnlistedBrain.signals(s)
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Leadership", "What do you owe your people? Keep development, readiness and commitments visible.") }
        item {
            PlannerCard {
                Text("Work-center picture", fontWeight = FontWeight.Bold)
                Text(EnlistedBrain.teamReadiness(s), color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                val overdue = signals.count { it.memberId.isNotBlank() && (it.severity == Severity.CRITICAL || it.severity == Severity.WARNING) }
                if (overdue > 0) StatusChip("$overdue troop action(s) need attention", Severity.WARNING)
            }
        }
        item { SectionTitle("Your people", "Promotion • feedback • training • qualifications", "Add Airman") { onOpen("tool:Add Airman") } }
        if (s.team.isEmpty()) item { EmptyState("No Airmen added", "Add only the minimum planning data you need to take care of your people. Avoid sensitive medical, disciplinary or CUI data.") }
        items(s.team, key = { it.id }) { member ->
            val promo = PromotionRules.assess(member)
            val feedback = EvaluationRules.nextFeedbackPlanningDate(member).first
            PlannerCard(onClick = { onOpen("member:${member.id}") }) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("${member.grade.label} ${member.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(listOf(member.dutyTitle, member.workCenter).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Duty details not set" }, color = TextMuted)
                    }
                    StatusChip(member.availability.label, if (member.availability == Availability.AVAILABLE) Severity.GOOD else Severity.INFO)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text("Promotion", style = MaterialTheme.typography.labelSmall, color = TextMuted); Text(promo.headline, style = MaterialTheme.typography.bodySmall, maxLines = 2) }
                    Column(Modifier.weight(1f)) { Text("Feedback", style = MaterialTheme.typography.labelSmall, color = TextMuted); Text(feedback?.let { Dates.display(it.toString()) } ?: "Need date", style = MaterialTheme.typography.bodySmall) }
                    Column(Modifier.weight(1f)) { Text("UGT", style = MaterialTheme.typography.labelSmall, color = TextMuted); Text("${member.upgradeProgress}%", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        item { SectionTitle("Leader tools") }
        item { ToolGrid(listOf("Programs & Projects", "Force Management", "Leadership Commitments", "Recognition Review", "Evaluation & Feedback", "Promotion Analyzer"), onOpen) }
        item { SectionTitle("Team needs attention") }
        val troopSignals = signals.filter { it.memberId.isNotBlank() }.take(6)
        if (troopSignals.isEmpty()) item { EmptyState("No troop signals", "No current promotion, feedback, training or qualification signal is red/amber/info based on recorded data.") }
        items(troopSignals, key = { it.id }) { SignalCard(it) }
        item { Spacer(Modifier.height(70.dp)) }
    }
}

@Composable
fun ToolsScreen(vm: PlannerViewModel, modifier: Modifier = Modifier, onOpen: (String) -> Unit) {
    val favorite = vm.state.settings.favoriteTools
    val categories = linkedMapOf(
        "Career" to listOf("Promotion Analyzer", "Evaluation & Feedback", "Awards & Recognition", "Quarter Summary", "Career Roadmap", "Experience Inventory", "WAPS Study Plan", "Pro Hands / Professional References"),
        "Readiness" to listOf("Fitness", "Whole Airman / CAF", "Upgrade Training", "Qualifications", "Life Planning", "Helping Resources"),
        "Money" to listOf("Financial Readiness"),
        "Leadership" to listOf("Programs & Projects", "Force Management", "Leadership Commitments", "Recognition Review"),
        "Planning" to listOf("Tasks", "Calendar", "Time Blocks", "Notes", "Attention Center", "Airman Launchpad"),
        "Data" to listOf("Backup & Restore")
    )
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Tools", "The full toolbox stays here. Navigation is simpler; capability is not.") }
        item { SectionTitle("Favorites") }
        item { ToolGrid(favorite, onOpen) }
        categories.forEach { (category, tools) ->
            item { SectionTitle(category) }
            item { ToolGrid(tools, onOpen) }
        }
        item { Spacer(Modifier.height(70.dp)) }
    }
}

@Composable
fun ToolGrid(tools: List<String>, onOpen: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tools.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tool ->
                    Card(
                        modifier = Modifier.weight(1f),
                        onClick = { onOpen(if (tool == "Airman Launchpad") "launchpad" else "tool:$tool") },
                        colors = CardDefaults.cardColors(containerColor = CardBlue.copy(alpha = .72f)),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(tool, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
