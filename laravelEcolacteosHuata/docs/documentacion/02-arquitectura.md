# 02 · Arquitectura

## Visión general

```
Navegador (paneles Livewire + tienda pública)   App móvil (Kotlin Multiplatform)
                    │  HTTPS                                    │  HTTPS + token
                    ▼                                           ▼
        ┌────────────────────────── Laravel 13 (PHP 8.3) ──────────────────────────┐
        │ Rutas y middleware (rol, equipo, CSRF, límites de intentos)               │
        │ Componentes Livewire + Flux UI │ Controladores (pedidos, PDF, API móvil)  │
        │ Modelos Eloquent y reglas de negocio · Enums · Notificaciones             │
        └───────────────┬───────────────────────────────┬───────────────────────────┘
                        ▼                               ▼
                MySQL (SQLite en pruebas)         PDF (dompdf) · enlace wa.me
```

## Tecnologías

| Capa | Tecnología | Uso |
|---|---|---|
| Lenguaje y framework | PHP 8.3, Laravel 13 | Rutas, modelos, validación, autenticación |
| Interfaz de paneles | Livewire 4 (componentes de un solo archivo), Flux UI | Páginas interactivas sin API propia |
| Estilos | Tailwind CSS 4 con Vite 8 | Sistema de diseño con tokens `--hh-*`, modo claro y oscuro |
| Página pública 3D | Three.js, GSAP | Intro, vaso de leche y vitrina de productos |
| Autenticación | Laravel Fortify, passkeys | Login, verificación en dos pasos, passkeys |
| PDF | barryvdh/laravel-dompdf | Boleta, comprobante de pedido, inventario, laboratorio |
| Calidad | Pest 4, Laravel Pint, Larastan (PHPStan) | Pruebas, estilo y análisis estático |

## Patrón de diseño

Laravel sigue el patrón **Modelo-Vista-Controlador**: los modelos Eloquent representan las tablas, las vistas Blade presentan la interfaz y los controladores reciben las peticiones. En este sistema casi todas las pantallas del panel son **componentes Livewire de un solo archivo** (`⚡nombre.blade.php`) que reúnen la lógica y la vista de una página. Se registran con `Route::livewire('ruta', 'pages::rol.nombre')`.

Los controladores clásicos se reservan para lo que no es una pantalla: PDF, comprobantes, pedidos públicos y la API móvil.

## Estructura del código

```
app/
├── Enums/             Rol, EstadoLeche, EstadoProduccion, EstadoPedidoWeb, MetodoPago, Periodo, …
├── Models/            33 modelos (Recojo, Proveedor, ResultadoLaboratorio, Produccion, Venta, PedidoWeb, …)
├── Http/Controllers/  PedidoWebController, PedidoWebBoletaController, *PdfController, Api/Mobile*
├── Http/Middleware/   EnsureRole, EnsureTeamMembership, SetTeamUrlDefaults, AuthenticateMobileToken
├── Notifications/     ComunicadoPublicado, ProveedorUbicacionActualizada, notificaciones de equipos
└── Console/Commands/  RegistrarFaltasAcopiadores
resources/
├── views/pages/<rol>/ Componentes Livewire por rol (admin, planta, acopiador, proveedor, produccion, vendedor, asistencia, settings)
├── views/components/  Componentes Blade (ui/page-header, ui/kpi, ui/panel, mapas, campana de notificaciones…)
├── views/layouts/     Menú lateral y layouts de autenticación
├── views/welcome.blade.php    Tienda pública
├── css/               app.css, sistema.css (tokens y tema), landing.css
└── js/                intro3d.js, hero3d.js, productos3d.js, modelos3d.js, interacciones.js
routes/                web.php, api.php, settings.php y un archivo por rol
database/              64 migraciones, 32 fábricas, seeders
tests/Feature/         53 archivos de pruebas Pest
```

## Roles, equipos y acceso

- El enum `App\Enums\Rol` define seis roles: `administrador`, `personal_planta`, `acopiador`, `proveedor`, `vendedor` y `produccion`. Cada uno tiene etiqueta, prefijo de ruta, menú lateral y panel.
- Un usuario tiene un rol principal (`rol`) y puede tener roles extra (`roles`, JSON).
- Las rutas internas viven bajo `{current_team}/…`. Existe un equipo principal (`ecolacteos-huata`).
- Middleware: `EnsureTeamMembership` verifica pertenencia al equipo, y `EnsureRole` (por ejemplo `role:vendedor,administrador`) restringe cada módulo.
- Tras iniciar sesión, la ruta `dashboard` redirige al panel del rol del usuario.
- La asistencia se controla con el permiso `tomar-asistencia` (administrador o quien tenga `toma_asistencia`).

## Seguridad

- **CSRF** en todos los formularios y en `POST /pedidos`.
- **Límites de intentos:** `POST /pedidos` 8 por minuto, `GET /pedido/{token}/boleta` 30 por minuto, `POST /api/mobile/login` 30 por minuto.
- **Pedidos públicos:** el servidor toma precios y stock del catálogo; el navegador no puede manipularlos. El comprobante se abre solo con un token secreto de 40 caracteres.
- **API móvil:** token de 60 caracteres guardado como hash SHA-256 en `mobile_tokens`; el usuario dueño debe seguir activo.
- **Verificación en dos pasos y passkeys** con Fortify.
- **Archivos de justificaciones** servidos por una ruta autenticada, no por carpeta pública.

## Interfaz

Sistema de diseño «Huata Corporate»: fondo claro con la foto de la finca bajo un velo azul translúcido, menú lateral azul hielo, tarjetas blancas y banner de bienvenida. Colores: azul corporativo `#2864B4`, verde Huata `#23865B`, azul profundo `#173D72`, rojo `#C93445`, amarillo `#F2C230`. Tipografías Bricolage Grotesque (títulos) y Figtree (interfaz). Incluye modo oscuro y respeta «reducir movimiento».
