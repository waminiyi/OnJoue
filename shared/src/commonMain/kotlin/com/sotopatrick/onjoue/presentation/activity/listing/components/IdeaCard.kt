package com.sotopatrick.onjoue.presentation.activity.listing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.designsystem.components.BadgeTone
import com.sotopatrick.onjoue.designsystem.components.CardElevation
import com.sotopatrick.onjoue.designsystem.components.DsBadge
import com.sotopatrick.onjoue.designsystem.components.DsCard
import com.sotopatrick.onjoue.designsystem.components.DsIconToggleButton
import com.sotopatrick.onjoue.designsystem.components.DsRemoteIllustration
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.IllustrationStyle
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.presentation.activity.common.ageRangeLabel
import com.sotopatrick.onjoue.presentation.activity.favorites.IdeaFavoritesState
import onjoue.shared.generated.resources.Res
import onjoue.shared.generated.resources.idea_image_placeholder
import onjoue.shared.generated.resources.ic_favorite_filled
import onjoue.shared.generated.resources.ic_favorites
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun IdeaCard(
    activity: Activity,
    favorites: IdeaFavoritesState,
    onSelectActivity: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val selected = activity.id in favorites.entries
    DsCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onSelectActivity(activity.id.id) },
        elevation = CardElevation.Low,
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            DsRemoteIllustration(
                imageUrl = activity.image?.value,
                fallbackPainter = painterResource(Res.drawable.idea_image_placeholder),
                contentDescription = null,
                style = IllustrationStyle.Plain,
                modifier = Modifier.fillMaxHeight(),
            )
            Column(
                Modifier.weight(1f).padding(Spacing.MediumPlus),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    DsText(activity.name, Modifier.weight(1f), role = TextRole.TitleMedium)
                    DsIconToggleButton(
                        checked = selected,
                        icon = painterResource(
                            if (selected) Res.drawable.ic_favorite_filled else Res.drawable.ic_favorites,
                        ),
                        contentDescription = if (selected) "Retirer des favoris" else "Ajouter aux favoris",
                        onCheckedChange = { onToggleFavorite(activity.id.id) },
                        enabled = favorites.ready && favorites.busy == null,
                    )
                }
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
                ) {
                    DsBadge(ageRangeLabel(activity), tone = BadgeTone.Plain)
                    DsBadge("${activity.duration.value} min", tone = BadgeTone.Plain)
                    activity.tags.firstOrNull { it.group == TagGroup.MOOD }?.let {
                        DsBadge(it.label, tone = BadgeTone.Plain)
                    }
                }
            }
        }
    }
}
