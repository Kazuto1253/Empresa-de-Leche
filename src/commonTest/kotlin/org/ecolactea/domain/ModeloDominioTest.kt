package org.ecolactea.domain

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.ecolactea.domain.enums.ModalidadEntrega
import org.ecolactea.domain.enums.TipoEvento
import org.ecolactea.domain.model.AnalisisCalidad
import org.ecolactea.domain.model.Entrega
import org.ecolactea.domain.model.Evento
import org.ecolactea.domain.model.Proveedor
import org.ecolactea.domain.state.EstadoProveedor

class ModeloDominioTest {

    @Test
    fun `proveedor activo puede registrar una entrega`() {
        val proveedor = Proveedor(
            id = "P001",
            nombre = "Proveedor 1",
            telefono = "999999999",
            estado = EstadoProveedor.Activo
        )

        assertTrue(proveedor.puedeRegistrarEntrega())
    }

    @Test
    fun `proveedor inactivo no puede registrar una entrega`() {
        val proveedor = Proveedor(
            id = "P002",
            nombre = "Proveedor 2",
            telefono = "999999998",
            estado = EstadoProveedor.Inactivo
        )

        assertTrue(!proveedor.puedeRegistrarEntrega())
    }

    @Test
    fun `entrega no permite litros cero o negativos`() {
        assertFailsWith<IllegalArgumentException> {
            Entrega(
                id = "E001",
                proveedorId = "P001",
                fecha = "2026-08-24",
                litros = 0.0,
                modalidad = ModalidadEntrega.DIRECTA
            )
        }
    }

    @Test
    fun `entrega directa no permite acopiador ni sector`() {
        assertFailsWith<IllegalArgumentException> {
            Entrega(
                id = "E002",
                proveedorId = "P001",
                fecha = "2026-08-24",
                litros = 100.0,
                modalidad = ModalidadEntrega.DIRECTA,
                acopiadorId = "A001",
                sector = "Sector 1"
            )
        }
    }

    @Test
    fun `entrega por acopiador requiere acopiador y sector`() {
        assertFailsWith<IllegalArgumentException> {
            Entrega(
                id = "E003",
                proveedorId = "P001",
                fecha = "2026-08-24",
                litros = 100.0,
                modalidad = ModalidadEntrega.ACOPIADOR
            )
        }
    }

    @Test
    fun `analisis de calidad debe estar vinculado a una entrega`() {
        assertFailsWith<IllegalArgumentException> {
            AnalisisCalidad(
                id = "AC001",
                entregaId = "",
                densidad = 1.03,
                unidad = "g/mL",
                fecha = "2026-08-24"
            )
        }
    }

    @Test
    fun `evento rechaza hora final anterior o igual al inicio`() {
        assertFailsWith<IllegalArgumentException> {
            Evento(
                id = "EV001",
                tema = "Capacitación",
                fecha = "2026-08-30",
                horaInicio = "15:00",
                horaFin = "14:00",
                tipo = TipoEvento.CAPACITACION
            )
        }
    }

    @Test
    fun `evento acepta horario valido en formato HH mm`() {
        val evento = Evento(
            id = "EV002",
            tema = "Reunión de proveedores",
            fecha = "2026-08-30",
            horaInicio = "09:00",
            horaFin = "10:30",
            tipo = TipoEvento.REUNION
        )

        assertIs<TipoEvento>(evento.tipo)
        assertTrue(evento.horaFin > evento.horaInicio)
    }
}
