# Documentación de Ecolácteos Huata

Sistema web de gestión y tienda en línea de la Asociación de Productores de Huata (Puno, Perú). Da trazabilidad a la leche desde el proveedor y el acopio, pasando por laboratorio y producción, hasta la venta con boleta, y paga a los proveedores.

| Documento | Contenido | Para quién |
|---|---|---|
| [01 · Instalación y despliegue](01-instalacion-y-despliegue.md) | Requisitos, configuración, puesta en marcha y servidor | Quien monta el sistema |
| [02 · Arquitectura](02-arquitectura.md) | Stack, capas, estructura del código, seguridad | Desarrolladores |
| [03 · Modelo de datos](03-modelo-de-datos.md) | Tablas, campos, relaciones y enums | Desarrolladores |
| [04 · Reglas de negocio](04-reglas-de-negocio.md) | Cálculos y reglas que el sistema aplica | Analistas y desarrolladores |
| [05 · API móvil](05-api-movil.md) | Endpoints, autenticación, sincronización y ejemplos | Quien desarrolla la app |
| [06 · Rutas y módulos](06-rutas-y-modulos.md) | Pantallas por rol y rutas públicas | Todos |
| [07 · Manual de usuario](07-manual-de-usuario.md) | Guía de uso por rol | Personal de la asociación |
| [08 · Pruebas y calidad](08-pruebas-y-calidad.md) | Cómo ejecutar las pruebas y qué cubren | Desarrolladores |

## Resumen del sistema

- **Seis roles:** administrador, personal de planta, acopiador, proveedor, producción y vendedor. Cada uno entra a su propio panel.
- **Tienda pública** en la página de inicio, con carrito, pago por Yape (QR), Plin o efectivo y comprobante por enlace, sin iniciar sesión.
- **API móvil** para una aplicación nativa que trabaja sin conexión y sincroniza cuando hay internet.
- **Stack:** PHP 8.3, Laravel 13, Livewire 4, Flux UI, Tailwind CSS 4, Vite, MySQL (SQLite en pruebas).

## Estado actual

- Pruebas automatizadas: 352 ejecutadas, 349 aprobadas, 3 fallidas conocidas (ver [08](08-pruebas-y-calidad.md)).
- El código vive en la rama `web-laravel-huata` de https://github.com/Kazuto1253/Empresa-de-Leche.
