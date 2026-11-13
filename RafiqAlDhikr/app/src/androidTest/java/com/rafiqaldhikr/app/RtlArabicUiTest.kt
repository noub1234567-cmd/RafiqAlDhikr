package com.rafiqaldhikr.app

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafiqaldhikr.app.core.model.ThemeMode
import com.rafiqaldhikr.app.ui.theme.RafiqAlDhikrTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RtlArabicUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun themeEnforcesRtlInBothLightAndDarkModes() {
        var capturedDirection: LayoutDirection? = null
        composeRule.setContent {
            RafiqAlDhikrTheme(themeMode = ThemeMode.DARK) {
                capturedDirection = LocalLayoutDirection.current
            }
        }
        composeRule.waitForIdle()
        assertEquals(LayoutDirection.Rtl, capturedDirection)
    }
}
