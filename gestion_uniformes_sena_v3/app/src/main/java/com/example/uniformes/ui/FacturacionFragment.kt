package com.example.uniformes.ui

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.uniformes.R
import com.example.uniformes.data.Factura
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
import com.google.android.material.tabs.TabLayout

class FacturacionFragment : Fragment(R.layout.fragment_facturacion) {

    private val tasaImpuesto = 0.12

    private lateinit var adapterPendientes: ListaAdapter<Pedido>
    private lateinit var adapterHistorial: ListaAdapter<Factura>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapterPendientes = ListaAdapter<Pedido>(R.layout.item_factura_pendiente) { v, p -> bindPendiente(v, p) }
        adapterHistorial = ListaAdapter<Factura>(R.layout.item_factura_historial) { v, f -> bindHistorial(v, f) }
        view.findViewById<RecyclerView>(R.id.rvPendientesFacturar).adapter = adapterPendientes
        view.findViewById<RecyclerView>(R.id.rvHistorialFacturas).adapter = adapterHistorial

        // Pestañas: 0 = pendientes de facturar, 1 = historial
        val panelPendientes = view.findViewById<View>(R.id.panelPendientes)
        val panelHistorial = view.findViewById<View>(R.id.panelHistorial)
        view.findViewById<TabLayout>(R.id.tabLayout).addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                panelPendientes.visibility = if (tab.position == 0) View.VISIBLE else View.GONE
                panelHistorial.visibility = if (tab.position == 1) View.VISIBLE else View.GONE
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        refrescarTodo()
    }

    private fun refrescarTodo() {
        val v = view ?: return
        val facturas = Repository.facturas
        val totalFacturado = facturas.sumOf { it.total }

        v.tv(R.id.statTotalFacturado).text = Fmt.dos(totalFacturado)
        v.tv(R.id.statFacturasEmitidas).text = facturas.size.toString()
        v.tv(R.id.statPagadas).text = facturas.count { it.estado == "pagada" }.toString()
        v.tv(R.id.statPendientesFact).text = facturas.count { it.estado == "pendiente" }.toString()

        val paraFacturar = Repository.pedidos.filter { it.estado == "completado" || it.estado == "entregado" }
        adapterPendientes.items = paraFacturar
        v.findViewById<View>(R.id.tvEmptyPendientes).visibility =
            if (paraFacturar.isEmpty()) View.VISIBLE else View.GONE

        adapterHistorial.items = facturas.toList()
        v.findViewById<View>(R.id.tvEmptyHistorial).visibility =
            if (facturas.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun bindPendiente(v: View, p: Pedido) {
        v.tv(R.id.tvOrden).text = p.numeroOrden
        v.tv(R.id.tvTotal).text = Fmt.simple(p.total)
        v.tv(R.id.tvCliente).text = p.cliente
        v.tv(R.id.tvColegio).text = p.colegio
        v.tv(R.id.tvFechaPedido).text = Fmt.fecha(p.fechaPedido)
        v.tv(R.id.tvItems).text = p.items.size.toString()

        val boton = v.btn(R.id.btnFacturar)
        val yaFacturado = Repository.facturas.any { it.pedidoId == p.id }
        if (yaFacturado) {
            boton.text = "✓ Facturado"
            boton.isEnabled = false
            boton.icon = null
        } else {
            boton.text = "Generar factura"
            boton.isEnabled = true
            boton.setOnClickListener { abrirDialogoFacturar(p) }
        }
    }

    private fun bindHistorial(v: View, f: Factura) {
        v.tv(R.id.tvFactura).text = f.numeroFactura
        v.tv(R.id.tvEstadoFactura).also {
            it.text = Fmt.cap(f.estado)
            Badges.aplicar(it, f.estado)
        }
        v.tv(R.id.tvCliente).text = f.cliente
        v.tv(R.id.tvOrdenFecha).text = "${f.numeroOrden} · ${Fmt.fecha(f.fecha)}"
        v.tv(R.id.tvSubtotal).text = Fmt.dos(f.subtotal)
        v.tv(R.id.tvImpuesto).text = Fmt.dos(f.impuesto)
        v.tv(R.id.tvTotal).text = Fmt.dos(f.total)
        v.tv(R.id.tvMetodoPago).text = f.metodoPago
        v.btn(R.id.btnVerFactura).setOnClickListener { verFactura(f) }
        v.btn(R.id.btnDescargarFactura).setOnClickListener { descargar(f) }
    }

    private fun abrirDialogoFacturar(p: Pedido) {
        if (Repository.facturas.any { it.pedidoId == p.id }) {
            toast("Este pedido ya tiene una factura generada")
            return
        }

        val d = layoutInflater.inflate(R.layout.dialog_generar_factura, null)
        d.tv(R.id.tvGfOrden).text = p.numeroOrden
        d.tv(R.id.tvGfCliente).text = p.cliente
        d.tv(R.id.tvGfColegio).text = p.colegio
        d.tv(R.id.tvGfFecha).text = Fmt.hoyLocal()

        val metodos = resources.getStringArray(R.array.metodos_pago).toList()
        val actvMetodo = d.findViewById<AutoCompleteTextView>(R.id.actvMetodoPago)
        actvMetodo.setItems(metodos, metodos[0])

        val subtotal = p.total.toDouble()
        val impuesto = subtotal * tasaImpuesto
        d.tv(R.id.tvGfSubtotal).text = Fmt.dos(subtotal)
        d.tv(R.id.tvGfImpuesto).text = Fmt.dos(impuesto)
        d.tv(R.id.tvGfTotal).text = Fmt.dos(subtotal + impuesto)

        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(d).create()
        d.btn(R.id.btnCancelarFactura).setOnClickListener { dialog.dismiss() }
        d.btn(R.id.btnConfirmarFactura).setOnClickListener {
            val nueva = Factura(
                id = System.currentTimeMillis().toString(),
                numeroFactura = "FAC-2026-%03d".format(Repository.facturas.size + 1),
                pedidoId = p.id,
                numeroOrden = p.numeroOrden,
                cliente = p.cliente,
                colegio = p.colegio,
                fecha = Fmt.hoyIso(),
                subtotal = subtotal,
                impuesto = impuesto,
                total = subtotal + impuesto,
                metodoPago = actvMetodo.text.toString(),
                estado = "pagada"
            )
            Repository.facturas.add(0, nueva)
            toast("Factura ${nueva.numeroFactura} generada exitosamente")
            dialog.dismiss()
            refrescarTodo()
        }
        dialog.show()
    }

    private fun verFactura(f: Factura) {
        val d = layoutInflater.inflate(R.layout.dialog_ver_factura, null)
        d.tv(R.id.tvVfNumero).text = f.numeroFactura
        d.tv(R.id.tvVfCliente).text = f.cliente
        d.tv(R.id.tvVfColegio).text = f.colegio
        d.tv(R.id.tvVfFecha).text = Fmt.fecha(f.fecha)
        d.tv(R.id.tvVfOrden).text = f.numeroOrden
        d.tv(R.id.tvVfMetodo).text = f.metodoPago
        d.tv(R.id.tvVfSubtotal).text = Fmt.dos(f.subtotal)
        d.tv(R.id.tvVfImpuesto).text = Fmt.dos(f.impuesto)
        d.tv(R.id.tvVfTotal).text = Fmt.dos(f.total)

        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(d).create()
        d.btn(R.id.btnCerrarFactura).setOnClickListener { dialog.dismiss() }
        d.btn(R.id.btnDescargarPdf).setOnClickListener { descargar(f) }
        dialog.show()
    }

    private fun descargar(f: Factura) {
        toast("Descargando factura ${f.numeroFactura}...")
    }
}
