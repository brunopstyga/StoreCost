package com.tickets.store_cost.presentation.viewmodel

import com.tickets.store_cost.data.repository.FakeClosingRepository
import com.tickets.store_cost.data.repository.FakeSaleRepository
import com.tickets.store_cost.domain.model.DailyClosing
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.repository.IClosingRepository
import com.tickets.store_cost.domain.repository.ISaleRepository
import com.tickets.store_cost.domain.usecase.CalculatePriceUseCase
import com.tickets.store_cost.domain.usecase.CloseDayUseCase
import com.tickets.store_cost.domain.usecase.GetDailyClosingUseCase
import com.tickets.store_cost.domain.usecase.GetSalesSummaryUseCase
import com.tickets.store_cost.domain.usecase.RegisterSaleUseCase
import com.tickets.store_cost.presentation.state.ClosingPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSaleRepository
    private lateinit var closingRepository: FakeClosingRepository
    private lateinit var viewModel: SalesViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeSaleRepository(sampleSales())
        closingRepository = FakeClosingRepository()
        viewModel = buildViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleSales(): List<Sale> = listOf(
        Sale(
            id = 1,
            basePrice = 1000.0,
            quantity = 1,
            operationType = OperationType.FACTURA_A,
            unitPrice = 1210.0,
            subtotal = 1210.0,
            timestamp = 1_000L
        ),
        Sale(
            id = 2,
            basePrice = 1000.0,
            quantity = 1,
            operationType = OperationType.EFECTIVO_NEGRO,
            unitPrice = 1350.0,
            subtotal = 1350.0,
            timestamp = 2_000L
        )
    )

    private fun buildViewModel(
        saleRepository: ISaleRepository,
        closingRepo: IClosingRepository = closingRepository
    ): SalesViewModel =
        SalesViewModel(
            registerSaleUseCase = RegisterSaleUseCase(saleRepository, CalculatePriceUseCase()),
            getSalesSummaryUseCase = GetSalesSummaryUseCase(saleRepository),
            closeDayUseCase = CloseDayUseCase(saleRepository, closingRepo),
            getDailyClosingUseCase = GetDailyClosingUseCase(closingRepo)
        )

    private fun todayIso(): String = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .toString()

    @Test
    fun testInitialFormState() {
        val state = viewModel.salesUiState.value

        assertEquals("1000.0", state.amount)
        assertEquals("1", state.quantity)
        assertIs<Result.Loading>(state.summary)
        assertNull(state.registerState)
    }

    @Test
    fun testRegisterSaleUsesEnteredAmountWithoutSurcharge() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }

        viewModel.onAmountChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onRegisterSale()
        advanceUntilIdle()

        val state = viewModel.salesUiState.value

        assertIs<Result.Success<Unit>>(state.registerState)
        assertTrue(state.isRegistered)
        val registered = repository.lastRegisteredSale
        assertEquals(1000.0, registered?.basePrice ?: 0.0, 0.001)
        assertEquals(2, registered?.quantity)
        assertEquals(1000.0, registered?.unitPrice ?: 0.0, 0.001)
        assertEquals(2000.0, registered?.subtotal ?: 0.0, 0.001)
    }

    @Test
    fun testRegisterSaleError() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }

        repository.shouldReturnError = true
        viewModel.onAmountChange("1000")
        viewModel.onQuantityChange("2")
        viewModel.onRegisterSale()
        advanceUntilIdle()

        val state = viewModel.salesUiState.value

        assertIs<Result.Error>(state.registerState)
        assertEquals("Database error", state.errorMessage)
    }

    @Test
    fun testInvalidInputsDoNotRegister() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }

        viewModel.onAmountChange("abc")
        viewModel.onQuantityChange("0")
        viewModel.onRegisterSale()
        advanceUntilIdle()

        val state = viewModel.salesUiState.value

        assertIs<Result.Error>(state.registerState)
        assertNull(repository.lastRegisteredSale)
    }

    @Test
    fun testSalesUiStateSuccess() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.salesUiState.value

        assertIs<Result.Success<*>>(state.summary)
        assertEquals(2560.0, state.totalBilled, 0.001)
        assertEquals(2, state.totalSalesCount)
        assertEquals(2, state.sales.size)
        assertEquals(1210.0, state.breakdown[OperationType.FACTURA_A] ?: 0.0, 0.001)
        assertEquals(1350.0, state.breakdown[OperationType.EFECTIVO_NEGRO] ?: 0.0, 0.001)
    }

    @Test
    fun testSalesUiStateError() = runTest(dispatcher) {
        val errorViewModel = buildViewModel(object : ISaleRepository {
            override suspend fun registerSale(sale: Sale): Result<Unit> = Result.Success(Unit)

            override fun getSalesByDateRange(
                startTime: Long,
                endTime: Long
            ): Flow<Result<List<Sale>>> =
                flowOf(Result.Error(ErrorCode.FETCH_SALES_FAILED, "Base de datos no disponible"))

            override suspend fun deleteSalesBefore(limit: Long): Result<Int> = Result.Success(0)
        })
        backgroundScope.launch { errorViewModel.salesUiState.collect {} }
        advanceUntilIdle()

        val state = errorViewModel.salesUiState.value

        assertIs<Result.Error>(state.summary)
        assertEquals("Base de datos no disponible", state.errorMessage)
    }

    @Test
    fun testPeriodSelectionUpdatesState() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }
        viewModel.onPeriodSelected(ClosingPeriod.MONTHLY)
        advanceUntilIdle()

        val state = viewModel.salesUiState.value

        assertEquals(ClosingPeriod.MONTHLY, state.period)
        assertIs<Result.Success<*>>(state.summary)
    }

    @Test
    fun testCloseDayIsEnabledWhenThereAreSalesToday() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.salesUiState.value

        assertTrue(state.canCloseDay)
        assertEquals(2560.0, state.todayTotal, 0.001)
        assertEquals(2, state.todaySalesCount)
    }

    @Test
    fun testCloseDayIsDisabledWithoutSales() = runTest(dispatcher) {
        val emptyViewModel = buildViewModel(FakeSaleRepository(emptyList()))
        backgroundScope.launch { emptyViewModel.salesUiState.collect {} }
        advanceUntilIdle()

        assertFalse(emptyViewModel.salesUiState.value.canCloseDay)
    }

    @Test
    fun testRequestCloseDayShowsConfirmationWithoutPersisting() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }
        advanceUntilIdle()

        viewModel.onRequestCloseDay()
        advanceUntilIdle()

        val state = viewModel.salesUiState.value
        val pending = assertNotNull(state.pendingClosing)
        assertEquals(2560.0, pending.totalAmount, 0.001)
        assertEquals(2, pending.salesCount)
        assertTrue(closingRepository.savedClosings.isEmpty())
        assertNull(state.savedClosing)
    }

    @Test
    fun testDismissClosingDialogKeepsNothingPersisted() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }
        advanceUntilIdle()

        viewModel.onRequestCloseDay()
        advanceUntilIdle()
        viewModel.onDismissClosingDialog()
        advanceUntilIdle()

        assertNull(viewModel.salesUiState.value.pendingClosing)
        assertTrue(closingRepository.savedClosings.isEmpty())
    }

    @Test
    fun testConfirmCloseDayPersistsClosing() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }
        advanceUntilIdle()

        viewModel.onRequestCloseDay()
        advanceUntilIdle()
        viewModel.onConfirmCloseDay()
        advanceUntilIdle()

        val state = viewModel.salesUiState.value
        assertNull(state.pendingClosing)
        val saved = assertNotNull(state.savedClosing)
        assertEquals(2560.0, saved.totalAmount, 0.001)
        assertEquals(2, saved.salesCount)
        assertEquals(todayIso(), saved.closingDate)
        assertEquals(1, closingRepository.savedClosings.size)
        assertIs<Result.Success<DailyClosing>>(state.closingState)
    }

    @Test
    fun testConfirmCloseDayErrorIsShown() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }
        advanceUntilIdle()

        viewModel.onRequestCloseDay()
        advanceUntilIdle()

        closingRepository.shouldReturnError = true
        viewModel.onConfirmCloseDay()
        advanceUntilIdle()

        val state = viewModel.salesUiState.value
        assertNull(state.pendingClosing)
        assertIs<Result.Error>(state.closingState)
        assertNotNull(state.closingErrorMessage)
        assertTrue(closingRepository.savedClosings.isEmpty())
    }

    @Test
    fun testRequestCloseDayWithoutSalesShowsError() = runTest(dispatcher) {
        val emptyViewModel = buildViewModel(FakeSaleRepository(emptyList()))
        backgroundScope.launch { emptyViewModel.salesUiState.collect {} }
        advanceUntilIdle()

        emptyViewModel.onRequestCloseDay()
        advanceUntilIdle()

        val state = emptyViewModel.salesUiState.value
        assertNull(state.pendingClosing)
        assertIs<Result.Error>(state.closingState)
        assertNotNull(state.closingErrorMessage)
    }

    @Test
    fun testExistingClosingIsLoadedOnStart() = runTest(dispatcher) {
        closingRepository = FakeClosingRepository(
            listOf(
                DailyClosing(
                    id = 1,
                    closingDate = todayIso(),
                    totalAmount = 1000.0,
                    salesCount = 1,
                    closedAt = 1_000L
                )
            )
        )
        viewModel = buildViewModel(repository)
        backgroundScope.launch { viewModel.salesUiState.collect {} }
        advanceUntilIdle()

        val saved = assertNotNull(viewModel.salesUiState.value.savedClosing)
        assertEquals(1000.0, saved.totalAmount, 0.001)
        assertEquals(1, saved.salesCount)
    }

    @Test
    fun testHistoryPeriodRequestsFullRangeAndExposesAllSales() = runTest(dispatcher) {
        val requestedRanges = mutableListOf<Pair<Long, Long>>()
        val historicalSales = listOf(
            Sale(
                id = 1,
                basePrice = 1000.0,
                quantity = 1,
                operationType = OperationType.FACTURA_A,
                unitPrice = 1210.0,
                subtotal = 1210.0,
                timestamp = 1_000L
            ),
            Sale(
                id = 2,
                basePrice = 1000.0,
                quantity = 1,
                operationType = OperationType.FACTURA_A,
                unitPrice = 1210.0,
                subtotal = 1210.0,
                timestamp = 1_700_000_000_000L
            )
        )
        val historyRepository = object : ISaleRepository {
            override suspend fun registerSale(sale: Sale): Result<Unit> = Result.Success(Unit)

            override fun getSalesByDateRange(
                startTime: Long,
                endTime: Long
            ): Flow<Result<List<Sale>>> {
                requestedRanges += startTime to endTime
                return flowOf(Result.Success(historicalSales.filter { it.timestamp in startTime..endTime }))
            }

            override suspend fun deleteSalesBefore(limit: Long): Result<Int> = Result.Success(0)
        }
        val historyViewModel = buildViewModel(historyRepository)
        backgroundScope.launch { historyViewModel.salesUiState.collect {} }

        historyViewModel.onPeriodSelected(ClosingPeriod.HISTORY)
        advanceUntilIdle()

        val state = historyViewModel.salesUiState.value

        assertEquals(ClosingPeriod.HISTORY, state.period)
        assertTrue(state.isHistory)
        assertFalse(state.showSaleForm)
        assertTrue(requestedRanges.contains(Long.MIN_VALUE to Long.MAX_VALUE))
        assertEquals(2, state.sales.size)
    }

    @Test
    fun testMonthlyPeriodExposesMonthSummaryAndHidesDailyControls() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }

        viewModel.onPeriodSelected(ClosingPeriod.MONTHLY)
        advanceUntilIdle()

        val state = viewModel.salesUiState.value

        assertEquals(ClosingPeriod.MONTHLY, state.period)
        assertTrue(state.isMonthly)
        assertFalse(state.isDaily)
        assertFalse(state.showSaleForm)
        assertIs<Result.Success<*>>(state.summary)
        assertEquals(2560.0, state.totalBilled, 0.001)
        assertEquals(2, state.totalSalesCount)
    }

    @Test
    fun testDateRangeSelectionIsIgnoredOutsideDaily() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }

        viewModel.onPeriodSelected(ClosingPeriod.MONTHLY)
        advanceUntilIdle()
        val monthlyRange = viewModel.salesUiState.value.let { it.startTime to it.endTime }

        viewModel.onDateRangeSelected(1_000L, 2_000L)
        advanceUntilIdle()

        val state = viewModel.salesUiState.value
        assertEquals(monthlyRange.first, state.startTime)
        assertEquals(monthlyRange.second, state.endTime)
    }

    @Test
    fun testDateRangeSelectionAppliesOnDaily() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }

        viewModel.onDateRangeSelected(1_000L, 2_000L)
        advanceUntilIdle()

        val state = viewModel.salesUiState.value
        assertEquals(ClosingPeriod.DAILY, state.period)
        assertEquals(1_000L, state.startTime)
        assertEquals(2_000L, state.endTime)
    }

    @Test
    fun testSelectingDailyRestoresFormAndClosing() = runTest(dispatcher) {
        backgroundScope.launch { viewModel.salesUiState.collect {} }

        viewModel.onPeriodSelected(ClosingPeriod.HISTORY)
        advanceUntilIdle()
        viewModel.onPeriodSelected(ClosingPeriod.DAILY)
        advanceUntilIdle()

        val state = viewModel.salesUiState.value

        assertEquals(ClosingPeriod.DAILY, state.period)
        assertFalse(state.isHistory)
        assertTrue(state.showSaleForm)
    }

    @Test
    fun testRegisteringSaleUpdatesHistory() = runTest(dispatcher) {
        val historyRepository = InMemorySaleRepository(sampleSales())
        val historyViewModel = buildViewModel(historyRepository)
        backgroundScope.launch { historyViewModel.salesUiState.collect {} }

        historyViewModel.onPeriodSelected(ClosingPeriod.HISTORY)
        advanceUntilIdle()
        assertEquals(2, historyViewModel.salesUiState.value.sales.size)

        historyViewModel.onAmountChange("500")
        historyViewModel.onQuantityChange("2")
        historyViewModel.onRegisterSale()
        advanceUntilIdle()

        assertEquals(3, historyViewModel.salesUiState.value.sales.size)
    }
}

/**
 * Repositorio en memoria que emite las ventas registradas para poder validar
 * la actualización reactiva del historial.
 */
private class InMemorySaleRepository(initialSales: List<Sale>) : ISaleRepository {

    private val salesFlow = MutableStateFlow(initialSales)

    override suspend fun registerSale(sale: Sale): Result<Unit> {
        salesFlow.update { current ->
            current + sale.copy(id = (current.maxOfOrNull { it.id } ?: 0L) + 1L)
        }
        return Result.Success(Unit)
    }

    override fun getSalesByDateRange(startTime: Long, endTime: Long): Flow<Result<List<Sale>>> =
        salesFlow.map { sales ->
            Result.Success(sales.filter { it.timestamp in startTime..endTime })
        }

    override suspend fun deleteSalesBefore(limit: Long): Result<Int> = Result.Success(0)
}
