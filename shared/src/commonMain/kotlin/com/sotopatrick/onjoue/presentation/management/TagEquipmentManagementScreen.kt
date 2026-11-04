package com.sotopatrick.onjoue.presentation.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import onjoue.shared.generated.resources.Res
import onjoue.shared.generated.resources.ic_delete
import onjoue.shared.generated.resources.ic_edit
import onjoue.shared.generated.resources.ic_back
import onjoue.shared.generated.resources.ic_add
import onjoue.shared.generated.resources.ic_expand
import org.jetbrains.compose.resources.painterResource

@Composable
fun TagEquipmentManagementScreen(
    vm: ManagementViewModel,
    category: ManagementCategory,
    onBack: () -> Unit,
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
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
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
) {
    val catalog = state.data
    val tagsSelected = category == ManagementCategory.TAGS
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var collapsedGroups by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var targetId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var lastSuccess by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(state.success) {
        if (state.success > lastSuccess) {
            editorOpen = false
            deleteTarget = null
        }
        lastSuccess = state.success
    }

    val data = catalog
    val groups = data?.let { managementGroups(it, category, query, filter?.let(TagGroup::valueOf)) }.orEmpty()
    val noRows = groups.all { it.rows.isEmpty() }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (tagsSelected) "Tags" else "Matériel") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !state.busy) {
                        Icon(painterResource(Res.drawable.ic_back), contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
        floatingActionButton = {
            if (catalog != null && !state.busy) {
                FloatingActionButton(
                    onClick = { targetId = null; onClearError(); editorOpen = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painterResource(Res.drawable.ic_add),
                        contentDescription = if (tagsSelected) "Créer un tag" else "Créer un matériel",
                    )
                }
            }
        },
    ) { padding ->
    LazyVerticalGrid(
        columns = GridCells.Adaptive(560.dp),
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "entete", span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    val search: @Composable (Modifier) -> Unit = { modifier ->
                        OutlinedTextField(
                            value = query, onValueChange = { query = it },
                            placeholder = { Text(if (tagsSelected) "Rechercher un tag" else "Rechercher un matériel") },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true, modifier = modifier,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                        )
                    }
                    val groupFilter: @Composable (Modifier) -> Unit = { modifier ->
                        SelectionMenu(
                            label = filter?.let { TagGroup.valueOf(it).displayLabel() } ?: "Tous les groupes",
                            options = listOf(null to "Tous les groupes") + TagGroup.entries.map { it.name to it.displayLabel() },
                            enabled = !state.busy,
                            onSelect = { filter = it },
                            modifier = modifier,
                        )
                    }
                    if (tagsSelected && maxWidth >= 340.dp * fontScale) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            search(Modifier.weight(1.2f))
                            groupFilter(Modifier.weight(1f))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            search(Modifier.fillMaxWidth())
                            if (tagsSelected) groupFilter(Modifier.fillMaxWidth())
                        }
                    }
                }
                if (state.busy || state.loading) CircularProgressIndicator()
                if (state.error != null && !editorOpen && deleteTarget == null) {
                    Text(state.error.content, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onReload) { Text("Réessayer") }
                }
                if (data != null && noRows) Text("Aucun élément.")
            }
        }
        groups.forEach { group ->
            val expanded = group.label !in collapsedGroups
            group.label?.let { label ->
                item(key = "group-$label", span = { GridItemSpan(maxLineSpan) }) {
                    TextButton(
                        onClick = {
                            collapsedGroups = if (expanded) collapsedGroups + label else collapsedGroups - label
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics {
                            stateDescription = if (expanded) "Déplié" else "Replié"
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        Text(
                            "${label.uppercase()} \u00b7 ${group.rows.size}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Icon(
                            painterResource(Res.drawable.ic_expand),
                            contentDescription = null,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f),
                        )
                    }
                }
            }
            if (expanded) items(group.rows, key = { it.id }) { row ->
                ManagementRowCard(
                    row, enabled = !state.busy, showInitial = !tagsSelected,
                    onEdit = { targetId = row.id; onClearError(); editorOpen = true },
                    onDelete = { onClearError(); deleteTarget = row.id },
                )
            }
        }
        item(key = "bottom-spacing", span = { GridItemSpan(maxLineSpan) }) {
            Box(Modifier.padding(bottom = 12.dp))
        }
    }
    }
    if (data != null) {
        if (editorOpen) {
            ManagementForm(
                data, tagsSelected, targetId, state,
                onCancel = { editorOpen = false; onClearError() },
                onSave = { label, group, parent ->
                    val id = targetId
                    if (tagsSelected) {
                        onSaveTag(label, group, parent, id?.let(::TagId))
                    } else {
                        onSaveEquipment(label, id?.let(::EquipmentId))
                    }
                },
            )
        }
        deleteTarget?.let { id ->
            val name = if (tagsSelected) data.tags.find { it.id.value == id }?.label
            else data.equipmentItems.find { it.id.value == id }?.label
            AlertDialog(
                onDismissRequest = { if (!state.busy) deleteTarget = null },
                title = { Text("Supprimer « ${name.orEmpty()} » ?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cette action est définitive. Les éléments utilisés et les tags ayant des enfants ne peuvent pas être supprimés.")
                        state.error?.let { Text(it.content, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !state.busy,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            if (tagsSelected) onDeleteTag(TagId(id))
                            else onDeleteEquipment(EquipmentId(id))
                        },
                    ) { Text("Supprimer") }
                },
                dismissButton = {
                    TextButton(
                        enabled = !state.busy,
                        onClick = { deleteTarget = null; onClearError() },
                    ) { Text("Annuler") }
                },
            )
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
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(16.dp)) {
            val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
            val details: @Composable () -> Unit = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (showInitial) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(
                                row.label.take(1).uppercase(),
                                Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            (if (row.parentLabel != null) "\u21b3 " else "") + row.label,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        ManagementBadge("${row.activityCount} " + if (row.activityCount > 1) "activités" else "activité")
                        row.parentLabel?.let { ManagementBadge("Parent : $it") }
                    }
                }
            }
            val actions: @Composable () -> Unit = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = enabled, onClick = onEdit, shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.size(48.dp),
                    ) { Icon(painterResource(Res.drawable.ic_edit), contentDescription = "Modifier") }
                    OutlinedButton(
                        enabled = enabled, onClick = onDelete, shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.size(48.dp),
                    ) { Icon(painterResource(Res.drawable.ic_delete), contentDescription = "Supprimer") }
                }
            }
            if (maxWidth >= (if (showInitial) 280.dp else 240.dp) * fontScale) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) { details() }
                    actions()
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { details(); actions() }
            }
        }
    }
}

@Composable
private fun ManagementBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge)
    }
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
    val equipment = catalog.equipmentItems.find { it.id.value == id }
    var label by rememberSaveable(tags, id) { mutableStateOf(if (tags) tag?.label.orEmpty() else equipment?.label.orEmpty()) }
    var groupName by rememberSaveable(tags, id) { mutableStateOf((tag?.group ?: TagGroup.TYPE).name) }
    var parentId by rememberSaveable(tags, id) { mutableStateOf(tag?.parent?.value) }
    val group = TagGroup.valueOf(groupName)
    val hasChildren = id != null && catalog.tags.any { it.parent?.value == id }
    val parents = catalog.tags.filter {
        it.group == TagGroup.LOCATION && it.parent == null && it.id.value != id
    }.sortedBy { it.label.lowercase() }

    Dialog(onDismissRequest = { if (!state.busy) onCancel() }) {
        Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.widthIn(max = 560.dp).imePadding()) {
            Column(
                Modifier.heightIn(max = 640.dp).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    (if (id == null) "Créer" else "Modifier") + if (tags) " un tag" else " un matériel",
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(
                    value = label, onValueChange = { label = it }, enabled = !state.busy,
                    label = { Text("Libellé") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
                if (tags) {
                    SelectionMenu(
                        "Groupe : ${group.displayLabel()}",
                        TagGroup.entries.map { it.name to it.displayLabel() },
                        enabled = !state.busy && !hasChildren,
                    ) { name ->
                        if (name != null) {
                            groupName = name
                            if (group != TagGroup.valueOf(name)) parentId = null
                        }
                    }
                    if (group == TagGroup.LOCATION) {
                        SelectionMenu(
                            "Parent : ${parents.find { it.id.value == parentId }?.label ?: "Aucun"}",
                            listOf(null to "Aucun") + parents.map { it.id.value to it.label },
                            enabled = !state.busy && !hasChildren,
                        ) { parentId = it }
                    }
                    if (hasChildren) Text("Ce lieu possède des enfants : il doit rester un lieu sans parent.")
                }
                state.error?.let { Text(it.content, color = MaterialTheme.colorScheme.error) }
                if (state.busy) CircularProgressIndicator()
                Button(
                    onClick = { onSave(label, group, parentId?.let(::TagId)) },
                    enabled = label.isNotBlank() && !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Enregistrer") }
                TextButton(onClick = onCancel, enabled = !state.busy) { Text("Annuler") }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectionMenu(
    label: String,
    options: List<Pair<String?, String>>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (String?) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded && enabled) },
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { expanded = false; onSelect(value) })
            }
        }
    }
}
