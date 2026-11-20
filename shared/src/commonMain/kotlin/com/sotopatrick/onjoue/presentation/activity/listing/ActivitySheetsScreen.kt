package com.sotopatrick.onjoue.presentation.activity.listing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.activity.AgeMonths
import com.sotopatrick.onjoue.data.models.activity.AgeRange
import com.sotopatrick.onjoue.data.models.activity.DurationMinutes
import com.sotopatrick.onjoue.data.models.favorite.Favorite
import com.sotopatrick.onjoue.data.models.favorite.FavoriteId
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.designsystem.components.BackgroundTone
import com.sotopatrick.onjoue.designsystem.components.DsBackground
import com.sotopatrick.onjoue.presentation.activity.favorites.IdeaFavoritesState
import com.sotopatrick.onjoue.presentation.common.ShowToast
import com.sotopatrick.onjoue.presentation.errors.Message
import androidx.compose.ui.tooling.preview.Preview
import com.sotopatrick.onjoue.presentation.activity.listing.components.IdeaListing

@Composable
fun ActivitySheetsScreen(
    state: ActivitySheetsState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectActivity: (String) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
    onRetryFavorites: () -> Unit = {},
    onClearNotice: () -> Unit = {},
) {
    state.favorites.notice?.let { notice ->
        ShowToast(notice.content)
        onClearNotice()
    }
    DsBackground(modifier.fillMaxSize(), tone = BackgroundTone.Screen) {
        if (state.activities.isEmpty()) {
            IdeaCatalogState(state, onRetry)
        } else {
            IdeaListing(state, onRetry, onSelectActivity, onToggleFavorite, onRetryFavorites)
        }
    }
}

@Preview(name = "Phone", widthDp = 390, heightDp = 844)
@Preview(name = "Small landscape", widthDp = 640, heightDp = 360)
@Preview(name = "Tablet", widthDp = 1024, heightDp = 768)
@Preview(name = "Large text", widthDp = 390, heightDp = 844, fontScale = 1.5f)
private annotation class PreviewListingSizes

@PreviewListingSizes
@Composable
private fun IdeasListingPreview() {
    IdeasPreviewContent(ideasPreviewState())
}

@Preview(name = "Dark", widthDp = 390, heightDp = 844)
@Composable
private fun DarkIdeasListingPreview() {
    IdeasPreviewContent(ideasPreviewState(), darkTheme = true)
}

@Preview(name = "Loading", widthDp = 390, heightDp = 844)
@Composable
private fun IdeasLoadingPreview() {
    IdeasPreviewContent(ActivitySheetsState())
}

@Preview(name = "Error", widthDp = 390, heightDp = 844)
@Composable
private fun IdeasErrorPreview() {
    IdeasPreviewContent(ActivitySheetsState(
        loading = false,
        error = Message("Impossible d’accéder aux données locales. Réessayez."),
    ))
}

@Preview(name = "Empty", widthDp = 390, heightDp = 844)
@Composable
private fun IdeasEmptyPreview() {
    IdeasPreviewContent(ActivitySheetsState(loading = false))
}

@Composable
private fun IdeasPreviewContent(state: ActivitySheetsState, darkTheme: Boolean = false) {
    com.sotopatrick.onjoue.designsystem.OnJoueTheme(darkTheme = darkTheme) {
        com.sotopatrick.onjoue.presentation.navigation.AppNavigationScaffold(
            com.sotopatrick.onjoue.presentation.navigation.TopLevelDestination.ACTIVITY_SHEETS,
            onSelect = {},
        ) {
            ActivitySheetsScreen(state, onRetry = {})
        }
    }
}

private fun ideasPreviewState(): ActivitySheetsState {
    val inside = Tag(TagId("inside"), TagGroup.LOCATION, "Dedans")
    val outside = Tag(TagId("outside"), TagGroup.LOCATION, "Dehors")
    val mood = Tag(TagId("active"), TagGroup.MOOD, "Se dépenser")
    val activities = listOf("La danse des animaux", "Le parcours des petits explorateurs", "Le ballon voyageur")
        .mapIndexed { index, name ->
            Activity(
                id = ActivityId("idea-$index"),
                name = name,
                description = "Un jeu à partager avec votre enfant.",
                age = AgeRange(AgeMonths(36), AgeMonths(60)),
                duration = DurationMinutes(15),
                tags = setOf(inside, mood),
            )
        }
    val favorite = Favorite(FavoriteId("favorite"), activities.first().id)
    return ActivitySheetsState(
        activities = activities,
        loading = false,
        locations = listOf(inside, outside),
        favorites = IdeaFavoritesState(entries = mapOf(favorite.activityId to favorite), ready = true),
    )
}
