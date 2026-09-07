# Evidencia de RF-34 — 2026-09-07

Autor: Jhon Willian Mayta Arotaype (202413545).

## Git y entorno

- Rama inicial: `main`, árbol limpio. No se modificaron archivos antes del cambio de rama.
- Rama de trabajo: `willian-mayta`.
- HEAD inicial, `origin/main`, `origin/willian-mayta` y `main`: `c6855e1b0b1cd1e6a8cdb6c8cf3298916e2c28d1`.
- `git fetch origin --prune --tags`: primer intento denegado por sandbox al escribir FETCH_HEAD; reintento autorizado, código 0.
- `git switch willian-mayta`: código 0, seguimiento de origin configurado.
- `git pull --ff-only origin willian-mayta`: código 0, Already up to date.
- No hizo falta `git merge --ff-only origin/main`: ambas referencias eran idénticas.
- Windows; wrapper Gradle 9.1.0. Se seleccionó el JBR instalado para lanzar el wrapper porque JAVA_HOME estaba vacío y PATH exponía Java 8. El daemon usa Azul JDK 21, conforme a `gradle-daemon-jvm.properties` existente.

## Comandos y resultados

| Comando | Resultado ejecutado |
|---|---|
| `git status --short` inicial | Vacío; código 0 |
| `git branch --show-current` inicial | main; código 0 |
| Tres `git rev-parse` iniciales y posteriores al fetch | Mismo hash inicial; código 0 |
| `:core:allTests` | OK, 2m40s; 14 pruebas JVM y las mismas 14 Android host. iOS omitido por plataforma |
| `:app:shared:jvmTest` | OK, 43s; 9 pruebas, incluyendo DPAPI Windows |
| `:server:test` | OK, 35s; 9 pruebas (varios escenarios por prueba) |
| `build` | OK, 2m7s, 265 tareas; incluye 8 pruebas compartidas Android host adicionales |
| `:app:androidApp:assembleDebug` | OK, 9s; APK debug generado |
| GET `/health` real | HTTP 200; status=ok, service=ecolactea-server |
| GET `/health/db` real | HTTP 200; configured=false, reachable=false |
| GET `/api/v1/auth/me` real, sin DB | HTTP 503; UNAVAILABLE, sin información sensible |
| `git diff --check` | Código 0 en revisión intermedia; avisos de conversión LF/CRLF, sin errores de whitespace |

El primer intento de `:core:allTests` no inició pruebas: la descarga de Gradle fue denegada por el sandbox. El reintento autorizado fue el que terminó correctamente. No se deshabilitaron pruebas para obtener estos resultados. Los avisos nativos iOS corresponden a la configuración existente de Kotlin/Windows.

El build avisó de nombres KLIB duplicados entre dependencias Compose/AndroidX al compilar metadatos iOS. No hubo errores de compilación. Esto no valida enlace ni ejecución iOS. Las consultas reales se hicieron al JAR del servidor en 127.0.0.1:18080; el proceso temporal se detuvo después. No se detuvo MySQL.

## MySQL

Servicio `MySQL84`: Running. Puertos locales 3306 y 33060 escuchando. No hubo que iniciar ni reiniciar el servicio. No se encontraron DB_URL/DB_USER/DB_PASSWORD configurados para el proceso, ni DB_URL de usuario/máquina, ni un perfil MySQL de acceso local.

No se adivinaron ni restablecieron credenciales. Sin una conexión autorizada, V2 no se aplicó a MySQL y no se ejecutaron login/restauración/revocación contra esa base. Las pruebas backend usan un adaptador transaccional de prueba en memoria, no SQLite ni una base alternativa de producción.

## Control del alcance y pendientes

- UI sin selector de rol; cuatro roles oficiales sin ampliación.
- Sin contraseñas ni tokens reales hardcodeados; las cadenas sintéticas de pruebas no son credenciales de producción. Backend genera aleatoriamente las credenciales de sus fixtures.
- Logs HTTP limitados a método y estado. Sin cuerpos, URLs, cabeceras ni mensajes de excepciones.
- V1 intacta. No se implementaron RF ajenos ni se crearon módulos paralelos.
- Namespace Kotlin conservado. La inspección detectó un `com.example` **preexistente** en `app/iosApp/Configuration/Config.xcconfig`; este incremento no lo introdujo ni modifica el identificador iOS.
- RF-34 **no terminado**: falta integración MySQL; prueba de Android Keystore en dispositivo; Keychain iOS y adaptadores persistentes macOS/Linux; pruebas visuales y recorrido real con backend/base configurados.
- TLS debe configurarse en despliegue. Rate limiting, operación multiinstancia y limpieza programada del historial de sesiones requieren endurecimiento antes de producción.
- No se implementó administración pública de cuentas ni recuperación automática. Solo bootstrap externo y contrato de permiso administrativo.
- No se hizo push ni se modificó la referencia `main`.

## Inventario

Archivos modificados (19):

```text
README.md
app/androidApp/build.gradle.kts
app/androidApp/src/main/AndroidManifest.xml
app/androidApp/src/main/kotlin/pe/gob/huata/ecolactea/MainActivity.kt
app/desktopApp/src/main/kotlin/pe/gob/huata/ecolactea/desktop/Main.kt
app/shared/build.gradle.kts
app/shared/src/commonMain/kotlin/pe/gob/huata/ecolactea/App.kt
app/shared/src/commonMain/kotlin/pe/gob/huata/ecolactea/shared/network/ApiClientFactory.kt
app/shared/src/commonMain/kotlin/pe/gob/huata/ecolactea/shared/presentation/EcolacteaApp.kt
core/src/commonMain/kotlin/pe/gob/huata/ecolactea/core/application/AppResult.kt
core/src/commonMain/kotlin/pe/gob/huata/ecolactea/core/application/auth/AuthContracts.kt
core/src/commonMain/kotlin/pe/gob/huata/ecolactea/core/model/Role.kt
docs/ARCHITECTURE.md
docs/DEVELOPMENT.md
docs/requerimientos.md
server/src/main/kotlin/pe/gob/huata/ecolactea/server/Application.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/Http.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/Routing.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/ServerConfig.kt
```

Archivos creados (22):

```text
app/androidApp/src/debug/AndroidManifest.xml
app/shared/src/androidMain/kotlin/pe/gob/huata/ecolactea/shared/auth/AndroidSessionStore.kt
app/shared/src/commonMain/kotlin/pe/gob/huata/ecolactea/shared/network/HttpAuthRepository.kt
app/shared/src/commonMain/kotlin/pe/gob/huata/ecolactea/shared/presentation/AuthController.kt
app/shared/src/commonTest/kotlin/pe/gob/huata/ecolactea/shared/network/HttpAuthRepositoryTest.kt
app/shared/src/commonTest/kotlin/pe/gob/huata/ecolactea/shared/presentation/AuthControllerTest.kt
app/shared/src/jvmMain/kotlin/pe/gob/huata/ecolactea/shared/auth/DesktopSessionStore.kt
app/shared/src/jvmTest/kotlin/pe/gob/huata/ecolactea/shared/auth/WindowsSessionStoreTest.kt
core/src/commonMain/kotlin/pe/gob/huata/ecolactea/core/model/Permission.kt
core/src/commonMain/kotlin/pe/gob/huata/ecolactea/core/network/AuthDtos.kt
core/src/commonTest/kotlin/pe/gob/huata/ecolactea/core/application/auth/AuthUseCasesTest.kt
docs/RF-34.md
docs/evidencias/RF-34-validacion.md
server/src/main/kotlin/pe/gob/huata/ecolactea/server/auth/AuthPersistence.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/auth/AuthRoutes.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/auth/AuthService.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/auth/JdbcAuthPersistence.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/auth/PasswordHasher.kt
server/src/main/resources/db/migration/V2__authentication_sessions.sql
server/src/test/kotlin/pe/gob/huata/ecolactea/server/AuthRoutesTest.kt
server/src/test/kotlin/pe/gob/huata/ecolactea/server/AuthTestFixture.kt
server/src/test/kotlin/pe/gob/huata/ecolactea/server/PasswordHasherTest.kt
```
