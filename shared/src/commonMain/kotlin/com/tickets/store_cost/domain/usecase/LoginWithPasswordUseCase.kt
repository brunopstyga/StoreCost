package com.tickets.store_cost.domain.usecase

import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.domain.repository.IAuthRepository
import org.koin.core.annotation.Factory

/**
 * Valida la contraseña ingresada y delega la verificación al repositorio.
 * El campo vacío se rechaza antes de tocar la capa de datos.
 */
@Factory
class LoginWithPasswordUseCase(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(password: String): Result<Boolean> =
        if (password.isBlank()) {
            Result.Error(ErrorCode.EMPTY_PASSWORD)
        } else {
            repository.login(password)
        }
}
