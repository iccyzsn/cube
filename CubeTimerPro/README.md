# CubeTimer Pro (Android, Kotlin + Jetpack Compose)

Time. Train. Compete. Connect. — Phase 1–3 foundation of the master spec.

## Open / build
1. Open this folder in **Android Studio (Koala or newer, JDK 17)**. It will fetch Gradle 8.7 from `gradle-wrapper.properties` and sync.
   (CLI users: run `gradle wrapper` once, then `./gradlew assembleDebug`.)
2. Run tests: `./gradlew testDebugUnitTest` (pure-JVM tests of timer, stats, scrambles, cube engine).
3. Debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.

> **Not yet compiled or run.** This project was generated in a sandbox with no Android SDK or network, so
> the code and tests have not been executed. Expect to fix a few compile errors on first sync, and run the tests before trusting anything.

## What exists
| Area | Status |
|---|---|
| Timer engine (monotonic clock, hold-to-ready, 15 s inspection, +2/DNF from inspection overrun, duplicate-stop guard) | Implemented, unit-tested (`core/TimerEngine.kt`) |
| Penalties (raw time never overwritten) and manual +2/DNF/delete on last solve | Implemented |
| Trimmed Ao5/Ao12/Ao100 with DNF handling, session mean | Implemented, unit-tested (`core/Stats.kt`) |
| Scrambles: 2×2–7×7, 3×3 OH, Pyraminx, Skewb, Megaminx | Random-move practice scrambles, **not** official random-state |
| Room storage, DataStore settings (theme, inspection, decimals, hold time), CSV export | Implemented |
| Cube-state engine (cubie model, U R F D L B turns, inverse, facelets) | Implemented, unit-tested |
| Algorithm viewer (2-D net, step/play) | 7 starter algorithms; **not** the 57 OLL / 21 PLL sets |
| Stats screen + line chart; white/orange light + dark theme; 5-tab navigation | Implemented |
| 3-D rotatable renderer, wide/slice moves (M, r, x…), tutorials, trainer | Not started |
| BLD, FMC, Clock, Square-1 events | Not exposed (no support yet) |
| Auth, profiles, sync, leaderboards, recording/AI/community verification, moderation, social, races, challenges, notifications, achievements | **Not implemented.** Tabs show honest "needs backend" states; no fake data |

## Known limitations
- A solve in progress is lost if the process is killed (no mid-solve persistence yet).
- Onboarding, haptics/sound, accent colours and import are not done.
- Algorithms are checked structurally in tests (order, F2L/orientation preserved); verify against a trusted database before shipping.

## Next phases (from the spec)
4 Firebase Auth + Firestore (needs your `google-services.json`) · 5 server-validated leaderboards (Cloud Functions + security rules) ·
6 camera recording, private storage, AI-assisted checks, voting, moderator tools · 7 social, races, achievements.
All verification/leaderboard authority must live server-side; never trust client-set "verified" flags.
