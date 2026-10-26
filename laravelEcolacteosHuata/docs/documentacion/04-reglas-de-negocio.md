# 04 · Reglas de negocio

## Calidad de la leche y bloqueo de proveedores

- El acopiador marca el estado de la leche en cada recojo: `normal`, `soleado`, `acidificado`, `aguado` u `otro`.
- Todo estado distinto de `normal` cuenta como **alteración** y suma 1 al contador del proveedor.
- Al llegar a **3 alteraciones** (`Proveedor::UMBRAL_BLOQUEO`) el proveedor queda **bloqueado** automáticamente.
- Desbloquearlo reinicia el contador a 0.
- En laboratorio se registran densidad, acidez, temperatura, grasa y demás componentes del analizador, más el ticket (serie, modo, fecha y hora del análisis). El resultado se deriva del interruptor **conforme**.
- Solo un resultado **conforme** puede enviarse a producción, y **una sola vez**, para que los litros no se sumen dos veces al stock del insumo «leche».

## Litros y entregas a planta

- El recojo guarda los litros medidos en campo; la entrega a planta guarda los litros entregados y la guía. Ambos datos se comparan para documentar diferencias.
- Registrar o corregir un recojo, o registrar una entrega a planta, **no modifica** el stock del insumo de leche; solo lo hace el envío de un resultado conforme a producción.

## Pagos a proveedores

- El pago se calcula por período (`turno`, `dia`, `semana`, `mes`, `anio`) sobre los recojos del proveedor, con el precio por litro registrado en cada recojo.
- Al registrar un pago se guardan monto, método y período, y **todos los recojos del proveedor dentro de ese período quedan marcados como pagados**.

## Producción

- **Compras de insumos:** cada compra aumenta el stock del insumo y recalcula su costo unitario con el valor del stock anterior y el de lo comprado.
- **Costo de receta:** suma de cantidad × costo unitario de cada insumo.
- **Precio de venta:** `Receta::precioVenta()` devuelve el precio de venta propio si es mayor que 0 y, si no, el costo total de la receta. Se usa en ventas, inventario, productos, panel del vendedor y la tienda.
- **Registrar una producción** descuenta del stock los insumos de la receta. Si no alcanzan, la operación se **bloquea**.
- **Publicar** (estado `completada`) deja el producto disponible para vender.
- **Disponible** = cantidad producida − cantidad vendida. Cada ítem de venta descuenta de la producción.

## Ventas y boletas

- El número de boleta es `B001-` seguido de un correlativo de 6 dígitos (`Venta::siguienteNumeroBoleta()`).
- Una venta admite uno o varios descuentos de los configurados por el administrador (cada uno es un porcentaje; sus nombres quedan en `descuento_tipo`), método de pago y, en efectivo, monto recibido y vuelto. La boleta sale en PDF (A5).
- El cliente puede ser uno registrado o un nombre y documento libres.

## Tienda pública y pedidos web

1. El carrito vive en el navegador (`localStorage`) y se valida contra el catálogo vigente.
2. `POST /pedidos` valida: de 1 a 30 líneas, cantidad de 1 a 1 000, nombre de 2 a 80 caracteres, celular opcional con dígitos, espacios, `+` y `-`, y método de pago `yape`, `plin` o `efectivo`.
3. **El servidor toma el producto y el precio del catálogo**; rechaza productos no publicados o cantidades sobre el stock. Nunca confía en precios del navegador.
4. Con **Yape** el N.º de operación es obligatorio. El sistema muestra el QR de la tienda (`public/images/yape-qr.jpg`) y el monto; el pago lo hace el cliente en su app y **la tienda lo verifica manualmente** (Yape no ofrece una API pública para cuentas personales, por lo que no hay cobro automático). Con Plin es opcional y con efectivo no se pide.
5. El pedido y sus líneas se guardan en una transacción con estado `pendiente` y un `token` secreto.
6. La respuesta incluye el enlace `/pedido/{token}/boleta`: mientras no se confirma muestra un **comprobante de pedido**; cuando el vendedor confirma, el mismo enlace muestra la **boleta oficial**.
7. El vendedor, en «Compras por web», revisa el pago y pulsa **Confirmar y facturar**. `PedidoWeb::confirmarComoVenta()` corre en una transacción con bloqueo: verifica que el pedido siga pendiente y el stock de **todas** las líneas (todo o nada), crea la venta y sus ítems, descuenta stock y enlaza el pedido (`venta_id`). Es **idempotente**: confirmar dos veces no duplica la venta. En efectivo el monto recibido es el total exacto y el vuelto 0.
8. Cada pedido avisa a la tienda por WhatsApp (`https://wa.me/<número>`), usando el teléfono de la empresa.

## Asistencia

- Quien tiene el permiso `tomar-asistencia` registra presente, tardanza o falta por tabla.
- Los trabajadores ven su propia asistencia y pueden **justificar** faltas y tardanzas con un archivo, **dentro de 7 días**; la justificación queda pendiente hasta que se acepta o rechaza.
- Los acopiadores marcan su propia asistencia según el horario y los días asignados. Si no marcan antes de que cierre el horario, el sistema les registra **falta automática** (comando programado cada 5 minutos).

## Comunicados

- Se publican por rol; cada destinatario recibe una notificación en la campana. Tipos: `comunicado` y `asesoramiento`.

## Zona horaria y moneda

- Fechas de negocio con `America/Lima` (`config('app.zona_horaria_local')`).
- Moneda: soles (`S/`).
