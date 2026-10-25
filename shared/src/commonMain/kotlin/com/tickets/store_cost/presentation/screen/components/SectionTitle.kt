package com.tickets.store_cost.presentation.screen.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tickets.store_cost.presentation.theme.AppStyles

/**
 * Título de sección homogéneo para todas las pantallas.
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = AppStyles.Typography.sectionTitle,
        modifier = modifier
    )
}
