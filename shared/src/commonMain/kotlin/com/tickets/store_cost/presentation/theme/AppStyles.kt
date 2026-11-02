package com.tickets.store_cost.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tokens visuales compartidos por toda la UI.
 *
 * Centraliza tipografías, colores y espaciados para que las pantallas no
 * dependan directamente de `MaterialTheme` ni repitan valores en `dp`.
 */
object AppStyles {

    /** Escala de espaciados usada en pantallas, tarjetas y listas. */
    object Spacing {
        val hairline: Dp = 2.dp
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 24.dp
    }

    /**
     * Dimensiones compartidas por componentes que necesitan un tamaño fijo
     * (diálogos, QR, gestos de deslizamiento).
     */
    object Sizes {
        /** Ancho máximo del formulario de login en pantallas grandes. */
        val loginFormMaxWidth: Dp = 420.dp

        /** Tamaño del indicador de carga dentro de los botones. */
        val buttonProgress: Dp = 18.dp
        val buttonProgressStroke: Dp = 2.dp

        /** Ancho del diálogo de confirmación de borrado. */
        val confirmationDialogWidth: Dp = 320.dp

        /** Tamaño del código QR del alias bancario. */
        val qrCode: Dp = 220.dp

        /** Distancia mínima de deslizamiento para pedir confirmación. */
        val swipeDismissThreshold: Dp = 96.dp

        /** Radio de las esquinas de las filas arrastrables. */
        val swipeCornerRadius: Dp = 12.dp

        /** Radio del fondo que destella en el tag "Historial". */
        val historyFlashCornerRadius: Dp = 12.dp

        /** Radios de las formas del tema Material 3. */
        val cornerSmall: Dp = 4.dp
        val cornerMedium: Dp = 4.dp
        val cornerLarge: Dp = 0.dp
    }

    /** Duraciones (en milisegundos) usadas por la lógica de presentación. */
    object Durations {
        /** Tiempo que el botón de venta queda bloqueado tras registrar. */
        const val SALE_REGISTER_DEBOUNCE_MS: Long = 1_500L

        /** Duración del destello del tag Historial. */
        const val HISTORY_FLASH_MS: Long = 600L

        /** Duración de la transición de entrada/salida del destello. */
        const val HISTORY_FLASH_FADE_MS: Long = 180L
    }

    /** Tipografías con un rol semántico dentro de la aplicación. */
    object Typography {
        val appTitle: TextStyle @Composable get() = MaterialTheme.typography.headlineSmall
        val metric: TextStyle @Composable get() = MaterialTheme.typography.headlineMedium
        val sectionTitle: TextStyle @Composable get() = MaterialTheme.typography.titleMedium
        val itemTitle: TextStyle @Composable get() = MaterialTheme.typography.titleSmall
        val itemAmount: TextStyle @Composable get() = MaterialTheme.typography.titleLarge
        val body: TextStyle @Composable get() = MaterialTheme.typography.bodyMedium
        val caption: TextStyle @Composable get() = MaterialTheme.typography.bodySmall
    }

    /** Colores con un rol semántico dentro de la aplicación. */
    object Colors {
        val secondaryText: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
        val error: Color @Composable get() = MaterialTheme.colorScheme.error
        val success: Color @Composable get() = MaterialTheme.colorScheme.primary

        /** Fondo de las tarjetas del historial registradas en el día actual. */
        val todayHistoryCard: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer

        /** Fondo de las tarjetas del historial registradas en días anteriores. */
        val pastHistoryCard: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    }
}
