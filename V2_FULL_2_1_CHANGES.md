# V2 Full 2.1 — Feedback / Calculator Restoration

This update responds to the V2 Full field-test feedback and is intentionally a **depth restoration**, not a navigation redesign.

## 1. Calendar date pickers

All editable date values in the principal workflows now use the shared Android calendar picker built into the common date field instead of requiring ISO date typing. This includes profile/member career dates, tasks, accomplishments, awards, feedback records, promotion dates, PFRA, qualifications, upgrade training, goals, programs/projects, commitments, calendar events, time blocks, notes, Whole Airman check-ins, SWOT, life planning, experience and WAPS target dates.

Stored format remains `YYYY-MM-DD` to preserve sorting/backups and compatibility; the UI displays a readable date.

## 2. Feedback calculator restored

RegAF recurring feedback planning is now explicitly calculated as:

**last grade SCOD + 6 calendar months**

The Evaluation & Feedback tool includes a standalone grade/SCOD calculator. Each RegAF troop record shows its recurring midterm calculation. Initial-supervision planning remains a separate cue rather than replacing the SCOD + 6 month calculation. AFR/ANG records continue to require status-specific verification rather than silently applying RegAF rules.

## 3. 2026 PFRA calculator rebuilt

The standard USAF calculator is now driven by the user-provided **Final USAF Physical Fitness Readiness Assessment Scoring (Effective 1 Mar 26)** tables:

- age bands from Under 25 through 60+
- male/female columns
- push-ups
- hand-release push-ups
- sit-ups
- cross-leg reverse crunch
- forearm plank
- 2-mile run
- 20m HAMR
- 2 km walk maximum-time standard
- waist-to-height ratio (WHtR), including the 20-point body-composition scale

The app computes points from entered raw performance. AFSPECWAR/EOD remains explicitly separated because the uploaded source contains a different chart for that population.

## 4. Financial Readiness expanded

Financial Readiness now includes:

- monthly spending-plan margin
- fixed-obligation and savings-rate visibility
- emergency-fund months and gap-to-3/6-month targets
- credit utilization with a 30% starting-point cue
- 2026 TSP elective-deferral planning
- age-based TSP catch-up limits
- BRS automatic/service-match estimate and 5% member-contribution cue

These are planning estimates, not financial/tax/investment advice.

## 5. Helping Hands questionnaire restored

The page-79 matrix is now an interactive questionnaire. The user selects an area and concern; the app returns the agencies marked for that concern in the planner. The concern itself is not persisted. Optional local installation phone/e-mail/building information can be stored for agencies, matching the blank contact table on the source page.

## 6. Professional Resources restored

The page-85 reference map is searchable by category and keyword. High-value official destinations can be opened in the device browser while the app itself remains local-first and requires no INTERNET permission.

## 7. Detailed performance capture restored

Quick Capture remains intentionally fast. Career → **Detailed** opens the deeper performance workflow based on pages 68–69:

- select Airman/self
- calendar date
- MGA
- ALQ filtered to that MGA
- ALQ definition
- MGA/MILE focus selection
- organizational impact level
- Action
- Impact
- Result
- evidence/metric
- challenge/assistance needed

Quarter Summary, search and the PDF export now retain/display the richer evidence.

## 8. SWOT rebuilt

SWOT now follows the page-71 structure:

- Strengths — helpful/internal
- Weaknesses — harmful/internal
- Opportunities — helpful/external
- Threats — harmful/external

Each quadrant includes the source-page prompts. Users can scope the SWOT to Self, Work Center, Team, Program or Project and create one next action with a target date.

## Validation

- Core Kotlin models/rules/fitness/guidance compile: PASS
- Smoke tests for SCOD + 6 months and known PFRA table values: PASS
- Capability/privacy source verification: PASS
- Full Android/Compose assembly: use the included GitHub Actions workflow

## v3.1 build hotfix — 9 Sep 2026
- APK workflow now runs a clean `assembleDebug` build and is no longer blocked by unit-test compiler cache/ABI state.
- Added a separate unit-test workflow that forces `clean`, disables the Gradle build cache, reruns tasks, and disables Kotlin incremental compilation.
- Added explicit imports for the new feedback/PFRA test symbols.
- App version bumped to `2.1.1-full`.
- APK artifact name: `Enlisted-Planner-V2-Full-v3.1.apk`.
