package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.room.Room
import androidx.navigation3.runtime.NavKey
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.sotopatrick.onjoue.App
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.management.ManagementViewModel
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppNavigationTest {
    @Test
    fun releaseHasFourTabsButNoManagement() = runComposeUiTest {
        setContent { App(darkTheme = false) }
        onNodeWithTag("navigation-IDEA").assertIsSelected()
        onNodeWithTag("navigation-ACTIVITY_SHEETS").performClick().assertIsSelected()
        onNodeWithText("Fonctionnalité non disponible pour le moment.").assertIsDisplayed()
        onNodeWithTag("navigation-FAVORITES").performClick().assertIsSelected()
        onNodeWithTag("navigation-SETTINGS").performClick().assertIsSelected()
        onNodeWithText("Aucun paramètre disponible pour le moment.").assertIsDisplayed()
        onNodeWithText("Gérer les tags").assertDoesNotExist()
        onNodeWithText("Gérer les matériels").assertDoesNotExist()
        onNodeWithText("Gérer les activités").assertDoesNotExist()
    }

    @Test
    fun restoresSelectedTabAndManagementStackAndRemovesItInRelease() = runComposeUiTest {
        val enabled = mutableStateOf(true)
        lateinit var state: AppNavigationState
        val restoration = StateRestorationTester(this)
        restoration.setContent {
            val remembered = rememberAppNavigationState(enabled.value)
            SideEffect { state = remembered }
        }
        runOnIdle { AppNavigator(state, true).openManagement(AppRoute.EquipmentManagement) }
        restoration.emulateSaveAndRestore()
        runOnIdle {
            assertEquals(TopLevelDestination.SETTINGS, state.selectedDestination)
            assertEquals<List<NavKey>>(listOf(AppRoute.Settings, AppRoute.EquipmentManagement), state.currentStack)
            enabled.value = false
        }
        waitForIdle()
        runOnIdle { assertEquals<List<NavKey>>(listOf(AppRoute.Settings), state.currentStack) }
    }

    @Test
    fun managementOpensTheRequestedCategoryAndKeepsDraftAcrossTabsAndRestoration() {
        val database = Room.inMemoryDatabaseBuilder<AppDatabase>(RuntimeEnvironment.getApplication())
            .setDriver(AndroidSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()
        try {
            runComposeUiTest {
                val restoration = StateRestorationTester(this)
                restoration.setContent {
                    App(
                        createManagement = {
                            ManagementViewModel(
                                RoomTagRepository(database, debug = true),
                                RoomEquipmentRepository(database, debug = true),
                                FrenchErrorParser(),
                            )
                        },
                        darkTheme = false,
                    )
                }
                onNodeWithTag("navigation-SETTINGS").performClick()
                onNodeWithText("Gérer les matériels").performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(androidx.compose.ui.test.hasContentDescription("Créer un matériel") and androidx.compose.ui.test.isEnabled())
                        .fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithContentDescription("Créer un tag").assertDoesNotExist()
                onNodeWithContentDescription("Créer un matériel").performClick()
                onNodeWithText("Libellé").performTextInput("Ballon")
                restoration.emulateSaveAndRestore()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(androidx.compose.ui.test.hasText("Ballon")).fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Ballon").assertIsDisplayed()
                onNodeWithText("Enregistrer").performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Libellé")).fetchSemanticsNodes().isEmpty()
                }
                onNodeWithText("Ballon").assertIsDisplayed()
                onNodeWithText("0 activité").assertIsDisplayed()
                onNodeWithText("Rechercher un matériel").performTextInput("ball")
                onNodeWithTag("navigation-FAVORITES").performClick()
                onNodeWithTag("navigation-SETTINGS").performClick()
                onNodeWithContentDescription("Créer un matériel").assertIsDisplayed()
                onNodeWithText("ball").assertIsDisplayed().performTextClearance()
                onNodeWithContentDescription("Modifier").performScrollTo().performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Libellé")).fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Libellé").performTextClearance()
                onNodeWithText("Libellé").performTextInput("Grand ballon")
                onNodeWithText("Enregistrer").performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Libellé")).fetchSemanticsNodes().isEmpty()
                }
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Grand ballon")).fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Grand ballon").performScrollTo().assertIsDisplayed()
                onNodeWithContentDescription("Supprimer").performScrollTo().performClick()
                onNode(hasText("Supprimer") and hasAnyAncestor(isDialog())).performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(hasText("Grand ballon")).fetchSemanticsNodes().isEmpty()
                }
                onNodeWithText("Aucun élément.").assertIsDisplayed()
                onNodeWithContentDescription("Retour").performClick()
                onNodeWithText("Gérer les activités").performClick()
                onNodeWithText("Fonctionnalité non disponible pour le moment.").assertIsDisplayed()
                onNodeWithText("Retour aux paramètres").performClick()
                onNodeWithText("Gérer les tags").performClick()
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodes(androidx.compose.ui.test.hasContentDescription("Créer un tag")).fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithContentDescription("Créer un tag").assertIsDisplayed()
            }
        } finally {
            database.close()
        }
    }
}
