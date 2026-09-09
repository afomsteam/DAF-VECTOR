# Source and validation notes — V2 Full 2.1

## Baselines used

- User-provided original Enlisted Planner APK: capability baseline.
- V2 shell: cleaner UX/navigation direction.
- User-provided `2026_USAF_Digital_Planner_Updated(1).pdf`: source for the requested page-68/69 performance capture, page-71 SWOT, page-79 Helping Hands matrix and page-85 professional resources.
- User-provided `PFRA Scoring Charts(1).pdf`: source for the standard USAF age/sex/event fitness scoring tables and WHtR scale.

## Feedback / evaluation

For RegAF planning, the recurring feedback calculator uses the planner-owner requested rule:

**last grade SCOD + 6 calendar months**

The current grade-SCOD map remains:
- AB/Amn/A1C/SrA: 31 Mar
- SSgt: 31 Jan
- TSgt: 30 Nov
- MSgt: 30 Sep
- SMSgt: 31 Jul
- CMSgt: 31 May

An initial-supervision cue is kept separately. AFR/ANG records are deliberately marked for status-specific cycle verification.

## Fitness

`logic/FitnessScoring.kt` contains the standard USAF tables transcribed from the uploaded PFRA scoring chart effective 1 Mar 2026. Known chart points were smoke-tested, including WHtR, push-ups, 2-mile run, HAMR and 2 km walk standards. The app does not route AFSPECWAR/EOD through standard scoring because the source includes a separate table.

## Helping Hands / Professional Resources / Performance / SWOT

- `PlannerGuidance.helpingIssues` implements the page-79 concern-to-agency dot matrix. Concern selections are ephemeral; optional agency contact details may be stored.
- `PlannerGuidance.professionalReferences` uses the page-85 professional-reference categories. A short list of stable/high-value official destinations is provided as external-browser links.
- Detailed Performance Capture implements the page-68 AIR model (Action, Impact, Result), the 4 MGAs and 10 ALQs, plus page-69 MGA/MILE focus prompts and challenge/follow-through capture.
- SWOT uses the page-71 internal/external and helpful/harmful structure and prompt questions.

## Financial readiness web research

The calculator's current planning assumptions are documented in the source/UI and were checked against official/current public sources:
- Military OneSource: emergency funds generally target 3–6 months of expenses; credit utilization below 30% is a useful starting point.
- IRS 2026 elective-deferral limit: $24,500; standard 50+ catch-up $8,000; age 60–63 catch-up $11,250.
- DoD BRS guide: 1% automatic service contribution plus matching that reaches 4% at a 5% member contribution, for a maximum 5% service contribution when eligible.

## Local validation performed

- `python3 scripts/verify_source.py` → PASS.
- Core Kotlin compile (`Models.kt`, `EnlistedBrain.kt`, `FitnessScoring.kt`, `PlannerGuidance.kt`) → PASS.
- Core smoke execution for feedback/PFRA/guidance → PASS.
- GitHub Actions remains the full Android/Compose integration compile and APK assembly environment.

Policy-sensitive outputs remain planning aids. Current official DAF publications, myFSS/FSS/CSS/MPF, commanders and official records are controlling.
