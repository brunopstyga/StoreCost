package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tickets.store_cost.presentation.theme.AppStyles
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.app_name
import com.tickets.store_cost.resources.nav_logout
import com.tickets.store_cost.resources.nav_share_logs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Item del menú lateral de navegación.
 */
data class NavigationItem(
    val route: String,
    val label: StringResource
)

/**
 * Contenido del menú desplegable (Drawer) que permite alternar entre
 * Gestión de Precios y Ventas y Cierre.
 */
@Composable
fun NavigationDrawerContent(
    items: List<NavigationItem>,
    selectedRoute: String,
    onItemClick: (String) -> Unit,
    onShareLogsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(AppStyles.Spacing.xl))
        Text(
            text = stringResource(Res.string.app_name),
            style = AppStyles.Typography.appTitle,
            modifier = Modifier.padding(
                horizontal = AppStyles.Spacing.xl,
                vertical = AppStyles.Spacing.sm
            )
        )
        Spacer(modifier = Modifier.height(AppStyles.Spacing.sm))
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(stringResource(item.label)) },
                selected = selectedRoute == item.route,
                onClick = { onItemClick(item.route) },
                modifier = Modifier.padding(
                    horizontal = AppStyles.Spacing.md,
                    vertical = AppStyles.Spacing.xs
                )
            )
        }
        Spacer(modifier = Modifier.height(AppStyles.Spacing.lg))
        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.nav_share_logs)) },
            selected = false,
            onClick = onShareLogsClick,
            modifier = Modifier.padding(
                horizontal = AppStyles.Spacing.md,
                vertical = AppStyles.Spacing.xs
            )
        )
        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.nav_logout)) },
            selected = false,
            onClick = onLogoutClick,
            modifier = Modifier.padding(
                horizontal = AppStyles.Spacing.md,
                vertical = AppStyles.Spacing.xs
            )
        )
    }
}
