# Modelo lógico global V2

Estado: contrato de arquitectura y propuesta lógica para las ramas. **No es un esquema implementado ni autoriza desarrollar nuevos RF.** La persistencia actual se limita a V1/V2 de Flyway. No se crean migraciones funcionales en este ciclo.

## Fuentes y límites

- `project-management/Ecolactea_Digital_Matriz_Integral_V2.xlsx`: hojas Reglas de Negocio (filas 5–30), Incertidumbres (5–25), Matriz Requerimientos y Trazabilidad.
- `project-management/Ecolactea_Digital_Plan_Ejecucion_4_Integrantes_VALIDADO.xlsx`: Requerimientos, filas 5–40. Define el ownership indicado abajo.
- Instrucción arquitectónica V2 del ciclo: Android, iOS, Windows, macOS, Linux y Web; Kotlin/Compose Multiplatform; Ktor REST JSON `/api/v1`; MySQL 8.4 central; SQLite local para clientes offline. Sin Laravel, Spring, Angular ni Docker.
- Se mantienen 36 RF, 12 RNF y cuatro roles. FC-01–FC-04 son complementarias del coordinador, fuera del reparto. RF-13 continúa diferido, sin dispositivo real.

La matriz registra incertidumbres sobre permisos. Los cuatro roles y la creación exclusiva de usuarios por administrador quedan fijados por la instrucción actual; permisos empresariales detallados aún requieren validación. Toda regla, catálogo, estado empresarial, precisión, límite o dato adicional no confirmado se marca **PENDIENTE DE VALIDACIÓN CON EL CLIENTE**. Las cardinalidades siguientes son propuestas técnicas que preservan lo confirmado; no convierten supuestos en reglas de negocio.

## Convenciones

- PK `id`: UUID estable generado antes de sincronizar cuando la operación nace offline. Catálogos técnicos pueden usar `code` como PK.
- FK: no duplicar identidad de proveedor, ruta o usuario dentro de cada módulo. Mantener referencias, no copias centrales de maestros.
- Relación `A 1:N B`: cada B referencia un A; `0..1` indica FK opcional. Una tabla de enlace resuelve N:M.
- `version`: contador del servidor para concurrencia optimista. `created_at`, `updated_at`, `created_by`, `updated_by` en agregados modificables. Tiempos de recepción del servidor prevalecen para auditoría; fecha observada en campo se conserva separada.
- Cantidades/importes: DECIMAL en MySQL, representación decimal exacta en Kotlin/JSON. Escala, unidad, redondeo y moneda: **PENDIENTE DE VALIDACIÓN CON EL CLIENTE**. No usar Float/Double para dinero.
- Retención, borrado lógico, anonimización, índices de negocio y unicidades adicionales se deciden con los responsables antes de cada migración. No aplicar CASCADE que elimine historia financiera o trazabilidad.
- Los nombres siguientes son lógicos. Las migraciones futuras deben mantener la convención física real, sin renombrar V1/V2 ni crear tablas equivalentes paralelas.

## Identidad y seguridad — central

| Entidad lógica | PK y FK | Cardinalidad / responsabilidad | Estado y ownership |
|---|---|---|---|
| users | PK id; FK role_code→roles.code; futura FK provider_id→providers.id | roles 1:N users; provider 1:N users como propuesta, política de cuentas por proveedor pendiente | Física `app_user`, RF-34, I1. La FK provider_id aún no existe porque providers no está implementada |
| roles | PK code | Exactamente ADMINISTRADOR_GENERAL, PERSONAL_PLANTA, ACOPIADOR, PROVEEDOR | Física `app_role`, RF-34, I1 |
| permissions | PK code | Catálogo técnico de capacidades; sin privilegios implícitos universales | Actualmente enum `Permission`, RF-34, I1; tabla solo si se aprueba persistir la matriz |
| role_permissions | PK (role_code, permission_code); FK a roles y permissions | roles N:M permissions | Actualmente `Authorization.permissions`, RF-34, I1; no duplicar autoridad en SQL y Kotlin |
| auth_sessions | PK id; FK user_id→users.id; family_id; hashes únicos de acceso/refresh | users 1:N sesiones; familia 1:N rotaciones | Física `auth_session`, RF-34, I1; expiración y revocación existentes |
| audit_events | PK id; FK actor_user_id→users.id; tipo/id de agregado, acción, motivo, cambios, fecha | users 1:N eventos; referencia a agregado heterogéneo exige validación de aplicación | Propuesta RF-35, I1. Solo append, sin secretos ni payloads indiscriminados |
| system_parameters | PK id; código, versión, vigencia, valor tipado; FK approved_by→users.id | código lógico 1:N versiones | Propuesta RF-36, I1. Valores y aprobación empresarial pendientes |

La contraseña nunca sale del servidor. Se conserva PBKDF2 existente. Tokens únicamente en SessionStore seguro o cookies HttpOnly cuando se implementen para navegador; **nunca en SQLite, outbox, auditoría, localStorage ni IndexedDB**. El servidor obtiene rol, permisos y ownership; no acepta el rol declarado por cliente.

Bootstrap externo solo cuando no hay usuarios. Después, únicamente ADMINISTRADOR_GENERAL podrá crear cuentas mediante una futura operación autorizada. No hay registro público ni recuperación automática por correo/SMS. Restablecimiento administrativo pendiente como complemento.

## Proveedores y rutas — central, I1

| Entidad | PK y FK propuestas | Cardinalidad / datos confirmados | RF |
|---|---|---|---|
| providers | PK id; DNI único según criterio V2 | nombre, DNI y celular confirmados; otros datos/estado/consentimiento pendientes | RF-01, consulta RF-03 |
| zones | PK id | Catálogo configurable; nombres y límites pendientes | RF-04 |
| routes | PK id; FK zone_id→zones.id | zones 1:N routes; vigencia/actividad, sin fijar nombres o capacidad | RF-04 |
| route_provider_assignments | PK id; FK route_id→routes.id, provider_id→providers.id | providers N:M routes a través del tiempo; inicio/fin e historial; sin solapamientos incompatibles, política exacta pendiente | RF-02 |
| workdays | PK id; FK route_id→routes.id, unit_id→collection_units.id | routes 1:N workdays; fecha y salida prevista; horario no fijo | RF-05 |
| collection_units | PK id | unidad/vehículo como referencia de jornada; catálogo/capacidad pendiente | RF-04/05 |
| workday_staff | PK (workday_id,user_id); FK a workdays y users | workdays N:M users; funciones y compatibilidad horaria pendientes | RF-05 |

Las asignaciones y jornadas son auditables. No fijar anticipación de dos/tres días como validación: RN-04 e INC-06 pendientes.

## Acopio, entrega y conciliación — central, I2

| Entidad | PK y FK propuestas | Cardinalidad / origen | RF |
|---|---|---|---|
| milk_collections | PK id; FK provider_id→providers.id, workday_id→workdays.id, collected_by→users.id; client_operation_id | provider 1:N acopios; jornada 1:N acopios; cantidad positiva, fecha/hora real | RF-06 |
| plant_deliveries | PK id; FK workday_id→workdays.id opcional; provider_id→providers.id opcional; received_by→users.id | recepción de jornada o entrega directa identificada; directa no exige ruta. Restricción de origen a precisar con I2 | RF-07/10 |
| delivery_collection_links | PK (delivery_id,collection_id); FK a plant_deliveries y milk_collections | enlace técnico N:M para trazar campo→planta. Si no existen entregas parciales, I2 debe restringir collection_id a una sola entrega | RF-10/11; parcialidad PENDIENTE DE VALIDACIÓN CON EL CLIENTE |
| reconciliations | PK id; FK delivery_id→plant_deliveries.id, resolved_by→users.id opcional | entrega 1:N revisiones propuestas; conserva volumen campo, volumen planta, diferencia, decisión y motivo | RF-11/12 |

No sumar entregas de ruta y sus acopios dos veces en liquidación. Entrega directa es un origen distinto. Tolerancias, aprobadores y estados de resolución: **PENDIENTE DE VALIDACIÓN CON EL CLIENTE**. No hay tablas de caudalímetro en este ciclo (RF-13 diferido).

## Calidad — central, I3

| Entidad | PK y FK propuestas | Cardinalidad / contenido | RF |
|---|---|---|---|
| quality_tests | PK id; FK provider_id→providers.id, collection_id→milk_collections.id o delivery_id→plant_deliveries.id, analyst_id→users.id | origen 1:N muestras/análisis; origen exacto validado, equipo y fecha | RF-14/15/16 |
| quality_parameters | PK id; código y versión/vigencia; posible FK system_parameter_id | catálogo 1:N resultados; no duplicar parámetros generales de RF-36 | RF-14/16 + RF-36 I1 |
| quality_test_results | PK (test_id,parameter_id); FK a quality_tests y quality_parameters | análisis N:M parámetros; valor, unidad y versión utilizada | RF-14/16 |
| quality_incidents | PK id; FK test_id→quality_tests.id, provider_id→providers.id | análisis 1:N incidencias; adulteración y acidificación distintas | RF-17 |
| quality_measures | PK id; FK incident_id→quality_incidents.id, decided_by→users.id | incidencia 1:N decisiones con motivo, sin sanción automática inventada | RF-18 |
| quality_followups | PK id; FK incident_id→quality_incidents.id, responsible_id→users.id | incidencia 1:N seguimientos | RF-19 |

RN-09 menciona grasa, proteína, lactosa, densidad, temperatura, agua añadida y pH. Unidades, obligatoriedad, rangos, precisión, fórmulas, umbral inclusivo y consecuencias: **PENDIENTE DE VALIDACIÓN CON EL CLIENTE**. No insertar catálogos ni automatizar el 5 %. Todas las correcciones/decisiones son auditables.

## Comunicaciones y eventos — central, I3

| Entidad | PK y FK propuestas | Cardinalidad / contenido | RF |
|---|---|---|---|
| communications | PK id; FK author_id→users.id; provider_id opcional o audiencia referenciada | autor 1:N comunicaciones; definir destinatarios mediante enlaces si son múltiples | RF-25/26 |
| events | PK id; FK organized_by→users.id | responsable 1:N eventos; fecha y contenido | RF-26 |
| event_participants | PK (event_id,provider_id); FK a events y providers | eventos N:M proveedores; convocatoria/asistencia según estados por validar | RF-27 |

Canales, notificaciones, confirmación de lectura, asistencia y destinatarios no proveedores: **PENDIENTE DE VALIDACIÓN CON EL CLIENTE**. No se implementan mensajería, correo ni SMS aquí.

## Liquidaciones y pagos — central, I4

| Entidad | PK y FK propuestas | Cardinalidad / contenido | RF |
|---|---|---|---|
| settlement_periods | PK id; FK closed_by→users.id | periodo 1:N liquidaciones; jueves-miércoles confirmado | RF-20 |
| milk_prices | PK id; vigencia; FK authorized_by→users.id | versión de precio 1:N ítems; alcance por proveedor/calidad pendiente | RF-21 |
| settlements | PK id; FK provider_id→providers.id, period_id→settlement_periods.id | proveedor 1:N liquidaciones; revisiones/cierre sin sobrescribir historia | RF-22/23 |
| settlement_items | PK id; FK settlement_id→settlements.id, price_id→milk_prices.id; collection_id o direct_delivery_id como origen excluyente | liquidación 1:N ítems; trazabilidad al origen y precio aplicado | RF-22 |
| payments | PK id; FK settlement_id→settlements.id, recorded_by→users.id | liquidación 1:N pagos propuestos; pagos parciales requieren confirmación | RF-24 |

Conservar valor aplicado como snapshot junto a FK de versión. No recalcular historia con precios actuales. Feriados del viernes, redondeo, anticipos, ajustes, descuentos, anulaciones y revisiones: **PENDIENTE DE VALIDACIÓN CON EL CLIENTE**. Los sobres son salidas de consulta/documentos, no duplicación de ítems financieros.

## Producción y ventas — central

| Entidad | PK y FK propuestas | Cardinalidad / contenido | Ownership |
|---|---|---|---|
| products | PK id | catálogo de producto, unidades por validar | RF-30 I1 |
| customers | PK id; FK provider_id opcional→providers.id | cliente puede ser proveedor, sin copiar padrón | RF-30 I1 |
| sale_prices | PK id; FK product_id→products.id; vigencia y segmento | producto 1:N precios históricos; segmentación pendiente | RF-30 I1 |
| production_batches | PK id; FK responsible_id→users.id | lote y leche procesada; origen de insumos detallado por confirmar | RF-28 I4 |
| production_outputs | PK id; FK batch_id→production_batches.id, product_id→products.id | lote 1:N salidas; producto 1:N salidas | RF-28/29 I4 |
| inventory_movements | PK id; FK product_id→products.id; FK output_id→production_outputs.id o sale_item_id→sale_items.id opcionales | producto 1:N movimientos; trazabilidad de origen sin duplicar saldo como autoridad | RF-28/31 I4, contrato catálogo I1 |
| sales | PK id; FK customer_id→customers.id opcional, seller_id→users.id | cliente 0..1 por venta (público general por validar); venta 1:N ítems | RF-31 I4 |
| sale_items | PK id; FK sale_id→sales.id, product_id→products.id, price_id→sale_prices.id opcional | cantidades/precio aplicado como snapshot | RF-31 I4 |

Catálogo, pesos, unidades, fórmula de rendimiento, metas, precios, descuentos, devoluciones y stock negativo: **PENDIENTE DE VALIDACIÓN CON EL CLIENTE**. No fijar metas 11/12 ni precios transcritos contradictorios. Lotes, ventas, anulaciones e inventario son auditables.

## Sincronización y almacenamiento local — contratos, ejecución futura I2

| Local SQLite (Android/iOS/Desktop) | PK / referencias locales | Fuente / responsabilidad |
|---|---|---|
| local_providers | (owner_user_id, provider_id); versión central | proyección autorizada de providers, RF-08; contrato I1/I2 |
| local_routes | (owner_user_id, route_id); versión central | rutas y asignaciones autorizadas |
| local_workdays | (owner_user_id, workday_id); route_id local | jornadas precargadas |
| local_collections | (owner_user_id, id); provider_id/workday_id; versión base | datos capturados RF-06/08, I2 |
| outbox_operations | (owner_user_id, operation_id); device_id, agregado, payload, versión base, intentos, estado | `PendingOperationStore`, RF-08/09, I2 |
| sync_state | (owner_user_id, stream); cursor opaco de servidor | descarga incremental y recuperación |

`LocalDataStore.transaction(OfflineScope(userId, deviceId))` comparte conexión/transacción entre registros, outbox y cursor. El adaptador SQLite debe implementar `LocalTransaction`, que reutiliza `PendingOperationStore`; no se construye un segundo contrato de sincronización. SQLDelight con drivers por plataforma es la opción prevista en el plan validado RF-08. No se añade un motor ni esquema funcional hasta que I2 implemente el RF. Web permanece online y puede tener otro adaptador futuro.

1. Generar `client_operation_id` estable y `aggregate_id` antes de guardar. Escritura local y outbox se confirman atómicamente. Cancelación/fallo revierte ambas.
2. Enviar después del commit local, con sesión vigente. `device_id` identifica instalación, nunca sustituye autenticación. El servidor deriva actor/ownership de la sesión y valida cada operación.
3. Central existente: `processed_client_operation(operation_id PK, actor_user_id FK, aggregate_type, aggregate_id, server_version, processed_at)`. Futuro refuerzo transversal: digest canónico del payload y device_id, mediante migración incremental cuando RF-09 lo necesite.
4. Misma ID y mismo contenido/actor: devolver la confirmación anterior; misma ID y contenido/actor diferente: rechazar, sin filtrar información de otro usuario. Escritura de agregado y confirmación idempotente en una transacción central.
5. Comparar `baseVersion` contra versión central. Diferencia genera `SyncConflict`; no aplicar último escritor silenciosamente. Responsable/política de resolución: **PENDIENTE DE VALIDACIÓN CON EL CLIENTE**, INC-17.
6. Marcar sincronizado solo con confirmación del servidor. Reintentos mantienen la ID. Recuperar operaciones en estado SYNCING tras caída mediante política técnica de arrendamiento/reintento que definirá I2.
7. Avanzar cursor junto con páginas de datos aplicadas. Tombstones/versiones preservan eliminaciones y cambios autorizados. Retención y días offline pendientes.
8. Separar datos por usuario/dispositivo; no presentar outbox de una cuenta a otra. Logout revoca sesión; política de retención/cifrado del padrón offline pendiente, sin borrar silenciosamente capturas no sincronizadas.

## Auditoría, permisos y reportes

Maestros, asignaciones, jornadas, acopios, recepciones, conciliaciones, análisis, decisiones, liquidaciones, pagos, lotes, ventas y parámetros son auditables en RF-35. Actor, fecha, motivo y antes/después deben referirse a IDs/versiones. Nunca registrar password, hashes de password, tokens o secretos en auditoría.

RF-03/RF-32 (I1) y RF-33 (I4) usan queries, vistas o proyecciones derivadas; **no tablas duplicadas como fuentes de verdad**. Cada total conserva filtros y trazabilidad a registros. El aislamiento de proveedor se aplica en API y consulta central, no solo en UI.

Antes de una migración funcional, el dueño del RF acuerda FK, unicidades, permisos, estados, precisión y compatibilidad de DTO con los consumidores. I1 coordina el contrato global; este documento no transfiere la implementación de RF de otros integrantes.
