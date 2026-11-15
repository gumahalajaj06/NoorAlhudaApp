package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_name_is_noor_alhuda() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("نور الهدى", appName)
    }

    @Test
    fun home_and_navigation_tabs_work_end_to_end() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("مرحباً بك في نور الهدى").assertIsDisplayed()
        composeRule.onNodeWithTag("home_prayer_card").assertIsDisplayed()

        // Navigate to Tasbeeh via bottom nav and increment counter
        composeRule.onNodeWithTag("nav_tasbeeh").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tasbeeh_tap_circle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tasbeeh_tap_circle").assertIsDisplayed()
        composeRule.onNodeWithTag("tasbeeh_count_text", useUnmergedTree = true).assertTextEquals("1")

        // Navigate to Quran Index
        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("quran_search_input").assertIsDisplayed()

        // Navigate to Azkar and verify font picker opens, updates preview, and persists selection
        composeRule.onNodeWithTag("nav_azkar").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("الأذكار اليومية").assertIsDisplayed()
        composeRule.onNodeWithTag("azkar_font_picker_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("font_option_scheherazade_new").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("معاينة حية — شهرزاد الجديد").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("font_live_preview_verse").assertIsDisplayed()

        val app = ApplicationProvider.getApplicationContext<NoorApplication>()
        val savedAfterAzkar = runBlocking(Dispatchers.IO) {
            app.container.preferencesManager.preferencesFlow.first().selectedArabicFont
        }
        assertEquals("scheherazade_new", savedAfterAzkar)

        // Close sheet and navigate to More -> Settings and select Noto Naskh Arabic
        composeRule.onNodeWithTag("font_sheet_close_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_more").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_list").performScrollToNode(hasTestTag("more_settings"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_settings").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("font_option_noto_naskh"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("font_option_noto_naskh").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("معاينة حية — نوتو نسخ عربي").fetchSemanticsNodes().isNotEmpty()
        }
        val savedAfterSettings = runBlocking(Dispatchers.IO) {
            app.container.preferencesManager.preferencesFlow.first().selectedArabicFont
        }
        assertEquals("noto_naskh", savedAfterSettings)

        // Navigate back to More & About Developer
        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_list").performScrollToNode(hasTestTag("more_about"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_about").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("جمعة العجاج").assertIsDisplayed()
    }
}
