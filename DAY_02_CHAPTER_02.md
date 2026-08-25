# Day 2 — Chapter 2: Follow App Startup

Today you will trace one path:

```text
launcher icon
    → AndroidManifest.xml
    → MainActivity
    → onCreate
    → setContent
    → NativeAndroidLabTheme
    → Greeting
    → Text
```

Do not try to memorize every keyword. Your goal is to explain how Android reaches
the text on the screen.

## 1. Android does not start at `main()`

Flutter begins from a Dart `main()` that calls `runApp()`. Android applications are
different: the Android system creates registered components and invokes their
lifecycle callbacks.

For this project, the first component is an **Activity**. An Activity owns a window
where the app can draw UI. The system knows about it because it is declared in:

```text
app/src/main/AndroidManifest.xml
```

Open the manifest and locate these three clues:

- `android:name=".MainActivity"` — the Activity class to create.
- `android.intent.action.MAIN` — this is a main entry point.
- `android.intent.category.LAUNCHER` — show it in the launcher.

The leading dot in `.MainActivity` is shorthand. Android combines it with the app's
namespace/package to find `com.saif.nativeandroidlab.MainActivity`.

## 2. Read `MainActivity` from the outside inward

Open:

```text
app/src/main/java/com/saif/nativeandroidlab/MainActivity.kt
```

Read only these landmarks:

| Code landmark | Meaning |
|---|---|
| `class MainActivity : ComponentActivity()` | Our class inherits Android Activity behavior. |
| `override fun onCreate(...)` | The system calls this when it creates the Activity. |
| `super.onCreate(...)` | Let the parent Activity perform its required setup. |
| `setContent { ... }` | Start a Compose UI inside this Activity. |
| `NativeAndroidLabTheme { ... }` | Supply the generated app theme to descendants. |
| `Scaffold { ... }` | Material screen structure and safe content area. |
| `Greeting(...)` | Our own composable function. |
| `Text(...)` | A Material composable that displays text. |

`savedInstanceState: Bundle?` means the parameter may contain previously saved
Activity state, or it may be `null`. The `?` is Kotlin's nullable marker. We will
study Kotlin properly tomorrow.

## 3. Your first trace

Use **Find Usages** or Command-click symbols where useful. Without editing, answer:

1. Which manifest line points to `MainActivity`?
2. Which callback is the first code in our class that Android invokes here?
3. Which call crosses from the Activity world into the Compose world?
4. Which function finally creates the visible text?

## 4. Your first tiny edit

Run the untouched app once and confirm it shows `Hello Android!`.

Then find the call that passes `"Android"` into `Greeting`. Change only that argument
to your name. Predict the result before pressing Run.

After it runs, answer:

- Did the launcher label change?
- Did the text inside the screen change?
- Why did only one of them change?

Now open `app/src/main/res/values/strings.xml`. Change only `app_name`, run again,
and observe where that resource is used. A Kotlin string literal and an Android
string resource are not the same thing.

## 5. One controlled mistake

Temporarily remove `@Composable` from `Greeting`, then build.

Do not panic at the red code. Read the first compiler message and write it down.
Restore `@Composable`, build again, and confirm the error disappears.

This teaches an important habit: compiler errors are navigation clues, not merely
failures.

## Flutter → Android bridge

| Flutter | Native Android here |
|---|---|
| `main()` | No exact equivalent; Android launches the registered Activity. |
| `runApp(...)` | Roughly the role of `setContent { ... }` for this Compose host. |
| root `MaterialApp` theme | `NativeAndroidLabTheme { ... }`. |
| a widget-building function | a function marked `@Composable`. |
| `Text('Hello')` | `Text(text = "Hello")`. |

## Exit questions

Answer before Day 3:

1. Why can Android launch `MainActivity` even though there is no `main()`?
2. What is the purpose of `onCreate`?
3. What does `setContent` establish?
4. What is the difference between `app_name` and the `"Android"` argument?
5. What happened when `@Composable` was removed?

## Hint ladder

- **Hint 1:** Follow declarations before implementations: manifest → class → callback → UI.
- **Hint 2:** For launcher behavior, inspect the `<activity>` and its `<intent-filter>`.
- **Hint 3:** For visible UI, start at `setContent` and keep entering nested functions.

Stop after you can narrate the startup chain without looking at the diagram.

Official references:

- https://developer.android.com/guide/components/activities/intro-activities
- https://developer.android.com/develop/ui/compose/mental-model
