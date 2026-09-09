# GitHub Build — Enlisted Planner V2 Full

## Fastest path to an installable APK

1. Extract `Enlisted-Planner-V2-Full-GitHub-Ready.zip`.
2. Copy **everything inside the extracted folder**, including the `.github` folder, to the root of your GitHub repository.
3. Commit and push to `main` (or `master`).
4. Open **GitHub → Actions**.
5. Select **Build Enlisted Planner V2 Full APK**.
6. If it did not start automatically, choose **Run workflow**.
7. When the job is green, download the artifact named **Enlisted-Planner-V2-Full**.
8. Extract the artifact. It contains:
   - `Enlisted-Planner-V2-Full.apk`
   - `Enlisted-Planner-V2-Full.apk.sha256`

## Build chain

The workflow performs:

`checkout → Java 17 → Gradle 8.9 → Android SDK 35 → source verification → test task → assembleDebug → checksum → artifact upload`

It does **not** use GitHub Pages or `actions/configure-pages`.

## Unsigned release build

Use **Build Enlisted Planner V2 Full Release APK (Unsigned)** only when you want the minified release output for release engineering. Android will not accept an unsigned APK as a normal production install. Configure a persistent release signing key before distribution.

## If GitHub fails

Copy the first meaningful Gradle/Kotlin error and the surrounding lines back into ChatGPT. The first compile error is usually more useful than the final `BUILD FAILED` line.

## Android SDK setup (September 2026 fix)

The workflows use `android-actions/setup-android@v4` before any Android SDK command is needed. The action installs Android command-line tools, accepts licenses, adds `sdkmanager` to `PATH`, and installs `platform-tools`, `platforms;android-35`, and `build-tools;35.0.0`.

If an older workflow fails with `sdkmanager: command not found` or `yes: standard output: Broken pipe`, replace `.github/workflows/build-apk.yml` (and `build-release-apk.yml` if used) with the current files in this package. Do not add a manual `yes | sdkmanager --licenses` step ahead of Android SDK setup.
