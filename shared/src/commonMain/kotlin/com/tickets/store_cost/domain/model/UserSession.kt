package com.tickets.store_cost.domain.model

/**
 * Sesión local del usuario autenticado.
 *
 * El dominio solo conoce si hay una sesión activa; el hash de la contraseña
 * y el flag persistido pertenecen a la capa de datos.
 */
data class UserSession(
    val isAuthenticated: Boolean
)
