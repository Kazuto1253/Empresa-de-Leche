# Modelo de dominio — Ecolactea Digital

## 1. Objetivo

Este documento registra las decisiones de diseño del modelo de dominio para la primera versión académica de Ecolactea Digital. El modelo se construye a partir de las reglas de negocio y requisitos de la matriz definitiva.

La implementación se mantiene independiente de UI, base de datos, red y frameworks, siguiendo la decisión de Clean Architecture indicada en la matriz.

## 2. Entidad principal

### Entrega

`Entrega` es la entidad principal del modelo porque representa el hecho central de recepción de leche y conecta al proveedor con la cantidad recibida, fecha, modalidad, acopiador/sector cuando corresponde y estado de sincronización.

Reglas relacionadas:

- RN-01: existen dos modalidades de recepción: DIRECTA y ACOPIADOR.
- RN-05: cada entrega se asocia a un proveedor y a los litros entregados.
- RF-05: la entrega directa requiere proveedor, fecha y litros.
- RF-06: el acopio por acopiador requiere acopiador, fecha, sector y litros.
- RF-19: los registros móviles pueden quedar pendientes de sincronización.

## 3. Entidades secundarias

### Proveedor

Representa a quien entrega leche. Conserva su historial aunque pase a estado inactivo.

### Acopiador

Representa a quien registra diariamente leche recepcionada por sectores.

### CoberturaAcopio

Relaciona un acopiador, proveedor y sector. La vigencia y la posibilidad de cambio de acopiador permanecen pendientes de validación.

### AnalisisCalidad

Registra el resultado de densidad asociado a una entrega. No contiene rangos de aceptación porque la unidad, precisión y límites todavía deben ser validados.

### ObservacionAdulteracion

Conserva el hallazgo de posible adulteración asociado a una entrega. No aplica sanciones automáticas porque el procedimiento posterior aún está pendiente.

### Evento

Representa reuniones o capacitaciones con tema, fecha y horario.

### Asistencia

Relaciona un proveedor convocado con un evento y su estado de asistencia.

## 4. Enums

### ModalidadEntrega

Se modela como `enum` porque RN-01 confirma exactamente dos modalidades:

- `DIRECTA`
- `ACOPIADOR`

### TipoEvento

Se modela como `enum` para distinguir:

- `REUNION`
- `CAPACITACION`

### EstadoAsistencia

Se usa como `enum` para la V1 con `PRESENTE` y `AUSENTE`. El catálogo definitivo queda sujeto a validación.

## 5. Estados con sealed

Se utilizan `sealed interface` cuando el estado representa un conjunto cerrado de situaciones del dominio y puede crecer con información específica.

- `EstadoProveedor`: Activo / Inactivo.
- `EstadoAcopiador`: Activo / Inactivo.
- `EstadoEntrega`: Vigente / Anulada.
- `EstadoSincronizacion`: Sincronizado / Pendiente / Conflicto(motivo).
- `EstadoCalidad`: Registrado / Observado.
- `EstadoObservacion`: Registrada / Confirmada / Descartada.

## 6. Reglas implementadas

### RN-01

Una entrega solo puede tener modalidad `DIRECTA` o `ACOPIADOR`.

### RN-03

Una entrega mediante acopiador requiere acopiador y sector.

### RN-05

Toda entrega requiere proveedor, fecha y una cantidad de litros mayor que cero.

### RF-02

Un proveedor inactivo no puede registrar nuevas entregas mediante `puedeRegistrarEntrega()`. Su historial no se elimina.

### RF-08

Un análisis de densidad debe estar asociado a una entrega y contener valor, unidad y fecha.

### RF-14

Un evento requiere tema, fecha, hora de inicio y hora de fin; la hora de fin debe ser posterior a la de inicio.

### RF-19/RNF-01

El dominio contempla el estado `Pendiente` de sincronización para registros capturados sin Internet.

## 7. Decisiones que NO se convierten en reglas inventadas

La matriz contiene incertidumbres explícitas. Por eso no se implementaron como reglas obligatorias:

- rangos de densidad;
- unidad y precisión definitivas de densidad;
- sanciones automáticas por adulteración;
- canal definitivo de notificaciones;
- horarios definitivos de notificaciones;
- método definitivo de asistencia;
- roles y permisos definitivos;
- política final de resolución de conflictos;
- detalle por proveedor del acopio;
- asignación fija o variable de proveedores a acopiadores;
- registro de vehículos/rutas en la V1.

Estas decisiones deben validarse antes de convertirlas en reglas permanentes.

## 8. Trazabilidad de pruebas

| Prueba | Regla/requisito validado |
|---|---|
| proveedor activo | RF-02 |
| proveedor inactivo | RF-02 |
| litros válidos | RN-05 / RF-05 / RF-06 |
| entrega directa | RN-01 / RF-05 |
| entrega por acopiador | RN-01 / RN-03 / RF-06 |
| análisis vinculado | RN-07 / RF-08 |
| horario del evento | RN-11 / RF-14 |

## 9. Evidencia de ejecución

Las pruebas deben ejecutarse desde Gradle/IntelliJ y la evidencia final debe incluir capturas reales de:

1. La estructura del modelo en `commonMain`.
2. `ModeloDominioTest.kt`.
3. La consola con las pruebas exitosas y `BUILD SUCCESSFUL`.

No se debe declarar una prueba como ejecutada hasta haberla ejecutado realmente.
