# Flujo de trabajo Git

## Ramas oficiales

- `main`: línea estable e integrada.
- `willian-mayta`: trabajo de Jhon Willian Mayta Arotaype.
- `luis-chino`: trabajo de Luis Alejandro Chino Leon.
- `yenifher-sanchez`: trabajo de Yenifher Sharai Sanchez Chipa.
- `kevin-marca`: trabajo de Kevin Jefherson Marca Huaman.
- `backup/pre-kmp-reestructuracion`: respaldo histórico; no recibe desarrollo.

No se utilizan `develop`, `master` ni ramas genéricas `integrante-*` en el flujo vigente.

## Procedimiento

1. Actualizar la rama personal desde `main` antes de iniciar una tarea.
2. Implementar únicamente el bloque asignado en la Matriz de Ejecución.
3. Mantener la lógica compartida en los módulos existentes; no crear módulos paralelos.
4. Ejecutar pruebas relacionadas y registrar evidencia real.
5. Usar commits pequeños, comprensibles y atribuibles al autor real.
6. Publicar la rama personal y abrir un Pull Request hacia `main`.
7. Solicitar revisión a un compañero distinto del autor.
8. Corregir observaciones, verificar conflictos y fusionar solo con aprobación.

## Convención de commits

Se recomienda comenzar con la semana o el tipo de cambio:

```text
s4: implementar navegación del módulo de acopio
feat: registrar proveedor con validaciones
fix: evitar duplicados durante sincronización
test: agregar límites de cantidad de leche
docs: actualizar trazabilidad de RF-06
```

Evitar mensajes como `cambios`, `avance`, `final` o `prueba` porque no explican el contenido.

## Reglas de integración

- Nadie trabaja directamente sobre `main`.
- Nadie fusiona su propio Pull Request sin revisión.
- Un Pull Request no mezcla requerimientos sin relación.
- Los contratos compartidos se acuerdan antes de modificar componentes usados por otro integrante.
- Los conflictos no se resuelven eliminando trabajo ajeno.
- Las migraciones aplicadas no se reescriben; se crea la siguiente versión incremental.
- Las evidencias y documentos afectados se actualizan en el mismo Pull Request.

## Versionado

El proyecto usa versionado semántico `MAYOR.MENOR.PARCHE`:

- MAYOR: cambio incompatible.
- MENOR: funcionalidad nueva compatible.
- PARCHE: corrección sin cambio funcional previsto.

La etiqueta `v0.1.0` identifica la primera línea base técnica y documental aprobada por el equipo.
