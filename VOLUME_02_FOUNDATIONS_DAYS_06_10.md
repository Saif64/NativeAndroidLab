# Book 2 — Android and UI Foundations

Finish Days 1–5 first. This book turns basic Compose syntax into Android behavior you
can reason about.

# Day 6 — Lifecycle, Configuration, and Process Death

## Goal

Distinguish four events that beginners often mix together:

```text
recomposition ≠ Activity recreation ≠ process death ≠ force-stop
```

## Mental model

Android owns component lifecycles. Your Activity is a temporary host, not the app
itself. A process can contain the Activity and other components, and the system can
remove that process when resources are needed.

Core Activity callbacks:

```text
onCreate → onStart → onResume
                         ↓
onDestroy ← onStop ← onPause
```

The exact path depends on what happens. Rotation commonly recreates the Activity.
Home usually pauses and stops it without immediately destroying it. Process removal
can happen later without a final callback you can rely on.

## Lab

1. Add temporary `Log.d` statements to all six callbacks in `MainActivity`.
2. Run and record the startup order.
3. Press Home, return through Recents, and record the callbacks.
4. Rotate and record the callbacks.
5. Change the Day 5 counter with `remember`, rotate, and record the value.
6. Repeat with `rememberSaveable`.
7. Enable **Don't keep activities** only for an experiment, repeat, then disable it.

Do not place business logic in callbacks. These logs are probes and must be removed.

## State survival table

Fill this from observation before checking documentation:

| Owner | Recomposition | Rotation | System process recreation | Force-stop |
|---|---:|---:|---:|---:|
| plain local variable | ? | ? | ? | ? |
| `remember` | ? | ? | ? | ? |
| `rememberSaveable` | ? | ? | ? | ? |
| disk/database | ? | ? | ? | ? |

Saved instance state is for small, transient UI state. Durable user data belongs on
disk. A force-stop or removed task is not the same restoration contract as
system-initiated process recreation.

## Controlled failures

- Omit `super.onCreate` temporarily, read the failure, then restore it.
- Start a long delay in an unmanaged scope, rotate, and observe why ownership matters.
  Do not keep that code.

## Exit gate

Explain without notes:

1. Why an Activity is not a safe database.
2. Why `remember` surviving recomposition says nothing about rotation.
3. What configuration change means.
4. Why `onDestroy` cannot be treated as guaranteed cleanup.
5. Which state belongs in saved state versus persistent storage.

Interview check: “What happens to an Android app when the device rotates?” Give a
layered answer covering Activity recreation, ViewModel later, saved state, and disk.

Hints: use Logcat filtered by one tag; change only one environmental condition per
run; draw a timeline instead of listing callbacks.

---

# Day 7 — State Hoisting and Unidirectional Data Flow

## Goal

Make UI ownership explicit instead of scattering mutable state through composables.

## Mental model

```text
state flows down
events flow up
```

A reusable composable should usually receive values and callbacks. Its caller owns
the state. This resembles a Flutter widget receiving fields and `onPressed`, but
Compose state observation and lifecycle are platform-specific.

## Lab

Split yesterday's counter into two levels:

```text
DeveloperRoute       owns state
└── DeveloperScreen  receives state and callbacks
```

Create an immutable state model with the values the screen renders. Give the screen
callbacks such as `onCompleteSession` and `onReset`.

Acceptance conditions:

- `DeveloperScreen` contains no `remember`.
- its preview can render at least zero, success, and disabled states;
- only the owner mutates state;
- parameters describe behavior rather than exposing `MutableState`;
- state is replaced immutably instead of mutating a list in place.

## Add an action boundary

When callbacks become noisy, model user intentions:

```text
CompleteSession
Reset
NameChanged(value)
```

Use either focused callbacks or one `onAction`; be able to defend the choice. Do not
create an action hierarchy merely because a tutorial says every screen needs one.

## Failure experiment

Pass a `MutableState<Int>` directly into the reusable screen. Observe that it works,
then explain why it leaks ownership and makes previews/tests less independent. Revert.

## Exit gate

1. What is state hoisting?
2. What makes a composable stateless?
3. Why are immutable inputs easier to reason about?
4. When are several named callbacks clearer than one action type?
5. Where should UI-only state live today?

Interview check: sketch a UDF loop and name the single writer for each state value.

Hints: start by moving only one value; make all preview inputs explicit; prefer the
smallest owner shared by every reader and writer.

---

# Day 8 — FieldLog Lists and Forms

## Goal

Begin the cumulative capstone and learn lazy lists, stable identity, controlled text
inputs, and validation.

## Product slice

FieldLog records small field observations. Begin with an in-memory model:

```text
FieldEntry
├── id
├── title
├── note
├── status
└── updatedAt
```

Use appropriate Kotlin types. Keep `id` stable. Do not use list position as identity.

## Lab A — list

Replace the profile screen with an entry list:

- render entries with `LazyColumn`;
- provide `key = { entry.id }`;
- extract a stateless `EntryRow`;
- show title, status, and last-updated label;
- call `onEntryClick(id)` rather than passing navigation objects;
- render an explicit empty state.

Generate enough sample entries to scroll. Confirm that only visible content is
composed as needed.

## Lab B — form

Build a separate `EntryEditorScreen` preview containing:

- controlled `OutlinedTextField` values and change callbacks;
- title required, trimmed, with a visible error;
- optional note with a deliberate maximum length;
- status selection;
- Save disabled until valid;
- one screen-level `onSave` event carrying validated input.

Do not add a database, navigation library, or ViewModel yet.

## Experiments

1. Use list index as the lazy key, reorder items, and note identity problems; restore ID.
2. Mutate a plain `MutableList` in place and observe stale UI risk; restore immutable
   replacement.
3. Enter a long title with large font scale and record the layout weakness.

## Exit gate

1. Why does a lazy item need a stable key?
2. What makes a text field controlled?
3. Where should validation rules live when they become business rules?
4. Why is empty state different from loading state?
5. Why should navigation receive an ID rather than a whole mutable object?

Interview check: compare `LazyColumn` with Flutter `ListView.builder`, including keys
and item identity rather than stopping at syntax.

Hints: build list and editor independently in Preview; keep the model simple; use
immutable `copy` to change an entry.

---

# Day 9 — Navigation and the Back Stack

## Goal

Model destinations and back-stack behavior without coupling screens to navigation.

## Version checkpoint

Navigation evolves. Follow the current official Compose navigation guide when adding
the stable dependency. Current official guidance favors Navigation 3 for new
Compose-only single-activity apps; understand Navigation Compose/NavController
concepts as well because existing production codebases still use them.

The enduring concepts are:

```text
typed route → destination → back stack → state holder scope → deep link
```

## Lab

Create three destinations:

- entry list;
- entry detail with an `entryId` route argument;
- create/edit entry.

Acceptance conditions:

- route types are compile-time safe rather than hand-built string concatenation;
- the navigation host owns navigation wiring;
- screens receive callbacks and never receive the navigation controller;
- only an ID crosses into detail/edit;
- Up and system Back behave predictably;
- repeated taps do not accidentally create duplicate destinations;
- the list preserves useful UI state when returning from detail.

Draw the stack after: launch → detail A → edit A → save → Back.

## Intents versus in-app navigation

Navigation changes destinations inside your app. An Android `Intent` asks the system
to activate a component, possibly in another app. You will use intents deeply later;
do not call every navigation action an intent.

## Failure experiments

- Pass an entire `FieldEntry` through the route, then list serialization/staleness
  problems and revert to ID.
- Trigger the same navigation event twice and inspect the stack.
- Rotate while on detail and verify route restoration.

## Exit gate

1. What does the back stack contain conceptually?
2. Why are typed routes safer?
3. Why should a reusable screen not know the navigation controller?
4. What is the difference between Up and Back?
5. Why pass an ID and reload current data?

Interview check: explain single-activity Compose navigation and how state-holder scope
relates to destinations and graphs.

Hints: make each destination render before connecting the next; log route IDs; draw
the stack every time behavior surprises you.

---

# Day 10 — ViewModel and Saved State

## Goal

Move screen business logic out of composables while understanding exactly what a
ViewModel does and does not survive.

## Mental model

A ViewModel is a screen-level state holder and UI business-logic owner. It survives
configuration change because its store outlives the recreated Activity/destination.
It does not make data durable through every form of process or task removal.

```text
Screen observes UiState
Screen sends actions
ViewModel reduces actions and calls dependencies
```

## Dependency checkpoint

Add stable lifecycle ViewModel Compose and lifecycle-aware Compose collection
artifacts through the version catalog. Sync and compile before writing ViewModel code.

## Lab

Create `EntryListViewModel` and an immutable `EntryListUiState`.

- expose read-only state;
- keep mutable implementation details private;
- move filtering and entry actions out of composables;
- collect in the route with lifecycle awareness;
- pass only values/callbacks to `EntryListScreen`;
- keep preview functions independent of ViewModel;
- use a simple factory/manual dependency until the Hilt chapter.

Add `SavedStateHandle` only for small state needed after system process recreation,
such as the current query or route ID. Do not put entry lists or bitmaps into it.

## Survival experiment

Record the value of a query held in:

1. `remember`;
2. ViewModel only;
3. ViewModel backed by `SavedStateHandle`;
4. persistent storage later.

Test recomposition, rotation, system-initiated recreation, removed task, and force-stop
as distinct cases.

## Rules

- no `Activity`, `View`, or navigation controller reference in ViewModel;
- avoid `AndroidViewModel`; move context-requiring work to the proper data/platform
  dependency;
- ViewModels belong at screen/destination boundaries, not every reusable composable;
- observable state should not expose mutable setters to UI.

## Exit gate

1. Why does ViewModel survive rotation?
2. Why does it not replace Room or saved state?
3. What belongs in `SavedStateHandle`?
4. Why collect UI state with lifecycle awareness?
5. Why keep the content composable free of ViewModel?

Interview check: answer “ViewModel survives process death” by correcting the premise
and describing `SavedStateHandle` plus disk persistence.

Hints: first move state without changing behavior; create one immutable UI state;
inject a fake manually before introducing a DI framework.

## Book 2 reference shelf

- https://developer.android.com/guide/components/activities/activity-lifecycle
- https://developer.android.com/topic/libraries/architecture/saving-states
- https://developer.android.com/develop/ui/compose/state-hoisting
- https://developer.android.com/guide/navigation/design
- https://developer.android.com/topic/libraries/architecture/viewmodel
- https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate
