package com.example.uniformes.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.uniformes.R
import com.example.uniformes.data.ItemForm
import com.example.uniformes.data.Repository
import com.example.uniformes.util.Fmt
import com.example.uniformes.util.ListaAdapter
import com.example.uniformes.util.btn
import com.example.uniformes.util.setItems
import com.example.uniformes.util.toast
import com.example.uniformes.util.tv
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NuevoPedidoFragment : Fragment(R.layout.fragment_nuevo_pedido) {

    private val formItems = mutableListOf(ItemForm("1"))
    private var contador = 1
    private var fechaEntregaIso = ""
    private lateinit var adapter: ListaAdapter<ItemForm>

    private fun etiquetaTipo(nombre: String, precio: Int) = "$nombre - ${Fmt.simple(precio)}"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ListaAdapter<ItemForm>(R.layout.item_form_item) { v, item -> bindItem(v, item) }
        view.findViewById<RecyclerView>(R.id.rvItemsForm).adapter = adapter
        refrescarLista()

        // Colegio
        view.findViewById<AutoCompleteTextView>(R.id.actvColegio).also {
            it.setItems(Repository.colegios)
            it.setOnItemClickListener { _, _, _, _ -> actualizarResumen() }
        }

        // Prioridad (por defecto "Media", igual que el HTML)
        val prioridades = resources.getStringArray(R.array.prioridades).toList()
        view.findViewById<AutoCompleteTextView>(R.id.actvPrioridad).also {
            it.setItems(prioridades, prioridades[1])
            it.setOnItemClickListener { _, _, _, _ -> actualizarResumen() }
        }

        // Cliente
        view.findViewById<EditText>(R.id.etCliente).doAfterTextChanged { actualizarResumen() }

        // Fecha de entrega
        val etFecha = view.findViewById<EditText>(R.id.etFechaEntrega)
        etFecha.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Fecha de entrega")
                .build()
            picker.addOnPositiveButtonClickListener { millis ->
                val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                formato.timeZone = TimeZone.getTimeZone("UTC")
                fechaEntregaIso = formato.format(Date(millis))
                etFecha.setText(Fmt.fecha(fechaEntregaIso))
                actualizarResumen()
            }
            picker.show(parentFragmentManager, "fecha_entrega")
        }

        view.btn(R.id.btnAgregarItem).setOnClickListener {
            contador += 1
            formItems.add(ItemForm(contador.toString()))
            refrescarLista()
        }

        view.btn(R.id.btnGuardarPedido).setOnClickListener { guardarPedido(view) }
        view.btn(R.id.btnCancelarPedido).setOnClickListener {
            findNavController().navigate(R.id.pedidosFragment)
        }

        actualizarResumen()
    }

    private fun refrescarLista() {
        adapter.items = formItems.toList()
        actualizarResumen()
    }

    private fun bindItem(v: View, item: ItemForm) {
        val posicion = formItems.indexOfFirst { it === item }
        v.tv(R.id.tvNumItem).text = "Item ${posicion + 1}"

        v.btn(R.id.btnQuitarItem).also {
            it.visibility = if (formItems.size > 1) View.VISIBLE else View.GONE
            it.setOnClickListener {
                if (formItems.size > 1) {
                    formItems.removeAll { f -> f === item }
                    refrescarLista()
                }
            }
        }

        // Tipo de uniforme
        val opcionesTipo = Repository.tiposUniforme.map { etiquetaTipo(it.nombre, it.precio) }
        val actvTipo = v.findViewById<AutoCompleteTextView>(R.id.actvTipo)
        actvTipo.setItems(opcionesTipo)
        actvTipo.setText(if (item.tipo.isEmpty()) "" else etiquetaTipo(item.tipo, item.precio), false)
        actvTipo.setOnItemClickListener { parent, _, pos, _ ->
            val texto = parent.getItemAtPosition(pos) as String
            val tipo = Repository.tiposUniforme.first { etiquetaTipo(it.nombre, it.precio) == texto }
            item.tipo = tipo.nombre
            item.precio = tipo.precio
            actualizarSubtotal(v, item)
            actualizarResumen()
        }

        // Talla
        val actvTalla = v.findViewById<AutoCompleteTextView>(R.id.actvTalla)
        actvTalla.setItems(Repository.tallas)
        actvTalla.setText(item.talla, false)
        actvTalla.setOnItemClickListener { parent, _, pos, _ ->
            item.talla = parent.getItemAtPosition(pos) as String
        }

        // Cantidad (se quita el watcher anterior porque la vista se reutiliza)
        val etCantidad = v.findViewById<EditText>(R.id.etCantidad)
        (etCantidad.tag as? TextWatcher)?.let { etCantidad.removeTextChangedListener(it) }
        etCantidad.setText(item.cantidad.toString())
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val n = s?.toString()?.toIntOrNull() ?: 1
                item.cantidad = if (n > 0) n else 1
                actualizarSubtotal(v, item)
                actualizarResumen()
            }
        }
        etCantidad.tag = watcher
        etCantidad.addTextChangedListener(watcher)

        actualizarSubtotal(v, item)
    }

    private fun actualizarSubtotal(v: View, item: ItemForm) {
        if (item.tipo.isEmpty()) {
            v.tv(R.id.tvPrecioUnit).text = ""
            v.tv(R.id.tvSubtotal).text = ""
        } else {
            v.tv(R.id.tvPrecioUnit).text = "Precio unitario: ${Fmt.simple(item.precio)}"
            v.tv(R.id.tvSubtotal).text = "Subtotal: ${Fmt.simple(item.precio * item.cantidad)}"
        }
    }

    private fun actualizarResumen() {
        val v = view ?: return
        v.tv(R.id.tvResumenItems).text = formItems.size.toString()
        v.tv(R.id.tvResumenUnidades).text = formItems.sumOf { it.cantidad }.toString()
        v.tv(R.id.tvResumenTotal).text = Fmt.dinero(formItems.sumOf { it.precio * it.cantidad })

        val cliente = v.findViewById<EditText>(R.id.etCliente).text.toString().trim()
        val colegio = v.findViewById<AutoCompleteTextView>(R.id.actvColegio).text.toString()
        val prioridad = v.findViewById<AutoCompleteTextView>(R.id.actvPrioridad).text.toString()
        val panel = v.findViewById<View>(R.id.layoutResumenDetalle)

        if (cliente.isNotEmpty() && colegio.isNotEmpty()) {
            panel.visibility = View.VISIBLE
            v.tv(R.id.tvDetalleCliente).text = cliente
            v.tv(R.id.tvDetalleColegio).text = colegio
            v.tv(R.id.tvDetallePrioridad).text = prioridad
            v.tv(R.id.tvDetalleEntrega).text =
                if (fechaEntregaIso.isEmpty()) "—" else Fmt.fecha(fechaEntregaIso)
        } else {
            panel.visibility = View.GONE
        }
    }

    private fun guardarPedido(v: View) {
        val cliente = v.findViewById<EditText>(R.id.etCliente).text.toString().trim()
        val colegio = v.findViewById<AutoCompleteTextView>(R.id.actvColegio).text.toString()
        val prioridad = v.findViewById<AutoCompleteTextView>(R.id.actvPrioridad).text.toString().lowercase()

        if (cliente.isEmpty() || colegio.isEmpty() || fechaEntregaIso.isEmpty()) {
            toast("Por favor completa todos los campos requeridos")
            return
        }

        val validos = formItems.filter { it.tipo.isNotEmpty() && it.talla.isNotEmpty() && it.cantidad > 0 }
        if (validos.isEmpty()) {
            toast("Por favor agrega al menos un item válido")
            return
        }

        Repository.agregarPedido(cliente, colegio, fechaEntregaIso, prioridad, validos)
        toast("Pedido creado exitosamente")
        v.postDelayed({
            if (isAdded) findNavController().navigate(R.id.pedidosFragment)
        }, 900)
    }
}
