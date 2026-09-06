# Ecoláctea Digital

Ecoláctea Digital es una aplicación multiplataforma para apoyar la gestión láctea municipal de Huata. Esta base técnica prepara un único cliente Kotlin Multiplatform para Android, iOS y Desktop, más un backend independiente en Ktor con API REST y MySQL.

## Stack

- Kotlin Multiplatform y Compose Multiplatform.
- Android, iOS y Desktop JVM.
- Backend Kotlin con Ktor.
- MySQL como base de datos central.
- Flyway para migraciones.
- HikariCP para pool JDBC.
- kotlinx.serialization, coroutines y Ktor Client para comunicación compartida.

No hay target Web, Dockerfile ni Docker Compose en esta base. El plugin Ktor expone tareas opcionales Docker/Jib, pero no son necesarias para compilar, testear ni ejecutar el servidor local.

## Módulos y Componentes

- `:core`: dominio compartido, modelos puros, resultados, errores, contratos de autenticación, sesión y sincronización offline-first.
- `:app:shared`: UI Compose compartida, navegación base, configuración cliente y networking multiplataforma.
- `:app:androidApp`: entrada Android y configuración propia de Android.
- `:app:desktopApp`: entrada Compose Desktop para Windows, macOS y Linux.
- `:server`: servidor Ktor, configuración HTTP, rutas, migraciones y conexión opcional a MySQL.
- `app/iosApp`: proyecto Xcode, no módulo Gradle, que consume el framework KMP `Shared` generado desde `:app:shared`.

## Requisitos

- JDK compatible con Gradle/Kotlin del proyecto.
- Android Studio o IntelliJ IDEA con soporte KMP.
- Android SDK para compilar Android.
- Xcode en macOS para compilar y ejecutar iOS.
- MySQL local o remoto solo cuando se prueben endpoints que dependan de base de datos.

## Abrir el Proyecto

Abre `D:\Ecolactea\Ecolactea_V100` desde el IDE. La estructura Gradle esperada es:

```text
:app
|-- :app:androidApp
|-- :app:desktopApp
|-- :app:shared
:core
:server
```

## Ejecutar Desktop

```powershell
.\gradlew.bat :app:desktopApp:run
```

En Windows este es el target ejecutable principal para validar la app visualmente.

## Compilar Android

```powershell
.\gradlew.bat :app:androidApp:assembleDebug
```

No requiere emulador ni dispositivo para validar compilación.

## Ejecutar Server

```powershell
.\gradlew.bat :server:run
```

Luego prueba:

```powershell
Invoke-RestMethod http://localhost:8080/health
```

Respuesta esperada:

```json
{"status":"ok","service":"ecolactea-server"}
```

## Configurar MySQL

El servidor no necesita MySQL para compilar, testear ni responder `/health`. Para activar conexión y migraciones, define variables de entorno antes de iniciar `:server:run`:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/ecolactea"
$env:DB_USER = "ecolactea_user"
$env:DB_PASSWORD = "cambiar-en-entorno-local"
$env:DB_POOL_SIZE = "10"
```

No versiones contraseñas reales. Las migraciones viven en `server/src/main/resources/db/migration`.

## iOS Desde Windows

Los targets `iosArm64` e `iosSimulatorArm64` se mantienen. En Windows puede aparecer el aviso de que `iosSimulatorArm64Test` requiere macOS; eso es esperado y no significa que el proyecto esté roto. La ejecución final de iOS se realiza en macOS con Xcode abriendo `app/iosApp`.

## Dónde Desarrollar

- Código común de dominio: `core/src/commonMain/kotlin/pe/gob/huata/ecolactea/core`.
- UI común, navegación y networking cliente: `app/shared/src/commonMain/kotlin/pe/gob/huata/ecolactea/shared`.
- Android específico: `app/androidApp` y `app/shared/src/androidMain`.
- iOS específico: `app/iosApp` y `app/shared/src/iosMain`.
- Desktop específico: `app/desktopApp` y `app/shared/src/jvmMain`.
- Backend: `server/src/main/kotlin/pe/gob/huata/ecolactea/server`.

## Pruebas

```powershell
.\gradlew.bat :core:allTests
.\gradlew.bat :app:shared:jvmTest
.\gradlew.bat :server:test
```

Para validación amplia en Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat :app:androidApp:assembleDebug
.\gradlew.bat :app:desktopApp:run
```
