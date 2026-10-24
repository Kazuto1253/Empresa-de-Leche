package com.example.ecolacteoshuata.data.sync

import app.cash.sqldelight.db.SqlDriver

/**
 * Anota en `cambio_pendiente` todo lo que se crea, edita o elimina en el teléfono en las tablas
 * que también existen en Laravel, sin importar qué pantalla o rol lo hizo. Son triggers de
 * SQLite: no dependen de que cada repositorio se acuerde de registrar el cambio. Mientras se
 * guarda lo que llega del servidor (`sync_control.aplicando = 1`) no anotan nada.
 */
object RegistroCambios {
    /** Tablas locales que Laravel refleja, en el orden en que dependen unas de otras. */
    val TABLAS = listOf(
        "usuario", "acopiador", "proveedor", "empresa", "precio_leche", "descuento", "turno",
        "turno_produccion", "ruta", "cliente", "unidad_medida", "insumo", "receta", "receta_detalle",
        "comunicado", "recojo", "resultado_laboratorio", "entrega_planta", "pago_proveedor", "compra",
        "compra_detalle", "produccion", "venta", "horario_asistencia", "dia_asistencia_acopiador",
        "asistencia", "justificacion",
    )

    // Subir la versión obliga a recrear los triggers si cambia su definición.
    private const val VERSION = 1

    private const val SI_NO_ES_DEL_SERVIDOR =
        "WHEN COALESCE((SELECT aplicando FROM sync_control WHERE id = 1), 0) = 0"

    fun instalar(driver: SqlDriver) {
        driver.execute(null, "INSERT OR IGNORE INTO sync_control(id, aplicando) VALUES (1, 0)", 0)
        TABLAS.forEach { tabla ->
            trigger(driver, "${tabla}_ins", "AFTER INSERT ON $tabla", "'$tabla', NEW.id, 'guardar'")
            trigger(driver, "${tabla}_upd", "AFTER UPDATE ON $tabla", "'$tabla', NEW.id, 'guardar'")
            trigger(driver, "${tabla}_del", "AFTER DELETE ON $tabla", "'$tabla', OLD.id, 'eliminar'")
        }
        // Los proveedores de una ruta (y su orden) viajan con la ruta, y los ítems de una venta con
        // la venta (se insertan junto con ella, así que no necesitan trigger propio).
        trigger(driver, "ruta_proveedor_ins", "AFTER INSERT ON ruta_proveedor", "'ruta', NEW.ruta_id, 'guardar'")
        trigger(driver, "ruta_proveedor_upd", "AFTER UPDATE ON ruta_proveedor", "'ruta', NEW.ruta_id, 'guardar'")
        trigger(driver, "ruta_proveedor_del", "AFTER DELETE ON ruta_proveedor", "'ruta', OLD.ruta_id, 'guardar'")
    }

    private fun trigger(driver: SqlDriver, nombre: String, evento: String, valores: String) {
        driver.execute(
            null,
            "CREATE TRIGGER IF NOT EXISTS cambio_${nombre}_v$VERSION $evento $SI_NO_ES_DEL_SERVIDOR BEGIN " +
                "INSERT INTO cambio_pendiente(tabla, registro_id, operacion) VALUES ($valores); END",
            0,
        )
    }
}
