package com.afomsteam.enlistedplanner.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import java.time.LocalDate

class PlannerViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlannerRepository(app)
    var state by mutableStateOf(repo.load())
        private set

    var officialEvents by mutableStateOf<List<PlannerEvent>>(emptyList())
        private set

    init { officialEvents = loadOfficialEvents() }

    fun update(block: (PlannerState) -> PlannerState) {
        state = block(state)
        repo.save(state)
    }

    fun exportJson(): String = repo.exportJson(state)

    fun importJson(raw: String): Result<Unit> = runCatching {
        state = repo.importJson(raw)
        repo.save(state)
    }

    fun reset() {
        state = PlannerState(settings = state.settings.copy(disclosureAccepted = true))
        repo.save(state)
    }

    fun setDisclosureAccepted() = update { it.copy(settings = it.settings.copy(disclosureAccepted = true)) }

    fun addTask(task: Task) = update { it.copy(tasks = it.tasks + task) }
    fun toggleTask(id: String) = update { s -> s.copy(tasks = s.tasks.map { if (it.id == id) it.copy(completed = !it.completed) else it }) }
    fun deleteTask(id: String) = update { it.copy(tasks = it.tasks.filterNot { t -> t.id == id }) }

    fun addAccomplishment(item: Accomplishment) = update { it.copy(accomplishments = it.accomplishments + item) }
    fun addGoal(item: Goal) = update { it.copy(goals = it.goals + item) }
    fun toggleGoal(id: String) = update { s -> s.copy(goals = s.goals.map { if (it.id == id) it.copy(completed = !it.completed) else it }) }

    fun addMember(member: TeamMember) = update { it.copy(team = it.team + member) }
    fun updateMember(member: TeamMember) = update { s -> s.copy(team = s.team.map { if (it.id == member.id) member else it }) }
    fun deleteMember(id: String) = update { s -> s.copy(team = s.team.filterNot { it.id == id }, commitments = s.commitments.filterNot { it.memberId == id }) }

    fun addProgram(item: ProgramItem) = update { it.copy(programs = it.programs + item) }
    fun addCommitment(item: LeadershipCommitment) = update { it.copy(commitments = it.commitments + item) }
    fun toggleCommitment(id: String) = update { s -> s.copy(commitments = s.commitments.map { if (it.id == id) it.copy(completed = !it.completed) else it }) }
    fun addRecognition(item: RecognitionItem) = update { it.copy(recognition = it.recognition + item) }
    fun addCriticalRole(item: CriticalRole) = update { it.copy(criticalRoles = it.criticalRoles + item) }
    fun addWholeAirman(item: WholeAirmanCheck) = update { it.copy(wholeAirmanChecks = it.wholeAirmanChecks + item) }
    fun addLifePlan(item: LifePlan) = update { it.copy(lifePlans = it.lifePlans + item) }
    fun addNote(item: NoteItem) = update { it.copy(notes = it.notes + item) }
    fun addEvent(item: PlannerEvent) = update { it.copy(events = it.events + item) }
    fun addTimeBlock(item: TimeBlock) = update { it.copy(timeBlocks = it.timeBlocks + item) }
    fun addExperience(item: ExperienceItem) = update { it.copy(experience = it.experience + item) }

    fun toggleLaunchpad(id: String) = update { s -> s.copy(launchpad = s.launchpad.map { if (it.id == id) it.copy(completed = !it.completed) else it }) }

    private fun loadOfficialEvents(): List<PlannerEvent> = runCatching {
        val raw = getApplication<Application>().assets.open("data/official-events-2026.json").bufferedReader().use { it.readText() }
        val a = JSONArray(raw)
        buildList {
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                val date = o.optString("date")
                if (date.isBlank() || date == "null") continue
                val title = o.optString("title")
                val description = o.optString("description")
                val officerOnly = title.startsWith("Capt ") || title.startsWith("Maj ") || title.startsWith("Lt Col ") || title.startsWith("Colonel ") || title.startsWith("2Lt") || title.startsWith("1Lt") || description.contains("Officer planning item", ignoreCase = true)
                if (officerOnly) continue
                add(
                    PlannerEvent(
                        id = o.optString("id", "official-$i"),
                        title = title,
                        date = date,
                        category = o.optString("category", "Official"),
                        description = listOf(description, o.optString("verification")).filter { it.isNotBlank() }.joinToString("\n")
                    )
                )
            }
        }.sortedBy { it.date }
    }.getOrElse { emptyList() }

    fun upcomingOfficialEvents(days: Long = 120): List<PlannerEvent> {
        val today = LocalDate.now()
        val end = today.plusDays(days)
        return officialEvents.filter { event ->
            runCatching { LocalDate.parse(event.date) }.getOrNull()?.let { !it.isBefore(today) && !it.isAfter(end) } == true
        }
    }
}
