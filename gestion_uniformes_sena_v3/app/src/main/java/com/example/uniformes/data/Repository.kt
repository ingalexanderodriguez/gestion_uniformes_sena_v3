package com.example.uniformes.data

import com.example.uniformes.util.Fmt

/**
 * Equivalente a data.js: datos de ejemplo en memoria.
 * Se comparte entre pantallas mientras la app esté abierta.
 */
object Repository {

    val colegios = listOf(
        "Colegio San José",
        "Instituto Nacional",
        "Colegio Santa María",
        "Colegio Bilingüe",
        "Escuela Primaria Central"
    )

    val tiposUniforme = listOf(
        TipoUniforme("Camisa manga corta", 25),
        TipoUniforme("Camisa manga larga", 28),
        TipoUniforme("Pantalón", 30),
        TipoUniforme("Falda", 28),
        TipoUniforme("Sweater", 35),
        TipoUniforme("Blazer", 45),
        TipoUniforme("Medias", 8),
        TipoUniforme("Corbata", 12)
    )

    val tallas = listOf("XS", "S", "M", "L", "XL", "XXL", "Única")

    val pedidos: MutableList<Pedido> = mutableListOf(
        Pedido(
            "1", "ORD-2026-001", "María González", "Colegio San José", "2026-03-15", "2026-04-15",
            listOf(
                ItemPedido("1-1", "Camisa manga corta", "M", 2, 25, "completado"),
                ItemPedido("1-2", "Pantalón", "M", 2, 30, "completado"),
                ItemPedido("1-3", "Falda", "S", 1, 28, "completado")
            ),
            138, "completado", "media"
        ),
        Pedido(
            "2", "ORD-2026-002", "Carlos Ramírez", "Instituto Nacional", "2026-03-18", "2026-04-20",
            listOf(
                ItemPedido("2-1", "Camisa manga larga", "L", 3, 28, "costura"),
                ItemPedido("2-2", "Pantalón", "L", 3, 30, "cortado"),
                ItemPedido("2-3", "Sweater", "L", 2, 35, "pendiente")
            ),
            224, "en_produccion", "alta"
        ),
        Pedido(
            "3", "ORD-2026-003", "Ana Martínez", "Colegio Santa María", "2026-03-20", "2026-05-01",
            listOf(
                ItemPedido("3-1", "Camisa manga corta", "S", 2, 25, "acabado"),
                ItemPedido("3-2", "Falda", "S", 2, 28, "costura"),
                ItemPedido("3-3", "Medias", "Única", 4, 8, "completado")
            ),
            138, "en_produccion", "media"
        ),
        Pedido(
            "4", "ORD-2026-004", "Pedro Sánchez", "Colegio Bilingüe", "2026-03-22", "2026-04-25",
            listOf(
                ItemPedido("4-1", "Camisa manga corta", "XL", 1, 25, "pendiente"),
                ItemPedido("4-2", "Pantalón", "XL", 1, 30, "pendiente")
            ),
            55, "pendiente", "baja"
        ),
        Pedido(
            "5", "ORD-2026-005", "Laura Díaz", "Colegio San José", "2026-03-22", "2026-04-10",
            listOf(
                ItemPedido("5-1", "Camisa manga corta", "M", 2, 25, "cortado"),
                ItemPedido("5-2", "Falda", "M", 2, 28, "pendiente"),
                ItemPedido("5-3", "Sweater", "M", 1, 35, "pendiente")
            ),
            141, "en_produccion", "alta"
        )
    )

    /** Todos los items de todos los pedidos (pantalla Producción). */
    val itemsProduccion: MutableList<ItemSeguimiento> =
        pedidos.flatMap { p -> p.items.map { it.aSeguimiento(p) } }.toMutableList()

    /** Items en "acabado" o "completado" (pantalla Control de Calidad), copias independientes. */
    val itemsInspeccion: MutableList<ItemSeguimiento> =
        itemsProduccion
            .filter { it.estado == "acabado" || it.estado == "completado" }
            .map { it.copy() }
            .toMutableList()

    val defectos: MutableList<Defecto> = mutableListOf()

    val facturas: MutableList<Factura> = mutableListOf(
        Factura(
            "1", "FAC-2026-001", "1", "ORD-2026-001", "María González", "Colegio San José",
            "2026-03-20", 138.0, 16.56, 154.56, "Efectivo", "pagada"
        )
    )

    private fun ItemPedido.aSeguimiento(p: Pedido) =
        ItemSeguimiento(id, p.id, p.numeroOrden, p.cliente, tipo, talla, cantidad, estado)

    /** Guarda un pedido nuevo (en el HTML solo mostraba un aviso; aquí aparece en la lista). */
    fun agregarPedido(
        cliente: String,
        colegio: String,
        fechaEntrega: String,
        prioridad: String,
        formItems: List<ItemForm>
    ) {
        val n = pedidos.size + 1
        val id = n.toString()
        val items = formItems.mapIndexed { i, f ->
            ItemPedido("$id-${i + 1}", f.tipo, f.talla, f.cantidad, f.precio, "pendiente")
        }
        val pedido = Pedido(
            id = id,
            numeroOrden = "ORD-2026-%03d".format(n),
            cliente = cliente,
            colegio = colegio,
            fechaPedido = Fmt.hoyIso(),
            fechaEntrega = fechaEntrega,
            items = items,
            total = items.sumOf { it.precio * it.cantidad },
            estado = "pendiente",
            prioridad = prioridad
        )
        pedidos.add(pedido)
        itemsProduccion.addAll(items.map { it.aSeguimiento(pedido) })
    }
}
