# Book 3 — Asynchrony, Architecture, and Data

This book replaces temporary in-memory state with boundaries that remain testable as
the app gains concurrency, storage, and network failures.

# Day 11 — Coroutines and Structured Concurrency

## Goal

Write asynchronous work whose lifetime, cancellation, dispatcher, and failure owner
you can explain.

## Mental model

A coroutine is not a thread. It is a cancellable computation running in a scope with
a context. `suspend` means a function may suspend; it does not promise a background
thread.

```text
scope
└── parent job
    ├── child job
    └── child job
```

Structured concurrency keeps child work attached to an owner. Cancellation and
failure then follow rules you can reason about.

## Lab A — one-shot save

Create a fake repository with a `suspend` save operation and a deliberate delay.
Call it from `viewModelScope` when the editor saves.

Model at least:

- idle;
- saving;
- success;
- failure with retry.

Disable duplicate Save actions while one is active. Do not show success before the
repository succeeds.

## Lab B — cancellation

Make the fake save slow. Leave the destination while it is running and determine:

- which scope owns it;
- whether it should be cancelled with the ViewModel;
- whether the user expects it to finish reliably after leaving.

Do not solve reliable persistence with `GlobalScope`; WorkManager comes later.

## Lab C — sequential versus concurrent

Simulate loading two independent values. Measure:

```text
load A; then load B
```

against two child `async` calls awaited together. Use concurrency only because the
operations are independent, not because `async` looks advanced.

## Dispatcher ownership

- Main: quick UI coordination and calling main-safe suspend functions.
- IO: blocking disk/network APIs when the called type does not already move work.
- Default: CPU-heavy computation.

The type doing blocking work should make itself main-safe. Inject dispatchers when
you need deterministic tests; do not scatter dispatcher choices through ViewModels.

## Failure experiments

- Replace `delay` with `Thread.sleep` and observe responsiveness; restore it.
- Catch `Exception` and accidentally swallow cancellation; inspect why this is wrong.
- Launch unowned work, leave the screen, and observe lifecycle confusion; remove it.

## Exit gate

1. Why is `suspend` not equivalent to “background thread”?
2. What owns a `viewModelScope` coroutine?
3. How does cancellation propagate in structured concurrency?
4. When are `async` children justified?
5. Who should choose the dispatcher for blocking work?

Interview check: explain why starting two async operations before awaiting them is
concurrent, whereas awaiting the first before starting the second is sequential.

Hints: log coroutine start/cancel/finally; rethrow cancellation; make every launched
job answer “who cancels me?”

---

# Day 12 — Flow, StateFlow, and Stream Thinking

## Goal

Model changing data as streams and expose a lifecycle-safe screen state.

## Mental model

```text
suspend function → one eventual result
Flow<T>          → zero or more values over time
StateFlow<T>     → hot state with a current value
SharedFlow<T>    → hot broadcast stream with configured replay/buffer
```

Cold flows begin upstream work per collector. Hot flows exist independently of one
collector. Operators such as `map`, `combine`, `debounce`, and `distinctUntilChanged`
build a pipeline; nothing magical removes the need to define ownership.

## Lab

Expose two inputs in `EntryListViewModel`:

- an entry stream from a fake repository;
- a query `MutableStateFlow`.

Combine them into one `EntryListUiState`, convert to `StateFlow` with `stateIn`, and
collect it from the route with `collectAsStateWithLifecycle`.

Acceptance conditions:

- UI never collects directly from a data source;
- `MutableStateFlow` remains private;
- public state is immutable/read-only;
- filtering runs from the combined data and query;
- the chosen `SharingStarted` policy is explained;
- rotation does not create uncontrolled duplicate collectors.

## Events versus state

Loading, visible errors, pending navigation decisions, and user-confirmation prompts
usually need durable state handling. A one-off event stream can lose events while no
collector is active. Prefer processing an action in the ViewModel and updating state;
use event streams only when their delivery semantics are deliberately chosen.

## Experiments

1. Add a second collector to a cold flow and count upstream executions.
2. Apply query changes quickly, then add debounce and compare behavior.
3. Remove lifecycle-aware collection and explain the resource/behavior risk; restore.
4. Trigger an error while UI is stopped and decide what the user should see on return.

## Exit gate

1. Flow versus StateFlow?
2. Cold versus hot?
3. Why expose immutable StateFlow?
4. What does `stateIn` add?
5. Why can one-off event channels be fragile?
6. Where should filtering occur?

Interview check: design UI state for loading, cached content, refresh, and error
without making them mutually exclusive when the UI can show cached content plus an
error banner.

Hints: print each upstream subscription; give UiState one current value; model what
the screen renders, not the order of callbacks that produced it.

---

# Day 13 — Layered Architecture and Repositories

## Goal

Give each kind of logic a clear home and create seams for storage, network, and tests.

## Default architecture

```text
Compose screen
    ↓ actions / ↑ UiState
ViewModel
    ↓
optional use case (only justified shared/complex logic)
    ↓
EntryRepository
    ↓                 ↓
local data source     remote data source
```

Dependencies point inward through contracts. Data returns upward as models/streams.
Do not create a `domain` package merely to look clean.

## Responsibility test

| Concern | Owner |
|---|---|
| pixels, semantics, callbacks | composable |
| screen state and UI business logic | ViewModel |
| combining/choosing data sources | repository |
| SQL/API/file details | data source |
| complex reused business operation | optional use case |

## Lab

Refactor FieldLog into feature-focused packages without adding modules yet:

```text
entries/
├── ui/
├── data/
│   ├── local/
│   └── remote/
└── domain/        only if a real use case justifies it
```

Define an `EntryRepository` contract around user capabilities, not storage verbs.
Provide an in-memory implementation and manually inject it through a composition
root/factory.

Acceptance conditions:

- ViewModel has no SQL, HTTP, or Android Context code;
- composables do not call repository methods;
- repository exposes `Flow` for changing data and suspend functions for actions;
- one source owns writes for each data type;
- models are mapped at boundaries when representations differ;
- a fake repository drives ViewModel tests.

## Single source of truth

Choose one canonical owner for current entry data. Once Room arrives, UI will read
local data only; network refresh will update local storage. Two independently exposed
copies create drift.

## Failure experiment

Create a use case that only forwards one repository call. Ask what it buys. Delete it
unless it adds reuse, policy, or meaningful orchestration.

## Exit gate

1. What problem does a repository solve?
2. Why is a data source not normally exposed to ViewModel?
3. What is single source of truth?
4. When is a domain/use-case layer justified?
5. Why map DTO/entity/UI models selectively rather than automatically?

Interview check: draw FieldLog's dependency graph and explain how you would replace
network/database implementations in tests.

Hints: move behavior by responsibility, not file count; write the contract from user
operations; stop refactoring when every current concern has one clear owner.

---

# Day 14 — Room, Schema Evolution, and Migration Tests

## Goal

Make local storage the source of truth and prove upgrades preserve existing user data.

## Dependency checkpoint

Use the current stable Room family supported by the project. Follow one generation's
documentation consistently; do not mix Room 2 and newer Room APIs in one setup. Add
the required plugin/processor and runtime/testing artifacts through the version
catalog. Export schemas into version control.

## Core pieces

```text
@Entity      database row shape
@Dao         verified queries and writes
@Database    schema/version and DAO factory
Migration    old schema → new schema without data loss
```

Entity is a storage model, not automatically the API DTO or UI model.

## Lab A — first schema

Create an entry entity, DAO, and database.

DAO contract should include:

- observe all entries as `Flow<List<...>>`;
- observe one entry by ID;
- insert/update transactionally as appropriate;
- delete deliberately;
- query ordering encoded in SQL, not assumed from storage.

Wire the local data source into the repository. UI must now read only the DAO stream.
Use Database Inspector to verify the rows.

For small preference-like state—theme choice, sort order, onboarding completion—add
DataStore as a separate exercise. Do not force relational entry data into DataStore or
put one boolean preference into Room merely because Room is already present. Compare
Preferences DataStore with typed Proto DataStore and choose from the data contract.

## Lab B — migration

After version 1 works and contains data:

1. add a nontrivial column or table;
2. increment the database version;
3. write automatic or manual migration deliberately;
4. add a migration test from version 1 to 2;
5. verify original rows and new defaults/transformations;
6. keep exported schema history.

`fallbackToDestructiveMigration` is not an acceptable production shortcut for user
data unless product requirements explicitly permit data loss.

## Failure experiments

- Change schema without a migration and read the startup failure.
- Write a DAO query with a wrong column name and observe compile-time SQL checking.
- Attempt a main-thread blocking database call and explain main-safety.

## Exit gate

1. Entity versus domain model?
2. Why can DAO return Flow?
3. Why is local storage now the source of truth?
4. Why export schemas?
5. What must a migration test prove beyond “database opened”?
6. When is a transaction required?

Interview check: design a migration that splits `fullName` into two columns while
preserving old rows and allowing rollback planning.

Hints: ship version 1 and insert data before editing schema; inspect generated schema
JSON; test the actual version path users will traverse.

---

# Day 15 — Networking and Failure Modeling

## Goal

Build a network boundary that treats HTTP, connectivity, parsing, cancellation, and
business rejection as different failures.

## Dependency checkpoint

Add stable HTTP client, Retrofit-style interface, serialization converter, and
MockWebServer/testing artifacts through the version catalog. Add only the Internet
manifest permission. Do not use a real production credential or private API.

## Boundary models

```text
JSON ↔ EntryDto ↔ domain/repository model ↔ EntryEntity
```

Separate models when fields, nullability, naming, lifecycle, or trust differ. Validate
remote data before persisting it.

## Lab A — deterministic HTTP contract

Use MockWebServer or another locally controlled fake to test:

- a successful list response;
- non-2xx HTTP response;
- malformed JSON;
- empty body when body is required;
- timeout/connection failure;
- cancellation.

Assert request method, path, headers that are safe to inspect, and DTO mapping. Keep
runtime FieldLog on a fake remote source until you own a suitable backend.

## Lab B — repository refresh

Implement refresh as:

```text
fetch remote → validate/map → transactionally update Room → Room Flow updates UI
```

The repository does not return a second network list directly to UI. Cached content
may remain visible while refresh fails; model that UI honestly.

## HTTP client discipline

- explicit timeouts;
- cancellation preserved;
- authentication added at one boundary;
- debug logging excludes tokens, private bodies, and sensitive headers;
- errors mapped once into meaningful application failures;
- retries only when operation semantics and idempotency permit them.

## Failure experiment

Catch every `Exception` and convert it to “No internet.” List the lies this tells:
HTTP authorization, server error, invalid payload, programmer bug, cancellation, and
actual connectivity are not one condition. Revert to typed/structured failure mapping.

## Exit gate

1. DTO versus entity?
2. Why should UI still read Room after refresh?
3. HTTP error versus network exception?
4. Why is automatic retry dangerous for non-idempotent writes?
5. Where should tokens and logging policy live?
6. Why use a deterministic test server?

Interview check: design an error model that can show cached data plus a refresh error,
and describe how cancellation crosses Retrofit/coroutine boundaries.

Hints: test one response class at a time; map at the boundary; preserve the original
cause for diagnostics without exposing sensitive internals to UI.

## Book 3 reference shelf

- https://developer.android.com/kotlin/coroutines
- https://developer.android.com/kotlin/coroutines/coroutines-best-practices
- https://developer.android.com/kotlin/flow
- https://developer.android.com/topic/architecture
- https://developer.android.com/topic/architecture/recommendations
- https://developer.android.com/training/data-storage/room
- https://developer.android.com/training/data-storage/room/migrating-db-versions
- https://developer.android.com/topic/libraries/architecture/datastore
