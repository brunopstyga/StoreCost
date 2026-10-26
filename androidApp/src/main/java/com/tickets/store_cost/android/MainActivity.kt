package com.tickets.store_cost.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.tickets.store_cost.android.report.EmailReportLauncher
import com.tickets.store_cost.presentation.screen.StoreCostApp
import com.tickets.store_cost.presentation.theme.AppTheme

/**
 * Actividad principal: configura el tema Material 3, renderiza el contenedor
 * raíz de navegación compartido y abre el correo cuando se toca la notificación
 * del reporte mensual.
 */
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        if (savedInstanceState == null) {
            handleReportIntent(intent)
        }
        setContent {
            AppTheme {
                StoreCostApp(
                    onSendReport = { filePath ->
                        EmailReportLauncher.launch(this, filePath)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleReportIntent(intent)
    }

    private fun handleReportIntent(intent: Intent?) {
        val filePath = intent?.getStringExtra(EXTRA_REPORT_PATH) ?: return
        intent.removeExtra(EXTRA_REPORT_PATH)
        EmailReportLauncher.launch(this, filePath)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_REPORT_PATH = "extra_report_path"
    }
}
