# Ecolactea Digital V2

Plataforma Kotlin Multiplatform + Compose para trazabilidad de acopio, calidad, liquidaciones, pagos, producción, inventario, ventas y operación offline.

## Stack

- Kotlin Multiplatform, Compose Multiplatform, Android, Web Wasm y Desktop.
- Backend Ktor con MySQL 8.4, Hikari/JDBC y Flyway.
- Migraciones canónicas en `database/migrations/` (V1–V8).
- Android usa Ktor; no accede directamente a MySQL.

## Clonar y rama

```powershell
git clone https://github.com/Kazuto1253/Empresa-de-Leche.git
cd Empresa-de-Leche
git checkout willian-mayta
```

## MySQL local

Instala MySQL 8.4, crea un schema de desarrollo y un usuario de aplicación con permisos sobre ese schema. Por ejemplo, usa un schema como `ecolactea_dev`; no subas bases físicas ni dumps.

## Configuración local

```powershell
Copy-Item config/secrets.properties.example config/secrets.properties
```

Completa los valores locales en `config/secrets.properties`. Este archivo está ignorado por Git y nunca debe subirse. Para ejecutar el backend, configura:

```powershell
$env:ECOLACTEA_CONFIG_FILE="<RUTA_DEL_PROYECTO>\config\secrets.properties"
```

La plantilla incluye `DEV_SEED_PASSWORD`; es necesaria únicamente para el seed manual de desarrollo.

## Ktor y Flyway

```powershell
.\gradlew.bat :server:run
```

Al iniciar contra una base nueva, Flyway aplica automáticamente V1–V8.

```text
http://127.0.0.1:8080/health
http://127.0.0.1:8080/health/db
```

## Seed DEV

El seed es manual, DEV-only e idempotente. Requiere `DEV_SEED_PASSWORD` en el archivo local de secretos y no se ejecuta automáticamente al levantar Ktor.

```powershell
.\gradlew.bat :server:seedDevData
```

## Web

```powershell
.\gradlew.bat :app:webApp:wasmJsBrowserDevelopmentRun
```

La API Web usa la configuración central de entorno. No agregues datos demo hardcodeados al cliente.

## Android

Abre la raíz del proyecto en Android Studio, selecciona la variante `debug` y ejecuta un emulador. En debug, el emulador usa `http://10.0.2.2:8080`; el permiso de Internet y el cleartext HTTP están limitados a desarrollo.

```powershell
.\gradlew.bat :app:androidApp:assembleDebug
```

## Pruebas

```powershell
.\gradlew.bat :server:test
.\gradlew.bat :core:allTests
.\gradlew.bat :app:shared:jvmTest
```

## Flujo Git

Trabaja en tu rama personal. No subas secretos, credenciales, tokens, archivos de IDE, `build/`, `.gradle/`, bases físicas ni dumps. Integra cambios a `main` mediante Pull Request revisado.
