package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavKey
import com.sotopatrick.onjoue.App
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppNavigationTest {
    @Test
    fun settingsHasNoEditorialTools() = runComposeUiTest {
        setContent { App(darkTheme = false) }
        onNodeWithTag("navigation-IDEA").assertIsSelected()
        onNodeWithTag("navigation-SETTINGS").performClick().assertIsSelected()
        onNodeWithText("Aucun paramètre disponible pour le moment.").assertIsDisplayed()
        onNodeWithText("Gérer les tags").assertDoesNotExist()
        onNodeWithText("Gérer les matériels").assertDoesNotExist()
        onNodeWithText("Gérer les activités").assertDoesNotExist()
        onNodeWithText("Remplacer le catalogue").assertDoesNotExist()
    }

    @Test
    fun restoredManagementStackIsRemoved() = runComposeUiTest {
        lateinit var state: AppNavigationState
        val restoration = StateRestorationTester(this)
        restoration.setContent {
            val remembered = rememberAppNavigationState()
            SideEffect { state = remembered }
        }
        runOnIdle {
            state.selectedDestination = TopLevelDestination.SETTINGS
            state.currentStack.add(AppRoute.EquipmentManagement)
        }
        restoration.emulateSaveAndRestore()
        runOnIdle {
            assertEquals(TopLevelDestination.SETTINGS, state.selectedDestination)
            assertEquals<List<NavKey>>(listOf(AppRoute.Settings), state.currentStack)
        }
    }
}
