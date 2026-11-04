package com.sotopatrick.onjoue

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.equipment.EquipmentUsage
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.data.models.tag.TagUsage
import com.sotopatrick.onjoue.presentation.management.ManagementCategory
import com.sotopatrick.onjoue.presentation.management.ManagementData
import com.sotopatrick.onjoue.presentation.management.ManagementState
import com.sotopatrick.onjoue.presentation.management.TagEquipmentManagementContent
import com.sotopatrick.onjoue.presentation.navigation.AppNavigationScaffold
import com.sotopatrick.onjoue.presentation.navigation.TopLevelDestination
import com.sotopatrick.onjoue.presentation.theme.OnJoueTheme

private val inside = Tag(TagId("inside"), TagGroup.LOCATION, "Dedans")
private val catalog = ManagementData(
    tagUsage = listOf(
        TagUsage(Tag(TagId("game"), TagGroup.TYPE, "Jeu"), 2),
        TagUsage(Tag(TagId("craft"), TagGroup.TYPE, "Bricolage"), 0),
        TagUsage(inside, 2),
        TagUsage(Tag(TagId("living"), TagGroup.LOCATION, "Salon", inside.id), 1),
    ),
    equipmentUsage = listOf(
        EquipmentUsage(Equipment(EquipmentId("ball"), "Ballon"), 0),
        EquipmentUsage(Equipment(EquipmentId("chair"), "Chaises"), 2),
    ),
)

@PreviewTest
@NavigationSizes
@Composable
fun TagsCatalogPreview() {
    OnJoueTheme(darkTheme = false) { CatalogPreviewContent(ManagementCategory.TAGS) }
}

@PreviewTest
@NavigationSizes
@Composable
fun EquipmentCatalogPreview() {
    OnJoueTheme(darkTheme = false) { CatalogPreviewContent(ManagementCategory.EQUIPMENT) }
}

@PreviewTest
@Preview(widthDp = 400, heightDp = 500)
@Composable
fun DarkTagsCatalogPreview() {
    OnJoueTheme(darkTheme = true) { CatalogPreviewContent(ManagementCategory.TAGS) }
}

@PreviewTest
@Preview(widthDp = 400, heightDp = 500, fontScale = 1.5f)
@Composable
fun LargeTextTagsCatalogPreview() {
    OnJoueTheme(darkTheme = false) { CatalogPreviewContent(ManagementCategory.TAGS) }
}

@Composable
private fun CatalogPreviewContent(category: ManagementCategory) {
    AppNavigationScaffold(TopLevelDestination.SETTINGS, onSelect = {}) {
        TagEquipmentManagementContent(
            state = ManagementState(data = catalog, loading = false),
            category = category,
            onBack = {}, onReload = {}, onClearError = {},
            onSaveTag = { _, _, _, _ -> }, onSaveEquipment = { _, _ -> },
            onDeleteTag = {}, onDeleteEquipment = {},
        )
    }
}
