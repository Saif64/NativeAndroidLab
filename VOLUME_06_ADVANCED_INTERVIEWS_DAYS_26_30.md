# Book 6 — Advanced Engineering and Interviews

The final book is about disciplined reasoning: effect ownership, measurement, build
systems, safe delivery, and defending tradeoffs under interview pressure.

# Day 26 — Advanced Compose and Effect Discipline

## Goal

Use Compose effects only for work that must cross the declarative UI boundary, and
diagnose unnecessary recomposition without superstition.

## Pure description first

A composable body may run many times, skip execution, or be discarded. It should
describe UI from inputs. Uncontrolled network calls, database writes, analytics, and
listener registration do not belong directly in the body.

## Effect decision guide

| Need | Tool to consider |
|---|---|
| coroutine tied to call-site and keys | `LaunchedEffect` |
| register resource/listener and clean it up | `DisposableEffect` |
| publish Compose state to non-Compose object after composition | `SideEffect` |
| latest callback/value inside long-lived effect | `rememberUpdatedState` |
| derive state while limiting invalidations | `derivedStateOf` when actually useful |
| convert snapshot reads into Flow | `snapshotFlow` |

Keys define restart identity. A wrong key creates stale capture or unnecessary restart;
`true` is not a universal key.

## Lab A — effect audit

Find every FieldLog effect and write beside it:

```text
owner / start condition / restart key / cancellation / cleanup / failure behavior
```

Remove effects that only compute UI values. Move business/data work to ViewModel or
repository. Keep navigation/platform effects narrowly at the route boundary.

## Lab B — scroll behavior

When a filter meaningfully changes, scroll the list to the top with a keyed
`LaunchedEffect`. Prove:

- it does not restart for unrelated recomposition;
- rapid key changes cancel prior scroll work safely;
- current list state is owned at the appropriate UI level.

## Lab C — external listener

Wrap one real listener-based platform API. Register in `DisposableEffect`, remove the
same listener on disposal, and use `rememberUpdatedState` only if the listener must
invoke the newest callback without re-registering.

## Stability and performance reasoning

- use immutable parameters/models;
- supply stable lazy keys;
- do expensive calculation outside composition or cache only with correct keys;
- avoid backward writes to state already read in the same composition;
- defer fast-changing state reads to later phases where an API supports it;
- use Compose compiler/recomposition tooling to inspect, not guess.

## Animation and custom layout boundary

Add one state-driven transition to FieldLog. Choose the smallest API that expresses
it: a simple `animate*AsState`, visibility/content transition, or coordinated
transition. Respect reduced-motion/user settings where relevant and test the final
state without depending on wall-clock sleeps.

Build a custom `Layout` only as a learning exercise after proving `Row`, `Column`,
`Box`, lazy/adaptive layouts, and modifiers cannot express the requirement. Explain
measurement constraints, placement, and why a custom layout increases testing and
maintenance cost.

## Failure experiments

- Launch work directly in the composable body and count duplicates; remove it.
- Use a changing object as an effect key and observe restarts.
- Register a listener without disposal and reproduce duplicate callbacks.
- add `derivedStateOf` around a cheap value and explain why complexity gained nothing.

## Exit gate

1. Why must composable bodies be side-effect disciplined?
2. `LaunchedEffect` versus ViewModel coroutine?
3. What do effect keys mean?
4. When is `DisposableEffect` required?
5. What problem does `rememberUpdatedState` solve?
6. When is `derivedStateOf` useful rather than decorative?

Interview check: diagnose a screen that makes duplicate API calls after recomposition
and propose ownership, effect, and ViewModel fixes with a regression test.

Hints: delete effects before adding new ones; log start/cancel/dispose; every effect
must have one sentence explaining why pure state cannot express it.

---

# Day 27 — Performance, Profiling, and Baseline Profiles

## Goal

Measure representative release behavior, locate the bottleneck, improve one cause,
and prove the result statistically rather than optimizing from intuition.

## Performance loop

```text
user journey → metric → repeatable baseline → trace → hypothesis → one change → remeasure
```

Debug builds distort Compose/runtime performance. Use release-like, non-debuggable
benchmarks and controlled device state.

## Important signals

- cold, warm, and hot startup;
- time to initial display and time to full display;
- slow/frozen frames and jank during scroll;
- composition, layout, and draw work;
- main-thread blocking I/O;
- allocations, retained objects, and memory growth;
- battery/network behavior for background work;
- APK/AAB size and method/resource contribution.

## Lab A — inspect

Use Android Studio tools and system trace to inspect:

1. cold startup;
2. scrolling a seeded large entry list;
3. opening detail with an image;
4. sync worker behavior.

Record evidence with timestamps and traces. Identify one largest user-visible issue.

## Lab B — benchmark

Add a dedicated Macrobenchmark/Baseline Profile module using current stable templates.

- benchmark cold startup repeatedly;
- benchmark a deterministic list-scroll journey;
- control compilation mode and setup;
- report fully drawn when FieldLog is genuinely interactive;
- generate an app-specific Baseline Profile covering critical journeys;
- confirm the profile is packaged and compare before/after distributions.

Compose libraries already contribute profiles; your app profile covers your critical
code paths. A profile is not proof until measurement shows the relevant journey.

## Diagnosis candidates

- work or DI graph initialization on startup;
- network call before first useful content;
- unstable lazy keys/models;
- expensive mapping or image decoding on Main;
- unnecessary state reads/recomposition;
- large objects retained by lifecycle owners;
- broad R8 keep rules;
- unbounded caches.

## Failure experiment

Apply a popular “optimization” before measuring. Then measure and determine whether it
improved, regressed, or merely moved the cost. Revert changes without evidence.

## Exit gate

1. Why benchmark release-like builds?
2. Cold versus warm versus hot start?
3. TTID versus TTFD?
4. What does a Baseline Profile do?
5. Macrobenchmark versus local stopwatch/log?
6. How can stable keys affect list performance?
7. Show evidence for one FieldLog improvement.

Interview check: given scrolling jank, describe the measurement path before proposing
Compose changes.

Hints: keep journeys deterministic; change one variable; save raw results; optimize
the slowest verified boundary, not the most fashionable one.

---

# Day 28 — Gradle, Variants, and Modularization

## Goal

Understand how Android artifacts are assembled and introduce build complexity only
when it creates a measurable ownership, isolation, or delivery benefit.

## Build model

```text
settings.gradle.kts includes modules
root build config declares shared plugins
module build.gradle.kts configures Android artifact
version catalog names plugins/libraries/versions
source sets + build type + flavor → variant
variant → compile/package/optimize/sign artifact
```

`compileSdk`, `targetSdk`, and `minSdk` solve different problems. `namespace` is code
identity; `applicationId` is installed/store identity.

## Lab A — staging variant

Add the smallest useful staging setup:

- a staging build type or flavor chosen for a real distinction;
- separate application ID suffix so it installs beside production;
- visible app-name/icon marker;
- endpoint/config supplied through build configuration without embedding a secret;
- variant-specific source/resource set;
- debug and release behavior remain understandable.

Use Android Studio's Build Variants tool and inspect the merged manifest/resources.

## Dependency literacy

- `implementation`: dependency internal to module consumers.
- `api`: leaks the dependency through a library's public API; use sparingly.
- `testImplementation`: local tests only.
- `androidTestImplementation`: device tests.
- `debugImplementation`: debug variant only.
- KSP/plugin configuration: build-time code generation/tooling.

Version catalogs centralize names/versions; they do not automatically guarantee
binary compatibility.

## Lab B — module decision

Create a decision record before creating a module. Candidate extraction must show at
least one benefit:

- independently reusable contract;
- strict dependency direction;
- ownership/team boundary;
- build isolation;
- dynamic delivery requirement.

If justified, extract only a small pure `core:model` or feature contract and verify
dependency direction. If no benefit exists, keep packages and record “not yet.”

Avoid `core`, `common`, and `utils` dumping grounds. More modules can worsen build
configuration, navigation, DI, and developer cognition.

## Build troubleshooting drill

Given a dependency resolution/build error, inspect in order:

1. exact failing task and first cause;
2. selected variant;
3. version-catalog alias/plugin location;
4. Gradle/AGP/Kotlin/JDK compatibility;
5. dependency graph and duplicate versions;
6. generated-code processor output;
7. clean rebuild only after understanding what cache is suspected.

## Exit gate

1. Module versus package?
2. Build type versus product flavor versus variant?
3. namespace versus applicationId?
4. `api` versus `implementation`?
5. What benefit justified or rejected a FieldLog module?
6. Why is “Clean Project” not a root-cause explanation?

Interview check: design modules for a 40-engineer app, then contrast that with the
correct structure for this one-developer capstone.

Hints: inspect generated variants before adding more; preserve one-way dependencies;
make build changes in isolated commits.

---

# Day 29 — Release Engineering, R8, Signing, and CI

## Goal

Produce a release-like artifact that preserves behavior under optimization and can be
delivered repeatedly without exposing signing material.

## Signing identities

- debug key: disposable developer signing for debug builds.
- app signing key: long-lived identity Android trusts for updates.
- upload key: authorizes uploads to Play when Play App Signing manages app signing.

Losing or exposing signing material has consequences beyond a broken build. Never
commit keystores, private keys, or passwords. Do not paste them into lesson logs.

## Release checklist lab

1. Set intentional `versionCode` and `versionName` policy.
2. Enable current recommended release optimization/resource shrinking configuration.
3. Build a minified release-like artifact using safe local signing for testing.
4. Run critical tests against release/minified behavior where feasible.
5. Inspect artifact with APK Analyzer and verify manifest, permissions, native libs,
   resources, mapping, Baseline Profile, and unexpected files.
6. Test deep links, serialization/reflection, Room, Hilt, and notifications under R8.
7. Add the narrowest keep rule only when evidence proves dynamic access needs it.
8. Produce an Android App Bundle and inspect it; do not upload or publish as part of
   this lesson.

Broad package keep rules and global “disable shrinking/optimization/obfuscation” flags
hide problems and harm release quality.

## CI contract

A clean environment should run at least:

```text
format/static analysis
unit tests
debug compile/assemble
lint
instrumented/managed-device tests on an appropriate cadence
release/minified build
artifact and report retention
```

Pin toolchains, use Gradle wrapper, cache safely, keep secrets in the CI secret store,
and make release inputs auditable. Reproducibility matters more than a long YAML file.

## Delivery knowledge

Understand internal testing, staged rollout, crash/ANR monitoring, rollback/stop-rollout
decision, Play pre-launch reports, data-safety declarations, target API/policy review,
and mapping/native-symbol upload. Policies change; verify current Play requirements at
release time.

## Failure experiments

- Make a fake reflection-only class disappear under R8, inspect why, then add the
  narrowest correct rule.
- Build on a clean checkout/environment and find accidental local-machine assumptions.
- Change an upload/signing identity in a disposable learning setup and inspect how
  Android rejects incompatible updates.

## Exit gate

1. App signing key versus upload key?
2. APK versus AAB?
3. What does R8 do?
4. Why test minified builds?
5. What belongs in a keep rule?
6. Which CI gates block a release?
7. What is the rollback/monitoring plan?

Interview check: explain why debug success does not prove release success, with examples
from R8, resources, signing, environment, and backend configuration.

Hints: isolate release changes; inspect artifacts instead of assuming; never “fix” R8
with a package-wide keep rule without narrowing it.

---

# Day 30 — Graduation, System Design, and Interview Defense

## Goal

Prove advanced readiness with a working artifact, evidence, and explanations—not a
claim based on chapter completion.

## FieldLog graduation acceptance matrix

### Product

- list, detail, create, and edit flows are coherent;
- validation, empty, loading, cached, pending, error, and retry states are visible;
- external notification/deep-link entry works from cold/background/foreground states;
- compact and expanded layouts preserve state;
- localization and accessibility audits pass the defined bar.

### Data and reliability

- Room is the single read source;
- schema migration preserves seeded old data and is tested;
- DTO/entity/domain mappings validate untrusted data;
- offline create/update survives process loss;
- outbox drain is unique, idempotent, constrained, and retry-classified;
- conflict policy is documented and tested;
- Paging is used only if the large-dataset requirement remains real.

### Architecture

- screen UiState and actions follow UDF;
- ViewModels are screen-scoped and Android-lifecycle independent;
- repositories own source coordination;
- use cases exist only for justified shared/complex rules;
- Hilt graph and scopes are explainable;
- no UI, database, network, and sync god class.

### Quality and delivery

- focused unit, ViewModel, database, migration, worker, and UI tests pass;
- critical offline-to-sync journey is covered;
- security threat model has verified mitigations;
- release-like startup and scroll measurements are recorded;
- Baseline Profile is packaged and measured;
- minified release-like build passes critical flows;
- CI runs the agreed gates from a clean environment;
- architecture decision record and learning log are current.

## Final debugging examination

Solve without asking for code replacement:

1. UI shows stale entry after network refresh.
2. duplicate uploads occur after worker retry.
3. rotation resets an editor draft but not the list.
4. one notification opens two detail destinations.
5. Room upgrade crashes only existing installs.
6. release build loses JSON fields while debug works.
7. list scroll janks only with images and large font.
8. a deep link exposes an entry after logout.

For each: reproduce, locate ownership boundary, rank hypotheses, gather evidence, add
a regression test, apply the smallest responsible fix, and verify preserved behavior.

## System-design interview prompt

“Design an offline-first field inspection app used by technicians with intermittent
connectivity. It supports notes, photos, shared assignments, conflict resolution,
background sync, and notifications.”

Your answer must cover:

- requirements and non-goals;
- data model and local source of truth;
- read/write/sync paths and idempotency;
- conflict and deletion policy;
- process/lifecycle restoration;
- background API choice and constraints;
- authentication, authorization, and sensitive data;
- pagination/media storage;
- observability, testing, rollout, and failure recovery;
- tradeoffs and what you would postpone.

## Interview question bank

### Kotlin and concurrency

- `val` versus `var`; data class/value equality; sealed types; null safety.
- coroutine versus thread; suspend; structured concurrency; cancellation.
- `launch` versus `async`; exception propagation; dispatcher ownership.
- Flow versus StateFlow versus SharedFlow; cold versus hot; `stateIn`.

### Android platform

- Activity lifecycle, configuration change, process death, saved state.
- component/Intent/task/back-stack model; exported surface.
- coroutine versus WorkManager versus service versus alarm.
- permission denial, Activity Result, URI access, notification/deep-link entry.

### Compose

- composition/recomposition; state hoisting; UDF; stability and keys.
- `remember`, `rememberSaveable`, ViewModel, SavedStateHandle, Room.
- effect APIs and keys; semantics; adaptive layout; interoperability.
- performance phases and diagnosis.

### Architecture and data

- repository, SSOT, optional domain layer, model mapping.
- Room Flow, transaction, migration, migration testing.
- offline reads/writes, outbox, idempotency, conflict resolution.
- error modeling, retries, caching, Paging/RemoteMediator.

### Delivery

- Hilt graph/scopes/testing; Gradle variants/modules/dependencies.
- testing pyramid and doubles; Macrobenchmark/Baseline Profile.
- signing, AAB, R8/keep rules, CI, staged rollout, observability.
- mobile threat model, Keystore, secrets, network security.

## Portfolio defense

Write a short case study:

1. user problem and constraints;
2. architecture diagram;
3. hardest failure and evidence-led diagnosis;
4. offline and process-death guarantees;
5. migration/security/performance proof;
6. tests and release pipeline;
7. tradeoffs, rejected complexity, and next iteration.

Present yourself accurately: four years of Flutter production experience plus a
serious Kotlin/Compose capstone and native Android depth. Do not imply four years of
native experience.

## Graduation defense

You graduate when you can demo the acceptance matrix, rebuild from a clean checkout,
and defend the system-design prompt for 45 minutes while an interviewer changes
constraints. Any weak category becomes the next focused practice loop.

## Exit gate

1. Demonstrate the full acceptance matrix from a clean build.
2. Defend one architecture decision and one rejected abstraction.
3. Diagnose two final debugging scenarios without code replacement.
4. Present measured performance and migration evidence.
5. Complete a timed system-design defense and identify the weakest answer category.

## Book 6 reference shelf

- https://developer.android.com/develop/ui/compose/side-effects
- https://developer.android.com/develop/ui/compose/performance
- https://developer.android.com/topic/performance/measuring-performance
- https://developer.android.com/topic/performance/baselineprofiles
- https://developer.android.com/build/gradle-build-overview
- https://developer.android.com/build/build-variants
- https://developer.android.com/build/dependencies
- https://developer.android.com/studio/publish
- https://developer.android.com/topic/performance/app-optimization/add-keep-rules
- https://developer.android.com/topic/architecture
