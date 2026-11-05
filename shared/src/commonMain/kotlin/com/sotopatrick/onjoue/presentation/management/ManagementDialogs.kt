package com.sotopatrick.onjoue.presentation.management

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.designsystem.components.ConfirmStyle
import com.sotopatrick.onjoue.designsystem.components.DsButton
import com.sotopatrick.onjoue.designsystem.components.DsConfirmDialog
import com.sotopatrick.onjoue.designsystem.components.DsFormDialog
import com.sotopatrick.onjoue.designsystem.components.DsProgressIndicator
import com.sotopatrick.onjoue.designsystem.components.DsSelectField
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.DsTextField
import com.sotopatrick.onjoue.designsystem.components.TextTone

/** Item edited by the form; a null [id] creates a new item. */
internal class EditorTarget(val id: String?)

@Composable
internal fun ManagementDialogs(
    data: ManagementData,
    tagsSelected: Boolean,
    state: ManagementState,
    editorTarget: EditorTarget?,
    deleteTarget: String?,
    onCloseEditor: () -> Unit,
    onCloseDelete: () -> Unit,
    onClearError: () -> Unit,
    onSaveTag: (String, TagGroup, TagId?, TagId?) -> Unit,
    onSaveEquipment: (String, EquipmentId?) -> Unit,
    onDeleteTag: (TagId) -> Unit,
    onDeleteEquipment: (EquipmentId) -> Unit,
) {
    editorTarget?.let { target ->
        ManagementForm(
            data, tagsSelected, target.id, state,
            onCancel = {
                onCloseEditor()
                onClearError()
            },
            onSave = { label, group, parent ->
                if (tagsSelected) {
                    onSaveTag(label, group, parent, target.id?.let(::TagId))
                } else {
                    onSaveEquipment(label, target.id?.let(::EquipmentId))
                }
            },
        )
    }
    deleteTarget?.let { id ->
        ManagementDeleteDialog(
            name = if (tagsSelected) {
                data.tags.find { it.id.value == id }?.label
            } else {
                data.equipmentItems.find { it.id.value == id }?.label
            },
            state = state,
            onConfirm = { if (tagsSelected) onDeleteTag(TagId(id)) else onDeleteEquipment(EquipmentId(id)) },
            onDismiss = {
                onCloseDelete()
                onClearError()
            },
        )
    }
}

@Composable
private fun ManagementDeleteDialog(
    name: String?,
    state: ManagementState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DsConfirmDialog(
        title = "Supprimer « ${name.orEmpty()} » ?",
        message = "Cette action est définitive. Les éléments utilisés et les tags ayant des enfants " +
            "ne peuvent pas être supprimés.",
        confirmLabel = "Supprimer",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmStyle = ConfirmStyle.Destructive,
        enabled = !state.busy,
        dismissEnabled = !state.busy,
        error = state.error?.content,
    )
}

@Composable
private fun ManagementForm(
    catalog: ManagementData,
    tags: Boolean,
    id: String?,
    state: ManagementState,
    onCancel: () -> Unit,
    onSave: (String, TagGroup, TagId?) -> Unit,
) {
    val tag = catalog.tags.find { it.id.value == id }
    var label by rememberSaveable(tags, id) { mutableStateOf(initialLabel(catalog, tags, id)) }
    var groupName by rememberSaveable(tags, id) { mutableStateOf((tag?.group ?: TagGroup.TYPE).name) }
    var parentId by rememberSaveable(tags, id) { mutableStateOf(tag?.parent?.value) }
    val group = TagGroup.valueOf(groupName)
    val hasChildren = id != null && catalog.tags.any { it.parent?.value == id }
    val parents = rootLocations(catalog, excluded = id)

    DsFormDialog(
        title = formTitle(tags, id),
        onDismissRequest = { if (!state.busy) onCancel() },
    ) {
        DsTextField(
            value = label,
            onValueChange = { label = it },
            enabled = !state.busy,
            label = "Libellé",
            modifier = Modifier.fillMaxWidth(),
        )
        if (tags) {
            TagHierarchyFields(
                group = group,
                onGroupChange = { selected ->
                    if (group != selected) parentId = null
                    groupName = selected.name
                },
                parentLabel = parents.find { it.id.value == parentId }?.label,
                parents = parents.map { it.id.value to it.label },
                onParentChange = { parentId = it },
                enabled = !state.busy && !hasChildren,
                hasChildren = hasChildren,
            )
        }
        ManagementFormFooter(
            state = state,
            canSave = label.isNotBlank(),
            onSave = { onSave(label, group, parentId?.let(::TagId)) },
            onCancel = onCancel,
        )
    }
}

@Composable
private fun ColumnScope.ManagementFormFooter(
    state: ManagementState,
    canSave: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    state.error?.let { DsText(it.content, tone = TextTone.Error) }
    if (state.busy) DsProgressIndicator()
    DsButton(
        "Enregistrer",
        onClick = onSave,
        enabled = canSave && !state.busy,
        modifier = Modifier.fillMaxWidth(),
    )
    DsTextButton("Annuler", onClick = onCancel, enabled = !state.busy)
}

private fun initialLabel(catalog: ManagementData, tags: Boolean, id: String?): String =
    if (tags) {
        catalog.tags.find { it.id.value == id }?.label.orEmpty()
    } else {
        catalog.equipmentItems.find { it.id.value == id }?.label.orEmpty()
    }

private fun formTitle(tags: Boolean, id: String?): String =
    (if (id == null) "Créer" else "Modifier") + if (tags) " un tag" else " un matériel"

private fun rootLocations(catalog: ManagementData, excluded: String?) = catalog.tags.filter {
    it.group == TagGroup.LOCATION && it.parent == null && it.id.value != excluded
}.sortedBy { it.label.lowercase() }

@Composable
private fun TagHierarchyFields(
    group: TagGroup,
    onGroupChange: (TagGroup) -> Unit,
    parentLabel: String?,
    parents: List<Pair<String, String>>,
    onParentChange: (String?) -> Unit,
    enabled: Boolean,
    hasChildren: Boolean,
) {
    DsSelectField(
        label = "Groupe : ${group.displayLabel()}",
        options = TagGroup.entries.map { it to it.displayLabel() },
        onSelect = onGroupChange,
        enabled = enabled,
    )
    if (group == TagGroup.LOCATION) {
        DsSelectField(
            label = "Parent : ${parentLabel ?: "Aucun"}",
            options = listOf(null to "Aucun") + parents,
            onSelect = onParentChange,
            enabled = enabled,
        )
    }
    if (hasChildren) DsText("Ce lieu possède des enfants : il doit rester un lieu sans parent.")
}
