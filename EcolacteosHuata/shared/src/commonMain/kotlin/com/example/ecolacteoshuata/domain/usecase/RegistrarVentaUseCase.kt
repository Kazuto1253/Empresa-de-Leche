package com.example.ecolacteoshuata.domain.usecase

import com.example.ecolacteoshuata.core.util.AppResult
import com.example.ecolacteoshuata.core.util.IdGenerator
import com.example.ecolacteoshuata.core.util.calcularVuelto
import com.example.ecolacteoshuata.core.util.nowLocalDateTime
import com.example.ecolacteoshuata.core.util.redondear2
import com.example.ecolacteoshuata.domain.model.CarritoItem
import com.example.ecolacteoshuata.domain.model.Cliente
import com.example.ecolacteoshuata.domain.model.MetodoPago
import com.example.ecolacteoshuata.domain.model.TotalesCarrito
import com.example.ecolacteoshuata.domain.model.Venta
import com.example.ecolacteoshuata.domain.model.VentaItem
import com.example.ecolacteoshuata.domain.repository.ClienteRepository
import com.example.ecolacteoshuata.domain.repository.ProductoRepository
import com.example.ecolacteoshuata.domain.repository.VentaRepository

data class RegistrarVentaInput(
    val items: List<CarritoItem>,
    val totales: TotalesCarrito,
    val cliente: Cliente?,
    val clienteNombreLibre: String?,
    val clienteDocLibre: String?,
    val metodoPago: MetodoPago,
    val efectivoRecibido: Double?,
    val vendedorId: String? = null,
)

class RegistrarVentaUseCase(
    private val ventaRepository: VentaRepository,
    private val clienteRepository: ClienteRepository,
    private val productoRepository: ProductoRepository,
) {
    suspend operator fun invoke(input: RegistrarVentaInput): AppResult<Venta> {
        if (input.items.isEmpty()) return AppResult.Error("Agrega al menos un producto al carrito.")

        // Como Laravel: no se vende más de lo que queda de cada producción.
        input.items.forEach { item ->
            val disponible = productoRepository.getById(item.producto.id)?.disponible ?: 0.0
            if (disponible < item.cantidad) return AppResult.Error("Cantidad insuficiente para ${item.producto.nombre}.")
        }

        var vuelto: Double? = null
        if (input.metodoPago == MetodoPago.EFECTIVO) {
            val efectivo = input.efectivoRecibido ?: return AppResult.Error("Ingrese el efectivo recibido.")
            if (efectivo < input.totales.total) {
                return AppResult.Error("El efectivo recibido es menor al total a cobrar.")
            }
            vuelto = calcularVuelto(input.totales.total, efectivo)
        }

        val numeroBoleta = ventaRepository.siguienteNumeroBoleta()
        val ventaId = IdGenerator.newId()
        val ventaItems = input.items.map { item ->
            VentaItem(
                id = IdGenerator.newId(),
                ventaId = ventaId,
                produccionId = item.producto.id,
                nombre = item.producto.nombre,
                precioUnitario = item.producto.precio,
                cantidad = item.cantidad,
                subtotal = redondear2(item.subtotal),
            )
        }
        val venta = Venta(
            id = ventaId,
            numeroBoleta = numeroBoleta,
            clienteId = input.cliente?.id,
            // Como Laravel: con cliente registrado no se guarda nombre/documento libre.
            clienteNombreLibre = if (input.cliente != null) null else input.clienteNombreLibre?.takeIf { it.isNotBlank() },
            clienteDocLibre = if (input.cliente != null) null else input.clienteDocLibre?.takeIf { it.isNotBlank() },
            fecha = nowLocalDateTime(),
            subtotal = input.totales.subtotal,
            descuento = input.totales.descuento,
            descuentoTipo = input.totales.descuentoTipo,
            total = input.totales.total,
            metodoPago = input.metodoPago,
            efectivoRecibido = input.efectivoRecibido,
            vuelto = vuelto,
            items = ventaItems,
            vendedorId = input.vendedorId,
        )
        val creada = ventaRepository.crear(venta)
        input.cliente?.let { clienteRepository.incrementarCompras(it.id) }
        return AppResult.Success(creada)
    }
}
