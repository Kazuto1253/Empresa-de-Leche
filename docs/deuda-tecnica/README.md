# Registro de deuda técnica

Este directorio registra decisiones temporales que generan trabajo futuro. No se usa para ocultar defectos ni requisitos omitidos.

## Formato

Cada deuda debe indicar:

- ID y título.
- Fecha y responsable.
- Contexto y razón de la postergación.
- Riesgo de mantenerla.
- RF, RNF, módulo y pruebas afectados.
- Condición o fecha para resolverla.
- Estado: abierta, en tratamiento o cerrada.

## Registro inicial

| ID | Deuda o pendiente técnico | Riesgo | Condición de resolución | Estado |
|---|---|---|---|---|
| DT-001 | Persistencia local KMP definitiva no seleccionada | Bloquea la implementación completa de RF-08/RF-09 | Evaluar alternativas al iniciar el bloque offline | Abierta |
| DT-002 | Almacenamiento seguro de Desktop no implementado | Sesión persistente incompleta en Desktop | Elegir adaptadores seguros por sistema operativo | Abierta |
| DT-003 | Validación de iOS pendiente de macOS | No existe evidencia de compilación final iOS | Ejecutar build y pruebas en equipo macOS | Abierta |
| DT-004 | Métricas de rendimiento y disponibilidad sin valores | RNF-07/RNF-08 no pueden aprobarse | Validar tiempos, volumen y horarios con la planta | Abierta |

Las incertidumbres del negocio permanecen en la Matriz Integral V2; no deben trasladarse aquí como si fueran decisiones técnicas.
