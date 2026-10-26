package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IAuthRepository
import org.koin.core.annotation.Factory

/**
 * Guarda la contraseña inicial del sistema. El repositorio se encarga de
 * hashearla antes de persistirla.
 */
@Factory
class SavePasswordUseCase(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(password: String): Result<Unit> =
        if (password.isBlank()) {
            Result.Error(ErrorCode.EMPTY_PASSWORD)
        } else {
            repository.saveInitialPassword(password)
        }
}
