package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.app_name
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
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(stringResource(item.label)) },
                selected = selectedRoute == item.route,
                onClick = { onItemClick(item.route) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
