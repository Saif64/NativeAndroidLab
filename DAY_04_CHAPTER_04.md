# Day 4 — Chapter 4: Build a Compose Layout

Today you will replace the generated greeting with a small developer profile card.
It will be static: no changing state yet.

## The Compose mental model

A composable does not create and return a persistent widget object. It describes
what the UI should look like for its current inputs.

```text
UI = composable(inputs)
```

Flutter is also declarative, so the idea is familiar. The syntax and platform rules
are different.

## The layout vocabulary

| Compose | Flutter approximation | Purpose |
|---|---|---|
| `Column` | `Column` | Place children vertically. |
| `Row` | `Row` | Place children horizontally. |
| `Box` | `Stack` | Place or align children in the same space. |
| `Text` | `Text` | Display text. |
| `Button` | `ElevatedButton` | Material button. |
| `Spacer` | `SizedBox` | Deliberate empty space. |
| `Modifier` | No single equivalent | Size, padding, input, drawing, semantics, and more. |

Compose functions usually start with uppercase names because they describe UI, even
though they are functions rather than classes.

## `Modifier` is a pipeline

```kotlin
Modifier
    .fillMaxWidth()
    .padding(16.dp)
```

Each call wraps or transforms the result before it. Order can change layout and
drawing behavior. Do not treat Modifier as Flutter's `BuildContext`—they are
unrelated.

- `dp` is density-independent layout size.
- `sp` is scale-independent text size.
- Prefer `MaterialTheme.typography` before hardcoding text sizes.

## Your screen contract

Create this function in `MainActivity.kt`:

```kotlin
@Composable
fun DeveloperCard(
    name: String,
    experienceYears: Int,
    modifier: Modifier = Modifier
)
```

This is only the function contract. You must write its body.

Make the screen satisfy all requirements:

1. A `Column` contains the content.
2. The outer modifier fills available space and adds comfortable padding.
3. The developer name uses a headline style from `MaterialTheme.typography`.
4. A second `Text` displays `"4 years of Flutter experience"` using the parameter.
5. A `Row` displays three skill labels: Flutter, Kotlin, and Compose.
6. A `Button` says `Continue learning`.
7. Clicking the button does nothing yet: use an empty `onClick` lambda.
8. `MainActivity` calls `DeveloperCard` instead of `Greeting`.

Use Android Studio autocomplete. When a symbol is red, place the cursor on it and
use the import quick-fix. Do not manually guess full import paths.

## Work in thin slices

Use this order and run or preview after every slice:

1. Empty `Column`.
2. Name `Text`.
3. Experience `Text`.
4. Skills `Row`.
5. Button.
6. Spacing and typography.

If a step fails, undo only that step. Do not rewrite the whole function.

## Build a Preview

Change the generated preview so it calls `DeveloperCard` with sample arguments.
The preview should be wrapped in `NativeAndroidLabTheme`.

The preview is an editor rendering aid. The emulator runs the actual Activity. Both
should ultimately show the same composable, but they enter it through different
paths.

## Two experiments

### Experiment A: modifier order

On one small element, compare:

```text
padding → background
background → padding
```

Predict whether the padded region receives the background, then observe. Restore
the version you prefer.

### Experiment B: narrow width

Resize the Preview or rotate the emulator. Observe what happens to the three skills.
Do not solve responsiveness yet; simply record the weakness.

## Exit questions

1. What does a composable function describe?
2. When would you choose `Column`, `Row`, or `Box`?
3. Why can Modifier order change the result?
4. Why accept `modifier: Modifier = Modifier` as a parameter?
5. What is the difference between Preview and running the Activity?
6. What layout weakness appeared at narrow width?

## Hint ladder

- **Hint 1:** Start with the parent layout, then add one child at a time.
- **Hint 2:** Type the component name and let Android Studio offer the import.
- **Hint 3:** If content touches screen edges, inspect the modifier passed to the root layout.
- **Hint 4:** If the Preview cannot render, first confirm it has `@Preview` and `@Composable`.

Stop when the screen runs, the Preview renders, and you can explain every composable
you added. Visual polish is not today's goal.

Official reference:

- https://developer.android.com/develop/ui/compose/mental-model

