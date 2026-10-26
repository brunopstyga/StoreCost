package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IAuthRepository
import org.koin.core.annotation.Factory

/**
 * Cierra la sesión local para que la UI vuelva a la pantalla de login.
 */
@Factory
class LogoutUseCase(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
