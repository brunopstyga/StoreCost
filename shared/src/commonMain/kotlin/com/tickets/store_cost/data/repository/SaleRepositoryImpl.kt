package com.tickets.store_cost.data.repository

import com.tickets.store_cost.core.logging.AppLogger
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.OperationType
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.model.Sale
import com.tickets.store_cost.domain.model.resultOf
import com.tickets.store_cost.domain.repository.ISaleRepository
import com.tickets.store_cost.data.local.dao.SaleDao
import com.tickets.store_cost.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.coroutines.cancellation.CancellationException

@Single(binds = [ISaleRepository::class])
class SaleRepositoryImpl(
    private val saleDao: SaleDao
) : ISaleRepository {

    override suspend fun registerSale(sale: Sale): Result<Unit> =
        resultOf(ErrorCode.REGISTER_SALE_FAILED) {
            saleDao.insertSale(
                SaleEntity(
                    id = sale.id,
                    basePrice = sale.basePrice,
                    quantity = sale.quantity,
                    operationType = sale.operationType.name,
                    unitPrice = sale.unitPrice,
                    subtotal = sale.subtotal,
                    timestamp = sale.timestamp
                )
            )
            Unit
        }

    override fun getSalesByDateRange(startTime: Long, endTime: Long): Flow<Result<List<Sale>>> =
        saleDao.observeSalesByDateRange(startTime, endTime)
            .map<List<SaleEntity>, Result<List<Sale>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { e ->
                // La cancelación debe propagarse: no es un error de negocio.
                if (e is CancellationException) throw e
                AppLogger.e("No se pudo observar el historial de ventas", e)
                emit(Result.Error(ErrorCode.FETCH_SALES_FAILED, e.message))
            }

    override suspend fun deleteSalesBefore(limit: Long): Result<Int> =
        resultOf(ErrorCode.DELETE_SALES_FAILED) {
            saleDao.deleteSalesBefore(limit)
        }

    private fun SaleEntity.toDomain(): Sale = Sale(
        id = id,
        basePrice = basePrice,
        quantity = quantity,
        operationType = OperationType.valueOf(operationType),
        unitPrice = unitPrice,
        subtotal = subtotal,
        timestamp = timestamp
    )
}
