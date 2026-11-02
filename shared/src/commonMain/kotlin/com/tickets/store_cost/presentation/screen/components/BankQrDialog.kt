package com.tickets.store_cost.presentation.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tickets.store_cost.presentation.theme.AppStyles
import com.tickets.store_cost.presentation.util.BankDetails
import com.tickets.store_cost.resources.Res
import com.tickets.store_cost.resources.bank_alias_label
import com.tickets.store_cost.resources.bank_qr_content_description
import com.tickets.store_cost.resources.bank_qr_hint
import com.tickets.store_cost.resources.bank_qr_title
import com.tickets.store_cost.resources.common_close
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource

/**
 * Diálogo modal que muestra el código QR y el alias bancario del comercio.
 * Se abre desde el menú lateral y no conoce lógica de navegación.
 */
@Composable
fun BankQrDialog(
    onDismiss: () -> Unit
) {
    // Colores fijos (negro sobre blanco) para que el QR sea legible por cualquier
    // lector sin importar el tema claro/oscuro de la app.
    val qrPainter = rememberQrCodePainter(data = BankDetails.QR_PAYLOAD) {
        colors {
            dark = QrBrush.solid(Color.Black)
            light = QrBrush.solid(Color.White)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.bank_qr_title)) },
        text = {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppStyles.Spacing.md)
            ) {
                // El margen blanco (zona de silencio) es parte del estándar QR:
                // sin él muchos lectores no detectan el código.
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(AppStyles.Spacing.md)
                ) {
                    Image(
                        painter = qrPainter,
                        contentDescription = stringResource(Res.string.bank_qr_content_description),
                        modifier = Modifier.size(AppStyles.Sizes.qrCode)
                    )
                }
                Text(
                    text = stringResource(Res.string.bank_alias_label),
                    style = AppStyles.Typography.caption,
                    color = AppStyles.Colors.secondaryText
                )
                Text(
                    text = BankDetails.ALIAS,
                    style = AppStyles.Typography.sectionTitle
                )
                Text(
                    text = stringResource(Res.string.bank_qr_hint),
                    style = AppStyles.Typography.body,
                    color = AppStyles.Colors.secondaryText
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.common_close))
            }
        }
    )
}
