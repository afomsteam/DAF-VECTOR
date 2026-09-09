# Upload V2 Full to GitHub — exact steps

## Recommended: replace the Android project files together

V2 Full changes both the app shell and the underlying data/rules structure. Do not mix random individual files with an older source tree.

1. Download and extract `Enlisted-Planner-V2-Full-GitHub-Ready-v3.zip`.
2. Keep a tag/copy of your current working repository first.
3. Replace the repository project files with the contents of the extracted V2 Full folder.
4. Confirm these exist at the repository root:
   - `settings.gradle.kts`
   - `build.gradle.kts`
   - `gradle.properties`
   - `app/`
   - `scripts/verify_source.py`
   - `.github/workflows/build-apk.yml`
5. Commit and push to `main` or `master`.
6. Open **Actions → Build Enlisted Planner V2 Full APK**.
7. Select **Run workflow** if the push did not already trigger it.
8. When green, download the artifact **Enlisted-Planner-V2-Full**.
9. Extract it and install `Enlisted-Planner-V2-Full-v3.apk` on your Android test device.

## Installing over an older APK

The application ID remains `com.afomsteam.enlistedplanner`, but Android also requires the **same signing key** for an in-place update. GitHub debug APKs may not share the signing key used by an older APK.

If Android reports a signature/package conflict, export any data you need from the old build, uninstall that test build, then install V2 Full. For long-term normal upgrades, use one persistent release signing key for every release.

## No GitHub Pages dependency

The Android APK workflow does not use `actions/configure-pages`, Pages enablement, or a Pages deployment. A Pages configuration error has no role in this APK build.
