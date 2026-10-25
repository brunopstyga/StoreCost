package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.tickets.store_cost.presentation.theme.AppStyles

/**
 * Tarjeta reutilizable con el padding y la separación vertical estándar.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = AppStyles.Spacing.lg,
    verticalSpacing: Dp = AppStyles.Spacing.sm,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content
        )
    }
}
