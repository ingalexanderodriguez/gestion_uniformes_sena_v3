package com.example.uniformes.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.uniformes.R
import com.example.uniformes.data.Defecto
import com.example.uniformes.data.ItemSeguimiento
import com.example.uniformes.data.Repository
import com.example.uniformes.util.Badges
import com.example.uniformes.util.Fmt
import com.example.uniformes.util.ListaAdapter
import com.example.uniformes.util.btn
import com.example.uniformes.util.setItems
import com.example.uniformes.util.toast
import com.example.uniformes.util.tv
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class ControlCalidadFragment : Fragment(R.layout.fragment_control_calidad) {

    private lateinit var adapterInspeccion: ListaAdapter<ItemSeguimiento>
    private lateinit var adapterDefectos: ListaAdapter<Defecto>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapterInspeccion = ListaAdapter<ItemSeguimiento>(R.layout.item_inspeccion) { v, item -> bindInspeccion(v, item) }
        view.findViewById<RecyclerView>(R.id.rvInspeccion).adapter = adapterInspeccion

        adapterDefectos = ListaAdapter<Defecto>(R.layout.item_defecto) { v, d ->
            val item = Repository.itemsInspeccion.firstOrNull { it.id == d.itemId }
            v.tv(R.id.tvDefectoTipo).text = d.tipo
            v.tv(R.id.tvGravedad).also {
                it.text = Fmt.cap(d.gravedad)
                Badges.aplicar(it, d.gravedad)
            }
            v.tv(R.id.tvDefectoDescripcion).text = d.descripcion
            v.tv(R.id.tvDefectoMeta).text =
                "Item: ${item?.tipo.orEmpty()} (${item?.numeroOrden.orEmpty()})  |  Fecha: ${Fmt.fechaHora(d.fechaMillis)}"
        }
        view.findViewById<RecyclerView>(R.id.rvDefectos).adapter = adapterDefectos

        refrescarTodo()
    }

    private fun refrescarTodo() {
        val v = view ?: return
        val items = Repository.itemsInspeccion
        val defectos = Repository.defectos

        val aprobados = items.count { it.estado == "completado" }
        val pendientes = items.count { it.estado == "acabado" }
        val graves = defectos.count { it.gravedad == "grave" }
        val tasa = if (items.isNotEmpty()) String.format(Locale.US, "%.1f", aprobados * 100.0 / items.size) else "0.0"

        v.tv(R.id.statAprobados).text = aprobados.toString()
        v.tv(R.id.statPendientesCc).text = pendientes.toString()
        v.tv(R.id.statDefectosGraves).text = graves.toString()
        v.tv(R.id.statTasa).text = "$tasa%"

        adapterInspeccion.items = items.toList()
        adapterDefectos.items = defectos.toList()
        v.findViewById<View>(R.id.cardDefectos).visibility =
            if (defectos.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun bindInspeccion(v: View, item: ItemSeguimiento) {
        val ctx = requireContext()
        v.tv(R.id.tvOrden).text = item.numeroOrden
        v.tv(R.id.tvCliente).text = item.cliente
        v.tv(R.id.tvTipoTallaCant).text = "${item.tipo} · Talla ${item.talla} · x${item.cantidad}"

        // Badge de estado
        v.tv(R.id.tvEstado).also {
            when (item.estado) {
                "completado" -> { it.text = "Aprobado"; Badges.aplicar(it, "completado") }
                "acabado" -> { it.text = "En Revisión"; Badges.aplicar(it, "acabado") }
                else -> { it.text = "Devuelto"; Badges.aplicar(it, "costura") }
            }
        }

        // Defectos del item
        val cantDefectos = Repository.defectos.count { it.itemId == item.id }
        v.tv(R.id.tvDefectos).also {
            if (cantDefectos > 0) {
                it.text = "⚠ $cantDefectos defecto(s)"
                it.setTextColor(ContextCompat.getColor(ctx, R.color.red_600))
            } else {
                it.text = "Sin defectos"
                it.setTextColor(ContextCompat.getColor(ctx, R.color.gray_400))
            }
        }

        // Acciones según el estado
        val btnAprobar = v.btn(R.id.btnAprobar)
        val btnDefecto = v.btn(R.id.btnRegistrarDefecto)
        val etiqueta = v.tv(R.id.tvAprobado)
        when (item.estado) {
            "acabado" -> {
                btnAprobar.visibility = View.VISIBLE
                btnDefecto.visibility = View.VISIBLE
                etiqueta.visibility = View.GONE
                btnAprobar.setOnClickListener { aprobar(item) }
                btnDefecto.setOnClickListener { abrirDialogoDefecto(item) }
            }
            "completado" -> {
                btnAprobar.visibility = View.GONE
                btnDefecto.visibility = View.GONE
                etiqueta.visibility = View.VISIBLE
                etiqueta.text = "✓ Aprobado"
                Badges.aplicar(etiqueta, "completado")
            }
            else -> {
                btnAprobar.visibility = View.GONE
                btnDefecto.visibility = View.GONE
                etiqueta.visibility = View.VISIBLE
                etiqueta.text = "Devuelto a producción"
                Badges.aplicar(etiqueta, "costura")
            }
        }
    }

    private fun aprobar(item: ItemSeguimiento) {
        item.estado = "completado"
        toast("Item aprobado y marcado como completado")
        refrescarTodo()
    }

    private fun abrirDialogoDefecto(item: ItemSeguimiento) {
        val d = layoutInflater.inflate(R.layout.dialog_registrar_defecto, null)
        d.tv(R.id.tvDefectoItem).text = item.tipo
        d.tv(R.id.tvDefectoOrden).text = item.numeroOrden

        val tipos = resources.getStringArray(R.array.tipos_defecto).toList()
        val actvTipo = d.findViewById<AutoCompleteTextView>(R.id.actvTipoDefecto)
        actvTipo.setItems(tipos)
        val etDescripcion = d.findViewById<EditText>(R.id.etDescripcionDefecto)

        // Selector de gravedad
        val botones = mapOf(
            "leve" to d.btn(R.id.btnGravedadLeve),
            "moderado" to d.btn(R.id.btnGravedadModerado),
            "grave" to d.btn(R.id.btnGravedadGrave)
        )
        var gravedad = "leve"
        marcarGravedad(botones, gravedad)
        botones.forEach { (clave, boton) ->
            boton.setOnClickListener {
                gravedad = clave
                marcarGravedad(botones, clave)
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(d).create()
        d.btn(R.id.btnCancelarDefecto).setOnClickListener { dialog.dismiss() }
        d.btn(R.id.btnGuardarDefecto).setOnClickListener {
            val tipo = actvTipo.text.toString()
            val descripcion = etDescripcion.text.toString().trim()
            if (tipo.isEmpty() || descripcion.isEmpty()) {
                toast("Por favor completa todos los campos")
                return@setOnClickListener
            }

            Repository.defectos.add(Defecto(item.id, tipo, descripcion, gravedad, System.currentTimeMillis()))

            if (gravedad == "grave") {
                item.estado = "costura"
                toast("Item devuelto a producción por defecto grave")
            } else {
                toast("Defecto registrado")
            }
            dialog.dismiss()
            refrescarTodo()
        }
        dialog.show()
    }

    private fun marcarGravedad(botones: Map<String, MaterialButton>, seleccionada: String) {
        val ctx = requireContext()
        botones.forEach { (clave, b) ->
            val activo = clave == seleccionada
            b.backgroundTintList = ColorStateList.valueOf(
                if (activo) ContextCompat.getColor(ctx, R.color.brand_100) else Color.TRANSPARENT
            )
            b.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, if (activo) R.color.brand_600 else R.color.gray_300)
            )
            b.setTextColor(ContextCompat.getColor(ctx, if (activo) R.color.brand_800 else R.color.gray_700))
        }
    }
}
