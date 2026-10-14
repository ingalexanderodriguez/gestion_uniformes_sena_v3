package com.example.uniformes.data

data class ItemPedido(
    val id: String,
    val tipo: String,
    val talla: String,
    val cantidad: Int,
    val precio: Int,
    val estado: String
)

data class Pedido(
    val id: String,
    val numeroOrden: String,
    val cliente: String,
    val colegio: String,
    val fechaPedido: String,   // yyyy-MM-dd
    val fechaEntrega: String,  // yyyy-MM-dd
    val items: List<ItemPedido>,
    val total: Int,
    val estado: String,        // pendiente | en_produccion | completado | entregado
    val prioridad: String      // baja | media | alta
)

/** Item "aplanado" que usan Producción y Control de Calidad. */
data class ItemSeguimiento(
    val id: String,
    val pedidoId: String,
    val numeroOrden: String,
    val cliente: String,
    val tipo: String,
    val talla: String,
    val cantidad: Int,
    var estado: String         // pendiente | cortado | costura | acabado | completado
)

data class Defecto(
    val itemId: String,
    val tipo: String,
    val descripcion: String,
    val gravedad: String,      // leve | moderado | grave
    val fechaMillis: Long
)

data class Factura(
    val id: String,
    val numeroFactura: String,
    val pedidoId: String,
    val numeroOrden: String,
    val cliente: String,
    val colegio: String,
    val fecha: String,
    val subtotal: Double,
    val impuesto: Double,
    val total: Double,
    val metodoPago: String,
    val estado: String         // pagada | pendiente | anulada
)

data class TipoUniforme(val nombre: String, val precio: Int)

/** Fila del formulario de Nuevo Pedido (clase normal: se compara por identidad). */
class ItemForm(
    val id: String,
    var tipo: String = "",
    var talla: String = "",
    var cantidad: Int = 1,
    var precio: Int = 0
)
