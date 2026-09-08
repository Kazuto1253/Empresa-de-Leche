# ADR-001: Arquitectura cliente-servidor con Clean Architecture

- Estado: Aceptado por el equipo
- Fecha: 2026-09-07
- Línea base: v0.1.0

## Contexto

Ecoláctea Digital debe operar en Android, iOS preparado y Desktop; compartir reglas entre plataformas; comunicarse con una base central MySQL; continuar registrando acopios cuando no exista Internet; y proteger información personal, operativa y financiera.

Los RNF arquitectónicamente significativos son RNF-01 operación offline, RNF-02 integridad de sincronización, RNF-03 seguridad, RNF-04 privacidad, RNF-05 auditabilidad, RNF-09 mantenibilidad y RNF-12 interoperabilidad.

## Decisión

Se adopta:

- arquitectura distribuida cliente-servidor;
- cliente Kotlin Multiplatform con Compose Multiplatform;
- backend monolítico modular en Ktor;
- Clean Architecture con dependencias dirigidas hacia dominio y aplicación;
- API REST JSON;
- MySQL como persistencia central y Flyway para migraciones;
- estrategia offline-first mediante almacenamiento local, outbox, identificadores idempotentes y conflictos explícitos;
- puertos y adaptadores para base de datos, red, almacenamiento seguro, conectividad, impresión y dispositivos futuros.

## Alternativas consideradas

### Aplicaciones independientes por plataforma

Se descarta porque duplicaría reglas, pruebas y mantenimiento entre Android, Desktop e iOS.

### Microservicios

Se descartan para esta etapa porque el tamaño del equipo y el alcance académico no justifican su complejidad operativa.

### Dependencia permanente del servidor

Se descarta porque contradice la conectividad inestable del trabajo de campo.

### Base local como fuente definitiva

Se descarta porque la planta necesita consolidación, permisos, auditoría y reportes centrales.

## Consecuencias

### Positivas

- Las reglas se prueban sin depender de UI, red o MySQL.
- La lógica puede compartirse entre plataformas.
- El backend conserva control de permisos e integridad central.
- Los dispositivos se reemplazan por simuladores durante pruebas.
- La operación offline puede reintentarse sin duplicar registros.

### Costos y riesgos

- La sincronización y resolución de conflictos requieren diseño explícito.
- El almacenamiento seguro necesita adaptadores por plataforma.
- iOS solo puede verificarse completamente en macOS.
- La disciplina de límites entre capas debe revisarse en cada Pull Request.

## Criterios de cumplimiento

- El dominio no importa Ktor, Compose, SQL, MySQL ni APIs de plataforma.
- Los casos de uso dependen de contratos.
- Los adaptadores implementan esos contratos.
- UI y API no duplican reglas de negocio.
- Las pruebas pueden usar repositorios y dispositivos falsos.

## Ampliación de plataforma V2

El ciclo posterior al Commit 01 incorpora Web Compose (wasmJs con fallback JS) como cliente de la misma API Ktor y establece SQLite local para los clientes offline. Ktor sigue siendo el único backend y MySQL 8.4 la base central. No se agrega un backend Web alternativo. La decisión original de Clean Architecture y composición manual se conserva; detalle vigente en ARCHITECTURE.md y DATA_MODEL_V2.md.
