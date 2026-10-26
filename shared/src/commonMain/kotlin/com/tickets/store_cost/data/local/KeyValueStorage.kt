package com.tickets.store_cost.data.local

/**
 * Abstracción mínima de almacenamiento clave-valor para persistir la sesión
 * y el hash de la contraseña sin acoplar el dominio a Android o iOS.
 */
interface KeyValueStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}
