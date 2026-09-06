# Desarrollo

## Regla de Base

No crear módulos paralelos como `shared2`, `server2` o `backend2`. Todo desarrollo debe integrarse en los módulos existentes.

## Cliente

El punto de entrada común es `pe.gob.huata.ecolactea.App`. La UI compartida vive bajo `app/shared/src/commonMain/kotlin/pe/gob/huata/ecolactea/shared`.

La URL del backend se configura con `AppEnvironment`; no codificar IPs personales en repositorios ni casos de uso.

## Backend

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

## Material de Entrada

`D:\Ecolactea\_work_input` es referencia externa, no producto. No copiar ZIPs ni carpetas completas dentro del repositorio. El Excel validado sí se conserva en `docs/project-management`.
