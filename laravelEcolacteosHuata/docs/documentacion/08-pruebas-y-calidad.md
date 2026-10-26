# 08 · Pruebas y calidad

## Resumen

| Herramienta | Uso | Resultado actual |
|---|---|---|
| **Pest 4** | Pruebas automatizadas (Feature y Unit) | 352 ejecutadas · 1 202 aserciones · 349 aprobadas · 3 fallidas |
| **Laravel Pint** | Estilo de código | Sin observaciones |
| **Larastan (PHPStan)** | Análisis estático (`phpstan.neon`) | Disponible con `composer types:check` |

Las pruebas usan **SQLite en memoria**, así que cada una parte de datos limpios y no toca la base de desarrollo.

## Cómo ejecutarlas

```bash
php artisan test                       # todas las pruebas
vendor/bin/pest --filter=PedidosWeb    # solo las que coinciden con un nombre
vendor/bin/pest tests/Feature/Api      # una carpeta
composer test                          # estilo (Pint) + análisis estático + pruebas
composer lint                          # arreglar el estilo con Pint
composer lint:check                    # solo comprobar el estilo
```

Si el terminal no tiene el controlador de SQLite habilitado:

```bash
php -d extension=pdo_sqlite -d extension=sqlite3 vendor/bin/pest
```

## Qué cubren las pruebas

Hay 53 archivos de pruebas en `tests/Feature`. Por área:

| Área | Archivos de ejemplo | Qué verifican |
|---|---|---|
| Autenticación y ajustes | `Auth/*`, `Settings/*` | Login, restablecer contraseña, dos pasos, perfil y seguridad |
| Equipos | `Teams/*` | Invitaciones, miembros y permisos |
| Acceso por rol y administración | `RoleAccessTest`, `Admin*Test`, `PanelesPorRolTest` | Que cada rol entre solo a sus módulos; usuarios, turnos y paneles |
| Acopio y rutas | `RecojoAcopiadorTest`, `EntregasAcopiadorTest`, `MapaRutasTest`, `Planta*Test`, `Proveedor*Test` | Recojos, entregas, rutas, asignaciones y bloqueo por calidad |
| Laboratorio y pagos | `PlantaLaboratorioTest`, `EnviarProduccionLaboratorioTest`, `PlantaPagosTest` | Resultados, envío único a producción y pagos |
| Producción e inventario | `ProduccionRoleTest`, `ProduccionStockInsuficienteTest`, `ComprasInsumosTest`, `InsumoStockTest` | Recetas, costos, descuento y bloqueo de stock |
| Ventas | `Venta*Test`, `Vendedor*Test`, `HistorialLimpiarTest` | Boleta, descuentos, clientes, productos e inventario |
| Tienda web | `PedidosWebTest`, `PaginaInicioProductosTest` | Catálogo con stock y precio, precios del servidor, Yape con N.º de operación, confirmación como venta |
| Asistencia y comunicados | `AsistenciaTest`, `Comunicado*Test` | Toma, justificaciones, horarios, falta automática y notificaciones |
| API móvil | `Api/MobileSyncTest`, `Api/MobileCambiosTest` | Login, sincronización y cambios sin conexión |

## Casos importantes

- **Bloqueo por calidad:** menos de 3 alteraciones no bloquea; con 3 se bloquea; desbloquear reinicia el contador.
- **Stock:** producir sin insumos suficientes se bloquea y no descuenta nada.
- **Pedido web:** guarda con precios del servidor aunque el navegador mande otros; con Yape exige el N.º de operación; si una línea no tiene stock, confirmar no crea la venta (todo o nada); confirmar crea la venta con boleta y descuenta stock.
- **Acceso:** un rol no entra a módulos de otro.

## Pruebas fallidas conocidas

Tres pruebas fallan y **ya fallaban antes del rediseño visual**, por lo que no se relacionan con él:

| Archivo | Pruebas | Tema |
|---|---|---|
| `PlantaPagosTest` | 2 | La tabla de pagos con el litraje confirmado por laboratorio |
| `ProveedorMisEntregasTest` | 1 | La pantalla «Mis entregas» del proveedor |

Están pendientes de corrección. Antes de un despliegue definitivo conviene resolverlas o, si el comportamiento esperado cambió, actualizar las pruebas.

## Buenas prácticas del proyecto

- Cada funcionalidad nueva lleva su prueba Pest.
- Ejecutar `vendor/bin/pint --format agent` (o `composer lint`) antes de confirmar cambios.
- Las páginas del panel se prueban con `Livewire::test('pages::rol.nombre')`.
- Los textos de las pruebas están en español, igual que el dominio del sistema.
