package com.tickets.store_cost.presentation.viewmodel

import com.tickets.store_cost.data.repository.FakeConsultingRepository
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.PriceConsultation
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.usecase.CalculatePriceUseCase
import com.tickets.store_cost.domain.usecase.GetPriceConsultationsUseCase
import com.tickets.store_cost.domain.usecase.SavePriceConsultationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConsultingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeConsultingRepository
    private lateinit var viewModel: ConsultingViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeConsultingRepository()
        viewModel = ConsultingViewModel(
            calculatePriceUseCase = CalculatePriceUseCase(),
            savePriceConsultationUseCase = SavePriceConsultationUseCase(repository),
            getPriceConsultationsUseCase = GetPriceConsultationsUseCase(repository)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiStateHasDefaults() {
        val state = viewModel.uiState.value

        assertEquals("1000.0", state.basePrice)
        assertEquals("1", state.quantity)
        assertEquals(OperationType.FACTURA_A, state.selectedModality)
        assertIs<Result.Loading>(state.consultations)
        assertNull(state.saveState)
    }

    @Test
    fun testConsultPricesShowsOnlyTheConsultedModality() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onBasePriceChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onModalitySelected(OperationType.FACTURA_A)
        viewModel.onConsultPrices()
        advanceUntilIdle()

        val consultations = assertIs<Result.Success<List<PriceConsultation>>>(
            viewModel.uiState.value.consultations
        )

        assertEquals(1, consultations.data.size)
        val consultation = consultations.data.single()
        assertEquals("FACTURA_A", consultation.selectedModality)
        assertEquals(1210.0, consultation.calculatedUnitPrice, 0.001)
        assertEquals(2420.0, consultation.calculatedSubtotal, 0.001)
    }

    @Test
    fun testConsultationsAccumulateInTheList() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onBasePriceChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onModalitySelected(OperationType.EFECTIVO_NEGRO)
        viewModel.onConsultPrices()
        advanceUntilIdle()

        viewModel.onBasePriceChange("300")
        viewModel.onModalitySelected(OperationType.FACTURA_A)
        viewModel.onConsultPrices()
        advanceUntilIdle()

        assertEquals(2, repository.storedConsultations.size)

        val consultations = assertIs<Result.Success<List<PriceConsultation>>>(
            viewModel.uiState.value.consultations
        )
        assertEquals(2, consultations.data.size)

        val efectivoNegro = consultations.data.first { it.selectedModality == "EFECTIVO_NEGRO" }
        assertEquals(1000.0, efectivoNegro.basePrice, 0.001)
        assertEquals(1350.0, efectivoNegro.calculatedUnitPrice, 0.001)
        assertEquals(2700.0, efectivoNegro.calculatedSubtotal, 0.001)

        val facturaA = consultations.data.first { it.selectedModality == "FACTURA_A" }
        assertEquals(300.0, facturaA.basePrice, 0.001)
        assertEquals(363.0, facturaA.calculatedUnitPrice, 0.001)
        assertEquals(726.0, facturaA.calculatedSubtotal, 0.001)
    }

    @Test
    fun testChangingModalityDoesNotCreateConsultation() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onBasePriceChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onModalitySelected(OperationType.LOTE_100)
        advanceUntilIdle()

        assertEquals(OperationType.LOTE_100, viewModel.uiState.value.selectedModality)
        assertTrue(repository.storedConsultations.isEmpty())

        val consultations = assertIs<Result.Success<List<PriceConsultation>>>(
            viewModel.uiState.value.consultations
        )
        assertTrue(consultations.data.isEmpty())
    }

    @Test
    fun testConsultPricesPersistsSelectedModality() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onBasePriceChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onModalitySelected(OperationType.FACTURA_A)
        viewModel.onConsultPrices()
        advanceUntilIdle()

        assertEquals(1, repository.storedConsultations.size)
        val stored = repository.storedConsultations.first()
        assertEquals(1000.0, stored.basePrice, 0.001)
        assertEquals(2, stored.quantity)
        assertEquals("FACTURA_A", stored.selectedModality)
        assertEquals(1210.0, stored.calculatedUnitPrice, 0.001)
        assertEquals(2420.0, stored.calculatedSubtotal, 0.001)
        assertIs<Result.Success<Unit>>(viewModel.uiState.value.saveState)
    }

    @Test
    fun testConsultPricesUpdatesHistoryReactively() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onBasePriceChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onConsultPrices()
        advanceUntilIdle()

        val consultations =
            assertIs<Result.Success<List<PriceConsultation>>>(viewModel.uiState.value.consultations)
        assertEquals(1, consultations.data.size)
        assertEquals("FACTURA_A", consultations.data.first().selectedModality)
        assertEquals(2420.0, consultations.data.first().calculatedSubtotal, 0.001)
    }

    @Test
    fun testConsultPricesErrorSetsErrorState() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        repository.shouldReturnError = true
        viewModel.onBasePriceChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onConsultPrices()
        advanceUntilIdle()

        assertIs<Result.Error>(viewModel.uiState.value.saveState)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testInvalidInputsDoNotPersist() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onBasePriceChange("abc")
        viewModel.onQuantityChange("0")
        viewModel.onConsultPrices()
        advanceUntilIdle()

        assertIs<Result.Error>(viewModel.uiState.value.saveState)
        assertTrue(repository.storedConsultations.isEmpty())
    }

    @Test
    fun testInputChangeClearsPreviousSaveState() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onBasePriceChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onConsultPrices()
        advanceUntilIdle()
        assertIs<Result.Success<Unit>>(viewModel.uiState.value.saveState)

        viewModel.onBasePriceChange("1500")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.saveState)
    }
}
