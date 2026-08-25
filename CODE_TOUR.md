# Code Tour — Day 5 Reference

This tour teaches a repeatable way to read native Android code. The current reference
is intentionally small: one Activity hosts a Compose screen with local saveable state.
Future architecture is absent until its chapter creates a real need.

## Establish a passing baseline

From the project root, prefer the clean container gate:

```bash
docker build -t native-android-lab .
docker run --rm native-android-lab
```

Its default command is the following, which you can run directly when the Android
toolchain is configured on the host:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
```

Do not begin a controlled failure from a red build. Record the result in
[LEARNING_LOG.md](LEARNING_LOG.md).

## Compare the starter with Day 5

The generated `Hello Android!` starter is preserved in commit `7d91c8f`. Inspect it
without switching branches or disturbing your worktree:

```bash
git show 7d91c8f:app/src/main/java/com/saif/nativeandroidlab/MainActivity.kt
git diff 7d91c8f -- app/src/main/java/com/saif/nativeandroidlab/MainActivity.kt
```

Read the diff as a sequence of decisions: first locate the changed entry call, then
follow each new symbol. This is safer and more instructive than scanning the whole
file as one undifferentiated block.

## Trace 1 — system launch to visible UI

Read outside-in. Use symbol navigation instead of scanning the whole file.

1. In `app/src/main/AndroidManifest.xml`, find `.MainActivity` and its `MAIN` and
   `LAUNCHER` intent filter. This is why Android can create the Activity without an
   application `main()` function.
2. Open `MainActivity.onCreate`. The Android system owns this callback.
3. Follow `setContent`. This crosses from the Activity/window boundary into Compose.
4. Follow `NativeAndroidLabTheme`. It provides Material values to descendant
   composables.
5. Follow `DeveloperCard`. This is the Day 5 screen; continue into
   `PracticeExperiment` to find the current UI-state owner.

Before opening each body, answer:

- Who calls this symbol?
- What data enters it?
- What can outlive it?
- Does it describe UI, mutate state, or cross a platform boundary?

Flutter bridge: `setContent` occupies roughly the UI-hosting role of `runApp`, but the
manifest and Activity lifecycle have no exact Flutter-side equivalent.

## Trace 2 — state to UI

Inside `PracticeExperiment`, locate `practiceSessions`.

```text
rememberSaveable + mutableIntStateOf
    → current Int value
    → Text describes that value
```

Read the two responsibilities separately:

- `mutableIntStateOf` creates observable integer state.
- `rememberSaveable` retains a saveable value across recomposition and eligible
  Activity recreation. It is not durable storage and does not replace Room.

Then follow the smaller UI symbols in call order:

- `DayHeader`
- `ExecutionTrace`
- `LearnerSummary`
- `PracticeExperiment`
- `ReadingPrompt`

For each helper, inspect its parameters before its body. Identify which helpers only
render inputs and why `PracticeExperiment` is the smallest owner that needs mutable
state. Day 7 will deliberately move that value and its events upward.

Flutter bridge: this resembles local state inside one focused `StatefulWidget`.
Compose does not require a general `setState`; writing the observed value invalidates
code that read it.

## Trace 3 — event to recomposition

Follow one button event without jumping ahead:

```text
button tap
    → PracticeExperiment changes practiceSessions
    → Compose observes the snapshot write
    → affected composable scopes may re-execute
    → the new count is displayed
```

Repeat for Reset and explain why both mutations remain in `PracticeExperiment`. At
this checkpoint there is no hoisted state or ViewModel: local UI state is sufficient.

Questions to answer without notes:

1. Why does `MainActivity` not own the counter as a regular property?
2. Which symbol is the first Compose boundary?
3. Why is `PracticeExperiment` the smallest sufficient owner of `practiceSessions`?
4. What schedules recomposition after a button tap?
5. What can `rememberSaveable` restore, and what can it not guarantee?
6. Why are ViewModel, StateFlow, Room, and Hilt absent today?

## Trace 4 — Preview and runtime entry

Find the `@Preview` function and follow its call to `DeveloperCard`. Compare its path
with the runtime path:

```text
Preview → NativeAndroidLabTheme → DeveloperCard

runtime → MainActivity.onCreate → setContent
        → NativeAndroidLabTheme → DeveloperCard
```

The two paths share the screen but not the Android Activity launch. A Preview proves
editor rendering for sample inputs; it does not prove lifecycle, process, permission,
or device behavior.

## Read the executable evidence

Open `app/src/test/java/com/saif/nativeandroidlab/KotlinBasicsTest.kt`. Read each test
name as a behavior sentence, then read arrange → act → assert. These tests are the Day
3 Kotlin workbench. Run only local tests:

```bash
./gradlew :app:testDebugUnitTest --no-daemon
```

Then read
`app/src/androidTest/java/com/saif/nativeandroidlab/DeveloperCardTest.kt`. Follow
`completeAndReset_updatePracticeSessionState` from `setContent` through semantic node
queries and button clicks. Then read
`savedStateRestoration_preservesPracticeSessionState` to see how the test recreates
saveable state without pretending it is durable storage. With an emulator or device
connected, run:

```bash
./gradlew :app:connectedDebugAndroidTest --no-daemon
```

This device test exercises Compose UI behavior. It is deliberately separate from the
container/local JVM gate.

Use the full reference gate before stopping:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
```

## Controlled failure — plain variable is not Compose state

Preserve the passing checkpoint first. In `PracticeExperiment`, temporarily replace the
saveable observable `practiceSessions` declaration with a plain local integer. Run the
app, press the completion button, and record whether the visible count changes.

Do not add another change while diagnosing. Explain why a plain value neither survives
re-execution nor asks Compose to recompose. Restore the original declaration and rerun:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
```

Record the prediction, observed behavior, root cause, and restored pass in
[LEARNING_LOG.md](LEARNING_LOG.md). Day 6 deliberately takes the next step into
Activity lifecycle, configuration change, and process loss.
