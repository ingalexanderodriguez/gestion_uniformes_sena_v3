package com.example.uniformes.util

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.uniformes.R

/** Reemplaza las clases CSS .badge-*: cambia fondo y color del texto según el estado. */
object Badges {
    private class Estilo(val bg: Int, val fg: Int)

    private val amber = Estilo(R.drawable.bg_badge_amber, R.color.amber_800)
    private val brand = Estilo(R.drawable.bg_badge_brand, R.color.brand_800)
    private val green = Estilo(R.drawable.bg_badge_green, R.color.green_800)
    private val yellow = Estilo(R.drawable.bg_badge_yellow, R.color.yellow_800)
    private val purple = Estilo(R.drawable.bg_badge_purple, R.color.purple_800)
    private val red = Estilo(R.drawable.bg_badge_red, R.color.red_800)
    private val gray = Estilo(R.drawable.bg_badge_gray, R.color.gray_700)

    fun aplicar(tv: TextView, clave: String) {
        val e = when (clave) {
            "pendiente" -> amber
            "en_produccion", "acabado", "moderado" -> brand
            "completado", "entregado", "pagada" -> green
            "cortado", "media" -> yellow
            "costura" -> purple
            "alta", "grave", "anulada" -> red
            else -> gray // baja, leve
        }
        tv.setBackgroundResource(e.bg)
        tv.setTextColor(ContextCompat.getColor(tv.context, e.fg))
    }
}
