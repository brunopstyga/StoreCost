package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.repository.ISaleRepository
import kotlinx.datetime.Clock
import kotlin.math.round
import org.koin.core.annotation.Factory

@Factory
class RegisterSaleUseCase(
    private val repository: ISaleRepository,
    private val calculatePriceUseCase: CalculatePriceUseCase
) {

    suspend operator fun invoke(
        basePrice: Double,
        quantity: Int,
        operationType: OperationType,
        timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ): Result<Unit> {
        if (basePrice <= 0.0) {
            return Result.Error("El precio base debe ser mayor a cero.")
        }
        if (quantity <= 0) {
            return Result.Error("La cantidad debe ser mayor a cero.")
        }

        val unitPrice = calculatePriceUseCase(basePrice, operationType)
        val subtotal = round((unitPrice * quantity) * 100.0) / 100.0

        val sale = Sale(
            id = 0L,
            basePrice = basePrice,
            quantity = quantity,
            operationType = operationType,
            unitPrice = unitPrice,
            subtotal = subtotal,
            timestamp = timestamp
        )

        return repository.registerSale(sale)
    }

    suspend operator fun invoke(sale: Sale): Result<Unit> {
        return repository.registerSale(sale)
    }
}
