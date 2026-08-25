package com.saif.nativeandroidlab

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.saif.nativeandroidlab.ui.theme.NativeAndroidLabTheme
import org.junit.Rule
import org.junit.Test

class DeveloperCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun completeAndReset_updatePracticeSessionState() {
        composeRule.setContent {
            NativeAndroidLabTheme {
                DeveloperCard(
                    name = "Saif",
                    experienceYears = 4,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Practice sessions: 0").assertExists()
        composeRule.onNodeWithText("Reset").assertIsNotEnabled()

        composeRule.onNodeWithText("Complete session").performClick()

        composeRule.onNodeWithContentDescription("Practice sessions: 1").assertExists()
        composeRule.onNodeWithText("Complete session").performClick()
        composeRule.onNodeWithContentDescription("Practice sessions: 2").assertExists()
        composeRule.onNodeWithText("Reset").assertIsEnabled().performClick()
        composeRule.onNodeWithContentDescription("Practice sessions: 0").assertExists()
    }

    @Test
    fun savedStateRestoration_preservesPracticeSessionState() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            NativeAndroidLabTheme {
                DeveloperCard(
                    name = "Saif",
                    experienceYears = 4,
                )
            }
        }

        composeRule.onNodeWithText("Complete session").performClick()
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithContentDescription("Practice sessions: 1").assertExists()
    }
}
