# Native Android Learning Log

Current runnable reference: **Day 5 — State and Recomposition**.

Keep observations here, not copied solutions. Duplicate the session template for each
attempt. Use `PASS`, `FAIL`, `BLOCKED`, or `NOT RUN` exactly so the verification boundary
remains honest.

## Session template

### Day __ — Topic

- Date:
- Mode: read-first / build-first
- Starting checkpoint:
- Goal in my own words:
- Flutter concept I am using as a bridge:
- Android behavior that is not equivalent:

#### Code-reading trail

- Entry symbol:
- Symbols followed in order:
- State owner:
- Platform/lifecycle boundary:
- Nearest executable test:
- My prediction before opening the implementation:

#### Baseline

Command:

```bash
docker build -t native-android-lab .
docker run --rm native-android-lab
```

Configured-host equivalent:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
```

- Result:
- First failure, if any:

#### One-change experiment

- Change made:
- Prediction:
- Actual result:
- First useful compiler/runtime/test message:
- Root cause:
- Smallest correction:

#### Controlled failure

- Deliberate fault:
- Narrow command or interaction used:
- Observed failure:
- Explanation before restoring:
- Restoration command:
- Restored result:

#### What I can now explain

- Native concept in two sentences:
- Ownership/lifetime rule:
- One tradeoff:
- Interview answer I can defend:
- Remaining question:
- Next checkpoint:

## Day 5 reading prompts

Use these for the current reference without inventing observations:

- Trace `MainActivity.onCreate → setContent → NativeAndroidLabTheme → DeveloperCard`.
- Identify why `PracticeExperiment` owns `practiceSessions` today.
- Compare `rememberSaveable` with Flutter local state and restoration APIs.
- Follow the button events, state writes, and counter read inside `PracticeExperiment`.
- Explain why future ViewModel, StateFlow, Room, and Hilt code is intentionally absent.
