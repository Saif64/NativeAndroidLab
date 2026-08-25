# Day 3 — Chapter 3: Kotlin Survival Kit for a Dart Developer

Today is not a complete Kotlin course. You will learn only enough Kotlin to read and
write the Android code needed next.

Work in the local test source set so experiments run quickly without an emulator:

```text
app/src/test/java/com/saif/nativeandroidlab/
```

Create a Kotlin file named `KotlinBasicsTest.kt`. Give it this empty test frame:

```kotlin
package com.saif.nativeandroidlab

import org.junit.Assert.assertEquals
import org.junit.Test

class KotlinBasicsTest {
    @Test
    fun kotlinSurvivalKit() {
        // Your experiments go here.
    }
}
```

This frame is a workbench, not a solution. Run the test from the green gutter icon
after every small exercise.

Place exercise functions as members of `KotlinBasicsTest`, below the test function.
Place the `Developer` data class below the test class. This keeps the workbench easy
to navigate while you are learning the syntax.

## 1. `val` and `var`

```kotlin
val name = "Saif"       // read-only reference
var years = 4           // can be reassigned
years += 1
```

Prefer `val`. Use `var` only when reassignment is genuinely required.

Flutter/Dart bridge:

| Dart | Kotlin |
|---|---|
| `final name = 'Saif'` | `val name = "Saif"` |
| `var years = 4` | `var years = 4` |
| `String name` | `val name: String` |

**Experiment:** declare a `val`, try reassigning it, read the compiler error, then
undo the reassignment.

## 2. Functions and expressions

Long form:

```kotlin
fun double(value: Int): Int {
    return value * 2
}
```

Expression form:

```kotlin
fun double(value: Int): Int = value * 2
```

**Exercise:** write `interviewLabel(name, years)` so this test passes:

```kotlin
assertEquals("Saif — 4 years", interviewLabel("Saif", 4))
```

Do not search for the complete function. Let autocomplete and the failing test guide
you.

## 3. Nullability is part of the type

```kotlin
val requiredName: String = "Saif"
val optionalNickname: String? = null
```

Important operators:

| Kotlin | Meaning |
|---|---|
| `value?.length` | Access only when `value` is non-null. |
| `value ?: fallback` | Use the fallback when `value` is null. Called Elvis. |
| `value!!` | Assert non-null and crash if wrong. Avoid it during this course. |

**Exercise:** write `displayName(nickname: String?): String` with these results:

```kotlin
assertEquals("Saif", displayName("Saif"))
assertEquals("Anonymous", displayName(null))
```

## 4. Data classes

A data class is useful for immutable values:

```kotlin
data class Developer(
    val name: String,
    val skills: List<String>
)
```

Kotlin generates useful value behavior such as `equals`, `hashCode`, `toString`,
component functions, and `copy`.

**Exercise:** create one developer, then use `copy` to create another with a changed
name. Prove with assertions that the first object did not change.

Flutter/Dart bridge: this is close to an immutable Dart model plus generated value
equality and `copyWith`, though Kotlin calls the generated method `copy`.

## 5. Collections and lambdas

```kotlin
val skills = listOf("Flutter", "Kotlin", "Compose")
val nativeSkills = skills.filter { it != "Flutter" }
val labels = nativeSkills.map { it.uppercase() }
```

- `listOf` returns a read-only list interface.
- `{ ... }` is a lambda.
- `it` is the implicit name for a single lambda parameter.

**Exercise:** starting from `listOf("Dart", "Kotlin", "Java", "Compose")`, produce
`listOf("KOTLIN", "COMPOSE")` using `filter` and `map`. Assert the result.

## 6. `when`

`when` is a more powerful Dart `switch` and can return a value:

```kotlin
fun seniority(years: Int): String = when {
    years < 2 -> "Junior"
    years < 5 -> "Mid"
    else -> "Senior"
}
```

**Exercise:** add assertions for boundary values `1`, `2`, `4`, and `5`. Decide
whether the rules above match your intended boundaries.

## Read yesterday's Activity again

Return to `MainActivity.kt` and translate these aloud:

- `class MainActivity : ComponentActivity()`
- `override fun onCreate(savedInstanceState: Bundle?)`
- `name: String`
- `modifier: Modifier = Modifier`
- `"Hello $name!"`

You should now recognize inheritance, a nullable parameter, explicit parameter
types, a default argument, and string interpolation.

## Exit questions

1. Why should `val` be your default?
2. What is the difference between `String` and `String?`?
3. What do `?.` and `?:` do?
4. Why is a data class convenient for UI models?
5. What is `it` inside a single-parameter lambda?
6. When can `when` replace an `if` chain?

## Hint ladder

- **Hint 1:** Make the compiler happy one type error at a time.
- **Hint 2:** For nullable values, first decide the fallback behavior.
- **Hint 3:** For collection exercises, inspect each intermediate result before chaining.

Stop when the local test passes and you can read the `Greeting` function without
translating every symbol into Dart.

Official references:

- https://kotlinlang.org/docs/basic-syntax.html
- https://kotlinlang.org/docs/null-safety.html
