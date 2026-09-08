# Ecoláctea Digital

> Plataforma multiplataforma para mejorar la trazabilidad del acopio, control de calidad, liquidaciones, producción y ventas de la planta municipal de leche de Huata.

## Estado del proyecto

Ecoláctea Digital se encuentra en fase de construcción académica. La base técnica Kotlin Multiplatform está preparada y validada; los requerimientos V2 fueron aprobados por el equipo y permanecen pendientes de validación formal con la planta y el docente.

La línea base documental propuesta es `v0.1.0`. Ningún requerimiento funcional se declara terminado en esta etapa.

La rama del Integrante 1 incorpora el [primer incremento de RF-34](docs/RF-34.md): autenticación, sesión y autorización por rol. Conserva pendientes de validación y almacenamiento por plataforma; no se declara terminado.

## Problema que resuelve

El acopio de leche depende parcialmente de planillas y registros distribuidos que dificultan consolidar oportunamente cantidades, calidad, diferencias entre campo y planta, liquidaciones y pagos. La conectividad variable en las rutas también impide depender de una conexión permanente.

La solución busca conservar una trazabilidad verificable desde el registro del proveedor y el acopio hasta la recepción, calidad, pago, producción, venta y reporte, incluyendo operación offline para el trabajo de campo.

## Público objetivo

- Personal administrativo y operativo de la planta municipal de Huata.
- Acopiadores que trabajan en rutas con conectividad inestable.
- Proveedores de leche que consultan exclusivamente su información.
- Responsables de supervisión y toma de decisiones.

## Alcance funcional

La fuente maestra V2 contiene 36 requerimientos funcionales y 12 no funcionales. Sus áreas principales son:

- Proveedores, zonas, rutas y jornadas de acopio.
- Acopio en campo y entregas directas.
- Operación offline, sincronización idempotente y gestión de conflictos.
- Recepción y conciliación entre campo y planta.
- Control de calidad, incidencias y seguimiento.
- Liquidaciones, pagos y comunicaciones al proveedor.
- Eventos, capacitaciones y asistencia.
- Producción, rendimiento, ventas y reportes.
- Autenticación, autorización, auditoría y configuración.

Los valores, fórmulas, rangos y procedimientos que no fueron confirmados se conservan como incertidumbres; no deben convertirse en reglas definitivas hasta su validación.

## Roles del sistema

La aplicación reconoce exactamente cuatro roles:

- `ADMINISTRADOR_GENERAL`
- `PERSONAL_PLANTA`
- `ACOPIADOR`
- `PROVEEDOR`

El rol es determinado por el backend. El usuario no selecciona su rol al iniciar sesión.

## Plataformas y tecnología

- Kotlin Multiplatform y Compose Multiplatform.
- Android como objetivo móvil principal del curso.
- Desktop JVM para Windows, macOS y Linux.
- iOS preparado mediante el proyecto Xcode de `app/iosApp`; requiere macOS para compilarse.
- Backend Kotlin con Ktor y API REST JSON.
- MySQL 8.4 central, HikariCP y migraciones Flyway.
- Web Compose Multiplatform: Wasm con distribución compatible y fallback JavaScript.
- SQLite local previsto para Android/iOS/Desktop offline; contratos preparados, sin RF de acopio implementados.
- Clean Architecture y operación offline-first.

Ktor es el único backend de Android, iOS, Desktop y Web. Todos consumen REST JSON `/api/v1`; ningún cliente accede directamente a MySQL. No se utiliza Laravel, Spring, Angular ni Docker.

## Estructura del repositorio

| Ruta | Responsabilidad |
|---|---|
| `core` | Dominio, contratos de aplicación, modelos compartidos y sincronización |
| `app/shared` | UI Compose, navegación y comunicación compartida |
| `app/androidApp` | Entrada y configuración Android |
| `app/desktopApp` | Entrada Desktop para Windows, macOS y Linux |
| `app/webApp` | Cliente Web Compose que reutiliza core y shared |
| `app/iosApp` | Proyecto Xcode que consume el framework KMP compartido |
| `server` | API Ktor, configuración, migraciones y acceso a MySQL |
| `docs` | Requerimientos, decisiones, pruebas y gestión del proyecto |

## Equipo Nexo Lácteo

| Integrante | Código | Rama de trabajo |
|---|---:|---|
| Jhon Willian Mayta Arotaype | 202413545 | `willian-mayta` |
| Luis Alejandro Chino Leon | 202410802 | `luis-chino` |
| Yenifher Sharai Sanchez Chipa | 202411762 | `yenifher-sanchez` |
| Kevin Jefherson Marca Huaman | 202410821 | `kevin-marca` |

El docente será agregado como colaborador cuando acepte la invitación correspondiente.

## Flujo de trabajo

1. `main` conserva únicamente entregas integradas y estables.
2. Cada integrante trabaja en su rama personal.
3. Antes del push se ejecutan las pruebas relacionadas con el cambio.
4. Cada aporte entra a `main` mediante Pull Request.
5. El Pull Request debe ser revisado por un integrante distinto del autor.
6. Las entregas aprobadas se identifican mediante versionado semántico.

Consulta [CONTRIBUTING.md](CONTRIBUTING.md) y [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md) antes de comenzar.

## Ejecutar el proyecto

### Requisitos

- JDK compatible con la versión de Gradle/Kotlin del proyecto.
- Android Studio o IntelliJ IDEA con soporte KMP.
- Android SDK para Android.
- Xcode en macOS para iOS.
- MySQL cuando se validen funciones que dependan de persistencia central.

### Desktop

```powershell
.\gradlew.bat :app:desktopApp:run
```

### Android

```powershell
.\gradlew.bat :app:androidApp:assembleDebug
```

### Web

```powershell
.\gradlew.bat :app:webApp:composeCompatibilityBrowserDistribution
.\scripts\run-local-server.ps1 -WithWeb
```

El artefacto se genera en `app/webApp/build/dist/composeWebCompatibility/productionExecutable`. Ktor sirve `/` y la API bajo el mismo origen. Configuración y límites de sesión: [Desarrollo](docs/DEVELOPMENT.md).

### Servidor

```powershell
.\gradlew.bat :server:run
```

Comprobación básica:

```powershell
Invoke-RestMethod http://localhost:8080/health
```

### MySQL

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/ecolactea"
$env:DB_USER = "ecolactea_user"
$env:DB_PASSWORD = "definir-solo-en-el-entorno-local"
$env:DB_POOL_SIZE = "10"
```

Nunca se deben versionar contraseñas, tokens ni archivos `.env` reales.

## Pruebas

```powershell
.\gradlew.bat :core:allTests
.\gradlew.bat :app:shared:jvmTest
.\gradlew.bat :server:test
```

Validación amplia en Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat :app:androidApp:assembleDebug
```

## Documentación

El índice y estado de los documentos se encuentra en [docs/README.md](docs/README.md). Las fuentes maestras son la Matriz Integral V2 y el Informe Integral V2 ubicados en `docs/project-management`.

## Nota de validación

Los requerimientos, casos de prueba y decisiones de negocio incluidos representan la propuesta aprobada por el equipo. Las 21 incertidumbres registradas deben resolverse con la planta antes de automatizar precios, sanciones, rangos de calidad, tolerancias o fórmulas definitivas.
