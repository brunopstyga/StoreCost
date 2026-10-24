package com.tickets.store_cost.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tickets.store_cost.presentation.screen.components.NavigationDrawerContent
import com.tickets.store_cost.presentation.screen.components.NavigationItem
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.app_name
import com.tickets.store_cost.resources.nav_prices
import com.tickets.store_cost.resources.nav_sales
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Rutas de navegación compartidas entre plataformas.
 */
object Routes {
    const val PRICES = "prices"
    const val SALES = "sales"
}

private val menuItems = listOf(
    NavigationItem(Routes.PRICES, Res.string.nav_prices),
    NavigationItem(Routes.SALES, Res.string.nav_sales)
)

/**
 * Contenedor raíz de la aplicación: menú de navegación (Drawer) + Scaffold con
 * TopAppBar + NavHost con las pantallas compartidas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreCostApp(
    onSendReport: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentRoute by remember { mutableStateOf(Routes.PRICES) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            entry.destination.route?.let { currentRoute = it }
        }
    }

    val currentTitle = menuItems.firstOrNull { it.route == currentRoute }?.label ?: Res.string.app_name

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                items = menuItems,
                selectedRoute = currentRoute,
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.PRICES) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(currentTitle)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text(text = "☰", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.PRICES,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.PRICES) {
                    GestionPreciosScreen()
                }
                composable(Routes.SALES) {
                    VentasScreen(onSendReport = onSendReport)
                }
            }
        }
    }
}
