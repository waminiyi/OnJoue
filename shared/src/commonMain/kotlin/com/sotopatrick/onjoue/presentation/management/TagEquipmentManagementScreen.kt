package com.sotopatrick.onjoue.presentation.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.designsystem.components.ActionTone
import com.sotopatrick.onjoue.designsystem.components.BadgeTone
import com.sotopatrick.onjoue.designsystem.components.DsAdaptiveGrid
import com.sotopatrick.onjoue.designsystem.components.DsBackAction
import com.sotopatrick.onjoue.designsystem.components.DsBadge
import com.sotopatrick.onjoue.designsystem.components.DsCard
import com.sotopatrick.onjoue.designsystem.components.DsCollapsibleHeader
import com.sotopatrick.onjoue.designsystem.components.DsFloatingActionButton
import com.sotopatrick.onjoue.designsystem.components.DsMonogram
import com.sotopatrick.onjoue.designsystem.components.DsOutlinedIconButton
import com.sotopatrick.onjoue.designsystem.components.DsProgressIndicator
import com.sotopatrick.onjoue.designsystem.components.DsScaffold
import com.sotopatrick.onjoue.designsystem.components.DsSelectField
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.DsTextField
import com.sotopatrick.onjoue.designsystem.components.FieldContainer
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Breakpoint
import com.sotopatrick.onjoue.designsystem.foundation.DsWidthBreakpoint
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import onjoue.shared.generated.resources.Res
import onjoue.shared.generated.resources.ic_add
import onjoue.shared.generated.resources.ic_back
import onjoue.shared.generated.resources.ic_delete
import onjoue.shared.generated.resources.ic_edit
import onjoue.shared.generated.resources.ic_expand
import org.jetbrains.compose.resources.painterResource

@Composable
fun TagEquipmentManagementScreen(
    vm: ManagementViewModel,
    category: ManagementCategory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    TagEquipmentManagementContent(
        state = state,
        category = category,
        onBack = onBack,
        onReload = vm::load,
        onClearError = vm::clearError,
        onSaveTag = vm::saveTag,
        onSaveEquipment = vm::saveEquipment,
        onDeleteTag = vm::deleteTag,
        onDeleteEquipment = vm::deleteEquipment,
        modifier = modifier,
    )
}

@Composable
fun TagEquipmentManagementContent(
    state: ManagementState,
    category: ManagementCategory,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onClearError: () -> Unit,
    onSaveTag: (String, TagGroup, TagId?, TagId?) -> Unit,
    onSaveEquipment: (String, EquipmentId?) -> Unit,
    onDeleteTag: (TagId) -> Unit,
    onDeleteEquipment: (EquipmentId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = state.data
    val tagsSelected = category == ManagementCategory.TAGS
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var targetId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteTarget by rememberSaveable { mutableStateOf<String?>(null) }
    OnNewSuccess(state.success) {
        editorOpen = false
        deleteTarget = null
    }

    val openEditor: (String?) -> Unit = { id ->
        targetId = id
        onClearError()
        editorOpen = true
    }
    DsScaffold(
        title = if (tagsSelected) "Tags" else "Matériel",
        modifier = modifier,
        backAction = DsBackAction(
            icon = painterResource(Res.drawable.ic_back),
            contentDescription = "Retour",
            enabled = !state.busy,
            onClick = onBack,
        ),
        floatingActionButton = {
            ManagementFab(visible = data != null && !state.busy, tagsSelected) { openEditor(null) }
        },
    ) { padding ->
        ManagementGrid(
            state = state,
            category = category,
            showError = !editorOpen && deleteTarget == null,
            onReload = onReload,
            onEdit = openEditor,
            onDelete = { id ->
                onClearError()
                deleteTarget = id
            },
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
    if (data != null) {
        ManagementDialogs(
            data = data,
            tagsSelected = tagsSelected,
            state = state,
            editorTarget = if (editorOpen) EditorTarget(targetId) else null,
            deleteTarget = deleteTarget,
            onCloseEditor = { editorOpen = false },
            onCloseDelete = { deleteTarget = null },
            onClearError = onClearError,
            onSaveTag = onSaveTag,
            onSaveEquipment = onSaveEquipment,
            onDeleteTag = onDeleteTag,
            onDeleteEquipment = onDeleteEquipment,
        )
    }
}

/** Runs [onSuccess] each time [success] grows, ignoring the value restored with the screen. */
@Composable
private fun OnNewSuccess(success: Int, onSuccess: () -> Unit) {
    var lastSuccess by rememberSaveable { mutableIntStateOf(0) }
    val currentOnSuccess by rememberUpdatedState(onSuccess)
    LaunchedEffect(success) {
        if (success > lastSuccess) currentOnSuccess()
        lastSuccess = success
    }
}

@Composable
private fun ManagementFab(visible: Boolean, tagsSelected: Boolean, onClick: () -> Unit) {
    if (visible) {
        DsFloatingActionButton(
            icon = painterResource(Res.drawable.ic_add),
            contentDescription = if (tagsSelected) "Créer un tag" else "Créer un matériel",
            onClick = onClick,
        )
    }
}

@Composable
private fun ManagementGrid(
    state: ManagementState,
    category: ManagementCategory,
    showError: Boolean,
    onReload: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tagsSelected = category == ManagementCategory.TAGS
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var collapsedGroups by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val groups = state.data
        ?.let { managementGroups(it, category, query, filter?.let(TagGroup::valueOf)) }
        .orEmpty()
    DsAdaptiveGrid(modifier) {
        item(key = "entete", span = { GridItemSpan(maxLineSpan) }) {
            ManagementHeader(
                state = state,
                tagsSelected = tagsSelected,
                query = query,
                onQueryChange = { query = it },
                filter = filter,
                onFilterChange = { filter = it },
                showError = showError,
                noRows = state.data != null && groups.all { it.rows.isEmpty() },
                onReload = onReload,
            )
        }
        managementGroupItems(
            groups = groups,
            collapsedGroups = collapsedGroups,
            onToggleGroup = { label, expanded ->
                collapsedGroups = if (expanded) collapsedGroups + label else collapsedGroups - label
            },
            enabled = !state.busy,
            showInitial = !tagsSelected,
            onEdit = onEdit,
            onDelete = onDelete,
        )
        item(key = "bottom-spacing", span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(Spacing.Medium))
        }
    }
}

@Composable
private fun ManagementHeader(
    state: ManagementState,
    tagsSelected: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: String?,
    onFilterChange: (String?) -> Unit,
    showError: Boolean,
    noRows: Boolean,
    onReload: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
        DsWidthBreakpoint(Breakpoint.Medium, Modifier.fillMaxWidth().padding(top = Spacing.Small)) { isWide ->
            val search: @Composable (Modifier) -> Unit = { modifier ->
                DsTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = if (tagsSelected) "Rechercher un tag" else "Rechercher un matériel",
                    textRole = TextRole.BodyMedium,
                    container = FieldContainer.Surface,
                    modifier = modifier,
                )
            }
            val groupFilter: @Composable (Modifier) -> Unit = { modifier ->
                DsSelectField(
                    label = filter?.let { TagGroup.valueOf(it).displayLabel() } ?: "Tous les groupes",
                    options = listOf(null to "Tous les groupes") +
                        TagGroup.entries.map { it.name to it.displayLabel() },
                    onSelect = onFilterChange,
                    enabled = !state.busy,
                    modifier = modifier,
                )
            }
            if (tagsSelected && isWide) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SmallPlus),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    search(Modifier.weight(SearchWeight))
                    groupFilter(Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.SmallPlus)) {
                    search(Modifier.fillMaxWidth())
                    if (tagsSelected) groupFilter(Modifier.fillMaxWidth())
                }
            }
        }
        if (state.busy || state.loading) DsProgressIndicator()
        if (state.error != null && showError) {
            DsText(state.error.content, tone = TextTone.Error)
            DsTextButton("Réessayer", onClick = onReload)
        }
        if (noRows) DsText("Aucun élément.")
    }
}

private fun LazyGridScope.managementGroupItems(
    groups: List<ManagementGroup>,
    collapsedGroups: List<String>,
    onToggleGroup: (label: String, expanded: Boolean) -> Unit,
    enabled: Boolean,
    showInitial: Boolean,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    groups.forEach { group ->
        val expanded = group.label !in collapsedGroups
        group.label?.let { label ->
            item(key = "group-$label", span = { GridItemSpan(maxLineSpan) }) {
                DsCollapsibleHeader(
                    label = "${label.uppercase()} \u00b7 ${group.rows.size}",
                    expanded = expanded,
                    expandIcon = painterResource(Res.drawable.ic_expand),
                    onToggle = { onToggleGroup(label, expanded) },
                    modifier = Modifier.padding(top = Spacing.Small),
                )
            }
        }
        if (expanded) {
            items(group.rows, key = { it.id }) { row ->
                ManagementRowCard(
                    row,
                    enabled = enabled,
                    showInitial = showInitial,
                    onEdit = { onEdit(row.id) },
                    onDelete = { onDelete(row.id) },
                )
            }
        }
    }
}

@Composable
internal fun ManagementRowCard(
    row: ManagementRow,
    enabled: Boolean,
    showInitial: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DsCard(modifier = modifier.fillMaxWidth()) {
        DsWidthBreakpoint(
            if (showInitial) Breakpoint.CompactPlus else Breakpoint.Compact,
            Modifier.fillMaxWidth().padding(Spacing.Large),
        ) { isWide ->
            if (isWide) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) { ManagementRowDetails(row, showInitial) }
                    ManagementRowActions(enabled, onEdit, onDelete)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
                    ManagementRowDetails(row, showInitial)
                    ManagementRowActions(enabled, onEdit, onDelete)
                }
            }
        }
    }
}

@Composable
private fun ManagementRowDetails(row: ManagementRow, showInitial: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Medium), verticalAlignment = Alignment.CenterVertically) {
        if (showInitial) DsMonogram(row.label.take(1).uppercase())
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)) {
            DsText(
                (if (row.parentLabel != null) "\u21b3 " else "") + row.label,
                role = TextRole.TitleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val activities = if (row.activityCount > 1) "activités" else "activité"
            DsBadge("${row.activityCount} $activities", tone = BadgeTone.Neutral)
            row.parentLabel?.let { DsBadge("Parent : $it", tone = BadgeTone.Neutral) }
        }
    }
}

@Composable
private fun ManagementRowActions(enabled: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Small)) {
        DsOutlinedIconButton(
            icon = painterResource(Res.drawable.ic_edit),
            contentDescription = "Modifier",
            onClick = onEdit,
            enabled = enabled,
        )
        DsOutlinedIconButton(
            icon = painterResource(Res.drawable.ic_delete),
            contentDescription = "Supprimer",
            onClick = onDelete,
            enabled = enabled,
            tone = ActionTone.Destructive,
        )
    }
}

private const val SearchWeight = 1.2f
