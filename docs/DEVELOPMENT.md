# Desarrollo

## Regla de Base

No crear módulos paralelos como `shared2`, `server2` o `backend2`. Todo desarrollo debe integrarse en los módulos existentes.

## Cliente

El punto de entrada común es `pe.gob.huata.ecolactea.App`. La UI compartida vive bajo `app/shared/src/commonMain/kotlin/pe/gob/huata/ecolactea/shared`.

La URL del backend se configura con `AppEnvironment`; no codificar IPs personales en repositorios ni casos de uso.

El incremento de autenticación, sus endpoints, bootstrap externo y almacenamiento por plataforma se describen en [RF-34](RF-34.md). Desktop admite `ECOLACTEA_API_URL`; Android usa la propiedad Gradle `ecolactea.apiBaseUrl`. Nunca pasar credenciales MySQL al cliente.

## Backend

Para desarrollo local, copiar `.env.example` a `secrets.properties` (ya ignorado por Git). El archivo usa UTF-8 y líneas literales `KEY=value`, sin comillas, interpolación ni escape de shell; los caracteres posteriores al primer `=` pertenecen al valor. No pegar secretos en comandos compartidos. Restringir los permisos del archivo al usuario local.

Completar DB_URL, DB_USER y DB_PASSWORD con una cuenta autorizada sobre una base MySQL 8.4 existente. No se crean bases ni usuarios MySQL automáticamente. La cuenta de migración necesita permisos DDL y la de ejecución permisos DML; en desarrollo pueden coincidir según la política local.

```powershell
# JAVA_HOME debe apuntar a un JDK compatible; el daemon conserva su configuración JDK 21.
.\scripts\run-local-server.ps1
```

El script pasa la ruta absoluta de configuración mediante `ECOLACTEA_CONFIG_FILE`. También se puede fijar esa variable y ejecutar `:server:run` directamente. El entorno sobrescribe el archivo. No hay carga implícita de archivos de secretos desde directorios arbitrarios.

Para el primer administrador, activar explícitamente AUTH_BOOTSTRAP_ENABLED y completar usuario/contraseña externamente. El bootstrap solo opera si no existe ningún usuario; después debe desactivarse. Nunca restablece contraseñas ni crea administradores adicionales. No hay registro público ni recuperación automática.

### Web y API bajo el mismo origen

```powershell
.\gradlew.bat :app:webApp:composeCompatibilityBrowserDistribution
.\scripts\run-local-server.ps1 -WithWeb
```

Abrir `http://localhost:8080/` con la configuración local indicada. `WEB_ROOT` debe contener exclusivamente el artefacto generado, nunca la raíz del repositorio ni archivos de configuración. La distribución compatible usa Wasm en navegadores compatibles y JS como fallback. Los targets individuales también ofrecen `wasmJsBrowserDistribution` y `jsBrowserDistribution`. No se afirma compatibilidad con todos los navegadores sin probarlos.

La Web comparte casos de uso, DTO, repositorio HTTP, componentes y navegación; opera online y utiliza el origen de la página para la API. No requiere otro backend ni CORS permisivo. La sesión queda en memoria de la pestaña; persistencia mediante cookies HttpOnly/CSRF pendiente. Nunca guardar tokens en localStorage, sessionStorage, IndexedDB o SQLite. El servidor conserva API 404 para rutas desconocidas; las raíces actuales de UI no requieren rutas SPA adicionales.

### Verificación MySQL explícita

```powershell
$env:ECOLACTEA_CONFIG_FILE = (Resolve-Path secrets.properties).Path
.\gradlew.bat :server:mysqlIntegrationTest
```

Usar una base de desarrollo autorizada: la prueba ejecuta Flyway y añade temporalmente usuarios/sesiones de prueba con IDs aleatorios. Elimina únicamente sus fixtures, conserva el historial Flyway y usuarios existentes. Falla si falta configuración o conexión; nunca reporta una simulación como integración real. No ejecutar en producción.

Validaciones focalizadas: `:core:jvmTest`, `:app:shared:jvmTest`, `:server:test`. Plataformas: `:app:androidApp:assembleDebug`, `:app:desktopApp:build`, `:app:webApp:composeCompatibilityBrowserDistribution`. En Windows no se ejecuta Xcode. `core:allTests` incluye ahora targets de navegador y requiere un navegador configurado para sus pruebas.

Las rutas Ktor viven en `server/src/main/kotlin/pe/gob/huata/ecolactea/server/Routing.kt`. Nuevas rutas deben depender de contratos o servicios de aplicación, no de tablas directamente desde el dominio.

Las migraciones Flyway deben agregarse con nombres incrementales:

```text
V2__descripcion.sql
V3__descripcion.sql
```

No modificar migraciones ya aplicadas en entornos compartidos.

## Código Específico por Plataforma

- Android: secure storage, permisos, conectividad, archivos y notificaciones.
- iOS: Keychain, permisos, conectividad, integración Xcode/framework y notificaciones.
- Desktop: secure storage por sistema operativo, archivos y conectividad.

Mantén la lógica de negocio en `core` o `app:shared` siempre que sea razonable.

## Material de entrada

Los archivos de trabajo locales y las descargas temporales no forman parte del producto. No copiar ZIPs ni carpetas completas dentro del repositorio. Las fuentes maestras validadas por el equipo se conservan en `docs/project-management`.
