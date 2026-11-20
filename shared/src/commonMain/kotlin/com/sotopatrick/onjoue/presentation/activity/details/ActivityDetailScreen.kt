package com.sotopatrick.onjoue.presentation.activity.details

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.activity.AgeMonths
import com.sotopatrick.onjoue.data.models.activity.AgeRange
import com.sotopatrick.onjoue.data.models.activity.DurationMinutes
import com.sotopatrick.onjoue.data.models.activity.Step
import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.designsystem.OnJoueTheme
import com.sotopatrick.onjoue.designsystem.components.ActionTone
import com.sotopatrick.onjoue.designsystem.components.BadgeTone
import com.sotopatrick.onjoue.designsystem.components.BackgroundTone
import com.sotopatrick.onjoue.designsystem.components.DsBackAction
import com.sotopatrick.onjoue.designsystem.components.DsBackground
import com.sotopatrick.onjoue.designsystem.components.DsBadge
import com.sotopatrick.onjoue.designsystem.components.DsBulletItem
import com.sotopatrick.onjoue.designsystem.components.DsButton
import com.sotopatrick.onjoue.designsystem.components.DsCallout
import com.sotopatrick.onjoue.designsystem.components.DsHeroBanner
import com.sotopatrick.onjoue.designsystem.components.DsLoadingState
import com.sotopatrick.onjoue.designsystem.components.DsNumberedItem
import com.sotopatrick.onjoue.designsystem.components.DsImage
import com.sotopatrick.onjoue.designsystem.components.DsScaffold
import com.sotopatrick.onjoue.designsystem.components.DsSection
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.presentation.activity.common.ageRangeLabel
import com.sotopatrick.onjoue.presentation.common.ShowToast
import onjoue.shared.generated.resources.Res
import onjoue.shared.generated.resources.idea_image_placeholder
import onjoue.shared.generated.resources.ic_age
import onjoue.shared.generated.resources.ic_back
import onjoue.shared.generated.resources.ic_duration
import org.jetbrains.compose.resources.painterResource

@Composable
private fun ActivityBadges(activity: Activity) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        DsBadge(
            label = ageRangeLabel(activity),
            tone = BadgeTone.Surface,
            icon = painterResource(Res.drawable.ic_age),
        )
        DsBadge(
            label = "${activity.duration.value} min",
            tone = BadgeTone.Surface,
            icon = painterResource(Res.drawable.ic_duration),
        )
    }
}

@Composable
fun ActivityDetailScreen(
    state: ActivityDetailsState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: () -> Unit = {},
    onToggleTriedStatus: () -> Unit = {},
    onClearNotice: () -> Unit = {},
) {
    state.notice?.let { notice ->
        ShowToast(notice.content)
        onClearNotice()
    }
    DsScaffold(
        title = "",
        modifier = modifier,
        backAction = DsBackAction(
            icon = painterResource(Res.drawable.ic_back),
            contentDescription = "Retour aux idées",
            enabled = true,
            onClick = onBack,
        ),
    ) { contentPadding ->
        DsBackground(
            Modifier.fillMaxSize().padding(contentPadding),
            tone = BackgroundTone.Screen,
        ) {
            when {
                state.loading && state.activity == null -> DsLoadingState(Modifier.fillMaxSize())
                state.activity == null -> Column(
                    Modifier.fillMaxSize().padding(Spacing.XXLarge),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Large),
                ) {
                    DsText(state.error?.content ?: "Cette idée n’est plus disponible.", tone = TextTone.Error)
                    DsButton("Réessayer", onClick = onRetry)
                }
                else -> ActivityDetailContent(
                    activity = state.activity,
                    isFavorite = state.isFavorite,
                    isTried = state.isTried,
                    isBusy = state.busy,
                    onToggleFavorite = onToggleFavorite,
                    onToggleTriedStatus = onToggleTriedStatus,
                )
            }
        }
    }
}

@Composable
private fun ActivityDetailContent(
    activity: Activity,
    isFavorite: Boolean,
    isTried: Boolean,
    isBusy: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleTriedStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.ExtraLarge)
            .padding(top = Spacing.Large),
        verticalArrangement = Arrangement.spacedBy(Spacing.LargePlus),
    ) {
        ActivityHero(activity)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
            DsText(activity.name, role = TextRole.HeadlineSmall)
            ActivityBadges(activity)
        }
        DsButton(
            text = if (isFavorite) "♥ Dans vos favoris" else "♡ Ajouter aux favoris",
            onClick = onToggleFavorite,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy,
            tone = ActionTone.OnPrimaryContainer,
        )
        DsText(activity.description, role = TextRole.BodyMedium)
        ActivityDetailSections(activity, isTried, isBusy, onToggleTriedStatus)
    }
}

@Composable
private fun ActivityHero(activity: Activity) {
    DsHeroBanner {
        DsImage(
            url = activity.image?.value,
            fallback = painterResource(Res.drawable.idea_image_placeholder),
            contentDescription = activity.name,
            modifier = Modifier.fillMaxWidth().aspectRatio(HeroImageAspectRatio),
        )
    }
}

@Composable
private fun ActivityDetailSections(
    activity: Activity,
    isTried: Boolean,
    isBusy: Boolean,
    onToggleTriedStatus: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.LargePlus),
    ) {
        if (activity.equipment.isNotEmpty()) {
            DsSection(title = "Matériel") {
                activity.equipment.forEach { item ->
                    DsBulletItem(text = item.label, icon = equipmentIconPainter(item.iconId))
                }
            }
        }
        DsSection(title = "Comment jouer") {
            activity.steps.forEachIndexed { index, step -> DsNumberedItem(index + 1, step.text) }
        }
        activity.note?.let { note ->
            DsCallout(title = "À garder en tête", body = note, modifier = Modifier.fillMaxWidth())
        }
        if (activity.variants.isNotEmpty()) {
            DsSection(title = "Pour varier") {
                activity.variants.forEach { variant -> DsText(variant, role = TextRole.BodyLarge) }
            }
        }
        activity.document?.let { source ->
            DsTextButton("Voir la source : Naître et grandir", onClick = { uriHandler.openUri(source.value) })
        }
        DsButton(
            text = if (isTried) "Déjà essayé ✓" else "J’ai essayé",
            onClick = onToggleTriedStatus,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy,
            tone = ActionTone.Tertiary,
        )
        Spacer(Modifier.height(Spacing.Medium))
    }
}

@Preview(name = "Activity Detail · Phone", widthDp = 390, heightDp = 844)
@Preview(name = "Activity Detail · Tablet", widthDp = 800, heightDp = 1000)
@Preview(name = "Activity Detail · Large text", widthDp = 390, heightDp = 844, fontScale = 1.5f)
@Composable
private fun ActivityDetailPreview() {
    ActivityDetailPreviewContent()
}

@Preview(name = "Activity Detail · Dark", widthDp = 390, heightDp = 844)
@Composable
private fun DarkActivityDetailPreview() {
    ActivityDetailPreviewContent(darkTheme = true)
}

@Composable
private fun ActivityDetailPreviewContent(darkTheme: Boolean = false) {
    val activity = Activity(
        id = ActivityId("idea-1"),
        name = "Rivière aux crocodiles",
        description = "Transformez le salon en rivière peuplée de crocodiles. " +
            "Inventez un parcours à traverser sans toucher l’eau.",
        age = AgeRange(AgeMonths(24), AgeMonths(72)),
        duration = DurationMinutes(10),
        steps = listOf(
            Step("Disposez les coussins pour créer un chemin."),
            Step("Traversez la rivière sans toucher le sol."),
        ),
        equipment = setOf(Equipment(EquipmentId("eq-1"), "Coussins", "ic_equipment_cushions")),
    )
    OnJoueTheme(darkTheme = darkTheme) {
        ActivityDetailScreen(
            state = ActivityDetailsState(
                activity = activity,
                loading = false,
                isFavorite = true,
                isTried = false,
            ),
            onBack = {},
            onRetry = {},
        )
    }
}

private const val HeroImageAspectRatio = 16f / 9f
