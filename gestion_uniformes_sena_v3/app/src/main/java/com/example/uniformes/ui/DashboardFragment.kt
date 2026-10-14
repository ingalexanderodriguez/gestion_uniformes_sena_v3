package com.example.uniformes.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.uniformes.R
import com.example.uniformes.data.Pedido
import com.example.uniformes.data.Repository
import com.example.uniformes.util.Badges
import com.example.uniformes.util.Fmt
import com.example.uniformes.util.ListaAdapter
import com.example.uniformes.util.tv
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pedidos = Repository.pedidos

        val pendientes = pedidos.count { it.estado == "pendiente" }
        val enProduccion = pedidos.count { it.estado == "en_produccion" }
        val completados = pedidos.count { it.estado == "completado" || it.estado == "entregado" }

        view.tv(R.id.statTotal).text = pedidos.size.toString()
        view.tv(R.id.statPendientes).text = pendientes.toString()
        view.tv(R.id.statProduccion).text = enProduccion.toString()
        view.tv(R.id.statCompletados).text = completados.toString()
        view.tv(R.id.totalIngresos).text = Fmt.dinero(pedidos.sumOf { it.total })

        configurarBarras(view.findViewById(R.id.chartColegios), pedidos)
        configurarTorta(view.findViewById(R.id.chartEstados), pendientes, enProduccion, completados)

        val adapter = ListaAdapter<Pedido>(R.layout.item_pedido_reciente) { v, p ->
            v.tv(R.id.tvOrden).text = p.numeroOrden
            v.tv(R.id.tvEstado).also {
                it.text = Fmt.estado(p.estado)
                Badges.aplicar(it, p.estado)
            }
            v.tv(R.id.tvCliente).text = p.cliente
            v.tv(R.id.tvColegio).text = p.colegio
            v.tv(R.id.tvFechaEntrega).text = Fmt.fecha(p.fechaEntrega)
            v.tv(R.id.tvTotal).text = Fmt.simple(p.total)
        }
        view.findViewById<RecyclerView>(R.id.rvRecientes).adapter = adapter
        adapter.items = pedidos.take(5)
    }

    private fun configurarBarras(chart: BarChart, pedidos: List<Pedido>) {
        val porColegio = pedidos.groupingBy { it.colegio }.eachCount()
        val etiquetas = porColegio.keys.toList()
        val entradas = porColegio.values.mapIndexed { i, n -> BarEntry(i.toFloat(), n.toFloat()) }

        val set = BarDataSet(entradas, "Pedidos")
        set.setColor(Color.parseColor("#41b8d8"))
        set.valueTextSize = 11f
        set.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = value.toInt().toString()
        }

        val datos = BarData(set)
        datos.barWidth = 0.6f

        chart.data = datos
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.granularity = 1f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(etiquetas)
        chart.xAxis.labelRotationAngle = -30f
        chart.setExtraBottomOffset(24f)
        chart.setFitBars(true)
        chart.animateY(600)
        chart.invalidate()
    }

    private fun configurarTorta(chart: PieChart, pendientes: Int, enProduccion: Int, completados: Int) {
        val entradas = listOf(
            PieEntry(pendientes.toFloat(), "Pendiente"),
            PieEntry(enProduccion.toFloat(), "En Producción"),
            PieEntry(completados.toFloat(), "Completado")
        )
        val set = PieDataSet(entradas, "")
        set.setColors(Color.parseColor("#f59e0b"), Color.parseColor("#41b8d8"), Color.parseColor("#10b981"))
        set.valueTextSize = 12f
        set.valueTextColor = Color.WHITE
        set.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String =
                if (value == 0f) "" else value.toInt().toString()
        }

        chart.data = PieData(set)
        chart.description.isEnabled = false
        chart.isDrawHoleEnabled = false
        chart.setDrawEntryLabels(false)
        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        chart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        chart.legend.setDrawInside(false)
        chart.animateY(600)
        chart.invalidate()
    }
}
