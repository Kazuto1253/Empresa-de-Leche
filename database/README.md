# Base de datos Ecoláctea

MySQL 8.4 es la base central. La base física y sus datos reales no viven en Git;
`database/migrations/` define únicamente el esquema aplicado por Flyway.

La conexión usa configuración externa: `DB_URL`, `DB_USER`, `DB_PASSWORD` y
`DB_POOL_SIZE`. El nombre del schema no está fijado por Kotlin. Para desarrollo
puede usarse `ecolactea_dev`; el mismo backend admite test o producción cambiando
solo `DB_URL` y las credenciales. `config/secrets.properties.example` muestra el
formato sin secretos.

Para Workbench, conecta a `127.0.0.1:3306` con el usuario de aplicación y el
schema indicado por `DB_URL`. Ejecuta `database/scripts/verify_schema.sql` para
comprobar tablas y el historial Flyway.

Para agregar cambios, crea `V4__descripcion.sql` en `database/migrations/`.
Nunca edites una migración ya aplicada, insertes filas manualmente en
`flyway_schema_history` ni guardes datos reales, contraseñas o fixtures
productivos en Git. Los roles oficiales de V1 son invariantes estructurales;
proveedores, usuarios, precios y parámetros son datos administrables y no se
siembran automáticamente.
