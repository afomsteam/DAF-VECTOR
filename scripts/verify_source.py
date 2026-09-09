#!/usr/bin/env python3
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/com/afomsteam/enlistedplanner/ui/MainScreens.kt').read_text()
tool = (root / 'app/src/main/java/com/afomsteam/enlistedplanner/ui/ToolScreens.kt').read_text()
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
gradle = (root / 'app/build.gradle.kts').read_text()

expected_tools = [
    'Promotion Analyzer','Evaluation & Feedback','Awards & Recognition','Quarter Summary',
    'Career Roadmap','Experience Inventory','WAPS Study Plan','Pro Hands / Professional References',
    'Fitness','Whole Airman / CAF','Upgrade Training','Qualifications','Life Planning','Helping Resources',
    'Financial Readiness','Programs & Projects','Force Management','Leadership Commitments','Recognition Review',
    'Tasks','Calendar','Time Blocks','Notes','Attention Center','Backup & Restore','Goals','SWOT Journal',
    'Add Airman','Add Accomplishment'
]
errors=[]
for name in expected_tools:
    if f'"{name}" ->' not in tool:
        errors.append(f'Missing ToolScreen route: {name}')

for public_name in [
    'Promotion Analyzer','Evaluation & Feedback','Awards & Recognition','Quarter Summary','Career Roadmap',
    'Experience Inventory','WAPS Study Plan','Pro Hands / Professional References','Fitness','Whole Airman / CAF',
    'Upgrade Training','Qualifications','Life Planning','Helping Resources','Financial Readiness','Programs & Projects',
    'Force Management','Leadership Commitments','Recognition Review','Tasks','Calendar','Time Blocks','Notes',
    'Attention Center','Airman Launchpad','Backup & Restore'
]:
    if public_name not in main:
        errors.append(f'Tool not exposed from main UX: {public_name}')

if 'android.permission.INTERNET' in manifest:
    errors.append('Unexpected INTERNET permission; local-first boundary was broken')
if 'android:allowBackup="false"' not in manifest:
    errors.append('Android cloud backup is not disabled')
if 'applicationId = "com.afomsteam.enlistedplanner"' not in gradle:
    errors.append('Application ID changed unexpectedly')

if errors:
    print('SOURCE VERIFICATION FAILED')
    for e in errors: print(' -', e)
    sys.exit(1)
print(f'SOURCE VERIFICATION OK — {len(expected_tools)} explicit tool routes checked; local-first manifest boundary intact.')
