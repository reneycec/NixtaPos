package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.entities.VentaDetalleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketPrinterUtil {

    data class TicketData(
        val empresaNombre: String = "EMPRESA NIXTA",
        val sucursalNombre: String = "SUCURSAL CENTRAL",
        val folio: String,
        val fechaMs: Long,
        val cajeroNombre: String = "Cajero",
        val clienteNombre: String? = null,
        val metodoPago: String,
        val subtotalCentavos: Long,
        val impuestosCentavos: Long,
        val totalCentavos: Long,
        val puntosGanados: Long = 0,
        val detalles: List<VentaDetalleEntity>
    )

    fun generatePlainTextTicket(ticket: TicketData): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaStr = dateFormat.format(Date(ticket.fechaMs))

        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine("          ${ticket.empresaNombre}")
        sb.appendLine("       ${ticket.sucursalNombre}")
        sb.appendLine("========================================")
        sb.appendLine("Folio:   ${ticket.folio}")
        sb.appendLine("Fecha:   $fechaStr")
        sb.appendLine("Cajero:  ${ticket.cajeroNombre}")
        if (!ticket.clienteNombre.isNullOrBlank()) {
            sb.appendLine("Cliente: ${ticket.clienteNombre}")
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine(String.format("%-18s %5s %6s %8s", "CANT PROD", "CANT", "P.UNIT", "TOTAL"))
        sb.appendLine("----------------------------------------")

        ticket.detalles.forEach { item ->
            val nombreCorto = if (item.producto_nombre.length > 18) item.producto_nombre.take(17) + "." else item.producto_nombre
            val cantStr = String.format("%.2f", item.cantidad)
            val unitStr = String.format("$%.2f", item.precio_unitario / 100.0)
            val subtotalStr = String.format("$%.2f", item.subtotal / 100.0)
            sb.appendLine(String.format("%-18s %5s %6s %8s", nombreCorto, cantStr, unitStr, subtotalStr))
        }

        sb.appendLine("----------------------------------------")
        sb.appendLine(String.format("%-28s $%8.2f", "SUBTOTAL:", ticket.subtotalCentavos / 100.0))
        sb.appendLine(String.format("%-28s $%8.2f", "IVA:", ticket.impuestosCentavos / 100.0))
        sb.appendLine(String.format("%-28s $%8.2f", "TOTAL:", ticket.totalCentavos / 100.0))
        sb.appendLine("----------------------------------------")
        sb.appendLine("Método de Pago: ${ticket.metodoPago}")
        if (ticket.puntosGanados > 0) {
            sb.appendLine("Puntos Ganados: ${ticket.puntosGanados} pts")
        }
        sb.appendLine("========================================")
        sb.appendLine("      ¡Gracias por su preferencia!")
        sb.appendLine("          www.nixtaerp.com")
        sb.appendLine("========================================")

        return sb.toString()
    }

    fun printViaAndroidPrintManager(context: Context, ticket: TicketData) {
        val htmlContent = """
            <html>
            <head>
                <style>
                    body { font-family: monospace; font-size: 12px; width: 280px; margin: 0 auto; }
                    .center { text-align: center; }
                    .bold { font-weight: bold; }
                    .hr { border-bottom: 1px dashed #000; margin: 6px 0; }
                    table { width: 100%; border-collapse: collapse; font-size: 11px; }
                    th, td { text-align: left; padding: 2px 0; }
                    .right { text-align: right; }
                </style>
            </head>
            <body>
                <div class="center bold" style="font-size: 14px;">${ticket.empresaNombre}</div>
                <div class="center">${ticket.sucursalNombre}</div>
                <div class="hr"></div>
                <div><b>Folio:</b> ${ticket.folio}</div>
                <div><b>Fecha:</b> ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ticket.fechaMs))}</div>
                <div><b>Cajero:</b> ${ticket.cajeroNombre}</div>
                ${if (!ticket.clienteNombre.isNullOrBlank()) "<div><b>Cliente:</b> ${ticket.clienteNombre}</div>" else ""}
                <div class="hr"></div>
                <table>
                    <thead>
                        <tr>
                            <th>Cant</th>
                            <th>Producto</th>
                            <th class="right">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${ticket.detalles.joinToString("") { item ->
                            "<tr><td>${String.format("%.1f", item.cantidad)}</td><td>${item.producto_nombre}</td><td class='right'>$${String.format("%.2f", item.subtotal / 100.0)}</td></tr>"
                        }}
                    </tbody>
                </table>
                <div class="hr"></div>
                <div class="right">Subtotal: $${String.format("%.2f", ticket.subtotalCentavos / 100.0)}</div>
                <div class="right">IVA: $${String.format("%.2f", ticket.impuestosCentavos / 100.0)}</div>
                <div class="right bold" style="font-size: 13px;">TOTAL: $${String.format("%.2f", ticket.totalCentavos / 100.0)}</div>
                <div class="hr"></div>
                <div class="center">Pago: ${ticket.metodoPago}</div>
                ${if (ticket.puntosGanados > 0) "<div class='center'>Puntos: +${ticket.puntosGanados} pts</div>" else ""}
                <div class="hr"></div>
                <div class="center bold">¡Gracias por su compra!</div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Ticket_${ticket.folio}")
                printManager.print(
                    "Ticket_${ticket.folio}",
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}
