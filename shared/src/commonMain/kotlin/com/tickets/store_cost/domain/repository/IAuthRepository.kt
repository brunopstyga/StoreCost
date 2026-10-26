package com.tickets.store_cost.domain.repository

import com.tickets.store_cost.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de autenticación local por contraseña.
 *
 * La contraseña nunca viaja al dominio en texto plano persistido: la capa de
 * datos es la responsable de hashearla antes de guardarla.
 */
interface IAuthRepository {

    /**
     * Verifica la contraseña y, si es correcta, inicia la sesión.
     * [Result.Success] con `false` indica credenciales inválidas.
     */
    suspend fun login(password: String): Result<Boolean>

    /** Emite de forma reactiva si existe una sesión activa. */
    fun observeIsLoggedIn(): Flow<Boolean>

    /** Cierra la sesión local. */
    suspend fun logout(): Result<Unit>

    /** Guarda la contraseña inicial (hasheada) si todavía no existe. */
    suspend fun saveInitialPassword(password: String): Result<Unit>
}
