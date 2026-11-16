package com.sotopatrick.onjoue.presentation.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import com.sotopatrick.onjoue.App
import com.sotopatrick.onjoue.data.models.activity.*
import com.sotopatrick.onjoue.data.models.favorite.*
import com.sotopatrick.onjoue.data.repositories.sampleActivity
import com.sotopatrick.onjoue.designsystem.OnJoueTheme
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.errors.Message
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ActivityIdeasScreenTest {
    private val activity = sampleActivity()
    private val locations = IdeaTestTags().rows.value
    private val state = ActivitySheetsState(
        activities = listOf(activity),
        loading = false,
        locations = locations,
        favorites = IdeaFavoritesState(ready = true),
    )

    @Test
    fun loadingIsAnExplicitScreen() = runComposeUiTest {
        setContent { OnJoueTheme { ActivitySheetsScreen(ActivitySheetsState(), onRetry = {}) } }
        onNodeWithText("Les idées arrivent…").assertIsDisplayed()
        onNodeWithText("Réessayer").assertDoesNotExist()
    }

    @Test
    fun errorOffersRetryInsteadOfEmptyCatalogMessage() = runComposeUiTest {
        var retries = 0
        setContent {
            OnJoueTheme {
                ActivitySheetsScreen(
                    ActivitySheetsState(loading = false, error = Message("Impossible de charger les idées.")),
                    onRetry = { retries++ },
                )
            }
        }
        onNodeWithText("Les idées n’ont pas pu arriver").assertIsDisplayed()
        onNodeWithText("Réessayer").performClick()
        assertEquals(1, retries)
        onNodeWithText("Les idées se préparent").assertDoesNotExist()
    }

    @Test
    fun emptyCatalogDoesNotSuggestRemovingNonexistentFilters() = runComposeUiTest {
        setContent {
            OnJoueTheme { ActivitySheetsScreen(ActivitySheetsState(loading = false), onRetry = {}) }
        }
        onNodeWithText("Les idées se préparent").assertIsDisplayed()
        onNodeWithText("Retirer les filtres").assertDoesNotExist()
        onNodeWithText("Réessayer").assertIsDisplayed()
    }

    @Test
    fun noResultsCanResetQueryAndAllQuickFilters() = runComposeUiTest {
        val short = activity.copy(age = AgeRange(AgeMonths(72), AgeMonths(96)), duration = DurationMinutes(20))
        setContent { OnJoueTheme { ActivitySheetsScreen(state.copy(activities = listOf(short)), onRetry = {}) } }
        val list = onNodeWithTag("ideas-list")
        list.performScrollToNode(hasText("1–3 ans"))
        onNodeWithText("1–3 ans").performClick()
        onNodeWithText("Dedans").performClick()
        onNodeWithText("15 min ou moins").performClick()
        list.performScrollToNode(hasText("Rechercher une idée"))
        onNodeWithText("Rechercher une idée").performTextInput("introuvable")
        list.performScrollToNode(hasText("Aucune idée pour cette recherche"))
        onNodeWithText("Aucune idée pour cette recherche").assertIsDisplayed()
        list.performScrollToNode(hasText("Retirer les filtres"))
        onNodeWithText("Retirer les filtres").performClick()
        list.performScrollToNode(hasText("Tous les âges"))
        onNodeWithText("Tous les âges").assertIsSelected()
        onNodeWithText("Dedans").assertIsNotSelected()
        onNodeWithText("15 min ou moins").assertIsNotSelected()
        list.performScrollToNode(hasText(activity.name))
        onNodeWithText(activity.name).assertIsDisplayed()
        onNodeWithText("Retirer les filtres").assertDoesNotExist()
    }

    @Test
    fun quickLocationsAreExclusiveAndRestoreWithOtherCriteria() = runComposeUiTest {
        val restoration = StateRestorationTester(this)
        restoration.setContent { OnJoueTheme { ActivitySheetsScreen(state, onRetry = {}) } }
        val list = onNodeWithTag("ideas-list")
        list.performScrollToNode(hasText("Dedans"))
        onNodeWithText("Dedans").performClick()
        onNodeWithText("Dehors").performClick()
        onNodeWithText("Dedans").assertIsNotSelected()
        onNodeWithText("3–5 ans").performClick()
        onNodeWithText("15 min ou moins").performClick()
        restoration.emulateSaveAndRestore()
        list.performScrollToNode(hasText("Dehors"))
        onNodeWithText("Dehors").assertIsSelected()
        onNodeWithText("3–5 ans").assertIsSelected()
        onNodeWithText("15 min ou moins").assertIsSelected()
    }

    @Test
    fun favoriteClickDoesNotOpenTheDetailAndShowsSelectedState() = runComposeUiTest {
        var selected = 0
        var toggles = 0
        var current by mutableStateOf(state)
        setContent {
            OnJoueTheme {
                ActivitySheetsScreen(
                    current, onRetry = {},
                    onSelectActivity = { selected++ },
                    onToggleFavorite = {
                        toggles++
                        current = current.copy(favorites = IdeaFavoritesState(
                            ready = true,
                            entries = mapOf(activity.id to Favorite(FavoriteId("favorite"), activity.id)),
                        ))
                    },
                )
            }
        }
        onNodeWithTag("ideas-list").performScrollToNode(hasContentDescription("Ajouter aux favoris"))
        onNodeWithContentDescription("Ajouter aux favoris").performClick()
        assertEquals(1, toggles)
        assertEquals(0, selected)
        onNodeWithContentDescription("Retirer des favoris").assertIsOn()
        onNodeWithText(activity.name).performClick()
        assertEquals(1, selected)
    }

    @Test
    fun scrollPositionSurvivesRestoration() = runComposeUiTest {
        val rows = (1..40).map { activity.copy(id = ActivityId("idea-$it"), name = "Idée $it") }
        val restoration = StateRestorationTester(this)
        restoration.setContent {
            OnJoueTheme { ActivitySheetsScreen(state.copy(activities = rows), onRetry = {}) }
        }
        onNodeWithTag("ideas-list").performScrollToNode(hasText("Idée 24"))
        onNodeWithText("Idée 24").assertIsDisplayed()
        restoration.emulateSaveAndRestore()
        onNodeWithText("Idée 24").assertIsDisplayed()
    }

    @Test
    fun switchingTabsPreservesSearchAndRenamedNavigation() = runComposeUiTest {
        val activities = IdeaTestActivities()
        setContent {
            App(
                darkTheme = false,
                createActivitySheets = {
                    ActivitySheetsViewModel(activities, FrenchErrorParser(), IdeaTestFavorites(), IdeaTestTags())
                },
            )
        }
        onNodeWithText("Aujourd’hui", useUnmergedTree = true).assertExists()
        onNodeWithTag("navigation-ACTIVITY_SHEETS").performClick()
        onNodeWithText("Idées", useUnmergedTree = true).assertExists()
        onNodeWithText("Rechercher une idée").performTextInput("introuvable")
        onNodeWithTag("navigation-FAVORITES").performClick()
        onNodeWithTag("navigation-ACTIVITY_SHEETS").performClick()
        onNodeWithText("introuvable").assertExists()
        onNodeWithTag("ideas-list").performScrollToNode(hasText("Aucune idée pour cette recherche"))
        onNodeWithText("Aucune idée pour cette recherche").assertIsDisplayed()
    }
}
