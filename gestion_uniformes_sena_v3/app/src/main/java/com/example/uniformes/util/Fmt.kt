package com.example.uniformes.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Equivalente a formatFecha / formatMoney / toFixed de ui.js. */
object Fmt {
    private val esCO = Locale("es", "CO")

    /** 2026-04-15 -> 15/04/2026 */
    fun fecha(iso: String): String {
        val p = iso.split("-")
        return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
    }

    fun fechaHora(millis: Long): String =
        SimpleDateFormat("d/M/yyyy, h:mm:ss a", esCO).format(Date(millis))

    fun hoyIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun hoyLocal(): String = SimpleDateFormat("d/M/yyyy", esCO).format(Date())

    /** $1.234 (separador de miles es-CO). */
    fun dinero(n: Number): String {
        val nf = NumberFormat.getNumberInstance(esCO)
        nf.minimumFractionDigits = 0
        nf.maximumFractionDigits = 2
        return "\$" + nf.format(n)
    }

    /** $138 (sin separadores, como las tablas del HTML). */
    fun simple(n: Int): String = "\$$n"

    /** $154.56 (equivale a toFixed(2)). */
    fun dos(n: Double): String = "\$" + String.format(Locale.US, "%.2f", n)

    fun estado(e: String): String = when (e) {
        "pendiente" -> "Pendiente"
        "en_produccion" -> "En Producción"
        "completado" -> "Completado"
        "entregado" -> "Entregado"
        "cortado" -> "Cortado"
        "costura" -> "Costura"
        "acabado" -> "Acabado"
        else -> e.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

    fun cap(s: String): String = s.replaceFirstChar { it.uppercase() }
}
