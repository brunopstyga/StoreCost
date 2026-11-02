package com.tickets.store_cost.presentation.util

/**
 * Datos bancarios del comercio centralizados en un único punto.
 *
 * El alias se usa como payload del código QR y como texto visible en el menú
 * lateral. Cuando exista configuración por usuario, solo debe cambiarse esta
 * fuente de verdad.
 *
 * El payload del QR es el alias en texto plano: no debe incluir URLs, esquemas
 * ni parámetros, para que al escanearlo el lector solo muestre el alias y el
 * cliente pueda copiarlo.
 */
object BankDetails {
    const val ALIAS: String = "meugine84"
    const val QR_PAYLOAD: String = ALIAS
}
