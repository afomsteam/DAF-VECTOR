# V2 Full — Capability Preservation Matrix

The acceptance rule for V2 Full is: **simplify navigation, not capability**.

| Baseline capability | V2 Full location | Status |
|---|---|---|
| Daily dashboard / command center | Home | Restored + improved |
| Calendar date entry | Shared across editable date fields | Restored as native date-picker UI |
| Tasks / Today's 3 | Home / Capture | Restored |
| Calendar / personal events | Home + Tools → Calendar | Restored |
| Official enlisted-relevant planning calendar | Home + Tools → Calendar / Next Up | Restored; legacy officer clutter filtered |
| Time blocking / suggestions | Tools → Time Blocks | Restored + due-soon task suggestions |
| Notes | Capture + Tools → Notes | Restored |
| Reminder center / reminder scheduler | Home → Needs Attention + local notifications | Restored as auto-generated signals + optional WorkManager daily notification; separate inbox intentionally removed |
| Accomplishment capture | Capture + Career | Restored + improved |
| MGA / ALQ evidence | Career → Detailed Performance Capture | Restored + guided AIR/MGA/ALQ/MILE workflow |
| Quarter summary | Career | Merged; preserved |
| Career / quarter PDF export | Career | Restored |
| Promotion status | Career + Tools | Restored as analysis |
| SrA / BTZ planning | Promotion analysis | Restored |
| WAPS / PECD planning | Promotion analysis + signals | Restored as planning aid |
| Evaluation / SCOD | Career + Tools | Restored |
| Feedback milestones | Career + Lead + Home signals | Restored: recurring RegAF calculator = last SCOD + 6 months; initial cue separate |
| Awards / recognition | Career + Lead | Restored |
| Experience inventory | Career + Tools | Restored |
| WAPS study plan | Develop + Tools | Restored |
| Career roadmap | Develop + Tools | Restored |
| Development goals | Develop | Restored |
| Upgrade training | Develop + Lead + Tools | Restored |
| Qualifications | Develop + Lead + Tools | Restored |
| SWOT journal | Develop + Tools | Restored + page-71 prompts + scope/action/target |
| Comprehensive Airman Fitness | Develop + Tools → Whole Airman / CAF | Restored |
| Financial readiness | Tools | Expanded: spending plan, emergency fund, utilization, TSP/BRS |
| Fitness calculator / readiness | Tools → Fitness | Restored age/sex/raw-performance scoring from uploaded 2026 PFRA chart + WHtR + 2 km walk standard |
| Life readiness | Tools | Restored |
| Helping Hands | Tools → Helping Hands | Restored page-79 questionnaire/resource routing + optional local contacts |
| Professional resources | Tools → Professional Resources | Restored page-85 reference categories + official destinations |
| Airman Compass | Airman Launchpad | Retired/merged intentionally |
| Airman Launchpad | Tools + Home prompt | Restored + improved |
| Team management | Lead | Restored under one shell |
| Reusable team groups | Lead → Force Management | Restored |
| Leadership commitments | Lead + Home signals | Restored |
| Leader dashboard | Lead + Home signals | Restored + improved |
| Program manager | Lead + Tools | Restored |
| Project plans | Lead + Tools | Restored |
| Recognition manager | Lead | Restored/merged |
| Force management | Lead → Force Management | Restored |
| Critical qualification depth | Lead → Force Management + Home signals | Restored + improved |
| Succession / backup coverage | Lead → Force Management | Restored |
| Universal search | Global search | Restored / expanded |
| Backup / restore | Tools | Restored |
| First-launch disclosure | First launch | Restored |
| Contact | Disclosure | `Contact: John Garcia` |

## Explicit non-regression rule

A future redesign is not accepted merely because it compiles or looks cleaner. Every baseline job must be classified as:

1. **KEEP** — directly preserved,
2. **MERGE** — underlying functions remain accessible and testable, or
3. **RETIRE** — intentionally redundant and documented.

No useful capability should silently disappear.
