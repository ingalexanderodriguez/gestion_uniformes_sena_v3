package com.example.uniformes.ui

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.uniformes.R
import com.example.uniformes.data.ItemSeguimiento
import com.example.uniformes.data.Repository
import com.example.uniformes.util.Badges
import com.example.uniformes.util.Fmt
import com.example.uniformes.util.ListaAdapter
import com.example.uniformes.util.btn
import com.example.uniformes.util.setItems
import com.example.uniformes.util.toast
import com.example.uniformes.util.tv
import kotlin.math.roundToInt

class ProduccionFragment : Fragment(R.layout.fragment_produccion) {

    private class Etapa(
        val clave: String,
        val siguiente: String?,
        val idStat: Int,
        val idKanbanConteo: Int,
        val idKanbanLista: Int
    )

    private val etapas = listOf(
        Etapa("pendiente", "cortado", R.id.statProdPendiente, R.id.tvKanbanCountPendiente, R.id.rvKanbanPendiente),
        Etapa("cortado", "costura", R.id.statProdCortado, R.id.tvKanbanCountCortado, R.id.rvKanbanCortado),
        Etapa("costura", "acabado", R.id.statProdCostura, R.id.tvKanbanCountCostura, R.id.rvKanbanCostura),
        Etapa("acabado", "completado", R.id.statProdAcabado, R.id.tvKanbanCountAcabado, R.id.rvKanbanAcabado),
        Etapa("completado", null, R.id.statProdCompletado, R.id.tvKanbanCountCompletado, R.id.rvKanbanCompletado)
    )

    private var filtro = "todos"
    private lateinit var adapterTabla: ListaAdapter<ItemSeguimiento>
    private val adaptersKanban = HashMap<String, ListaAdapter<ItemSeguimiento>>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapterTabla = ListaAdapter<ItemSeguimiento>(R.layout.item_produccion) { v, item -> bindItem(v, item) }
        view.findViewById<RecyclerView>(R.id.rvProduccion).adapter = adapterTabla

        for (etapa in etapas) {
            val a = ListaAdapter<ItemSeguimiento>(R.layout.item_kanban) { v, item ->
                v.tv(R.id.tvKanbanTitulo).text = item.tipo
                v.tv(R.id.tvKanbanOrden).text = item.numeroOrden
                v.tv(R.id.tvKanbanMeta).text = "Talla: ${item.talla}   x${item.cantidad}"
            }
            view.findViewById<RecyclerView>(etapa.idKanbanLista).adapter = a
            adaptersKanban[etapa.clave] = a
        }

        val claves = listOf("todos") + etapas.map { it.clave }
        val etiquetas = resources.getStringArray(R.array.estados_produccion).toList()
        val mapa = etiquetas.zip(claves).toMap()
        val actv = view.findViewById<AutoCompleteTextView>(R.id.actvFiltroProduccion)
        actv.setItems(etiquetas, etiquetas[0])
        actv.setOnItemClickListener { parent, _, pos, _ ->
            filtro = mapa[parent.getItemAtPosition(pos) as String] ?: "todos"
            refrescarTabla()
        }

        refrescarTodo()
    }

    private fun refrescarTodo() {
        val v = view ?: return
        val todos = Repository.itemsProduccion
        for (etapa in etapas) {
            val enEtapa = todos.filter { it.estado == etapa.clave }
            v.tv(etapa.idStat).text = enEtapa.size.toString()
            v.tv(etapa.idKanbanConteo).text = enEtapa.size.toString()
            adaptersKanban[etapa.clave]?.items = enEtapa
        }
        refrescarTabla()
    }

    private fun refrescarTabla() {
        val v = view ?: return
        val todos = Repository.itemsProduccion
        val lista = if (filtro == "todos") todos.toList() else todos.filter { it.estado == filtro }
        adapterTabla.items = lista
        v.tv(R.id.tvConteoProd).text = "Mostrando ${lista.size} de ${todos.size} items"
        v.findViewById<View>(R.id.tvEmptyProduccion).visibility =
            if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun bindItem(v: View, item: ItemSeguimiento) {
        val indice = etapas.indexOfFirst { it.clave == item.estado }
        val etapa = etapas[indice]
        val progreso = ((indice + 1) * 100.0 / etapas.size).roundToInt()

        v.tv(R.id.tvOrden).text = item.numeroOrden
        v.tv(R.id.tvEstado).also {
            it.text = Fmt.estado(item.estado)
            Badges.aplicar(it, item.estado)
        }
        v.tv(R.id.tvCliente).text = item.cliente
        v.tv(R.id.tvTipoTallaCant).text = "${item.tipo} · Talla ${item.talla} · x${item.cantidad}"
        v.findViewById<ProgressBar>(R.id.progressProduccion).progress = progreso
        v.tv(R.id.tvProgreso).text = "$progreso%"

        val boton = v.btn(R.id.btnAvanzar)
        val completo = v.findViewById<View>(R.id.layoutCompleto)
        if (etapa.siguiente != null) {
            boton.visibility = View.VISIBLE
            completo.visibility = View.GONE
            boton.text = "A ${etapa.siguiente}"
            boton.setOnClickListener { avanzar(item) }
        } else {
            boton.visibility = View.GONE
            completo.visibility = View.VISIBLE
        }
    }

    private fun avanzar(item: ItemSeguimiento) {
        val siguiente = etapas.firstOrNull { it.clave == item.estado }?.siguiente ?: return
        item.estado = siguiente
        toast("Item movido a $siguiente")
        refrescarTodo()
    }
}
