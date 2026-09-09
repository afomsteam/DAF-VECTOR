# Source and validation notes

## Baselines used

- User-provided `Enlisted-Planner(4).apk` was treated as the capability baseline.
- User-provided `Enlisted-Planner-V2.apk` was treated as the cleaner UX/navigation direction.
- Embedded V2 calendar/reference JSON and V2 visual assets are retained in this repository.

## Policy-sensitive logic

Promotion/evaluation/fitness outputs are deliberately conservative planning aids. The user should verify controlling requirements against current official publications, myFSS and appropriate personnel channels.

### Promotion

The RegAF promotion rules layer includes junior-enlisted timing estimates, SrA/BTZ planning, WAPS/board cues, selected-for-promotion tracking, six-year-enlistee accelerated A1C planning, and the TSgt skill-level distinction between competing/testing and promotion. AFR/ANG records are not falsely treated as RegAF WAPS cases.

### Evaluations and feedback

For RegAF planning, the app uses grade-based SCOD awareness. For AFR/ANG members, the UI deliberately says to verify the member's status-specific ARC cycle because AGR/Stat Tour/Non-AGR rules can differ and the app does not currently store every status discriminator.

Feedback planning uses recorded supervision and feedback dates to create cues for the initial 60-day period, the junior-Airman 180-day cadence when applicable, projected midterm, and end-of-reporting-period window. CROs, commander/supervisor actions and official exceptions still require verification.

### Fitness

The tool computes the user's WHtR ratio and totals the component points the user enters. It does not hard-code every age/sex/event scoring table; users should use the current official AFPC scoring chart for authoritative component points.

## Local validation performed

- Required feature routes/privacy boundary: `python3 scripts/verify_source.py` → PASS.
- Core Kotlin compile: `Models.kt` + `EnlistedBrain.kt` → PASS (warnings only).
- XML resources → parsed successfully.
- Embedded JSON data → parsed successfully.
- GitHub Actions YAML → parsed successfully.
- Full Kotlin tree was parser-screened without Android/Compose classpaths; no obvious parser/unclosed-token errors were found. Android/Compose unresolved references are expected in that limited check.
- Sample supervisor-state signal execution produced the expected categories of cross-module alerts.

A complete Android build still requires Android SDK/Gradle/Maven dependencies; the included GitHub Action performs the actual Android assembly.
