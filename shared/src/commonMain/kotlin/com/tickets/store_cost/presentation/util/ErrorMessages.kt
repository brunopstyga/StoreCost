package com.tickets.store_cost.presentation.util

import androidx.compose.runtime.Composable
import com.tickets.store_cost.domain.model.ErrorCode
import com.tickets.store_cost.domain.model.Result
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.error_fetch_closing_failed
import com.tickets.store_cost.resources.error_fetch_consultations_failed
import com.tickets.store_cost.resources.error_fetch_daily_sales_failed
import com.tickets.store_cost.resources.error_fetch_monthly_sales_failed
import com.tickets.store_cost.resources.error_fetch_sales_failed
import com.tickets.store_cost.resources.error_generate_report_failed
import com.tickets.store_cost.resources.error_invalid_base_price
import com.tickets.store_cost.resources.error_invalid_base_price_and_quantity
import com.tickets.store_cost.resources.error_invalid_month
import com.tickets.store_cost.resources.error_invalid_quantity
import com.tickets.store_cost.resources.error_invalid_sale_amount_and_quantity
import com.tickets.store_cost.resources.error_no_sales_today
import com.tickets.store_cost.resources.error_register_sale_failed
import com.tickets.store_cost.resources.error_save_closing_failed
import com.tickets.store_cost.resources.error_save_consultation_failed
import com.tickets.store_cost.resources.error_save_report_failed
import com.tickets.store_cost.resources.error_unknown
import org.jetbrains.compose.resources.stringResource

/**
 * Traduce un [ErrorCode] del dominio al texto localizado de la UI.
 *
 * Mantiene el dominio libre de recursos de Compose: el código viaja en
 * [Result.Error] y se resuelve recién en la capa de presentación.
 */
@Composable
fun ErrorCode.displayMessage(): String = when (this) {
    ErrorCode.REGISTER_SALE_FAILED -> stringResource(Res.string.error_register_sale_failed)
    ErrorCode.FETCH_SALES_FAILED -> stringResource(Res.string.error_fetch_sales_failed)
    ErrorCode.FETCH_DAILY_SALES_FAILED -> stringResource(Res.string.error_fetch_daily_sales_failed)
    ErrorCode.INVALID_BASE_PRICE -> stringResource(Res.string.error_invalid_base_price)
    ErrorCode.INVALID_QUANTITY -> stringResource(Res.string.error_invalid_quantity)
    ErrorCode.INVALID_SALE_AMOUNT_AND_QUANTITY ->
        stringResource(Res.string.error_invalid_sale_amount_and_quantity)
    ErrorCode.NO_SALES_TODAY -> stringResource(Res.string.error_no_sales_today)
    ErrorCode.SAVE_CLOSING_FAILED -> stringResource(Res.string.error_save_closing_failed)
    ErrorCode.FETCH_CLOSING_FAILED -> stringResource(Res.string.error_fetch_closing_failed)
    ErrorCode.SAVE_CONSULTATION_FAILED -> stringResource(Res.string.error_save_consultation_failed)
    ErrorCode.FETCH_CONSULTATIONS_FAILED ->
        stringResource(Res.string.error_fetch_consultations_failed)
    ErrorCode.INVALID_BASE_PRICE_AND_QUANTITY ->
        stringResource(Res.string.error_invalid_base_price_and_quantity)
    ErrorCode.INVALID_MONTH -> stringResource(Res.string.error_invalid_month)
    ErrorCode.FETCH_MONTHLY_SALES_FAILED ->
        stringResource(Res.string.error_fetch_monthly_sales_failed)
    ErrorCode.GENERATE_REPORT_FAILED -> stringResource(Res.string.error_generate_report_failed)
    ErrorCode.SAVE_REPORT_FAILED -> stringResource(Res.string.error_save_report_failed)
    ErrorCode.UNKNOWN -> stringResource(Res.string.error_unknown)
}

/**
 * Texto localizado de un [Result.Error].
 */
@Composable
fun Result.Error.displayMessage(): String = code.displayMessage()
