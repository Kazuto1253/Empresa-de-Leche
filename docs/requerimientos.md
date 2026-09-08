# Requerimientos de Ecoláctea Digital

## Fuente maestra

La fuente maestra es `project-management/Ecolactea_Digital_Matriz_Integral_V2.xlsx`, fechada el 31 de agosto de 2026. Contiene:

- 36 requerimientos funcionales.
- 12 requerimientos no funcionales.
- 26 reglas de negocio.
- 34 casos de prueba diseñados.
- Matriz de trazabilidad.
- 21 incertidumbres abiertas.

El informe narrativo completo se encuentra en `project-management/Ecolactea_Digital_Informe_Integral_V2.docx`. El Excel de ejecución distribuye los RF entre los cuatro integrantes.

## Estado de aprobación

La versión V2 fue aprobada por el equipo como base de trabajo. Permanece pendiente de validación formal por la planta municipal de Huata y el docente. Por ello, una fila marcada como pendiente no puede convertirse en una regla definitiva por decisión del desarrollador.

## Requerimientos funcionales

| Grupo | Requerimientos |
|---|---|
| Proveedores y rutas | RF-01 Registrar proveedor; RF-02 actualizar ubicación; RF-03 consultar historial; RF-04 administrar zonas y rutas; RF-05 programar jornada |
| Acopio y sincronización | RF-06 registrar acopio en campo; RF-07 entrega directa; RF-08 guardar sin Internet; RF-09 sincronizar pendientes |
| Recepción | RF-10 registrar recepción; RF-11 conciliar campo/planta; RF-12 resolver discrepancia; RF-13 puerto futuro de caudalímetro |
| Calidad | RF-14 registrar análisis; RF-15 entregar resultado; RF-16 clasificar resultado; RF-17 registrar incidencia; RF-18 registrar medida; RF-19 seguimiento técnico |
| Liquidación y pagos | RF-20 cerrar semana; RF-21 administrar precio; RF-22 calcular liquidación; RF-23 generar sobre; RF-24 registrar pago |
| Comunicaciones | RF-25 informar al proveedor; RF-26 comunicados y eventos; RF-27 asistencia |
| Producción y ventas | RF-28 lote de producción; RF-29 rendimiento; RF-30 productos/clientes/precios; RF-31 venta de queso |
| Reportes y gobierno | RF-32 reportes de acopio/calidad; RF-33 reportes financieros/productivos; RF-34 autenticación/autorización; RF-35 auditoría; RF-36 parámetros |

## Requerimientos no funcionales

| ID | Atributo |
|---|---|
| RNF-01 | Operación offline |
| RNF-02 | Integridad e idempotencia de sincronización |
| RNF-03 | Seguridad de acceso |
| RNF-04 | Privacidad de datos |
| RNF-05 | Auditabilidad |
| RNF-06 | Usabilidad en campo |
| RNF-07 | Rendimiento |
| RNF-08 | Disponibilidad central |
| RNF-09 | Mantenibilidad con Clean Architecture |
| RNF-10 | Portabilidad de interfaces |
| RNF-11 | Respaldo y recuperación |
| RNF-12 | Interoperabilidad con equipos |

## Reglas de control

RF-34: primer incremento de autenticación y sesión implementado, **no terminado**. Alcance, pruebas y pendientes: [RF-34](RF-34.md). Los demás RF conservan su estado.

- No usar la matriz anterior de 20 RF cuando contradiga V2.
- No fijar precios, sanciones, rangos, tolerancias o fórmulas pendientes.
- Toda implementación debe enlazar RF, criterio de aceptación y pruebas.
- Los permisos se aplican tanto en la UI como en la API.
- Un estado `Diseñado / No ejecutado` no equivale a una prueba superada.
