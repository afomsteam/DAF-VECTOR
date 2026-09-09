package com.afomsteam.enlistedplanner.logic

import com.afomsteam.enlistedplanner.data.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class EnlistedBrainTest {
    @Test
    fun sixYearEnlisteeUsesEarlierOfTechTrainingOrTwentyWeeksAfterBmt() {
        val member = TeamMember(
            name = "Airman Test",
            grade = Grade.AMN,
            component = Component.REGAF,
            sixYearEnlistee = true,
            bmtCompletionDate = "2026-01-01",
            technicalTrainingCompletionDate = "2026-06-15"
        )
        val result = PromotionRules.assess(member, LocalDate.of(2026, 2, 1))
        assertEquals("2026-05-21", result.nextDate)
        assertTrue(result.headline.contains("Accelerated A1C"))
    }

    @Test
    fun sraBtzPlanningIsSixMonthsBeforeNormalPromotionPoint() {
        val member = TeamMember(
            name = "A1C Test",
            grade = Grade.A1C,
            component = Component.REGAF,
            dateOfRank = "2025-01-01",
            dateEnteredService = "2024-01-01",
            skillLevel = 3
        )
        val result = PromotionRules.assess(member, LocalDate.of(2026, 1, 1))
        // Normal route is 36 months TIS / 20 months TIG here -> 2027-01-01; BTZ point is 2026-07-01.
        assertEquals("2026-07-01", result.nextDate)
        assertTrue(result.details.any { it.contains("BTZ") })
    }

    @Test
    fun ssgtWithFiveLevelCanCompeteForTsgtButSevenLevelIsTrackedBeforePromotion() {
        val member = TeamMember(
            name = "SSgt Test",
            grade = Grade.SSGT,
            component = Component.REGAF,
            dateOfRank = "2024-01-01",
            dateEnteredService = "2018-01-01",
            skillLevel = 5
        )
        val result = PromotionRules.assess(member, LocalDate.of(2026, 9, 1))
        assertEquals(Severity.INFO, result.status)
        assertTrue(result.details.any { it.contains("meets the planning threshold to compete") })
        assertTrue(result.details.any { it.contains("7-level requirement before promotion") })
    }

    @Test
    fun initialFeedbackPlanningMarkerIsSixtyDaysAfterSupervisionStarts() {
        val member = TeamMember(
            name = "A1C Feedback",
            grade = Grade.A1C,
            component = Component.REGAF,
            supervisionStartDate = "2026-07-01",
            dateEnteredService = "2026-01-01"
        )
        val (date, reason) = EvaluationRules.nextFeedbackPlanningDate(member, LocalDate.of(2026, 7, 15))
        assertEquals(LocalDate.of(2026, 8, 30), date)
        assertTrue(reason.contains("Initial feedback"))
    }

    @Test
    fun overdueTroopFeedbackBecomesCriticalHomeSignal() {
        val member = TeamMember(
            id = "m1",
            name = "SrA Signal",
            grade = Grade.SRA,
            component = Component.REGAF,
            supervisionStartDate = "2026-01-01",
            dateEnteredService = "2022-01-01"
        )
        val signals = EnlistedBrain.signals(
            PlannerState(team = listOf(member)),
            LocalDate.of(2026, 10, 15)
        )
        val feedback = signals.firstOrNull { it.id == "feedback-m1" }
        assertNotNull(feedback)
        assertEquals(Severity.CRITICAL, feedback!!.severity)
    }

    @Test
    fun feedbackCalculatorUsesLastScodPlusSixMonths() {
        assertEquals(LocalDate.of(2026, 9, 30), EvaluationRules.feedbackDueFromScod(LocalDate.of(2026, 3, 31)))
        assertEquals(LocalDate.of(2026, 7, 31), EvaluationRules.feedbackDueFromScod(LocalDate.of(2026, 1, 31)))
    }

    @Test
    fun pfraChartKnownValuesMatchUploaded2026Chart() {
        assertEquals(20.0, PfraScoring.whtrPoints(0.49), 0.001)
        assertEquals(12.5, PfraScoring.whtrPoints(0.55), 0.001)
        assertEquals(0.0, PfraScoring.whtrPoints(0.60), 0.001)
        assertEquals(15.0, PfraScoring.score(PfraEvent.PUSH_UP, 24, PfraSex.MALE, 67), 0.001)
        assertEquals(50.0, PfraScoring.score(PfraEvent.TWO_MILE_RUN, 24, PfraSex.MALE, 13*60+25), 0.001)
        assertEquals(50.0, PfraScoring.score(PfraEvent.HAMR, 24, PfraSex.MALE, 87), 0.001)
        assertEquals(16*60+16, PfraScoring.walkMaxSeconds(24, PfraSex.MALE))
    }

    @Test
    fun forceManagementDetectsUnavailableBackups() {
        val primary = TeamMember(id = "p", name = "Primary", availability = Availability.AVAILABLE)
        val backup = TeamMember(id = "b", name = "Backup", availability = Availability.TDY)
        val role = CriticalRole(title = "Critical Capability", primaryMemberId = "p", backupMemberIds = listOf("b"), minimumBackups = 1)
        val state = PlannerState(team = listOf(primary, backup), criticalRoles = listOf(role))
        assertTrue(EnlistedBrain.teamReadiness(state).contains("1 critical gap"))
        assertTrue(EnlistedBrain.signals(state, LocalDate.of(2026, 9, 1)).any { it.id == "role-${role.id}" })
    }
}
