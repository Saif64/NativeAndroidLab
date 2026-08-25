# Book 5 — Platform and Quality

This book fills the gaps a Compose-only tutorial often leaves: Android's component
model, legacy interoperability, diverse devices and users, testing economics, and
security boundaries.

# Day 21 — Android Components, Intents, Tasks, and Processes

## Goal

Understand the platform that hosts Compose so you can debug launches, background
behavior, inter-app communication, and interview scenarios.

## Four component types

| Component | Primary role | Activated by |
|---|---|---|
| Activity | user-facing interaction/window | Intent |
| Service | ongoing/background component work, no UI | Intent/binding |
| BroadcastReceiver | brief reaction to broadcast event | Intent |
| ContentProvider | URI-addressed data sharing | ContentResolver |

They are system entry points with distinct lifecycles. A Kotlin class in source is
not automatically a component; most components must be registered appropriately.

## Intents

- explicit intent names the target component;
- implicit intent describes an action/data and lets Android resolve a capable app;
- extras are untrusted serialized inputs, not an object-sharing mechanism;
- intent filters advertise capabilities and expand external entry surface.

## Task and process distinctions

A task is a user navigation history/back stack. A process is an OS runtime container.
One does not imply one Activity, task, or process forever. Launch modes and intent
flags can change stack behavior; default behavior should be preferred unless a product
requirement proves otherwise.

## Lab A — share

Add a FieldLog action that shares entry text with an implicit `ACTION_SEND` intent.

- set an appropriate MIME type;
- use a chooser;
- handle no matching application;
- share the minimum content;
- do not expose private file paths.

## Lab B — receive safely

Design, but only implement if useful, an external text entry point that creates a
draft. Validate action, MIME type, size, nullability, authentication assumptions, and
duplicate delivery. Keep exported surface minimal.

## Component choice drills

Choose and defend an API for:

1. music playback continuing with visible notification;
2. daily approximate sync;
3. exact calendar alarm;
4. reacting briefly to device boot;
5. sharing selected entries with another app;
6. exposing a controlled dataset to another app.

## Failure experiments

- Export a component unnecessarily, inspect the security surface, then close it.
- Put long work directly in a receiver and explain timeout/lifecycle risk.
- Use a launch flag to hide a stack bug, then remove it and fix navigation ownership.

## Exit gate

1. Component versus composable?
2. Intent versus Navigation route?
3. Task versus process?
4. Service versus WorkManager?
5. Broadcast receiver's responsibility limit?
6. Why are exported components trust boundaries?

Interview check: the OS kills FieldLog's process while its task remains. Explain how
the user can return and what must restore from route/saved state/disk.

Hints: draw system → component → app boundary; inspect merged manifest; use platform
components only for platform entry/lifetime needs.

---

# Day 22 — Views, XML, Fragments, and Compose Interoperability

## Goal

Become capable of maintaining real Android codebases without rebuilding FieldLog in
legacy UI.

## Legacy vocabulary

- XML layout resources inflate into a View hierarchy.
- Activity or Fragment owns/hosts that hierarchy.
- View Binding generates type-safe view references.
- RecyclerView uses Adapter/ViewHolder and recycling.
- Fragment has a lifecycle; its View has a shorter, separate lifecycle.
- LiveData is lifecycle-aware observable state common in older apps.

Understand Data Binding and raw Dagger when reading code, but do not introduce them
into this Compose-first capstone without an existing-code requirement.

## Interoperability directions

```text
Views screen → ComposeView → composable
Compose screen → AndroidView → legacy/custom View
```

Composition disposal must match the host lifecycle, especially inside a Fragment.

## Lab A — small View island

Create one isolated legacy custom View or XML widget and host it inside a Compose
screen with `AndroidView`/binding integration.

Requirements:

- construct the View in `factory`;
- update it from Compose state in `update`;
- do not retain Activity/View references in ViewModel;
- prove state changes update the View;
- dispose listeners/resources at the correct boundary.

## Lab B — small Compose island

Create a learning-only Fragment/XML layout containing `ComposeView`, set an
appropriate `ViewCompositionStrategy`, and render `EntryRow` inside it. Remove or keep
this isolated under a `legacy` package; do not migrate the whole app architecture.

## Fragment lifecycle trap

A Fragment instance can outlive its destroyed View. A binding reference must be
cleared in `onDestroyView`; observing against the Fragment rather than view lifecycle
can update a dead hierarchy.

## Exit gate

1. Fragment lifecycle versus Fragment View lifecycle?
2. RecyclerView versus LazyColumn at a conceptual level?
3. What does View Binding solve?
4. When use `ComposeView`?
5. When use `AndroidView`?
6. Why is interoperability a migration boundary, not a reason for two architectures?

Interview check: outline incremental migration of a Fragment/RecyclerView app to
Compose while shipping after every step.

Hints: keep the island small; identify lifecycle owner; never synchronize two copies
of state manually if one can drive both UI systems.

---

# Day 23 — Adaptive UI, Accessibility, and Localization

## Goal

Make FieldLog work for different window sizes, postures, input methods, languages,
font scales, and assistive technologies.

## Adaptive principle

Adapt to the current **window**, not a remembered device label. Rotation, split screen,
folding, freeform windows, and external displays can change available space while the
app is running.

## Lab A — list/detail adaptation

Implement:

- compact window: list and detail as separate destinations;
- expanded width: list-detail panes visible together;
- shared selection/navigation state with no duplicated data source;
- resizing without losing selected entry or draft;
- sensible maximum content widths rather than endlessly stretched text.

Test phone portrait/landscape, resizable emulator, tablet, and large font scale.

## Lab B — accessibility

Audit with TalkBack/Accessibility Scanner and keyboard where available.

- semantic role and label for interactive elements;
- meaningful `contentDescription` or null for decorative imagery;
- logical traversal/focus order;
- visible focus indication;
- adequate touch targets;
- state descriptions for pending/synced/error;
- no color-only meaning;
- no text clipped at large font scale;
- announcements only for meaningful changes.

Prefer native semantics from Material components before adding custom semantics.

## Lab C — localization and RTL

Move user-visible text to resources. Add a second locale such as Bangla, then test:

- plural resources for counts;
- formatting with placeholders rather than string concatenation;
- dates/numbers using locale-aware formatting;
- longer translations;
- RTL layout using start/end rather than left/right;
- app name and notification text.

## Failure experiments

- Lock orientation to avoid an adaptive bug; explain why this dodges the requirement,
  then restore resizability.
- Add descriptions to decorative icons and listen to noisy TalkBack output; correct it.
- Concatenate a count into English text and observe localization failure; use plurals.

## Exit gate

1. Why window size rather than device type?
2. What state must survive pane changes?
3. When should an image have no content description?
4. Why are strings/plurals resources behavioral, not cosmetic?
5. Why avoid left/right layout assumptions?
6. How did FieldLog behave at 200% font scale?

Interview check: design the same screen for phone, foldable, and tablet without three
separate business-logic implementations.

Hints: make compact correct first; resize continuously; test with real assistive output,
not visual intuition alone.

---

# Day 24 — A Production Testing Strategy

## Goal

Buy confidence at the cheapest reliable layer and reserve slow end-to-end tests for
critical integrated journeys.

## Test layers

```text
many:   pure unit and state-reducer tests
some:   ViewModel, repository, mapper, database/component tests
few:    Compose UI and critical application journeys
manual: exploratory, accessibility, unusual device/platform behavior
```

Test behavior and contracts, not private implementation details.

## Required FieldLog suite

### Local JVM tests

- editor validation boundaries;
- DTO/entity/domain mappings;
- conflict resolver and outbox state transitions;
- ViewModel UiState using coroutine test scheduler and fakes;
- Flow combination/filtering;
- retry classification and idempotency logic.

### Device/instrumented tests

- DAO queries against device SQLite;
- Room migration from every supported schema path;
- Compose screen semantics and important interactions;
- deep-link launch to a destination;
- WorkManager worker with controlled dependencies where appropriate.

### Critical journey

```text
launch offline → create entry → see pending → restore network → sync →
notification → open detail
```

Keep this small and deterministic. Do not make every assertion through the UI.

## Compose test discipline

Query by semantics visible to users, not fragile tree position. Give custom components
meaningful semantics/test tags only when user semantics cannot identify them. Control
time for animations/debounce rather than sleeping.

## Test-double vocabulary

- fake: working lightweight implementation, often preferred.
- stub: returns configured answers.
- spy: records interactions.
- mock: expectation-driven interaction object.

Use the simplest double that proves behavior. Excess mocking couples tests to wiring.

## Failure experiment

Write one UI test that verifies repository internals through multiple mocks. Then write
a focused ViewModel/repository test plus one user-visible UI assertion. Compare speed,
failure messages, and refactor resistance; keep the better split.

## Exit gate

1. Local versus instrumented test?
2. Why test Room on device?
3. What must a migration test preserve?
4. Fake versus mock?
5. Why query Compose by semantics?
6. What belongs in the one critical end-to-end journey?

Interview check: given a production bug in offline sync, propose the smallest regression
test that would fail before the fix and explain its layer.

Hints: start from risks; keep time/network/database controlled; every production bug
should leave a focused regression test when feasible.

---

# Day 25 — Security and Privacy Boundaries

## Goal

Threat-model FieldLog and reduce attack surface, data exposure, and misplaced trust.

## Trust boundaries

Treat these as untrusted until validated:

- Intent/deep-link extras and URIs;
- network responses even over TLS;
- files and content-provider results;
- user input;
- data restored from older app versions;
- values from other apps or exported components.

Client-side checks improve UX but do not replace server authorization.

## Threat-model exercise

For each asset—auth token, private note, photo, location-like metadata, database,
logs—write:

```text
asset → attacker → entry point → impact → mitigation → verification
```

Prioritize plausible high-impact threats rather than adding random cryptography.

## Security audit lab

1. Inspect the merged manifest: every exported component must be justified.
2. Remove unnecessary permissions.
3. Validate all external route/action/MIME/size inputs.
4. Require HTTPS and review Network Security Configuration.
5. Ensure release/debug trust and logging policies differ safely.
6. Search logs, crash metadata, backups, screenshots, and notifications for sensitive data.
7. Confirm no API secret is hardcoded in the APK; public mobile clients cannot safely
   keep a shared backend secret.
8. Store cryptographic key material with Android Keystore when encryption is actually
   required; do not invent crypto primitives.
9. Review backup/data-extraction rules for private data.
10. Dependency-scan and update through a controlled, tested process.

## Authentication storage reasoning

Minimize token lifetime and scope, protect refresh capability, clear on logout, and
design server revocation. Keystore can protect local key material; it cannot make an
embedded server secret unknowable to a determined device owner.

Certificate pinning adds operational failure modes and rotation requirements. Use it
only from a real threat model with a recovery strategy, not as a checkbox.

## Failure experiments

- Send a malformed deep link with oversized/unknown ID and verify safe rejection.
- Build a release artifact and inspect strings/resources for a deliberately fake marker.
- Enable verbose HTTP bodies, observe exposure, then enforce redacted debug-only logs.

## Exit gate

1. Why is an exported component a security boundary?
2. Why can't an APK keep a universal API secret?
3. What does Keystore protect?
4. Authentication versus authorization?
5. Why validate TLS-protected server data?
6. What is the operational cost of certificate pinning?
7. Which FieldLog data is excluded from backup/logs/notifications?

Interview check: threat-model a deep link that opens a private entry, including route
validation, authentication, authorization, task state, logs, and screenshots.

Hints: minimize before encrypting; validate at every boundary; prove mitigations with
tests/config inspection rather than policy prose alone.

## Book 5 reference shelf

- https://developer.android.com/guide/components/fundamentals
- https://developer.android.com/guide/components/intents-filters
- https://developer.android.com/develop/ui/compose/migrate/interoperability-apis
- https://developer.android.com/develop/adaptive-apps
- https://developer.android.com/develop/ui/compose/accessibility
- https://developer.android.com/guide/topics/resources/localization
- https://developer.android.com/training/testing/fundamentals/strategies
- https://developer.android.com/develop/ui/compose/testing
- https://developer.android.com/privacy-and-security/security-best-practices
- https://developer.android.com/privacy-and-security/security-config
