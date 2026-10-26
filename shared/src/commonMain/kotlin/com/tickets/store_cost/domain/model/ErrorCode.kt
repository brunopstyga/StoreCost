package com.tickets.store_cost.domain.model

/**
 * Catálogo de errores de negocio/datos independiente de la UI.
 *
 * El dominio y la capa de datos solo identifican el error con este código; la
 * capa de presentación es la que lo traduce a un texto localizado desde
 * `strings.xml` (ver `ErrorCode.displayMessage()` en `presentation/util`).
 */
enum class ErrorCode {
    // Ventas y cierre diario
    REGISTER_SALE_FAILED,
    FETCH_SALES_FAILED,
    FETCH_DAILY_SALES_FAILED,
    INVALID_BASE_PRICE,
    INVALID_QUANTITY,
    INVALID_SALE_AMOUNT_AND_QUANTITY,
    NO_SALES_TODAY,
    SAVE_CLOSING_FAILED,
    FETCH_CLOSING_FAILED,

    // Consultas de precios
    SAVE_CONSULTATION_FAILED,
    FETCH_CONSULTATIONS_FAILED,
    INVALID_BASE_PRICE_AND_QUANTITY,

    // Reporte mensual y respaldos
    INVALID_MONTH,
    FETCH_MONTHLY_SALES_FAILED,
    GENERATE_REPORT_FAILED,
    SAVE_REPORT_FAILED,

    // Limpieza periódica de datos
    DELETE_PRICES_FAILED,
    DELETE_SALES_FAILED,
    DELETE_CLOSINGS_FAILED,

    // Autenticación local
    EMPTY_PASSWORD,
    INVALID_PASSWORD,
    LOGIN_FAILED,
    LOGOUT_FAILED,
    SAVE_PASSWORD_FAILED,

    // Fallback
    UNKNOWN
}
