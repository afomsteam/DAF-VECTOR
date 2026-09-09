# Enlisted Planner V2 Full

**V2 Full = the cleaner V2 user experience with the enlisted brain restored underneath it.**

This repository is a native Android / Kotlin / Jetpack Compose app. It is local-first and intentionally has **no INTERNET permission**.

## Primary architecture

- **Home** — Daily Command Center: Today's 3, agenda, auto-generated Needs Attention signals, duty-day wrap-up.
- **Career** — accomplishments, MGA/ALQ evidence, promotion analysis, evaluation/SCOD awareness, awards/recognition, experience inventory, quarter summary and PDF export.
- **Develop** — goals, career roadmap, WAPS study plan, upgrade training, qualifications, Whole Airman/CAF, SWOT and life planning.
- **Lead** — troop records, promotion analysis, feedback timing, training, qualifications, commitments, programs/projects, recognition review, manning/availability, team groups and critical-role/succession depth.
- **Tools** — the complete toolbox organized into Career, Readiness, Money, Leadership, Planning and Data rather than deleting functions.
- **Airman Launchpad** — first 30 / 31–60 / 61–90 day transition guide.

## Enlisted-brain signals

`logic/EnlistedBrain.kt` converts locally entered planning data into actions/signals for:

- junior-enlisted promotion and SrA/BTZ timing estimates
- WAPS/board promotion review cues
- six-year-enlistee accelerated A1C planning
- RegAF SCOD awareness; ARC members are flagged for status-specific verification
- initial, junior-Airman recurring, midterm and end-of-reporting-period feedback planning
- upgrade-training target dates
- qualification expirations
- troop promotion selection / projected promotion information
- leadership commitments
- program and project suspenses
- program backup/continuity gaps
- critical-role single-point gaps
- PFRA planning
- Whole Airman check-in cadence
- overdue / due-soon work

The result is surfaced on **Home → Needs Attention** and in **Lead**. Optional daily local notifications use Android WorkManager; no cloud service is required.

## Policy safeguard

Promotion/evaluation calculations are **planning aids, not official determinations**. Current DAF publications, myFSS, FSS/CSS/MPF, unit guidance, commanders and official records remain controlling.

The app does not blindly apply RegAF WAPS or SCOD assumptions to AFR/ANG members. ARC records remain usable, but status/component-specific rules are explicitly marked for verification.

## Fitness safeguard

The Fitness tool calculates WHtR from the entered height and waist and can total the component points the user enters. Users should take cardio/strength/core/body-composition points from the current official AFPC scoring chart rather than treating embedded values as authoritative.

## Privacy

Do not enter classified information, CUI, SSNs, DoD ID numbers, passwords, detailed medical/mental-health information, disciplinary narratives, or operationally sensitive information.

Android cloud backup is disabled. App state is stored locally in SharedPreferences using Gson. The explicit JSON Backup & Restore tool is user-controlled and can contain names and planning information, so protect exported files accordingly.

**Contact: John Garcia**

## Build the APK in GitHub

Use:

`.github/workflows/build-apk.yml`

The workflow uses Node-24-capable GitHub Actions, Java 17, Gradle 8.9, `android-actions/setup-android@v4` for Android SDK 35, runs the source parity/privacy check, builds the debug APK and creates:

`Enlisted-Planner-V2-Full.apk`

GitHub uploads an artifact named:

`Enlisted-Planner-V2-Full`

The artifact also contains a SHA-256 checksum.

See **UPLOAD_TO_GITHUB.md** for exact steps.

## Optional release engineering build

`.github/workflows/build-release-apk.yml` builds a minified **unsigned** release APK. It is not a substitute for a persistent release signing key. Use the debug workflow for immediate install/testing; configure proper signing before normal distribution or in-place production upgrades.

## Validation performed before packaging

- `scripts/verify_source.py` checks the required capability routes and local-first privacy boundary.
- All bundled XML, JSON and GitHub workflow YAML files were parsed successfully.
- `Models.kt` + `EnlistedBrain.kt` compiled successfully with the installed Kotlin compiler.
- A sample supervisor-state signal test produced feedback, upgrade-training, qualification, commitment, program and single-point capability alerts.
- Full Android assembly cannot be completed in this environment because the Android SDK/Gradle dependency set is not installed here; the included GitHub Actions workflow is the intended full Android compile check.
