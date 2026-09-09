package com.afomsteam.enlistedplanner.logic

import com.afomsteam.enlistedplanner.data.*
import java.time.LocalDate
import java.time.MonthDay
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.min

object Dates {
    fun parse(value: String): LocalDate? = try {
        if (value.isBlank()) null else LocalDate.parse(value)
    } catch (_: Exception) { null }

    fun daysUntil(value: String, today: LocalDate = LocalDate.now()): Long? =
        parse(value)?.let { ChronoUnit.DAYS.between(today, it) }

    fun nextOccurrence(month: Int, day: Int, today: LocalDate): LocalDate {
        var d = LocalDate.of(today.year, month, day)
        if (d.isBefore(today)) d = d.plusYears(1)
        return d
    }

    fun display(value: String): String = parse(value)?.let {
        "${it.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${it.dayOfMonth}, ${it.year}"
    } ?: "Not set"
}

data class PromotionAssessment(
    val headline: String,
    val status: Severity,
    val details: List<String>,
    val nextDate: String = "",
    val verification: String = "Planning aid only — verify member-specific eligibility in myFSS/FSS and current DAF guidance."
)

object PromotionRules {
    fun assess(member: TeamMember, today: LocalDate = LocalDate.now()): PromotionAssessment {
        if (member.selectedForPromotion) {
            val selected = member.selectedGrade ?: member.grade.next()
            return PromotionAssessment(
                headline = "Selected for ${selected?.label ?: "promotion"}",
                status = Severity.GOOD,
                details = listOfNotNull(
                    member.lineNumber.takeIf { it.isNotBlank() }?.let { "Line number: $it" },
                    member.projectedPromotionDate.takeIf { it.isNotBlank() }?.let { "Projected promotion: ${Dates.display(it)}" },
                    "Continue to verify all promotion requirements remain satisfied before the effective date."
                ),
                nextDate = member.projectedPromotionDate
            )
        }

        if (member.component != Component.REGAF) {
            return PromotionAssessment(
                headline = "Verify ${member.component.label} promotion path",
                status = Severity.INFO,
                details = listOf(
                    "ARC promotion systems do not use the same RegAF WAPS rules.",
                    "Use the member record to track dates, readiness and development, then verify component-specific eligibility through the proper personnel channel."
                )
            )
        }

        return when (member.grade) {
            Grade.AB -> if (member.sixYearEnlistee) sixYearA1cAssessment(member, today) else juniorPromotion(member, Grade.AMN, monthsTig = 6, today = today)
            Grade.AMN -> {
                if (member.sixYearEnlistee) sixYearA1cAssessment(member, today)
                else juniorPromotion(member, Grade.A1C, monthsTig = 10, today = today)
            }
            Grade.A1C -> sraAssessment(member, today)
            Grade.SRA -> wapsAssessment(member, Grade.SSGT, requiredSkillToCompete = 5, requiredSkillToPromote = 5, minTigMonths = 6, minTisYears = 3, today = today)
            Grade.SSGT -> wapsAssessment(member, Grade.TSGT, requiredSkillToCompete = 5, requiredSkillToPromote = 7, minTigMonths = 23, minTisYears = 5, today = today)
            Grade.TSGT -> wapsAssessment(member, Grade.MSGT, requiredSkillToCompete = 7, requiredSkillToPromote = 7, minTigMonths = 24, minTisYears = 8, today = today)
            Grade.MSGT -> boardAssessment(member, Grade.SMSGT, requiredSkill = 7, minTigMonths = 20, minTisYears = 11, today = today, extraGate = "Also verify cumulative enlisted-service, degree, EPME, retainability and current board-cycle requirements.")
            Grade.SMSGT -> boardAssessment(member, Grade.CMSGT, requiredSkill = 9, minTigMonths = 21, minTisYears = 14, today = today, extraGate = "Also verify cumulative enlisted-service, degree, EPME, retainability and current board-cycle requirements.")
            Grade.CMSGT -> PromotionAssessment("Top enlisted grade", Severity.GOOD, listOf("No higher enlisted grade to calculate."))
        }
    }

    private fun juniorPromotion(member: TeamMember, next: Grade, monthsTig: Long, today: LocalDate): PromotionAssessment {
        val dor = Dates.parse(member.dateOfRank)
        if (dor == null) return missingDates(member, next)
        val projected = dor.plusMonths(monthsTig)
        val days = ChronoUnit.DAYS.between(today, projected)
        val severity = when {
            days < 0 -> Severity.WARNING
            days <= 60 -> Severity.INFO
            else -> Severity.GOOD
        }
        return PromotionAssessment(
            headline = "${next.label} planning date",
            status = severity,
            details = listOf(
                "Projected minimum TIG planning date: ${Dates.display(projected.toString())}.",
                "This does not replace commander/personnel eligibility verification."
            ),
            nextDate = projected.toString()
        )
    }

    private fun sixYearA1cAssessment(member: TeamMember, today: LocalDate): PromotionAssessment {
        val tech = Dates.parse(member.technicalTrainingCompletionDate)
        val bmt = Dates.parse(member.bmtCompletionDate)
        if (tech == null && bmt == null) {
            return PromotionAssessment(
                "Six-year enlistee A1C review",
                Severity.WARNING,
                listOf("Enter BMT completion and technical-training completion dates to estimate the accelerated A1C planning point.")
            )
        }
        val twentyWeeks = bmt?.plusWeeks(20)
        val candidates = listOfNotNull(tech, twentyWeeks)
        val projected = candidates.minOrNull()!!
        return PromotionAssessment(
            "Accelerated A1C planning point",
            if (projected.isBefore(today)) Severity.WARNING else Severity.INFO,
            listOf(
                "Estimated trigger: ${Dates.display(projected.toString())} (technical-training completion or 20 weeks from BMT completion, whichever occurs first).",
                "Verify six-year enlistee eligibility and effective DOR with MPF/FSS."
            ),
            projected.toString()
        )
    }

    private fun sraAssessment(member: TeamMember, today: LocalDate): PromotionAssessment {
        val dor = Dates.parse(member.dateOfRank)
        val dis = Dates.parse(member.dateEnteredService)
        if (dor == null || dis == null) return missingDates(member, Grade.SRA)

        val routeA = maxOf(dis.plusMonths(36), dor.plusMonths(20))
        val routeB = dor.plusMonths(28)
        val normal = minOf(routeA, routeB)
        val btz = normal.minusMonths(6)
        val daysToBtz = ChronoUnit.DAYS.between(today, btz)
        val details = mutableListOf(
            "Estimated normal SrA date: ${Dates.display(normal.toString())}.",
            "Estimated BTZ promotion point: ${Dates.display(btz.toString())} (up to six months early)."
        )
        if (daysToBtz in 0..180) details += "BTZ window is approaching — review local nomination suspense and records now."
        if (today.isAfter(btz) && today.isBefore(normal)) details += "Member is inside the estimated BTZ/early-promotion period; verify whether selected or considered."
        return PromotionAssessment(
            headline = if (today.isBefore(btz)) "SrA / BTZ planning" else "SrA promotion review",
            status = when {
                today.isAfter(normal) -> Severity.WARNING
                daysToBtz <= 90 -> Severity.WARNING
                daysToBtz <= 180 -> Severity.INFO
                else -> Severity.GOOD
            },
            details = details,
            nextDate = if (today.isBefore(btz)) btz.toString() else normal.toString()
        )
    }

    private fun wapsAssessment(
        member: TeamMember,
        next: Grade,
        requiredSkillToCompete: Int,
        requiredSkillToPromote: Int,
        minTigMonths: Long,
        minTisYears: Long,
        today: LocalDate
    ): PromotionAssessment {
        val detail = mutableListOf<String>()
        val dor = Dates.parse(member.dateOfRank)
        val dis = Dates.parse(member.dateEnteredService)
        val tigMonths = dor?.let { Period.between(it, today).toTotalMonths() }
        val tisMonths = dis?.let { Period.between(it, today).toTotalMonths() }
        val tigMet = tigMonths?.let { it >= minTigMonths }
        val tisMet = tisMonths?.let { it >= minTisYears * 12 }

        if (dor == null) detail += "Date of Rank missing — cannot compare current TIG to the ${minTigMonths}-month minimum planning gate."
        else detail += "TIG snapshot: ${tigMonths} month(s) — ${if (tigMet == true) "meets" else "below"} the ${minTigMonths}-month minimum gate used for ${next.label} planning."
        if (dis == null) detail += "Date Entered Service missing — cannot compare current service to the ${minTisYears}-year minimum planning gate."
        else detail += "Service snapshot: ${tisMonths} month(s) — ${if (tisMet == true) "meets" else "below"} the ${minTisYears}-year minimum gate used for ${next.label} planning."

        if (member.skillLevel >= requiredSkillToCompete) {
            detail += "Skill level ${member.skillLevel}: meets the planning threshold to compete used by this tool."
        } else {
            detail += "Skill level ${member.skillLevel}: below the planning threshold of $requiredSkillToCompete for this promotion path."
        }
        if (requiredSkillToPromote > requiredSkillToCompete) {
            detail += "For ${next.label}, the tool separately tracks the $requiredSkillToPromote-level requirement before promotion unless an authorized waiver applies."
        }
        detail += "Snapshot only: official TIG/TIS are evaluated at the cycle-specific dates/PECD, and commander recommendation, promotion-eligibility codes, EPME and other requirements still control."

        val missing = dor == null || dis == null
        val belowKnownGate = tigMet == false || tisMet == false || member.skillLevel < requiredSkillToCompete
        val severity = if (missing || belowKnownGate) Severity.WARNING else Severity.INFO
        val headline = if (!missing && !belowKnownGate) "${next.label}: minimum-gate snapshot met" else "${next.label} promotion analysis"
        return PromotionAssessment(headline, severity, detail)
    }

    private fun boardAssessment(
        member: TeamMember,
        next: Grade,
        requiredSkill: Int,
        minTigMonths: Long,
        minTisYears: Long,
        today: LocalDate,
        extraGate: String
    ): PromotionAssessment {
        val dor = Dates.parse(member.dateOfRank)
        val dis = Dates.parse(member.dateEnteredService)
        val tigMonths = dor?.let { Period.between(it, today).toTotalMonths() }
        val tisMonths = dis?.let { Period.between(it, today).toTotalMonths() }
        val detail = mutableListOf<String>()
        detail += if (tigMonths == null) "Date of Rank missing — TIG snapshot unavailable." else "TIG snapshot: $tigMonths month(s) vs $minTigMonths-month minimum planning gate."
        detail += if (tisMonths == null) "Date Entered Service missing — service snapshot unavailable." else "Service snapshot: $tisMonths month(s) vs ${minTisYears * 12}-month minimum planning gate."
        detail += "Skill level ${member.skillLevel} vs $requiredSkill-level minimum planning gate."
        detail += extraGate
        detail += "Review PECD, evaluation/decoration record, eligibility status and current board-cycle guidance."
        val tigBelow = tigMonths == null || tigMonths < minTigMonths
        val tisBelow = tisMonths == null || tisMonths < minTisYears * 12
        val below = tigBelow || tisBelow || member.skillLevel < requiredSkill
        return PromotionAssessment(
            if (!below) "${next.label}: minimum-gate snapshot met" else "${next.label} board-cycle review",
            if (below) Severity.WARNING else Severity.INFO,
            detail
        )
    }

    private fun missingDates(member: TeamMember, next: Grade) = PromotionAssessment(
        "${next.label} analysis needs data",
        Severity.WARNING,
        listOf("Enter ${member.name.ifBlank { "the member" }}'s Date of Rank and Date Entered Service so the app can calculate the next planning milestone.")
    )
}

object EvaluationRules {
    private val scodByGrade = mapOf(
        Grade.AB to MonthDay.of(3, 31), Grade.AMN to MonthDay.of(3, 31), Grade.A1C to MonthDay.of(3, 31), Grade.SRA to MonthDay.of(3, 31),
        Grade.SSGT to MonthDay.of(1, 31), Grade.TSGT to MonthDay.of(11, 30), Grade.MSGT to MonthDay.of(9, 30),
        Grade.SMSGT to MonthDay.of(7, 31), Grade.CMSGT to MonthDay.of(5, 31)
    )

    fun nextScod(grade: Grade, today: LocalDate = LocalDate.now()): LocalDate {
        val md = scodByGrade.getValue(grade)
        var date = md.atYear(today.year)
        if (date.isBefore(today)) date = md.atYear(today.year + 1)
        return date
    }

    fun nextFeedbackPlanningDate(member: TeamMember, today: LocalDate = LocalDate.now()): Pair<LocalDate?, String> {
        val supervision = Dates.parse(member.supervisionStartDate)
        val last = Dates.parse(member.lastFeedbackDate)
        if (supervision == null) {
            return if (last != null) last.plusDays(180) to "Enter supervision start date to calculate the formal cycle; 180-day review cue shown temporarily"
            else null to "Enter supervision start date to calculate initial and midterm feedback planning markers"
        }

        val initialDue = supervision.plusDays(60)
        if (last == null || last.isBefore(initialDue.minusDays(14))) return initialDue to "Initial feedback — within first 60 calendar days of supervision"

        val dis = Dates.parse(member.dateEnteredService)
        val under20Months = dis?.let { Period.between(it, today).toTotalMonths() < 20 } == true
        if (member.grade.order <= Grade.SRA.order && under20Months) {
            return last.plusDays(180) to "Junior Airman midterm planning — every 180 days after initial feedback until an evaluation/CRO, when applicable"
        }

        val projectedScod = nextScod(member.grade, supervision)
        val totalDays = ChronoUnit.DAYS.between(supervision, projectedScod)
        val midterm = if (totalDays < 150) projectedScod.minusDays(60) else supervision.plusDays(totalDays / 2)
        if (last.isBefore(midterm.minusDays(14))) return midterm to "Midterm feedback — midway between supervision start and projected evaluation closeout"

        val endPeriodDue = projectedScod.plusDays(60)
        return endPeriodDue to "End-of-reporting-period feedback — within 60 days after evaluation closeout"
    }
}

object EnlistedBrain {
    fun signals(state: PlannerState, today: LocalDate = LocalDate.now()): List<BrainSignal> {
        val out = mutableListOf<BrainSignal>()

        state.tasks.filterNot { it.completed }.forEach { task ->
            val days = Dates.daysUntil(task.dueDate, today)
            when {
                days != null && days < 0 -> out += BrainSignal("task-${task.id}", Severity.CRITICAL, task.title, "Task overdue by ${-days} day(s)", task.dueDate, "Task", task.linkedMemberId)
                days != null && days <= 7 -> out += BrainSignal("task-${task.id}", Severity.WARNING, task.title, "Task due in $days day(s)", task.dueDate, "Task", task.linkedMemberId)
                task.priority == TaskPriority.HIGH -> out += BrainSignal("task-${task.id}", Severity.INFO, task.title, "High-priority task", task.dueDate, "Task", task.linkedMemberId)
            }
        }

        if (state.profile.component == Component.REGAF) {
            val selfScod = EvaluationRules.nextScod(state.profile.grade, today)
            val selfScodDays = ChronoUnit.DAYS.between(today, selfScod)
            if (selfScodDays <= 90) out += BrainSignal("self-scod", if (selfScodDays <= 30) Severity.WARNING else Severity.INFO, "${state.profile.grade.label} SCOD approaching", "${selfScodDays} days until ${Dates.display(selfScod.toString())}. Review captured performance and official evaluation guidance.", selfScod.toString(), "Career")
        }

        val p = state.profile
        val selfMember = TeamMember(name=p.name.ifBlank{"You"}, grade=p.grade, component=p.component, dateOfRank=p.dateOfRank, dateEnteredService=p.dateEnteredService, afsc=p.afsc, skillLevel=p.skillLevel, sixYearEnlistee=p.sixYearEnlistee, bmtCompletionDate=p.bmtCompletionDate, technicalTrainingCompletionDate=p.technicalTrainingCompletionDate)
        val selfPromotion = PromotionRules.assess(selfMember, today)
        val selfPromoDays = Dates.daysUntil(selfPromotion.nextDate, today)
        if (selfPromotion.status == Severity.WARNING || (selfPromoDays != null && selfPromoDays in 0..180)) {
            out += BrainSignal("self-promotion", selfPromotion.status, selfPromotion.headline, selfPromotion.details.firstOrNull().orEmpty(), selfPromotion.nextDate, "Promotion")
        }

        val selfUpgradeDays = Dates.daysUntil(state.selfUpgradeTargetDate, today)
        if (state.selfUpgradeProgress < 100 && selfUpgradeDays != null && selfUpgradeDays <= 60) {
            out += BrainSignal("self-ugt", if (selfUpgradeDays < 0) Severity.CRITICAL else Severity.WARNING, "Upgrade training ${state.selfUpgradeProgress}%", if (selfUpgradeDays < 0) "Your target date passed" else "Target date in $selfUpgradeDays day(s)", state.selfUpgradeTargetDate, "Upgrade Training")
        }
        state.selfQualifications.forEach { q ->
            val days = Dates.daysUntil(q.expirationDate, today)
            if (days != null && days <= 60) out += BrainSignal("self-qual-${q.id}", if(days < 0) Severity.CRITICAL else Severity.WARNING, q.title, if(days < 0) "Qualification expired" else "Qualification expires in $days day(s)", q.expirationDate, "Qualification")
        }

        state.team.forEach { member ->
            val feedback = EvaluationRules.nextFeedbackPlanningDate(member, today)
            feedback.first?.let { due ->
                val days = ChronoUnit.DAYS.between(today, due)
                if (days <= 60) {
                    out += BrainSignal(
                        "feedback-${member.id}",
                        if (days < 0) Severity.CRITICAL else if (days <= 14) Severity.WARNING else Severity.INFO,
                        "${member.name}: feedback ${if (days < 0) "overdue" else "approaching"}",
                        "${feedback.second}: ${Dates.display(due.toString())}. Use this as a planning cue and verify current requirements.",
                        due.toString(), "Feedback", member.id
                    )
                }
            }

            if (member.component == Component.REGAF) {
                val scod = EvaluationRules.nextScod(member.grade, today)
                val scodDays = ChronoUnit.DAYS.between(today, scod)
                if (scodDays <= 90) out += BrainSignal("scod-${member.id}", if (scodDays <= 30) Severity.WARNING else Severity.INFO, "${member.name}: ${member.grade.label} SCOD", "$scodDays days until ${Dates.display(scod.toString())}.", scod.toString(), "Evaluation", member.id)
            }

            val promotion = PromotionRules.assess(member, today)
            if (promotion.status == Severity.WARNING || member.selectedForPromotion) {
                out += BrainSignal("promotion-${member.id}", promotion.status, "${member.name}: ${promotion.headline}", promotion.details.firstOrNull().orEmpty(), promotion.nextDate, "Promotion", member.id)
            }

            val upgradeDays = Dates.daysUntil(member.upgradeTargetDate, today)
            if (member.upgradeProgress < 100 && upgradeDays != null && upgradeDays <= 60) {
                out += BrainSignal("ugt-${member.id}", if (upgradeDays < 0) Severity.CRITICAL else Severity.WARNING, "${member.name}: upgrade training ${member.upgradeProgress}%", if (upgradeDays < 0) "Target date passed" else "Target date in $upgradeDays day(s)", member.upgradeTargetDate, "Upgrade Training", member.id)
            }

            member.qualifications.forEach { q ->
                val days = Dates.daysUntil(q.expirationDate, today)
                if (days != null && days <= 60) {
                    out += BrainSignal("qual-${member.id}-${q.id}", if (days < 0) Severity.CRITICAL else Severity.WARNING, "${member.name}: ${q.title}", if (days < 0) "Qualification expired" else "Expires in $days day(s)", q.expirationDate, "Qualification", member.id)
                }
            }
        }

        state.commitments.filterNot { it.completed }.forEach { c ->
            val days = Dates.daysUntil(c.dueDate, today)
            val member = state.team.firstOrNull { it.id == c.memberId }
            if (days != null && days <= 14) out += BrainSignal("commit-${c.id}", if (days < 0) Severity.CRITICAL else Severity.WARNING, "Commitment: ${member?.name ?: "Airman"}", c.text, c.dueDate, "Leadership", c.memberId)
        }

        state.programs.forEach { program ->
            val days = Dates.daysUntil(program.dueDate, today)
            if (days != null && days <= 30) out += BrainSignal("program-${program.id}", if (days < 0) Severity.CRITICAL else Severity.WARNING, "Program: ${program.title}", if (days < 0) "Suspense overdue" else "Suspense in $days day(s)", program.dueDate, "Program")
            if (program.backupMemberId.isBlank()) out += BrainSignal("program-backup-${program.id}", Severity.INFO, "${program.title}: no backup recorded", "Add continuity/backup coverage if this is a critical program.", category = "Force Management")
        }

        state.projects.forEach { project ->
            val days = Dates.daysUntil(project.dueDate, today)
            if (days != null && days <= 30 && !project.status.equals("Complete", true)) {
                out += BrainSignal("project-${project.id}", if (days < 0) Severity.CRITICAL else Severity.WARNING, "Project: ${project.title}", if (days < 0) "Milestone overdue" else "Milestone in $days day(s)", project.dueDate, "Program", project.ownerMemberId)
            }
        }

        state.criticalRoles.forEach { role ->
            val activeBackups = role.backupMemberIds.count { id -> state.team.firstOrNull { it.id == id }?.availability == Availability.AVAILABLE }
            if (activeBackups < role.minimumBackups) out += BrainSignal("role-${role.id}", Severity.WARNING, "Single-point capability: ${role.title}", "$activeBackups available backup(s); target ${role.minimumBackups}.", category = "Force Management")
        }

        val lastCheck = state.wholeAirmanChecks.maxByOrNull { it.date }
        val checkDays = lastCheck?.let { Dates.daysUntil(it.date, today)?.let { d -> -d } }
        if (lastCheck == null || (checkDays != null && checkDays >= 30)) out += BrainSignal("caf-check", Severity.INFO, "Whole Airman check-in", "Quickly review mental, physical, social and spiritual fitness and choose one action for this month.", category = "Development")

        Dates.daysUntil(state.fitness.nextPfraDate.ifBlank { state.profile.nextPfraDate }, today)?.let { days ->
            if (days <= 90) out += BrainSignal("pfra", if (days <= 30) Severity.WARNING else Severity.INFO, "PFRA approaching", if (days < 0) "PFRA date passed — update your record" else "$days day(s) until planned PFRA", state.fitness.nextPfraDate.ifBlank { state.profile.nextPfraDate }, "Fitness")
        }

        return out.sortedWith(compareBy<BrainSignal> { severityRank(it.severity) }.thenBy { Dates.parse(it.dueDate) ?: LocalDate.MAX })
    }

    private fun severityRank(s: Severity) = when (s) {
        Severity.CRITICAL -> 0
        Severity.WARNING -> 1
        Severity.INFO -> 2
        Severity.GOOD -> 3
    }

    fun teamReadiness(state: PlannerState): String {
        if (state.team.isEmpty()) return "No Airmen added"
        val assigned = state.team.size
        val available = state.team.count { it.availability == Availability.AVAILABLE }
        val gaps = state.criticalRoles.count { role -> role.backupMemberIds.count { id -> state.team.firstOrNull { it.id == id }?.availability == Availability.AVAILABLE } < role.minimumBackups }
        return "$assigned assigned • $available available • $gaps critical gap(s)"
    }
}
