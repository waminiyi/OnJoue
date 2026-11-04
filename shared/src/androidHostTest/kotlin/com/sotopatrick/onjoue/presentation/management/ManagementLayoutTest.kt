package com.sotopatrick.onjoue.presentation.management

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.sotopatrick.onjoue.data.models.equipment.EquipmentUsage
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagUsage
import com.sotopatrick.onjoue.data.repositories.sampleEquipment
import com.sotopatrick.onjoue.data.repositories.sampleTag
import com.sotopatrick.onjoue.presentation.theme.OnJoueTheme
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ManagementLayoutTest {
    @Test
    fun tagGroupsCollapseIndependentlyAndRestoreTheirState() = runComposeUiTest {
        val data = ManagementData(
            listOf(
                TagUsage(sampleTag("Jeu", TagGroup.TYPE), 2),
                TagUsage(sampleTag("Été", TagGroup.SEASON), 0),
            ),
            emptyList(),
        )
        val restoration = StateRestorationTester(this)
        restoration.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(400.dp, 800.dp))) {
                OnJoueTheme(darkTheme = false) {
                    TagEquipmentManagementContent(
                        ManagementState(data = data, loading = false), ManagementCategory.TAGS,
                        onBack = {}, onReload = {}, onClearError = {},
                        onSaveTag = { _, _, _, _ -> }, onSaveEquipment = { _, _ -> },
                        onDeleteTag = {}, onDeleteEquipment = {},
                    )
                }
            }
        }
        onNodeWithText("Jeu").assertIsDisplayed()
        onNodeWithText("TYPE \u00b7 1").performClick()
        onNodeWithText("Jeu").assertDoesNotExist()
        onNodeWithText("Été").assertIsDisplayed()
        restoration.emulateSaveAndRestore()
        onNodeWithText("Jeu").assertDoesNotExist()
        onNodeWithText("Été").assertIsDisplayed()
        onNodeWithText("TYPE \u00b7 1").performClick()
        onNodeWithText("Jeu").assertIsDisplayed()
    }

    @Test
    fun phoneHasTopBarFabDropdownAndRowActionsSideBySide() = runComposeUiTest {
        val category = mutableStateOf(ManagementCategory.TAGS)
        val data = ManagementData(
            listOf(TagUsage(sampleTag("Jeu", TagGroup.TYPE), 2)),
            listOf(EquipmentUsage(sampleEquipment("Ballon"), 2)),
        )
        setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(400.dp, 800.dp))) {
                OnJoueTheme(darkTheme = false) {
                    TagEquipmentManagementContent(
                        ManagementState(data = data, loading = false), category.value,
                        onBack = {}, onReload = {}, onClearError = {},
                        onSaveTag = { _, _, _, _ -> }, onSaveEquipment = { _, _ -> },
                        onDeleteTag = {}, onDeleteEquipment = {},
                    )
                }
            }
        }
        val heading = onNodeWithText("Tags").fetchSemanticsNode().boundsInRoot
        val back = onNodeWithContentDescription("Retour").fetchSemanticsNode().boundsInRoot
        val create = onNodeWithContentDescription("Créer un tag").fetchSemanticsNode().boundsInRoot
        assertTrue(back.right < heading.left && back.center.y in heading.top..heading.bottom)
        assertTrue(create.top > heading.bottom && create.left > heading.right)
        val search = onNodeWithText("Rechercher un tag").fetchSemanticsNode().boundsInRoot
        val filter = onNodeWithText("Tous les groupes").fetchSemanticsNode().boundsInRoot
        assertTrue(filter.left > search.right && filter.center.y in search.top..search.bottom)
        onNodeWithText("Tous les groupes").performClick()
        onNodeWithText("Type").performClick()
        onNodeWithText("Type").assertIsDisplayed()
        val label = onNodeWithText("Jeu").fetchSemanticsNode().boundsInRoot
        val count = onNodeWithText("2 activités").fetchSemanticsNode().boundsInRoot
        val edit = onNodeWithContentDescription("Modifier").fetchSemanticsNode().boundsInRoot
        val delete = onNodeWithContentDescription("Supprimer").fetchSemanticsNode().boundsInRoot
        assertTrue(edit.left > label.right && delete.left > edit.right)
        assertTrue(edit.center.y in label.top..count.bottom)
        runOnIdle { category.value = ManagementCategory.EQUIPMENT }
        waitForIdle()
        val equipmentLabel = onNodeWithText("Ballon").fetchSemanticsNode().boundsInRoot
        val equipmentEdit = onNodeWithContentDescription("Modifier").fetchSemanticsNode().boundsInRoot
        val equipmentDelete = onNodeWithContentDescription("Supprimer").fetchSemanticsNode().boundsInRoot
        assertTrue(equipmentEdit.left > equipmentLabel.right && equipmentDelete.left > equipmentEdit.right)
    }
}
