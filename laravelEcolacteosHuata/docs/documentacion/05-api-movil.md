# 05 · API móvil

API REST para la aplicación móvil nativa (Kotlin Multiplatform), diseñada para trabajar **sin conexión**: la app guarda todo en su base local y, cuando hay internet, descarga lo que cambió en el servidor y envía lo que se hizo en el teléfono.

- **Base:** `https://<tu-dominio>/api`
- **Formato:** JSON (`Content-Type: application/json`, `Accept: application/json`)
- **Autenticación:** token tipo *bearer* obtenido en el login
- **Zona horaria:** las marcas de tiempo vienen en ISO 8601

| Método | Ruta | Descripción | Protección |
|---|---|---|---|
| `POST` | `/api/mobile/login` | Inicia sesión y entrega el token | 30 intentos por minuto |
| `GET` | `/api/mobile/sync` | Descarga los datos (completos o desde una fecha) | Token |
| `POST` | `/api/mobile/cambios` | Envía lo creado, editado o eliminado en el teléfono | Token |

## Autenticación

### `POST /api/mobile/login`

Valida usuario y contraseña. El usuario debe estar activo.

**Petición**

```json
{ "usuario": "acopio", "password": "••••" }
```

**Respuesta 200**

```json
{
  "token": "Zk3…(60 caracteres)",
  "id": "5",
  "usuario": "acopio",
  "nombres": "Nombre",
  "apellidos": "Apellido",
  "documento": "",
  "celular": "",
  "rol": "acopiador",
  "activo": true
}
```

**Errores:** `422` si faltan datos («Usuario y contraseña son obligatorios.»); `401` si las credenciales son incorrectas o el usuario está inactivo.

El token se guarda en el servidor solo como hash SHA-256 (`mobile_tokens`). En las siguientes peticiones se envía en la cabecera:

```
Authorization: Bearer <token>
```

Sin token válido, o con un usuario inactivo, el servidor responde `401` con el mensaje «Inicie sesión en la app con conexión al servidor para descargar los datos.».

## Descarga de datos

### `GET /api/mobile/sync?since=<fecha ISO 8601>`

- **Sin `since`:** volcado completo (primera instalación).
- **Con `since`:** solo las filas cuyo `updated_at` es mayor o igual a esa fecha. Si la fecha lleva `+` (por ejemplo `+00:00`), envíala codificada en la URL (`%2B`).
- Los **turnos y las rutas** siempre vienen completos, porque sus cambios en tablas pivote no actualizan `updated_at`.
- Antes de responder, el servidor registra las faltas vencidas de asistencia, para que el teléfono las vea.

**Respuesta 200** (resumen de claves)

```json
{
  "servidor_hora": "2026-10-25T21:00:00+00:00",
  "empresa": [], "precio_leche": [], "descuentos": [],
  "usuarios": [], "acopiadores": [], "proveedores": [],
  "turnos": [], "turnos_produccion": [], "rutas": [],
  "clientes": [], "unidades_medida": [], "insumos": [],
  "recetas": [], "receta_detalles": [], "comunicados": [],
  "recojos": [], "resultados_laboratorio": [], "entregas_planta": [], "pagos_proveedor": [],
  "compras": [], "compra_detalles": [], "producciones": [], "ventas": [],
  "horarios_asistencia": [], "dias_asistencia_acopiador": [], "asistencias": [], "justificaciones": [],
  "notificaciones": [],
  "ids": { "<tabla>": ["<id vigente>", "…"] }
}
```

- `servidor_hora` se toma **antes** de consultar. Guárdala y úsala como `since` de la siguiente sincronización: lo que cambie mientras se arma la respuesta vuelve a bajar y no se pierde (el guardado local debe ser idempotente).
- `ids` lista los ids que **siguen existiendo** en el servidor, por tabla. La app debe borrar de su base local lo que ya no aparezca (un `since` nunca informa de lo eliminado).
- `notificaciones` trae las 30 más recientes del usuario del token; la app reemplaza su lista con estas.
- Las ventas bajan con sus `items`.

## Envío de cambios

### `POST /api/mobile/cambios`

Aplica en la base del servidor lo hecho en el teléfono, **en el mismo orden en que ocurrió**. Un cambio rechazado no detiene a los demás.

**Petición**

```json
{
  "cambios": [
    {
      "tabla": "recojo",
      "id": "f3a1c9e0-…",
      "operacion": "guardar",
      "datos": { "proveedor_id": "12", "litros": 18.5, "estado_leche": "normal" }
    },
    { "tabla": "venta", "id": "77", "operacion": "eliminar" }
  ]
}
```

| Campo | Regla |
|---|---|
| `cambios` | Obligatorio, lista |
| `tabla` | Obligatorio; una de las tablas admitidas (abajo) o `notificacion` |
| `id` | Obligatorio; UUID local para registros nuevos, o el id del servidor para editar o eliminar |
| `operacion` | `guardar` o `eliminar` |
| `datos` | Fila local completa (columnas del SQLite de la app) |

**Tablas admitidas:** `empresa`, `precio_leche`, `descuento`, `usuario`, `acopiador`, `proveedor`, `turno`, `turno_produccion`, `ruta`, `cliente`, `unidad_medida`, `insumo`, `receta`, `receta_detalle`, `comunicado`, `recojo`, `resultado_laboratorio`, `entrega_planta`, `pago_proveedor`, `compra`, `compra_detalle`, `produccion`, `venta`, `horario_asistencia`, `dia_asistencia_acopiador`, `asistencia`, `justificacion`.

La tabla especial `notificacion` con `datos.leida = true` marca como leída una notificación propia.

**Respuesta 200**

```json
{
  "ids": [ { "tabla": "recojo", "id_local": "f3a1c9e0-…", "id": "412" } ],
  "errores": [ { "tabla": "venta", "id": "77", "message": "No se puede eliminar porque tiene registros relacionados en el servidor." } ]
}
```

- `ids`: por cada registro **nuevo**, el id asignado por el servidor; el teléfono reemplaza su UUID por este (la relación queda en `mobile_ids`).
- `errores`: cada cambio rechazado, con un mensaje legible. La app puede mostrarlo o reintentar.

### Reglas que el servidor aplica

- Las mismas reglas del panel web: por ejemplo, **enviar un resultado conforme a producción** suma la leche al stock una sola vez; las tablas de horarios y días de asistencia solo las cambia quien tiene permiso de tomar asistencia; la marca propia del acopiador y las justificaciones del trabajador se revisan por separado.
- Cada cambio corre en su propia transacción. Si la base lo rechaza (datos repetidos o incompletos, o registros relacionados al eliminar) se devuelve el error correspondiente en `errores`.
- Un error inesperado del servidor se informa como «Error del servidor al guardar el cambio.» y se registra en el log.

## Flujo recomendado en la app

1. `POST /login` → guardar token y datos del usuario.
2. `GET /sync` sin `since` → poblar la base local; guardar `servidor_hora`.
3. Trabajar sin conexión; cada operación se guarda localmente con su UUID y queda en una cola de cambios.
4. Al detectar internet: `POST /cambios` con la cola (en orden) → reemplazar UUID por `ids` y mostrar `errores`.
5. `GET /sync?since=<servidor_hora anterior>` → aplicar cambios y borrar lo que no figure en `ids`.
6. Repetir desde el paso 3.

## Ejemplo con curl

```bash
# 1) Login
curl -s -X POST https://tu-dominio.pe/api/mobile/login \
  -H "Content-Type: application/json" -H "Accept: application/json" \
  -d '{"usuario":"acopio","password":"1234"}'

# 2) Sincronización completa
curl -s https://tu-dominio.pe/api/mobile/sync \
  -H "Authorization: Bearer <token>" -H "Accept: application/json"

# 3) Solo lo nuevo desde una fecha
curl -s "https://tu-dominio.pe/api/mobile/sync?since=2026-10-25T21:00:00%2B00:00" \
  -H "Authorization: Bearer <token>" -H "Accept: application/json"
```

> Los ejemplos de arriba usan datos de muestra. La contraseña `1234` es solo la de los usuarios de desarrollo.
