package com.sotopatrick.onjoue.presentation.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import com.sotopatrick.onjoue.App
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.activity.AgeMonths
import com.sotopatrick.onjoue.data.models.activity.AgeRange
import com.sotopatrick.onjoue.data.models.activity.DurationMinutes
import com.sotopatrick.onjoue.data.models.favorite.Favorite
import com.sotopatrick.onjoue.data.models.favorite.FavoriteId
import com.sotopatrick.onjoue.data.repositories.sampleActivity
import com.sotopatrick.onjoue.designsystem.OnJoueTheme
import com.sotopatrick.onjoue.presentation.activity.favorites.IdeaFavoritesState
import com.sotopatrick.onjoue.presentation.activity.listing.ActivitySheetsScreen
import com.sotopatrick.onjoue.presentation.activity.listing.ActivitySheetsState
import com.sotopatrick.onjoue.presentation.activity.listing.ActivitySheetsViewModel
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.errors.Message
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

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
        onNodeWithText("Réessayer").assertIsDisplayed()
    }

    @Test
    fun noResultsCanResetAllFiltersViaBottomSheet() = runComposeUiTest {
        val short = activity.copy(age = AgeRange(AgeMonths(72), AgeMonths(96)), duration = DurationMinutes(20))
        setContent { OnJoueTheme { ActivitySheetsScreen(state.copy(activities = listOf(short)), onRetry = {}) } }
        val list = onNodeWithTag("ideas-list")
        list.performScrollToNode(hasText("Tous âges ▾"))
        onNodeWithText("Tous âges ▾").performClick()
        onNodeWithText("1–3 ans").performClick()
        onNodeWithText("Lieu ▾").performClick()
        onNodeWithText("Dedans").performClick()
        onNodeWithText("≤ 15 min").performClick()
        list.performScrollToNode(hasText("Aucune idée pour cette recherche"))
        onNodeWithText("Aucune idée pour cette recherche").assertIsDisplayed()
        onNodeWithContentDescription("Filtres avancés").performClick()
        onNodeWithText("Réinitialiser").performClick()
        list.performScrollToNode(hasText("Tous âges ▾"))
        onNodeWithText("Tous âges ▾").assertIsDisplayed()
        onNodeWithText("Lieu ▾").assertIsDisplayed()
        list.performScrollToNode(hasText(activity.name))
        onNodeWithText(activity.name).assertIsDisplayed()
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
    fun switchingTabsPreservesRenamedNavigation() = runComposeUiTest {
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
        onNodeWithTag("navigation-FAVORITES").performClick()
        onNodeWithTag("navigation-ACTIVITY_SHEETS").performClick()
        onNodeWithText("Des idées à partager").assertExists()
    }
}
