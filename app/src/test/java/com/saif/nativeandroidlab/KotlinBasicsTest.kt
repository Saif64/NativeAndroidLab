package com.saif.nativeandroidlab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Day 3 Kotlin workbench. Each test isolates one Kotlin idea from the chapter. */
class KotlinBasicsTest {
    @Test
    fun stringInterpolation_buildsReadableSentence() {
        val name = "Saif"
        val age = 24

        assertEquals("Saif is 24 years old", "$name is $age years old")
    }

    @Test
    fun expressionFunction_returnsInterviewLabel() {
        assertEquals("Saif — 4 years", interviewLabel(name = "Saif", years = 4))
    }

    @Test
    fun nullableName_usesExplicitFallback() {
        assertEquals("Saif", displayName("Saif"))
        assertEquals("Anonymous", displayName(null))
    }

    @Test
    fun dataClassCopy_doesNotChangeOriginalValue() {
        val original = Developer(
            name = "Saif",
            skills = listOf("Flutter", "Kotlin"),
        )
        val nativeFocused = original.copy(name = "Saif · Native learner")

        assertEquals("Saif", original.name)
        assertEquals("Saif · Native learner", nativeFocused.name)
        assertNotEquals(original, nativeFocused)
    }

    @Test
    fun collectionPipeline_filtersThenTransforms() {
        val skills = listOf("Dart", "Kotlin", "Java", "Compose")

        val result = skills
            .filter { it == "Kotlin" || it == "Compose" }
            .map { it.uppercase() }

        assertEquals(listOf("KOTLIN", "COMPOSE"), result)
    }

    @Test
    fun whenExpression_handlesBoundaryYears() {
        assertEquals("Junior", seniority(years = 1))
        assertEquals("Mid", seniority(years = 2))
        assertEquals("Mid", seniority(years = 4))
        assertEquals("Senior", seniority(years = 5))
    }

    private fun interviewLabel(name: String, years: Int): String = "$name — $years years"

    private fun displayName(nickname: String?): String = nickname ?: "Anonymous"

    private fun seniority(years: Int): String = when {
        years < 2 -> "Junior"
        years < 5 -> "Mid"
        else -> "Senior"
    }
}

private data class Developer(
    val name: String,
    val skills: List<String>,
)
