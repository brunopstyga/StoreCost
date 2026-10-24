package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.PriceCalculationResult
import kotlin.math.round
import org.koin.core.annotation.Factory

@Factory
class CalculatePriceUseCase {

    operator fun invoke(basePrice: Double, operationType: OperationType): Double {
        val calculated = operationType.calculateUnitPrice(basePrice)
        return round(calculated * 100.0) / 100.0
    }

    fun calculate(
        basePrice: Double,
        quantity: Int,
        operationType: OperationType
    ): PriceCalculationResult {
        val unitPrice = invoke(basePrice, operationType)
        val subtotal = round((unitPrice * quantity) * 100.0) / 100.0
        return PriceCalculationResult(
            operationType = operationType,
            unitPrice = unitPrice,
            subtotal = subtotal
        )
    }
}
