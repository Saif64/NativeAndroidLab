# Native Android: Flutter Engineer to Advanced

This is the course home. Keep this file open when choosing the next chapter.

The chapters are finished; the learning is not. Reading a chapter does not complete
it. You advance only after building its lab, recording the experiment, and answering
its exit gate without copying a complete solution.

## Course contract

- You write and debug the application code.
- The book supplies mental models, contracts, failure experiments, hints, and checks.
- Ask for `hint 1`, then `hint 2`, before asking for a complete solution.
- Commit or otherwise preserve a working checkpoint before each destructive experiment.
- Do not add an abstraction or dependency before its chapter creates a real need.
- A “day” means one focused learning session. Advanced chapters may take several days.

## The cumulative app: FieldLog

From Day 8 onward, `NativeAndroidLab` becomes **FieldLog**, an offline-first field
inspection and notes app. By graduation it will have:

- entry list, detail, create, and edit destinations;
- local Room data as the source of truth;
- a remote data source with explicit DTO mapping and failure handling;
- an offline write queue drained by unique WorkManager work;
- Paging only where the dataset justifies it;
- photo selection, notifications, and deep links;
- adaptive list-detail UI, localization, and accessibility;
- unit, database, migration, ViewModel, and Compose UI tests;
- measured startup/scroll performance and a Baseline Profile;
- build variants, R8 verification, CI gates, and a release bundle.

## Dependency version checkpoint

This book intentionally does not freeze third-party versions. When a chapter adds a
library:

1. Open that library's official Android documentation or release page.
2. Select a stable version compatible with this project's Kotlin, AGP, and SDK.
3. Add the version and alias to `gradle/libs.versions.toml`.
4. Reference the alias from `app/build.gradle.kts`.
5. Sync, compile, and commit that dependency-only change before using the library.

Never use `+`, `latest.release`, or a version copied blindly from an old article.

## Daily ritual

```text
Predict → make one change → build/test → observe → explain → checkpoint
```

Keep a short `LEARNING_LOG.md` in the project root. For every chapter record:

- prediction;
- actual result;
- first useful error;
- root cause;
- final explanation in your own words;
- one interview answer you can now defend.

## Chapter map

### Book 1 — Orientation and first UI

1. [Know the Android Project](DAY_01_CHAPTER_01.md)
2. [Follow App Startup](DAY_02_CHAPTER_02.md)
3. [Kotlin Survival Kit](DAY_03_CHAPTER_03.md)
4. [Build a Compose Layout](DAY_04_CHAPTER_04.md)
5. [State and Recomposition](DAY_05_CHAPTER_05.md)

### Book 2 — Android and UI foundations

6. [Lifecycle, configuration, and process death](VOLUME_02_FOUNDATIONS_DAYS_06_10.md#day-6--lifecycle-configuration-and-process-death)
7. [State hoisting and unidirectional data flow](VOLUME_02_FOUNDATIONS_DAYS_06_10.md#day-7--state-hoisting-and-unidirectional-data-flow)
8. [FieldLog lists and forms](VOLUME_02_FOUNDATIONS_DAYS_06_10.md#day-8--fieldlog-lists-and-forms)
9. [Navigation and the back stack](VOLUME_02_FOUNDATIONS_DAYS_06_10.md#day-9--navigation-and-the-back-stack)
10. [ViewModel and saved state](VOLUME_02_FOUNDATIONS_DAYS_06_10.md#day-10--viewmodel-and-saved-state)

### Book 3 — Asynchrony, architecture, and data

11. [Coroutines and structured concurrency](VOLUME_03_ASYNC_DATA_DAYS_11_15.md#day-11--coroutines-and-structured-concurrency)
12. [Flow, StateFlow, and stream thinking](VOLUME_03_ASYNC_DATA_DAYS_11_15.md#day-12--flow-stateflow-and-stream-thinking)
13. [Layered architecture and repositories](VOLUME_03_ASYNC_DATA_DAYS_11_15.md#day-13--layered-architecture-and-repositories)
14. [Room, schema evolution, and migration tests](VOLUME_03_ASYNC_DATA_DAYS_11_15.md#day-14--room-schema-evolution-and-migration-tests)
15. [Networking and failure modeling](VOLUME_03_ASYNC_DATA_DAYS_11_15.md#day-15--networking-and-failure-modeling)

### Book 4 — Production data systems

16. [Dependency injection with Hilt](VOLUME_04_PRODUCTION_SYSTEMS_DAYS_16_20.md#day-16--dependency-injection-with-hilt)
17. [Reliable background work](VOLUME_04_PRODUCTION_SYSTEMS_DAYS_16_20.md#day-17--reliable-background-work)
18. [Offline-first synchronization](VOLUME_04_PRODUCTION_SYSTEMS_DAYS_16_20.md#day-18--offline-first-synchronization)
19. [Paging large datasets](VOLUME_04_PRODUCTION_SYSTEMS_DAYS_16_20.md#day-19--paging-large-datasets)
20. [Permissions, media, notifications, and deep links](VOLUME_04_PRODUCTION_SYSTEMS_DAYS_16_20.md#day-20--permissions-media-notifications-and-deep-links)

### Book 5 — Platform and quality

21. [Android components, intents, tasks, and processes](VOLUME_05_PLATFORM_QUALITY_DAYS_21_25.md#day-21--android-components-intents-tasks-and-processes)
22. [Views, XML, Fragments, and Compose interoperability](VOLUME_05_PLATFORM_QUALITY_DAYS_21_25.md#day-22--views-xml-fragments-and-compose-interoperability)
23. [Adaptive UI, accessibility, and localization](VOLUME_05_PLATFORM_QUALITY_DAYS_21_25.md#day-23--adaptive-ui-accessibility-and-localization)
24. [A production testing strategy](VOLUME_05_PLATFORM_QUALITY_DAYS_21_25.md#day-24--a-production-testing-strategy)
25. [Security and privacy boundaries](VOLUME_05_PLATFORM_QUALITY_DAYS_21_25.md#day-25--security-and-privacy-boundaries)

### Book 6 — Advanced engineering and interviews

26. [Advanced Compose and effect discipline](VOLUME_06_ADVANCED_INTERVIEWS_DAYS_26_30.md#day-26--advanced-compose-and-effect-discipline)
27. [Performance, profiling, and Baseline Profiles](VOLUME_06_ADVANCED_INTERVIEWS_DAYS_26_30.md#day-27--performance-profiling-and-baseline-profiles)
28. [Gradle, variants, and modularization](VOLUME_06_ADVANCED_INTERVIEWS_DAYS_26_30.md#day-28--gradle-variants-and-modularization)
29. [Release engineering, R8, signing, and CI](VOLUME_06_ADVANCED_INTERVIEWS_DAYS_26_30.md#day-29--release-engineering-r8-signing-and-ci)
30. [Graduation, system design, and interview defense](VOLUME_06_ADVANCED_INTERVIEWS_DAYS_26_30.md#day-30--graduation-system-design-and-interview-defense)

### Required companion

- [Appendix A — Kotlin and JVM Interview Depth](APPENDIX_A_KOTLIN_JVM_DEPTH.md) — complete after Day 12, then revisit before Day 30.

## What “advanced” means here

At the end, you should be able to:

- predict lifecycle and process behavior instead of patching symptoms;
- model UI as immutable state and events with clear ownership;
- keep UI, data, and platform responsibilities separated;
- design cancellation-safe, offline-capable data flows;
- choose between coroutine, WorkManager, service, alarm, and broadcast use cases;
- preserve user data through database evolution and release optimization;
- test at the cheapest layer that still gives enough confidence;
- measure performance before optimizing;
- explain security, architecture, and tradeoffs under interview pressure;
- read an XML/Fragment codebase without making it the architecture of a new app.

That is advanced readiness. Merely finishing all Markdown files is not.
