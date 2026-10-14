package com.example.uniformes.data

import com.example.uniformes.R

private val TODOS = setOf(
    R.id.dashboardFragment, R.id.pedidosFragment, R.id.produccionFragment,
    R.id.nuevoPedidoFragment, R.id.controlCalidadFragment, R.id.facturacionFragment
)

/** Perfiles del sistema y los módulos que puede ver cada uno (mínimo privilegio). */
enum class Rol(val etiqueta: String, val modulos: Set<Int>) {
    ADMIN("Administrador", TODOS),
    VENTAS(
        "Encargado de Ventas",
        setOf(R.id.dashboardFragment, R.id.pedidosFragment, R.id.nuevoPedidoFragment)
    ),
    INVENTARIO(
        "Encargado de Inventario",
        setOf(R.id.dashboardFragment, R.id.pedidosFragment)
    ),
    JEFE_PRODUCCION(
        "Jefe de Producción",
        setOf(R.id.dashboardFragment, R.id.pedidosFragment, R.id.produccionFragment, R.id.controlCalidadFragment)
    ),
    OPERARIO(
        "Operario / Calidad",
        setOf(R.id.dashboardFragment, R.id.produccionFragment, R.id.controlCalidadFragment)
    ),
    CONTADOR(
        "Contador",
        setOf(R.id.dashboardFragment, R.id.pedidosFragment, R.id.facturacionFragment)
    )
}

data class Usuario(
    val usuario: String,
    val nombre: String,
    val rol: Rol,
    val claveHash: String
)
