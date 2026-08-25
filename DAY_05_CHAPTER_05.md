# Day 5 — Chapter 5: State and Recomposition

Yesterday's UI depended only on function parameters. Today the button will change a
value and Compose will redraw the affected UI.

> **Current reference checkpoint:** the runnable app is complete through this chapter.
> Use [CODE_TOUR.md](CODE_TOUR.md) for read-first study, or attempt the contract below
> before tracing `DeveloperCard` into `PracticeExperiment` for build-first study.
> Day 6 and later code is intentionally absent until you implement those chapters.

## The governing idea

```text
state changes
    → Compose notices a state read is now stale
    → affected composable code may run again
    → UI describes the new state
```

This re-execution is **recomposition**. It is not Activity recreation, and it is not
the same as Flutter's `setState`, even though both update declarative UI.

## Ordinary variables are not Compose state

This does not create observable UI state:

```kotlin
var count = 0
```

It is recreated when the function runs and Compose is not asked to observe it.

The basic state shape is:

```kotlin
var count by remember { mutableStateOf(0) }
```

- `mutableStateOf(0)` creates observable Compose state.
- `remember` retains that state object across recompositions while this composable
  remains in the Composition.
- `by` lets you read and write `count` instead of `count.value`.
- Changing `count` schedules recomposition for code that read it.

Let Android Studio import `getValue`, `setValue`, `remember`, and `mutableStateOf`.

## Your task: learning-session counter

Extend yesterday's screen:

1. Add an integer state starting at zero.
2. Display `Practice sessions: 0` using that state.
3. Change the existing button label to `Complete session`.
4. On click, increment the state.
5. Add a second `Reset` button that returns it to zero.
6. Run the app and verify the sequence `0 → 1 → 2 → 0`.

Do not introduce ViewModel, Flow, repositories, or architecture today. Local UI state
is enough for this experiment.

## Predict before testing

Before every action, write your prediction:

| Action | Your predicted count |
|---|---|
| Tap Complete twice | ? |
| Change between light and dark mode | ? |
| Rotate the emulator | ? |
| Press Home, then return from Recents | ? |
| Force-stop and reopen | ? |

Run the experiment with `remember`. Record actual results; do not “fix” anything yet.

## Activity recreation is not recomposition

Rotation commonly causes the Activity to be destroyed and recreated. A value held
only by `remember` survives recomposition, but not removal of that Composition during
Activity recreation.

Now replace `remember` with `rememberSaveable`, repeat the rotation experiment, and
record the difference.

`rememberSaveable` is appropriate for small UI values that Android can save. It is
not a database, and it does not make force-stop persistence guarantees.

## Flutter → Compose bridge

| Flutter | Compose |
|---|---|
| mutable field inside a `State` object | value backed by observable state |
| call `setState(() { count++; })` | assign to `mutableStateOf`-backed value |
| framework schedules `build` | Compose schedules affected recomposition scopes |
| restoration APIs | `rememberSaveable` for small saveable UI state |

Important difference: in Compose you do not call a general `setState`. Writing the
observable value is the signal.

## Optional diagnostic experiment

Add a temporary log at the top of `DeveloperCard`, interact with the screen, and
watch Logcat. Remove the log afterward.

The body may execute more often than you expect, so composables should not perform
uncontrolled work such as network requests, database writes, or analytics directly
in their bodies. Later we will learn effect APIs and ViewModel ownership.

## Exit questions

1. Why does a plain local `var` fail as UI state?
2. What separate jobs do `mutableStateOf` and `remember` perform?
3. What event caused recomposition in your counter?
4. Why can rotation reset `remember` state?
5. What did `rememberSaveable` change?
6. Why should a composable body avoid uncontrolled side effects?
7. Is recomposition the same as Activity recreation?

## Hint ladder

- **Hint 1:** Put the state in the smallest composable that needs to change it.
- **Hint 2:** The displayed `Text` must read the same value the button changes.
- **Hint 3:** If `by` is red, use Android Studio's import quick-fix.
- **Hint 4:** If rotation still resets the value, confirm the declaration actually uses `rememberSaveable`.

Stop when you have a prediction table with actual observations and can distinguish
recomposition from Activity recreation. Do not begin architecture yet.

Official references:

- https://developer.android.com/develop/ui/compose/state
- https://developer.android.com/develop/ui/compose/phases
