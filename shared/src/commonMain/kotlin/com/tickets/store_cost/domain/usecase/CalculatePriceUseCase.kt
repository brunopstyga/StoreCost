package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.PriceCalculationResult
import com.tickets.store_cost.domain.util.roundToCents
import org.koin.core.annotation.Factory

@Factory
class CalculatePriceUseCase {

    operator fun invoke(basePrice: Double, operationType: OperationType): Double =
        operationType.calculateUnitPrice(basePrice).roundToCents()

    fun calculate(
        basePrice: Double,
        quantity: Int,
        operationType: OperationType
    ): PriceCalculationResult {
        val unitPrice = invoke(basePrice, operationType)
        val subtotal = (unitPrice * quantity).roundToCents()
        return PriceCalculationResult(
            operationType = operationType,
            unitPrice = unitPrice,
            subtotal = subtotal
        )
    }
}
