package com.sotopatrick.onjoue.presentation.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.equipment.EquipmentUsage
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.data.models.tag.TagUsage
import com.sotopatrick.onjoue.data.repositories.equipment.EquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.TagRepository
import com.sotopatrick.onjoue.presentation.errors.ErrorParser
import com.sotopatrick.onjoue.presentation.errors.Message
import com.sotopatrick.onjoue.presentation.errors.viewModelAction
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagementData(val tagUsage: List<TagUsage>, val equipmentUsage: List<EquipmentUsage>) {
    val tags: List<Tag> get() = tagUsage.map { it.tag }
    val equipmentItems: List<Equipment> get() = equipmentUsage.map { it.equipment }
}

data class ManagementState(
    val data: ManagementData? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: Message? = null,
    val success: Int = 0,
)

class ManagementViewModel(
    private val tags: TagRepository,
    private val equipmentItems: EquipmentRepository,
    private val errorParser: ErrorParser,
) : ViewModel() {
    private val _state = MutableStateFlow(ManagementState())
    val state = _state.asStateFlow()
    private var observation: Job? = null

    init { load() }

    fun clearError() { _state.update { it.copy(error = null) } }

    fun load() {
        observation?.cancel()
        _state.update { it.copy(loading = true, error = null) }
        observation = viewModelScope.launch {
            flow {
                emitAll(combine(tags.observeTagUsage(), equipmentItems.observeEquipmentUsage(), ::ManagementData))
            }
                .catch { error ->
                    if (error is CancellationException) throw error
                    if (error !is Exception) throw error
                    _state.update { it.copy(loading = false, error = errorParser.parse(error)) }
                }.collect { data ->
                    _state.update { it.copy(data = data, loading = false) }
            }
        }
    }

    fun saveTag(label: String, group: TagGroup, parent: TagId?, id: TagId? = null) =
        execute { tags.save(Tag(id ?: TagId(Uuid.random().toString()), group, label, parent)) }
    fun deleteTag(id: TagId) = execute { tags.deleteTag(id) }
    fun saveEquipment(label: String, id: EquipmentId? = null) =
        execute { equipmentItems.save(Equipment(id ?: EquipmentId(Uuid.random().toString()), label)) }
    fun deleteEquipment(id: EquipmentId) = execute { equipmentItems.deleteEquipment(id) }

    private fun execute(action: suspend () -> Unit) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, error = null) }
        viewModelAction(
            errorParser = errorParser,
            onError = { message -> _state.update { it.copy(error = message) } },
            onSuccess = { _state.update { it.copy(success = it.success + 1) } },
            onPostExecute = { _state.update { it.copy(busy = false) } },
            action = action,
        )
    }
}
