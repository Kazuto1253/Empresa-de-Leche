# 06 · Rutas y módulos

Las pantallas internas viven bajo `/{equipo}/…` (el equipo principal es `ecolacteos-huata`), por ejemplo `/ecolacteos-huata/admin/panel`. Tras iniciar sesión, `/{equipo}/dashboard` redirige al panel del rol del usuario.

## Rutas públicas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/` | Tienda pública: catálogo, carrito, intro 3D |
| `POST` | `/pedidos` | Guarda un pedido web (8 por minuto) |
| `GET` | `/pedido/{token}/boleta` | Comprobante de pedido o boleta, según el estado (30 por minuto) |
| `GET` | `/login` | Inicio de sesión |
| `POST` | `/api/mobile/login`, `GET /api/mobile/sync`, `POST /api/mobile/cambios` | API móvil ([05](05-api-movil.md)) |

## Administrador (`/admin/…`)

| Ruta | Módulo | Qué se hace |
|---|---|---|
| `panel` | Panel de administración | Indicadores, usuarios por rol y accesos rápidos |
| `usuarios` | Usuarios | Crear, editar y desactivar usuarios |
| `asignar-roles` | Asignar rol a usuarios | Rol principal y roles extra |
| `empresa` | Empresa | Razón social, RUC, teléfono (WhatsApp) y dirección |
| `descuentos` | Descuentos | Descuentos en porcentaje para las ventas |
| `turnos` | Turnos | Turnos de acopio y de producción |
| `precios` | Precios de la leche | Precio por litro y fecha de vigencia |
| `acopiador-recojo`, `acopiador-entrega` | Supervisión de acopiadores | Recojos y entregas a planta |
| `proveedor-entregas`, `proveedor-laboratorio` | Supervisión de proveedores | Entregas y resultados de laboratorio |

El administrador también accede a «Rutas» y «Comunicados» de planta, al laboratorio, a «Compras por web» y «Ventas» del vendedor, y a «Tomar asistencia». Las pantallas propias de acopiador y proveedor (por ejemplo `acopiador/recojo`) no están disponibles para él: las supervisa desde los módulos de supervisión.

## Personal de planta (`/planta/…`)

| Ruta | Módulo |
|---|---|
| `panel` | Panel de planta |
| `entregas` | Entregas de los acopiadores |
| `laboratorio` (+ `laboratorio/{resultado}/pdf`) | Resultados de laboratorio y su PDF |
| `rutas` | Rutas con mapa y trazo |
| `pagos` | Pago a proveedores |
| `comunicados` | Comunicados |

## Acopiador (`/acopiador/…`)

| Ruta | Módulo |
|---|---|
| `panel` | Panel del acopiador |
| `recojo` | Registro del recojo por proveedor |
| `mapa-recojo` | Mapa de la ruta |
| `entrega` | Entrega a planta |
| `inventario` (+ `inventario/pdf`) | Inventario y su PDF |

## Proveedor (`/proveedor/…`)

| Ruta | Módulo |
|---|---|
| `panel` | Panel del proveedor |
| `entregas` | Mis entregas |
| `laboratorio` | Resultados de laboratorio |
| `comunicados` | Comunicados |

## Producción (`/produccion/…`)

| Ruta | Módulo |
|---|---|
| `panel` | Panel de producción |
| `insumos`, `unidades` | Insumos y unidades de medida |
| `compras` | Compras de insumos |
| `recetas` | Recetas con costo y precio de venta |
| `registros` | Registros de producción |
| `productos` | Productos publicados |
| `inventario` | Inventario |

## Vendedor (`/vendedor/…`)

| Ruta | Módulo |
|---|---|
| `panel` | Panel del vendedor |
| `ventas` (+ `ventas/{venta}/boleta`) | Punto de venta y boleta en PDF |
| `produccion` | Productos disponibles |
| `historial` | Historial de ventas |
| `inventario` | Inventario |
| `clientes` | Clientes |
| `pedidos-web` | Compras por web: confirmar y facturar |

## Asistencia y ajustes (para todos)

| Ruta | Descripción |
|---|---|
| `asistencia/tomar` | Tomar asistencia (solo con el permiso `tomar-asistencia`) |
| `asistencia/mis-asistencias` | Mis marcas y justificaciones |
| `asistencia/justificaciones/{id}/archivo` | Archivo de una justificación (autenticado) |
| `settings/profile`, `settings/appearance`, `settings/security` | Perfil, apariencia y seguridad (dos pasos y passkeys) |
| `settings/teams` | Equipos |
