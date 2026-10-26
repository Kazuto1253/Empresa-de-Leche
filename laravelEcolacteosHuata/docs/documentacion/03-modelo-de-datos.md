# 03 · Modelo de datos

La base se crea con **64 migraciones** (`database/migrations`). Este documento resume las tablas del negocio; las de soporte de Laravel (`cache`, `jobs`, `sessions`, `notifications`, `passkeys`, `teams`, `team_invitations`) no se detallan.

## Convenciones

- Modelos Eloquent con `#[Fillable([...])]` y `casts()` como método.
- Montos con 2 decimales (`decimal:2`); densidad y acidez con 3.
- Fechas de negocio con zona horaria `America/Lima`.
- Cada modelo tiene su fábrica en `database/factories`.

## Usuarios y organización

| Tabla | Campos principales | Notas |
|---|---|---|
| `users` | `nombres`, `apellidos`, `documento` (15), `celular` (20), `usuario`, `email`, `password`, `rol`, `roles` (JSON), `toma_asistencia`, `activo`, `current_team_id` | Un rol principal y roles extra; dos pasos y passkeys en tablas aparte |
| `teams`, `team_invitations` | nombre, slug, invitaciones con vencimiento | Equipo principal `ecolacteos-huata` |
| `empresa` | `razon_social`, `ruc`, `telefono`, `direccion` | Fila única (`Empresa::actual()`); `whatsapp()` devuelve el teléfono con prefijo 51 |
| `acopiadores` | `user_id`, `ruta_nombre` | Un acopiador por usuario |
| `proveedores` | `user_id`, `acopiador_id`, `finca_ubicacion`, `finca_referencia`, `latitud`, `longitud`, `bloqueado`, `alteraciones` | `UMBRAL_BLOQUEO = 3` |

## Acopio

| Tabla | Campos principales |
|---|---|
| `turnos` | `nombre`, `hora_inicio`, `hora_fin`, `es_produccion` |
| `rutas` | `acopiador_id`, `nombre`, `activa`, `trazo` (JSON de puntos), `orden`, `rango` |
| `ruta_proveedor` | pivote; **un proveedor pertenece a una sola ruta** (clave única) |
| `ruta_turno` | pivote entre rutas y turnos |
| `precios_leche` | `precio_litro`, `vigente_desde` |
| `descuentos` | `nombre`, `porcentaje`, `activo` |
| `recojos` | `proveedor_id`, `acopiador_id`, `turno_id`, `fecha`, `litros`, `estado_leche`, `estado_otro`, `precio_litro`, `total`, `pagado`, `metodo_pago`, `entregado`, `litros_entregados` |
| `entregas_planta` | `acopiador_id`, `turno_id`, `fecha`, `litros_total`, `total_pagado`, `total_deuda`, `proveedores_no_recogidos`, `guia` |

## Calidad y pagos

| Tabla | Campos principales |
|---|---|
| `resultados_laboratorio` | `proveedor_id`, `recojo_id`, `turno_id`, `fecha`, `litros`, `resultado`, `densidad`, `acidez`, `temperatura`, `grasa`, `sng`, `proteina`, `lactosa`, `sales`, `conforme`, `enviado_produccion`, y datos del ticket del analizador (`sn`, `modo`, `hora_analisis`, `fecha_analisis`) |
| `pagos_proveedor` | `proveedor_id`, `recojo_id`, `fecha`, `monto`, `metodo_pago`, `periodo` |

## Producción

| Tabla | Campos principales |
|---|---|
| `unidades_medida` | `nombre`, `simbolo` |
| `insumos` | `nombre`, `unidad_medida_id`, `costo_unitario`, `stock_actual`, `es_leche` |
| `compras`, `compra_detalles` | compras de insumos con su detalle |
| `recetas`, `receta_detalles` | `nombre`, `precio_venta` (opcional); detalle con `insumo_id`, `cantidad`, `unidad_medida_id` |
| `producciones` | `fecha`, `nombre`, `imagen`, `receta_id`, `cantidad_producida`, `cantidad_vendida`, `lote`, `turno_id`, `responsable_id`, `estado` |
| `produccion_detalles` | consumo de insumos de cada producción |

## Ventas y tienda

| Tabla | Campos principales |
|---|---|
| `clientes` | `nombres`, `apellidos`, `documento` (único), `compras_count` |
| `ventas` | `numero_boleta`, `cliente_id`, `cliente_nombre_libre`, `cliente_doc_libre`, `vendedor_id`, `fecha`, `subtotal`, `descuento`, `descuento_tipo`, `total`, `metodo_pago`, `efectivo_recibido`, `vuelto` |
| `venta_items` | `venta_id`, `produccion_id`, `nombre`, `precio_unitario`, `cantidad`, `subtotal` |
| `pedidos_web` | `venta_id` (nulo hasta confirmar), `total`, `nombre`, `celular`, `metodo_pago` (por defecto `efectivo`), `codigo_operacion`, `token` (40 caracteres), `estado` (por defecto `pendiente`) |
| `pedido_web_items` | `pedido_web_id`, `produccion_id` (opcional), `producto`, `cantidad`, `precio_unitario`, `subtotal` |

## Asistencia y comunicación

| Tabla | Campos principales |
|---|---|
| `asistencias` | `user_id`, `fecha`, `horario_asistencia_id`, `estado`, `hora_marcada`, `registrado_por` |
| `justificaciones` | solicitud con archivo; se resuelve como aceptada o rechazada |
| `horarios_asistencia`, `dias_asistencia_acopiador` | horarios y días en que los acopiadores deben marcar |
| `comunicados` | `tipo`, `titulo`, `descripcion`, `fecha`, `autor_id` y roles destinatarios |

## API móvil

| Tabla | Uso |
|---|---|
| `mobile_tokens` | `user_id`, `token` (hash SHA-256), `last_used_at` |
| `mobile_ids` | relación entre el id local del teléfono (UUID) y el id del servidor |

## Relaciones principales

```
User ─┬─ Acopiador ──< Ruta >──< Proveedor ──< Recojo >── EntregaPlanta
      │                               │             └──< ResultadoLaboratorio
      │                               └──< PagoProveedor
      └─ (vendedor) ──< Venta ──< VentaItem >── Produccion >── Receta ──< RecetaDetalle >── Insumo
                         ▲                          └──< ProduccionDetalle
                  PedidoWeb ──< PedidoWebItem
```

## Enums

| Enum | Valores |
|---|---|
| `Rol` | `administrador`, `personal_planta`, `acopiador`, `proveedor`, `vendedor`, `produccion` |
| `EstadoLeche` | `normal`, `soleado`, `acidificado`, `aguado`, `otro` (todo lo distinto de `normal` es alteración) |
| `EstadoProduccion` | `planificada` (se muestra «Pendiente»), `completada` («Publicado») |
| `EstadoPedidoWeb` | `pendiente`, `confirmado`, `cancelado` |
| `MetodoPago` | `efectivo`, `yape`, `plin`, `tarjeta`, `otro` |
| `Periodo` | `turno`, `dia`, `semana`, `mes`, `anio` |
| `EstadoAsistencia` | `presente`, `tardanza`, `falta` |
| `EstadoJustificacion` | `pendiente`, `aceptada`, `rechazada` |
| `TipoComunicado` | `comunicado`, `asesoramiento` |
