# Modelo de dominio propuesto

## Estado

Este documento describe el modelo conceptual derivado de V2. No afirma que todas las entidades estén implementadas. Los nombres y relaciones deberán confirmarse al desarrollar cada bloque.

## Agregados principales

### Proveedores y cobertura

- `Proveedor`: identidad y estado del productor.
- `Zona` y `Ruta`: organización territorial configurable.
- `AsignacionProveedor`: vigencia e historial de cambios de ubicación.
- `JornadaAcopio`: ruta, fecha, unidad y responsables.

### Acopio y recepción

- `Acopio`: cantidad recibida en campo, proveedor, jornada e identidad offline.
- `EntregaDirecta`: ingreso del proveedor directamente a planta.
- `RecepcionPlanta`: volumen físico descargado.
- `Discrepancia`: diferencia, investigación, resolución y auditoría.
- `OperacionPendiente` y `ConflictoSincronizacion`: outbox y resolución offline.

### Calidad

- `AnalisisCalidad`: muestra, parámetros, equipo y responsable.
- `ResultadoCalidad`: clasificación según parámetros vigentes.
- `IncidenciaCalidad`: adulteración, acidificación u otra observación.
- `MedidaAplicada` y `SeguimientoTecnico`: decisión y trazabilidad posterior.

### Liquidaciones y pagos

- `SemanaAcopio`: periodo de jueves a miércoles.
- `PrecioLeche`: valor con vigencia temporal.
- `Liquidacion`: litros, precio, ajustes autorizados y total.
- `Pago`: estado, fecha, responsable y comprobante.

### Comunicaciones, producción y ventas

- `Comunicado`, `Evento` y `Asistencia`.
- `LoteProduccion` y `ResultadoProduccion`.
- `Producto`, `Cliente`, `PrecioVenta`, `Venta` y `MovimientoInventario`.

### Seguridad y gobierno

- `Usuario`, `Rol`, `Sesion`, `Permiso` y `RegistroAuditoria`.
- `ParametroVigente` para valores confirmados y versionados.

## Reglas de modelado

- Las entidades conservan identidad estable e historial cuando exista vigencia.
- Cantidades, precios y unidades no se representan con valores flotantes sin una decisión explícita de precisión.
- La operación offline usa identificadores idempotentes creados antes de sincronizar.
- Las correcciones críticas no sobrescriben silenciosamente el valor anterior.
- El proveedor solo consulta información propia.
- Las reglas parametrizadas no se activan hasta estar aprobadas.
- El dominio no depende de Compose, Ktor, MySQL ni APIs de plataforma.

## Trazabilidad

Las relaciones completas entre RF, reglas, componentes y pruebas se mantienen en la Matriz Integral V2. Este documento se actualizará mediante el mismo Pull Request que modifique el modelo real.
