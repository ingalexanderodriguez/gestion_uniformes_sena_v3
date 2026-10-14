package com.example.uniformes.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

fun View.tv(id: Int): TextView = findViewById(id)

fun View.btn(id: Int): MaterialButton = findViewById(id)

/** Equivalente al toast.success/error de ui.js. */
fun Fragment.toast(mensaje: String) {
    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
}

/** Carga las opciones de un dropdown (AutoCompleteTextView) y opcionalmente deja una seleccionada. */
fun AutoCompleteTextView.setItems(items: List<String>, seleccionado: String? = null) {
    setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, items))
    if (seleccionado != null) setText(seleccionado, false)
}

/** Adapter genérico: un layout por fila y una función que lo rellena. */
class ListaAdapter<T>(
    private val layoutId: Int,
    private val bind: (View, T) -> Unit
) : RecyclerView.Adapter<ListaAdapter.VH>() {

    var items: List<T> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        bind(holder.itemView, items[position])
    }
}
