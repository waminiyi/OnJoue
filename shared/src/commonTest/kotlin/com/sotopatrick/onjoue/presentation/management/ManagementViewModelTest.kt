@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.sotopatrick.onjoue.presentation.management

import androidx.lifecycle.ViewModelStore
import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.equipment.EquipmentUsage
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.data.models.tag.TagUsage
import com.sotopatrick.onjoue.data.repositories.equipment.EquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.TagRepository
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.errors.Message
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class ManagementViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    private class TagTestRepository : TagRepository {
        val data = MutableStateFlow(emptyList<Tag>())
        var readFailure = false
        var synchronousFailure = false
        var activeCollectors = 0
        override fun observeTags(): kotlinx.coroutines.flow.Flow<List<Tag>> {
            if (synchronousFailure) throw AppException.Storage(IllegalStateException())
            return flow {
                activeCollectors++
                try {
                    if (readFailure) throw AppException.Storage(IllegalStateException())
                    data.collect { emit(it) }
                } finally { activeCollectors-- }
            }
        }
        override suspend fun getTag(id: TagId) = data.value.find { it.id == id }
        override fun observeTagUsage() = observeTags().map { tags -> tags.map { TagUsage(it, 0) } }
        override suspend fun save(tag: Tag) {
            data.value = data.value.filterNot { it.id == tag.id } + tag
        }
        override suspend fun deleteTag(id: TagId) { data.value = emptyList() }
    }

    private class EquipmentTestRepository : EquipmentRepository {
        val data = MutableStateFlow(emptyList<Equipment>())
        var pending: CompletableDeferred<Unit>? = null
        var error: Exception? = null
        var creationCount = 0
        override fun observeEquipment() = data
        override fun observeEquipmentUsage() = data.map { items -> items.map { EquipmentUsage(it, 0) } }
        override suspend fun getEquipment(id: EquipmentId) = data.value.find { it.id == id }
        override suspend fun save(equipment: Equipment) {
            creationCount++
            pending?.await()
            error?.let { throw it }
            data.value = data.value.filterNot { it.id == equipment.id } + equipment
        }
        override suspend fun deleteEquipment(id: EquipmentId) { data.value = emptyList() }
    }

    @Test
    fun intentsAndObservation() = runTest(dispatcher) {
        val tags = TagTestRepository()
        val equipmentItems = EquipmentTestRepository()
        val vm = ManagementViewModel(tags, equipmentItems, FrenchErrorParser())
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)
        assertNotNull(vm.state.value.data)
        vm.saveTag("Calme", TagGroup.MOOD, null)
        advanceUntilIdle()
        assertEquals("Calme", vm.state.value.data?.tags?.single()?.label)
        val tagId = tags.data.value.single().id
        assertTrue(tagId.value.isNotBlank())
        vm.saveTag("Actif", TagGroup.MOOD, null, id = tagId)
        advanceUntilIdle()
        assertEquals("Actif", vm.state.value.data?.tags?.single()?.label)
        vm.saveEquipment("Ballon")
        advanceUntilIdle()
        val equipmentId = equipmentItems.data.value.single().id
        assertTrue(equipmentId.value.isNotBlank())
        vm.saveEquipment("Crayons", id = equipmentId)
        advanceUntilIdle()
        assertEquals("Crayons", vm.state.value.data?.equipmentItems?.single()?.label)
        assertEquals(tagId, tags.data.value.single().id)
        assertEquals(equipmentId, equipmentItems.data.value.single().id)
        vm.deleteTag(tagId)
        advanceUntilIdle()
        vm.deleteEquipment(equipmentId)
        advanceUntilIdle()
        assertEquals(ManagementData(emptyList(), emptyList()), vm.state.value.data)
        assertEquals(6, vm.state.value.success)
    }

    @Test
    fun loadingFailureAndRetry() = runTest(dispatcher) {
        val tags = TagTestRepository().apply { readFailure = true }
        val vm = ManagementViewModel(tags, EquipmentTestRepository(), FrenchErrorParser())
        advanceUntilIdle()
        assertEquals(Message("Impossible d'accéder aux données locales. Réessayez."), vm.state.value.error)
        assertFalse(vm.state.value.loading)
        tags.readFailure = false
        vm.load()
        advanceUntilIdle()
        assertEquals(null, vm.state.value.error)
        assertNotNull(vm.state.value.data)
    }

    @Test
    fun mutationErrorsAndDuplicateSubmissions() = runTest(dispatcher) {
        val equipmentItems = EquipmentTestRepository().apply { pending = CompletableDeferred() }
        val vm = ManagementViewModel(TagTestRepository(), equipmentItems, FrenchErrorParser())
        advanceUntilIdle()
        vm.saveEquipment("Ballon")
        vm.saveEquipment("Deuxième")
        advanceUntilIdle()
        assertTrue(vm.state.value.busy)
        assertEquals(1, equipmentItems.creationCount)
        equipmentItems.error = AppException.Conflict()
        equipmentItems.pending?.complete(Unit)
        advanceUntilIdle()
        assertEquals(Message("Un élément possède déjà cet identifiant ou ce libellé."), vm.state.value.error)
        assertFalse(vm.state.value.busy)
        assertEquals(0, vm.state.value.success)
        equipmentItems.error = AppException.Storage(IllegalStateException())
        vm.saveEquipment("Ballon")
        advanceUntilIdle()
        assertEquals(Message("Impossible d'accéder aux données locales. Réessayez."), vm.state.value.error)
        vm.clearError()
        assertEquals(null, vm.state.value.error)
        equipmentItems.error = null
        vm.saveEquipment("Ballon")
        advanceUntilIdle()
        assertEquals(1, vm.state.value.success)
    }

    @Test
    fun synchronousObservationErrorsAreParsedAndRetryable() = runTest(dispatcher) {
        val tags = TagTestRepository().apply { synchronousFailure = true }
        val vm = ManagementViewModel(tags, EquipmentTestRepository(), FrenchErrorParser())
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
        tags.synchronousFailure = false
        vm.load()
        advanceUntilIdle()
        assertNotNull(vm.state.value.data)
        assertEquals(null, vm.state.value.error)
    }

    @Test
    fun reloadAndViewModelClearingCancelObserversWithoutErrors() = runTest(dispatcher) {
        val tags = TagTestRepository()
        val equipment = EquipmentTestRepository().apply { pending = CompletableDeferred() }
        val vm = ManagementViewModel(tags, equipment, FrenchErrorParser())
        val store = ViewModelStore().apply { put("management", vm) }
        advanceUntilIdle()
        repeat(3) { vm.load(); advanceUntilIdle() }
        assertEquals(1, tags.activeCollectors)
        vm.saveEquipment("Ballon")
        advanceUntilIdle()
        assertTrue(vm.state.value.busy)
        store.clear()
        advanceUntilIdle()
        assertEquals(0, tags.activeCollectors)
        assertFalse(vm.state.value.busy)
        assertEquals(null, vm.state.value.error)
        assertEquals(0, vm.state.value.success)
    }

    @Test
    fun unexpectedMutationErrorsUseGenericMessage() = runTest(dispatcher) {
        val equipment = EquipmentTestRepository().apply { error = IllegalStateException("private file path") }
        val vm = ManagementViewModel(TagTestRepository(), equipment, FrenchErrorParser())
        advanceUntilIdle()
        vm.saveEquipment("Ballon")
        advanceUntilIdle()
        assertEquals(Message("Une erreur inattendue s'est produite. Réessayez."), vm.state.value.error)
        equipment.error = CancellationException()
        vm.clearError()
        vm.saveEquipment("Ballon")
        advanceUntilIdle()
        assertEquals(null, vm.state.value.error)
        assertFalse(vm.state.value.busy)
    }
}
