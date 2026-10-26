package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/**
 * Expone el flujo reactivo con el estado de la sesión local.
 */
@Factory
class CheckAuthStatusUseCase(
    private val repository: IAuthRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.observeIsLoggedIn()
}
