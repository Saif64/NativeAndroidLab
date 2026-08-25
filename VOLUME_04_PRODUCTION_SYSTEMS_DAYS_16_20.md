# Book 4 — Production Data Systems

This book connects FieldLog's pieces under real Android constraints: object lifetime,
process loss, unreliable networks, large data, permissions, and entry from outside
the app.

# Day 16 — Dependency Injection with Hilt

## Goal

Replace the manual object graph without forgetting that dependency injection is a
design technique and Hilt is one implementation.

## Begin with plain DI

```text
class EntryRepository(local, remote)
class EntryListViewModel(repository)
```

Constructor parameters expose requirements, support fakes, and prevent hidden global
lookups. Hilt generates and owns the Android-aware object graph around these contracts.

## Dependency checkpoint

Follow the current official Hilt setup for this project's AGP/Kotlin combination.
Current releases may require KSP and Java 17; update the version catalog, plugins, and
compile target exactly as the official stable guide requires. Sync and compile after
each build-system step.

## Hilt graph landmarks

- `@HiltAndroidApp`: application-level generated container.
- `@AndroidEntryPoint`: Android component receiving graph-backed injection.
- `@Inject constructor`: Hilt can construct this type from its dependencies.
- `@HiltViewModel`: ViewModel constructed by Hilt.
- `@Module` plus `@InstallIn`: bindings Hilt cannot infer.
- `@Binds`: interface to implementation binding.
- `@Provides`: factory for externally constructed types.
- qualifier: distinguishes two bindings with the same type.
- scope: one instance per graph component lifetime, not “make everything singleton.”

## Lab

1. Create the Application class and register it in the manifest.
2. Make `MainActivity` an entry point.
3. Constructor-inject local source, remote source, and repository where possible.
4. Bind repository interface to its implementation.
5. Provide the Room database/DAO and configured HTTP client.
6. Convert screen ViewModels to Hilt ViewModels.
7. Remove the manual service locator/factory only after behavior matches.

Acceptance conditions:

- no dependency is fetched from a global singleton in feature code;
- reusable composables still receive values/callbacks, not injected ViewModels;
- database/client lifetimes are deliberate;
- qualifiers identify variants such as authenticated versus public client;
- tests can replace bindings without production network/database use.

## Failure experiments

- Omit one binding and read Hilt's generated dependency-chain error from bottom up.
- Bind two same-type clients without qualifiers and explain the ambiguity.
- scope a repository more broadly than its dependencies permit, then fix the mismatch.

## Exit gate

1. DI versus service locator?
2. Why prefer constructor injection?
3. `@Binds` versus `@Provides`?
4. What does a scope actually promise?
5. Why not inject dependencies directly into every composable?
6. How would a test replace the remote source?

Interview check: draw the Hilt component lifetime relevant to Application, Activity,
navigation destination/ViewModel, and explain where a singleton is inappropriate.

Hints: migrate one vertical dependency chain; read generated errors as a missing path;
do not add a module when `@Inject constructor` is enough.

---

# Day 17 — Reliable Background Work

## Goal

Choose WorkManager only for work that must reliably continue beyond the visible
screen or process, then make that work idempotent and observable.

## Decision table

| Need | Typical tool |
|---|---|
| screen-owned async work | coroutine in lifecycle/ViewModel scope |
| deferrable reliable work across restart/reboot | WorkManager |
| exact user-facing alarm | AlarmManager when policy/use case qualifies |
| ongoing user-visible operation | foreground service with notification |
| react briefly to system/app event | broadcast receiver, often delegating work |

WorkManager is not a general thread pool and not the answer to every background task.

## Lab

Create a sync worker that drains pending FieldLog operations.

Requirements:

- enqueue **unique** work so repeated triggers do not create duplicates;
- require network connectivity;
- read pending operations from Room, not from UI memory or large WorkData payloads;
- perform idempotent upload/update logic;
- return success only after durable completion;
- return retry only for genuinely retryable failures;
- configure bounded exponential backoff;
- expose WorkInfo/sync state to UI without treating it as canonical entry data.

Trigger it from a manual Sync action first. Add periodic work only if product behavior
really needs periodic best-effort sync.

## Result semantics

- `success`: work completed or desired state already exists.
- `retry`: temporary condition; schedule again under backoff/constraints.
- `failure`: permanent invalid input or nonretryable condition.

Retries make at-least-once execution possible. Therefore server operations and local
state transitions must tolerate repetition.

## Experiments

1. Tap Sync rapidly and inspect unique work behavior.
2. Disable network, enqueue, restore network, and observe constraints.
3. Kill the app process after enqueueing and verify the persisted request remains.
4. Return retry forever from a fake and observe why retry classification matters.

## Exit gate

1. Coroutine versus WorkManager?
2. Why unique work?
3. Why must a worker be idempotent?
4. What survives process death: coroutine or persisted work request?
5. What data belongs in WorkData versus Room?
6. Retry versus failure?

Interview check: design reliable photo upload after the user leaves the app, including
constraints, progress, idempotency key, cancellation, and failure visibility.

Hints: give work a business name; store durable input by ID; make “already uploaded”
a success; test the worker with fake dependencies.

---

# Day 18 — Offline-First Synchronization

## Goal

Let users read and make critical changes without a reliable network while preserving
one truthful local view of data.

## Architecture

```text
UI observes Room only
       ↑
repository writes local transaction
       ↓
outbox/pending operation
       ↓
unique WorkManager sync
       ↓
remote API
       ↓
validated response updates Room
```

The local database is the canonical read source. A connectivity callback may suggest
when to retry; it does not itself prove the internet/server is reachable.

## Model sync explicitly

Design fields/tables for facts such as:

- local stable ID and optional server ID;
- pending operation type;
- local revision/timestamp;
- server revision/version token;
- sync state and last meaningful error;
- idempotency key;
- tombstone for pending delete when needed.

Avoid a vague `isSynced` boolean once create/update/delete and conflict states exist.

## Lab

1. Save a new entry in airplane mode.
2. In one transaction, persist the entry and its pending operation.
3. Show it immediately from Room with a pending indicator.
4. Restore network and let unique work drain the outbox.
5. Reconcile the server response into local rows transactionally.
6. Remove/complete the pending operation only after durable success.
7. Surface permanent rejection without deleting the user's local work.

## Conflict policy

Choose and document one per field/data class:

- server wins;
- client wins;
- last-write-wins with trustworthy comparable versions;
- merge by field;
- ask the user.

Timestamps from different devices are not automatically trustworthy. Prefer server
versions/ETags or domain-specific conflict rules where possible.

## Adversarial experiments

- Process dies after server success but before local completion: retry must not duplicate.
- Two devices edit the same entry offline: prove the chosen conflict behavior.
- Delete occurs while an update is pending: define ordering/tombstone behavior.
- Server permanently rejects one operation: queue must not block every later item.

## Exit gate

1. What makes an app offline-first?
2. Why is Room the read source rather than network results?
3. What is an outbox?
4. Why must local write plus outbox insert be transactional?
5. How do idempotency and retry interact?
6. What is FieldLog's conflict policy?

Interview check: whiteboard an offline create followed by process death at every step;
identify which transitions are durable and which can repeat.

Hints: design state transitions before code; assign every write a stable operation ID;
simulate each crash boundary with fakes.

---

# Day 19 — Paging Large Datasets

## Goal

Load large entry sets incrementally while preserving cache, errors, retry, and stable
identity. Also learn when not to use Paging.

## Use test

Paging is justified when data volume, latency, or memory makes full loading costly.
For ten FieldLog entries, a simple Room Flow is clearer. Generate or seed a large
dataset so this chapter tests a real constraint.

## Data flow

```text
DAO PagingSource
    → Pager
    → Flow<PagingData<Entry>>
    → cachedIn(viewModelScope)
    → collectAsLazyPagingItems
    → LazyColumn
```

For network plus database, `RemoteMediator` coordinates remote page acquisition into
Room; UI still pages from Room.

## Dependency checkpoint

Add current stable Paging runtime and Compose integration through the version catalog.
Use current official APIs rather than an old Paging 2 tutorial.

## Lab A — local paging

- seed enough entries to make full loading visibly wasteful;
- return `PagingSource` from a deterministic ordered DAO query;
- expose Pager from repository;
- cache PagingData in ViewModel scope;
- render stable keys and content types;
- handle refresh and append load states separately;
- show retry where the failure occurs;
- keep empty state distinct from initial loading.

## Lab B — remote mediation design

Before coding `RemoteMediator`, draw:

- page/cursor key ownership;
- refresh key behavior;
- transactional remote keys plus entity writes;
- end-of-pagination rule;
- cache invalidation and stale-data policy.

Implement only if the capstone's chosen backend can support deterministic pagination.
Otherwise keep it as a tested design exercise.

## Failure experiments

- Remove deterministic `ORDER BY` and inspect unstable pages.
- Use list position as identity and refresh.
- Treat append failure as full-screen failure and note the bad UX.
- Recreate Pager on every recomposition and inspect duplicate work.

## Exit gate

1. PagingSource versus Pager?
2. Why `cachedIn`?
3. Refresh versus append LoadState?
4. Why deterministic ordering?
5. What job does RemoteMediator perform?
6. When is Paging unnecessary complexity?

Interview check: design cursor-based offline paging with Room and explain invalidation,
refresh, remote keys, duplicates, and deletion.

Hints: start local-only; inspect load states independently; test paging transformations
without UI before debugging scrolling.

---

# Day 20 — Permissions, Media, Notifications, and Deep Links

## Goal

Cross Android system boundaries with minimum privilege and make outside entry into
FieldLog deterministic.

## Permission rule

Ask first: can a system picker or narrower API do the job without a broad permission?
Request permission only when the user initiates a feature needing it, explain the
value, handle denial, and keep the rest of the app useful.

## Lab A — photo attachment

Use the Activity Result API and system Photo Picker to attach an image to an entry.

Requirements:

- no broad storage permission when the picker suffices;
- persist or copy URI access according to the chosen storage lifetime;
- do not store bitmap bytes in saved state or navigation arguments;
- display loading/error state;
- handle picker cancellation;
- survive Activity recreation with durable attachment metadata.

## Lab B — notification to destination

After sync completes, post a notification for a meaningful user-visible outcome.

- create a notification channel where required;
- request notification permission on versions that require it, in context;
- use a stable notification ID/update policy;
- tapping opens the correct entry detail via a deep link;
- validate missing/deleted entry IDs gracefully;
- Back/Up behavior after external entry is intentional.

## Lab C — deep-link verification

Test these separately:

- app already foreground;
- app backgrounded;
- cold process start;
- unknown ID;
- malformed/untrusted link;
- repeated delivery.

Treat route data as untrusted input. Authentication/authorization still apply after a
link opens the app.

## Permission state machine

Model at least:

```text
not requested → granted
              → denied
              → denied with rationale opportunity
              → unavailable/restricted
```

Do not loop permission dialogs or interpret denial as a crash-worthy condition.

## Exit gate

1. Why prefer Photo Picker over storage permission?
2. What does an Activity Result contract provide?
3. Why are notification channels separate from runtime permission?
4. Explicit versus implicit deep link?
5. Why validate a route ID from outside the app?
6. How should cold-start deep-link behavior differ from in-app navigation?

Interview check: trace notification tap → PendingIntent/deep link → Activity →
destination → repository lookup, including process death and security checks.

Hints: implement picker, notification, and link as separate slices; test every entry
state; use IDs/URIs rather than large objects.

## Book 4 reference shelf

- https://developer.android.com/training/dependency-injection/hilt-android
- https://developer.android.com/develop/background-work/background-tasks/persistent
- https://developer.android.com/topic/architecture/data-layer/offline-first
- https://developer.android.com/topic/libraries/architecture/paging/v3-overview
- https://developer.android.com/training/permissions/requesting
- https://developer.android.com/training/data-storage/shared/photopicker
- https://developer.android.com/develop/ui/compose/notifications
- https://developer.android.com/guide/navigation/design/deep-link
