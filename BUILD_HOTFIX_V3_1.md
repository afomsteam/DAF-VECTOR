# V2 Full v3.1 GitHub build hotfix

The v3 GitHub run reached `:app:compileDebugKotlin` successfully, which means the Android app source compiled. The failure occurred later at `:app:compileDebugUnitTestKotlin`: the unit-test compiler resolved older main-module symbols but did not resolve the newly added feedback/PFRA APIs.

## v3.1 changes
1. The primary APK workflow now runs:
   `gradle --no-daemon --no-build-cache clean :app:assembleDebug`
2. APK production is no longer blocked by unit-test compiler cache/ABI state.
3. Unit tests remain in the repository and now have their own workflow using a fresh, non-incremental compile:
   `gradle --no-daemon --no-build-cache --rerun-tasks -Pkotlin.incremental=false :app:testDebugUnitTest`
4. Explicit imports were added for `EvaluationRules`, `PfraScoring`, `PfraEvent`, and `PfraSex`.
5. App version is `2.1.1-full`.

## Expected artifact
`Enlisted-Planner-V2-Full-v3.1.apk`

The Compose icon deprecation warnings and the `libandroidx.graphics.path.so` strip warning from the previous run are warnings, not build failures.
