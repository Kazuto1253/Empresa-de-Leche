# Plan de pruebas

## Estado y alcance

La Matriz Integral V2 contiene 34 casos de prueba diseñados. En esta línea base se encuentran **diseñados y no ejecutados**, salvo las pruebas técnicas existentes de la base KMP. Una captura o un test verde solo se considerará evidencia del requisito que realmente ejecute.

## Objetivos

- Verificar criterios de aceptación de RF y RNF.
- Aplicar partición de equivalencia y análisis de valores límite.
- Detectar duplicados, pérdidas y conflictos durante operación offline.
- Confirmar permisos, privacidad y auditoría.
- Comprobar que las capas respetan Clean Architecture.
- Mantener regresión sobre requisitos ya integrados.

## Niveles

| Nivel | Propósito | Responsable |
|---|---|---|
| Unitaria | Reglas, validaciones, cálculos y transiciones aisladas | Autor del cambio |
| Integración | Repositorios, API, MySQL, migraciones y sincronización | Autor con revisión del equipo |
| Sistema | Flujos completos por rol y plataforma | Equipo |
| Aceptación | Validación contra necesidades reales y criterios aprobados | Equipo con planta/docente |

## Casos prioritarios de la primera regresión

| Caso | Relación | Técnica | Resultado principal |
|---|---|---|---|
| CP-01 | RF-01 | Equivalencia | Acepta proveedor válido y rechaza DNI repetido/campos ausentes |
| CP-04 | RF-06 | Equivalencia y límites | Acepta cantidad positiva; rechaza cero, negativos y texto |
| CP-05 | RF-06, RNF-02 | Idempotencia | Un reintento con el mismo ID no duplica el acopio |
| CP-07 | RF-08, RNF-01 | Estado y recuperación | Los pendientes sobreviven al reinicio sin red |
| CP-08 | RF-09, RNF-02 | Integración | Sincroniza cada ID una sola vez |
| CP-09 | RF-09, RNF-02 | Tabla de decisión | Una versión incompatible queda como conflicto visible |
| CP-11 | RF-11 | Equivalencia | Distingue conforme, faltante y excedente |
| CP-18 | RF-20 | Límites temporales | Incluye jueves y miércoles, excluye días externos |
| CP-32 | RF-34, RNF-03 | Tabla de decisión | Permite y deniega acciones según rol en UI y API |
| CP-33 | RNF-04 | Seguridad | Un proveedor no consulta datos de otro |
| CP-34 | RNF-09, RNF-12 | Revisión estructural | Dominio no importa infraestructura |

Los 34 procedimientos, datos, resultados y observaciones están en `project-management/Ecolactea_Digital_Matriz_Integral_V2.xlsx`.

## Evidencia mínima por Pull Request

- Comando exacto ejecutado.
- Resultado y fecha.
- Target y entorno utilizados.
- Casos relacionados.
- Captura o reporte cuando corresponda.
- Defectos conocidos y casos no ejecutados.

## Criterio de salida

Un RF solo pasa a terminado cuando su código, persistencia, permisos, errores, pruebas y evidencia cumplen el criterio registrado en el Excel de ejecución. Las pruebas bloqueadas por incertidumbres se mantienen explícitamente pendientes.
