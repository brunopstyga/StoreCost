package com.tickets.store_cost.data.mapper

import com.tickets.store_cost.data.local.entity.PriceConsultationEntity
import com.tickets.store_cost.domain.model.PriceConsultation

/**
 * Mapeos entre la entidad de Room y el modelo puro de dominio.
 */
fun PriceConsultationEntity.toDomain(): PriceConsultation = PriceConsultation(
    id = id,
    basePrice = basePrice,
    quantity = quantity,
    selectedModality = selectedModality,
    calculatedUnitPrice = calculatedUnitPrice,
    calculatedSubtotal = calculatedSubtotal,
    timestamp = timestamp
)

fun PriceConsultation.toEntity(): PriceConsultationEntity = PriceConsultationEntity(
    id = id,
    basePrice = basePrice,
    quantity = quantity,
    selectedModality = selectedModality,
    calculatedUnitPrice = calculatedUnitPrice,
    calculatedSubtotal = calculatedSubtotal,
    timestamp = timestamp
)
