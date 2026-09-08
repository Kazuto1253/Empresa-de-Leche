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

## Ciclo de plataforma posterior al Commit 01 — 2026-09-07

Esta sección corresponde al ciclo siguiente; los resultados anteriores son históricos y no se atribuyen a las nuevas plataformas.

### Base y alcance

- Rama `willian-mayta`, HEAD inicial `6c710814284d0a5f684197b2cb25fc923d2419cb`, árbol inicialmente limpio.
- `main` se conserva en `c6855e1b0b1cd1e6a8cdb6c8cf3298916e2c28d1`. No merge, rebase, amend ni push.
- Se añade `app/webApp` con targets JS/Wasm, reutilizando core/shared; Ktor sirve el artefacto mediante WEB_ROOT.
- Configuración externa literal, script local y bootstrap restringido a base sin usuarios. V1/V2 no se modificaron ni se creó otra migración.
- Modelo lógico global DATA_MODEL_V2.md y contratos LocalDataStore/LocalTransaction. No se implementan RF funcionales de otros integrantes.

### Resultados ejecutados

| Comando / comprobación | Resultado real |
|---|---|
| `:core:jvmTest` | 14 pruebas, 0 fallos, 0 omitidas |
| `:app:shared:jvmTest` | 9 pruebas, 0 fallos, 0 omitidas; incluye DPAPI Windows |
| `:server:test` | 12 pruebas, 0 fallos, 0 omitidas; incluye configuración externa y rutas Web/API |
| `:server:compileIntegrationTestKotlin` | Correcto tras añadir la dependencia explícita de ContentNegotiation del cliente |
| `:app:androidApp:assembleDebug` | Correcto; APK debug generado |
| `:app:desktopApp:build` | Correcto; JVM compilado. No ejecuta pruebas Desktop propias (NO-SOURCE), ni certifica macOS/Linux |
| `:server:shadowJar` | Correcto; server/build/libs/server-all.jar |
| Comando conjunto de las siete tareas anteriores, `--max-workers=2` | BUILD SUCCESSFUL, código 0, 1m24s, 99 tareas (11 ejecutadas, 88 actualizadas) |
| `:app:webApp:composeCompatibilityBrowserDistribution`, `--max-workers=1` | BUILD SUCCESSFUL, código 0, 21m14s; artefactos JS y Wasm y selector compatible generados. Webpack advirtió por tamaño de bundles (Wasm de Skiko 8.25 MiB y aplicación 3.59 MiB) |
| Repetición incremental Web con `--max-workers=2` | BUILD SUCCESSFUL, código 0, 48s; 85 tareas (11 ejecutadas, 74 actualizadas). Confirma lockfiles y orden JS/Wasm |
| `:server:mysqlIntegrationTest` con ECOLACTEA_CONFIG_FILE | Falló, código 1, 1m42s: `DB_USER is required when DB_URL is configured`. No omitida ni simulada |
| Servicio MySQL84 y conexión TCP local 3306 | Running; conexión TCP establecida. Esto no prueba autenticación SQL |
| GET real `http://127.0.0.1:18080/health` | HTTP 200, `{"status":"ok","service":"ecolactea-server"}` |
| GET real `http://127.0.0.1:18080/health/db` sin credenciales | HTTP 200, `{"configured":false,"reachable":false}` |
| Artefacto JS servido por Ktor en 18081, navegador integrado | Login Compose renderizado; campos vacíos muestran validación. Envío sintético llega a Ktor (POST 503), UI muestra servicio no disponible y limpia contraseña |

Los tres conjuntos de pruebas suman 35 pruebas sin fallos. No se ejecutó Xcode ni se declara iOS probado. `build` global y `core:allTests` del ciclo anterior no se vuelven a atribuir a este ciclo: se priorizaron pruebas JVM y builds específicos de plataforma.

### Fallos encontrados y corregidos durante el trabajo

- DSL de compatibilidad Web no disponible en el plugin instalado: se usa la tarea Compose registrada automáticamente. ComposeViewport requiere opt-in experimental, añadido.
- Compilación de la prueba MySQL: dependencia de cliente ContentNegotiation no transitiva, declarada explícitamente y compilación repetida correctamente.
- Primera distribución Web: faltaba build/wasm/yarn.lock después de instalaciones Yarn concurrentes. Se repitió en serie y se añadió orden entre tareas de instalación JS/Wasm. Los lockfiles generados se conservan para reproducibilidad.

### MySQL y RF-34: limitación material

`secrets.properties` está ignorado por Git y no contiene credenciales válidas de DB_USER/DB_PASSWORD. Se solicitó completarlas externamente, sin enviarlas por chat. No se cambió ninguna contraseña MySQL ni se creó una cuenta privilegiada para eludir esta dependencia.

Flyway V1/V2, historial SQL, login de cuatro roles, refresh/rotación/replay, /me, logout, bloqueo y permisos contra MySQL quedan **NO VERIFICADOS**. La prueba explícita está compilada y preparada, pero falló antes de abrir JDBC. El servidor usado para comprobar salud y Web se ejecutó sin configuración DB efectiva, por eso /health/db sigue false/false.

RF-34 permanece **PARCIAL**. Android conserva Keystore y Windows DPAPI; iOS, macOS y Linux tienen memoria y requieren adaptadores seguros probados en sus plataformas. Web tiene memoria por pestaña, pierde sesión al recargar y requiere implementar cookies HttpOnly/CSRF para persistencia. No se guardan tokens en SQLite ni almacenamiento persistente de navegador. FC-01–FC-04 y nuevos RF continúan pendientes; bootstrap inicial no es un módulo administrativo.

### Inventario del ciclo

Archivos creados y versionados:

```text
.env.example
app/webApp/build.gradle.kts
app/webApp/src/webMain/kotlin/pe/gob/huata/ecolactea/web/Main.kt
app/webApp/src/webMain/resources/index.html
core/src/commonMain/kotlin/pe/gob/huata/ecolactea/core/application/sync/LocalStorageContracts.kt
docs/DATA_MODEL_V2.md
kotlin-js-store/wasm/yarn.lock
kotlin-js-store/yarn.lock
scripts/run-local-server.ps1
server/src/integrationTest/kotlin/pe/gob/huata/ecolactea/server/MySqlAuthIntegrationTest.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/ExternalConfig.kt
server/src/test/kotlin/pe/gob/huata/ecolactea/server/ExternalConfigTest.kt
server/src/test/kotlin/pe/gob/huata/ecolactea/server/WebRoutingTest.kt
```

Archivos modificados:

```text
README.md
app/shared/build.gradle.kts
build.gradle.kts
core/build.gradle.kts
docs/ARCHITECTURE.md
docs/DEVELOPMENT.md
docs/RF-34.md
docs/adr/ADR-001-estilo-arquitectonico.md
docs/evidencias/RF-34-validacion.md
server/build.gradle.kts
server/src/main/kotlin/pe/gob/huata/ecolactea/server/Routing.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/ServerConfig.kt
server/src/main/kotlin/pe/gob/huata/ecolactea/server/auth/JdbcAuthPersistence.kt
settings.gradle.kts
```

Configuración local ignorada: secrets.properties (no incluida en el commit). Logs, APK, JAR y distribución Web permanecen en directorios build ignorados.
