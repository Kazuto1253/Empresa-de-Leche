# Arquitectura

La base usa Clean Architecture de forma práctica: dependencias hacia el dominio, contratos compartidos en `core` e implementaciones en capas externas.

## Capas

- `core/model`: entidades y value objects independientes de frameworks. Aquí están `Role` y `AuthenticatedUser`.
- `core/application`: resultados, errores y contratos de casos de uso.
- `core/application/auth`: contratos de autenticación, sesión persistente y restauración de sesión.
- `core/application/sync`: contratos offline-first para conectividad, outbox, idempotencia y sincronización.
- `core/network`: modelos DTO compartidos para envelopes y errores API.
- `app/shared`: presentación Compose, navegación base, configuración de entorno y cliente HTTP.
- `app/webApp`: entrada Compose de navegador, targets wasmJs/js y distribución compatible. Reutiliza core/shared; Web opera online.
- `server`: Ktor, rutas REST, status pages, logging, configuración externa, MySQL, Flyway y adapters de infraestructura.

## Roles Oficiales

La aplicación reconoce exactamente:

- `ADMINISTRADOR_GENERAL`
- `PERSONAL_PLANTA`
- `ACOPIADOR`
- `PROVEEDOR`

El usuario no escoge rol en login. El backend autentica y devuelve identidad y rol autorizado.

## Autenticación y Sesión

RF-34 tiene un primer incremento implementado, pendiente de validación integral; véase [RF-34](RF-34.md). Reutiliza los contratos:

- `AuthRepository`: login, refresh, validate y logout.
- `SessionStore`: lectura/escritura/limpieza de sesión persistida.
- `RestoreSessionUseCase`: restauración de sesión al abrir la app.

La contraseña nunca debe persistirse. Los tokens deben guardarse usando almacenamiento seguro específico por plataforma: Keystore/Encrypted storage en Android, Keychain en iOS y una alternativa segura por sistema operativo en Desktop.

## Offline-First

La base incluye contratos para operación sin conexión:

- `ConnectivityMonitor`
- `PendingOperationStore`
- `SyncGateway`
- `SyncManager`
- `PendingOperation`
- `SyncConflict`

SQLite local es la base prevista para Android/iOS/Desktop. SQLDelight y sus drivers KMP se integrarán con RF-08 por su responsable. `LocalDataStore` define transacciones por usuario/dispositivo; `LocalTransaction` reutiliza `PendingOperationStore` para guardar registros y outbox atómicamente. No contiene tokens ni reglas de acopio. Web no requiere SQLite.

El [modelo lógico V2](DATA_MODEL_V2.md) define ownership, idempotencia, FK, cardinalidades y pendientes de validación. No se implementan RF offline en este ciclo.

## Backend

`/health` responde sin depender de MySQL. `/health/db` indica configuración y comprueba una conexión real del pool.

Android, iOS, Windows, macOS, Linux y Web consumen la misma API REST `/api/v1`. Ktor puede servir la distribución Web en `/` mediante `WEB_ROOT`, sin segundo backend ni acceso directo de clientes a MySQL. No hay Laravel, Spring, Angular ni Docker. Las rutas desconocidas de API siguen devolviendo 404, no el HTML de la aplicación.

La configuración admite variables de entorno y un archivo UTF-8 KEY=value seleccionado explícitamente mediante `ECOLACTEA_CONFIG_FILE`. El entorno tiene prioridad; no hay interpolación ni logs de secretos. MySQL se activa solo cuando existe `DB_URL`. Al arrancar con base configurada, Flyway ejecuta migraciones desde `server/src/main/resources/db/migration`.

## Dependency Injection

La base usa composición manual. No se añadió framework DI porque la estructura actual aún es pequeña y los contratos permiten sustituir implementaciones sin acoplar el dominio.
