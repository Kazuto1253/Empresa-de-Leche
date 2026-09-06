# Arquitectura

La base usa Clean Architecture de forma práctica: dependencias hacia el dominio, contratos compartidos en `core` e implementaciones en capas externas.

## Capas

- `core/model`: entidades y value objects independientes de frameworks. Aquí están `Role` y `AuthenticatedUser`.
- `core/application`: resultados, errores y contratos de casos de uso.
- `core/application/auth`: contratos de autenticación, sesión persistente y restauración de sesión.
- `core/application/sync`: contratos offline-first para conectividad, outbox, idempotencia y sincronización.
- `core/network`: modelos DTO compartidos para envelopes y errores API.
- `app/shared`: presentación Compose, navegación base, configuración de entorno y cliente HTTP.
- `server`: Ktor, rutas REST, status pages, logging, configuración externa, MySQL, Flyway y adapters de infraestructura.

## Roles Oficiales

La aplicación reconoce exactamente:

- `ADMINISTRADOR_GENERAL`
- `PERSONAL_PLANTA`
- `ACOPIADOR`
- `PROVEEDOR`

El usuario no escoge rol en login. El backend autentica y devuelve identidad y rol autorizado.

## Autenticación y Sesión

RF-34 queda preparado con contratos para:

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

No se implementan RF offline todavía. La persistencia local KMP definitiva debe añadirse cuando un RF real la necesite; SQLDelight puede evaluarse entonces si compensa su coste.

## Backend

`/health` siempre responde sin depender de MySQL. `/health/db` informa si la base fue configurada y si el pool está activo.

MySQL se activa solo cuando existe `DB_URL`. Al arrancar con base configurada, Flyway ejecuta migraciones desde `server/src/main/resources/db/migration`.

## Dependency Injection

La base usa composición manual. No se añadió framework DI porque la estructura actual aún es pequeña y los contratos permiten sustituir implementaciones sin acoplar el dominio.
