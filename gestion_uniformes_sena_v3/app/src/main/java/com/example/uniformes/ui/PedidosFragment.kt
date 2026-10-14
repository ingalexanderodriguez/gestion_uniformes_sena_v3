package com.example.uniformes.ui

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.uniformes.R
import com.example.uniformes.data.ItemPedido
import com.example.uniformes.data.Pedido
import com.example.uniformes.data.Repository
import com.example.uniformes.util.Badges
import com.example.uniformes.util.Fmt
import com.example.uniformes.util.ListaAdapter
import com.example.uniformes.util.btn
import com.example.uniformes.util.setItems
import com.example.uniformes.util.toast
import com.example.uniformes.util.tv
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PedidosFragment : Fragment(R.layout.fragment_pedidos) {

    private var estadoFiltro = "todos"
    private var textoBusqueda = ""
    private lateinit var adapter: ListaAdapter<Pedido>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ListaAdapter<Pedido>(R.layout.item_pedido) { v, p -> bindPedido(v, p) }
        view.findViewById<RecyclerView>(R.id.rvPedidos).adapter = adapter

        val claves = listOf("todos", "pendiente", "en_produccion", "completado", "entregado")
        val etiquetas = resources.getStringArray(R.array.estados_pedido).toList()
        val mapa = etiquetas.zip(claves).toMap()
        val actv = view.findViewById<AutoCompleteTextView>(R.id.actvFiltroEstado)
        actv.setItems(etiquetas, etiquetas[0])
        actv.setOnItemClickListener { parent, _, pos, _ ->
            estadoFiltro = mapa[parent.getItemAtPosition(pos) as String] ?: "todos"
            refrescar()
        }

        view.findViewById<EditText>(R.id.etBuscador).doAfterTextChanged {
            textoBusqueda = it?.toString().orEmpty()
            refrescar()
        }

        refrescar()
    }

    private fun pedidosFiltrados(): List<Pedido> {
        val texto = textoBusqueda.lowercase()
        return Repository.pedidos.filter { p ->
            val coincideTexto = p.numeroOrden.lowercase().contains(texto) ||
                p.cliente.lowercase().contains(texto) ||
                p.colegio.lowercase().contains(texto)
            val coincideEstado = estadoFiltro == "todos" || p.estado == estadoFiltro
            coincideTexto && coincideEstado
        }
    }

    private fun refrescar() {
        val v = view ?: return
        val lista = pedidosFiltrados()
        adapter.items = lista
        v.tv(R.id.tvConteo).text = "Mostrando ${lista.size} de ${Repository.pedidos.size} pedidos"
        v.findViewById<View>(R.id.tvEmptyPedidos).visibility =
            if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun bindPedido(v: View, p: Pedido) {
        v.tv(R.id.tvOrden).text = p.numeroOrden
        v.tv(R.id.tvEstado).also {
            it.text = Fmt.estado(p.estado)
            Badges.aplicar(it, p.estado)
        }
        v.tv(R.id.tvCliente).text = p.cliente
        v.tv(R.id.tvColegio).text = p.colegio
        v.tv(R.id.tvFechaPedido).text = Fmt.fecha(p.fechaPedido)
        v.tv(R.id.tvFechaEntrega).text = Fmt.fecha(p.fechaEntrega)
        v.tv(R.id.tvItems).text = p.items.size.toString()
        v.tv(R.id.tvTotal).text = Fmt.simple(p.total)
        v.tv(R.id.tvPrioridad).also {
            it.text = Fmt.cap(p.prioridad)
            Badges.aplicar(it, p.prioridad)
        }
        v.btn(R.id.btnVer).setOnClickListener { verPedido(p) }
        // En el HTML original estos dos botones no tenían acción.
        v.btn(R.id.btnEditar).setOnClickListener { toast("Editar pedidos aún no está disponible") }
        v.btn(R.id.btnEliminar).setOnClickListener { toast("Eliminar pedidos aún no está disponible") }
    }

    private fun verPedido(p: Pedido) {
        val d = layoutInflater.inflate(R.layout.dialog_detalle_pedido, null)
        d.tv(R.id.tvDetOrden).text = p.numeroOrden
        d.tv(R.id.tvDetCliente).text = p.cliente
        d.tv(R.id.tvDetColegio).text = p.colegio
        d.tv(R.id.tvDetEstado).text = Fmt.estado(p.estado)
        d.tv(R.id.tvDetFechaPedido).text = Fmt.fecha(p.fechaPedido)
        d.tv(R.id.tvDetFechaEntrega).text = Fmt.fecha(p.fechaEntrega)
        d.tv(R.id.tvDetTotal).text = Fmt.simple(p.total)

        val itemsAdapter = ListaAdapter<ItemPedido>(R.layout.item_detalle_item) { iv, item ->
            iv.tv(R.id.tvTipo).text = item.tipo
            iv.tv(R.id.tvEstadoItem).also {
                it.text = Fmt.estado(item.estado)
                Badges.aplicar(it, item.estado)
            }
            iv.tv(R.id.tvTallaCantidad).text = "Talla ${item.talla} · Cant. ${item.cantidad}"
            iv.tv(R.id.tvPrecioUnit).text = Fmt.simple(item.precio)
            iv.tv(R.id.tvSubtotal).text = Fmt.simple(item.precio * item.cantidad)
        }
        d.findViewById<RecyclerView>(R.id.rvDetItems).adapter = itemsAdapter
        itemsAdapter.items = p.items

        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(d).create()
        d.btn(R.id.btnCerrar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
