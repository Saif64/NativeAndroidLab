# NativeAndroidLab

`NativeAndroidLab` is a native Android reading-and-practice project for an engineer
with production Flutter experience. It uses one cumulative app so that every new
Android concept has an existing code path to extend, test, and explain.

> **Current runnable reference: Day 5 — State and Recomposition.** The current
> reference code intentionally stops there. Days 6–30 describe future exercises;
> ViewModel, Navigation, Room, networking, Hilt, WorkManager, offline sync, Paging,
> and the later production systems are intentionally absent until their chapters.
> **Begin your study at Day 1.** “Day 5” describes the code's current maturity, not
> the lesson you should start from.

## Start here

1. Verify the current reference from the project root in the provided container:

   ```bash
   docker build -t native-android-lab .
   docker run --rm native-android-lab
   ```

   The container's default command runs:

   ```bash
   ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
   ```

   `docker build` copies a snapshot of the project into the image. After any source or
   test edit, rebuild the image before running it again; otherwise `docker run` checks
   the previous snapshot. When the host toolchain is configured, the direct Gradle
   command gives the fastest edit/check loop.

2. Choose a learning mode below.
3. Keep [NATIVE_ANDROID_COURSE.md](NATIVE_ANDROID_COURSE.md) open as the curriculum
   map.
4. Record predictions, failures, and explanations in
   [LEARNING_LOG.md](LEARNING_LOG.md).

If Docker is unavailable but the Android toolchain is configured on the host, run the
same Gradle command directly. The container proves compilation, local tests, and lint;
it does not provide an emulator. To install the debug app on a running emulator or
connected device:

```bash
./gradlew :app:installDebug --no-daemon
```

Device tests require an emulator or device:

```bash
./gradlew :app:connectedDebugAndroidTest --no-daemon
```

## Choose how you learn

### Read-first

Use this when the goal is to improve code-reading and native reasoning.

1. Run the verification gate before reading implementation details.
2. Follow [CODE_TOUR.md](CODE_TOUR.md) using Android Studio's **Go to
   Declaration**, **Find Usages**, and call hierarchy.
3. Before opening a function body, predict its inputs, owner, output, and lifetime.
4. Read the nearest test and explain which behavior it protects.
5. Make one small variation, run the narrowest useful check, and record the result.

### Build-first

Use this when the goal is recall through implementation.

1. Read only the day's goal, contract, experiment, and exit gate.
2. Preserve a working checkpoint before editing.
3. Implement one thin slice without reading the completed function first.
4. Run the day's verification command and diagnose the first useful failure.
5. Use each chapter's exit gate; compare with the current reference only after your
   own Day 5 attempt, then explain the differences.

Read-first learners can use the current reference to revisit Days 1–5. Build-first
learners should use a separate worktree or clone based on starter commit `7d91c8f` and
implement Days 1–5 in order; intermediate completed checkpoints do not exist yet. From
Day 6 onward, the chapter is the build contract and you create the implementation;
there is deliberately no hidden advanced solution in the project.

## Current code-reading path

```text
AndroidManifest.xml
    → MainActivity.onCreate
    → setContent
    → NativeAndroidLabTheme
    → DeveloperCard
    → DayHeader / ExecutionTrace / LearnerSummary
    → PracticeExperiment / ReadingPrompt
```

`PracticeExperiment` owns `practiceSessions` with `rememberSaveable` and
`mutableIntStateOf`, because it is the smallest composable that needs the value. Day 7
will move that state upward and introduce callbacks. Follow the complete path in
[CODE_TOUR.md](CODE_TOUR.md).

## Flutter bridge

| Familiar Flutter idea                      | Native Android reference today                                | Important difference                                                                     |
|--------------------------------------------|---------------------------------------------------------------|------------------------------------------------------------------------------------------|
| `main()` and `runApp()`                    | manifest launch plus `MainActivity.onCreate` and `setContent` | Android creates registered components; the app does not begin at your own `main()`.      |
| `MaterialApp` theme ancestor               | `NativeAndroidLabTheme`                                       | The Activity/window exists outside the Compose tree.                                     |
| widget-building function                   | `@Composable` function                                        | Compose tracks state reads and may re-execute only affected scopes.                      |
| local `State` field plus `setState`        | `rememberSaveable { mutableIntStateOf(...) }`                 | Assigning observable state schedules recomposition; there is no general `setState` call. |
| widget constructor arguments and callbacks | composable parameters and event lambdas                       | State ownership follows lifetime and Android restoration rules.                          |
| `pubspec.yaml` dependencies                | `gradle/libs.versions.toml` plus `app/build.gradle.kts`       | Gradle also owns variants, compilation, packaging, and signing.                          |

These are bridges, not claims of exact equivalence. When behavior surprises you,
inspect Android ownership and lifecycle before translating it back into Flutter.

## Controlled-failure protocol

Failure experiments are part of the course, but each one must be bounded:

1. Start from a passing verification gate and preserve the working state.
2. Introduce exactly one deliberate fault described by the chapter.
3. Run the narrowest relevant build or test and record the first actionable message.
4. Explain the root cause before changing anything else.
5. Undo only the deliberate fault; do not reset unrelated work.
6. Rerun the same command and record the restored pass.

Never leave a deliberate compiler error, lifecycle leak, unsafe permission, exposed
component, broad keep rule, or fake secret in the working reference.

## Documentation map

- [Course home and all 30 days](NATIVE_ANDROID_COURSE.md)
- [Day 1 — Know the Android Project](DAY_01_CHAPTER_01.md)
- [Day 2 — Follow App Startup](DAY_02_CHAPTER_02.md)
- [Day 3 — Kotlin Survival Kit](DAY_03_CHAPTER_03.md)
- [Day 4 — Build a Compose Layout](DAY_04_CHAPTER_04.md)
- [Day 5 — State and Recomposition](DAY_05_CHAPTER_05.md)
- [Code tour for the current reference](CODE_TOUR.md)
- [Kotlin and JVM depth appendix](APPENDIX_A_KOTLIN_JVM_DEPTH.md)
