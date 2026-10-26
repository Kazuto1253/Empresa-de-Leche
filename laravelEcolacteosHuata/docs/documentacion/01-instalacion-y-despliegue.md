# 01 · Instalación y despliegue

## Requisitos

| Componente | Versión |
|---|---|
| PHP | 8.3 o superior, con extensiones `pdo_mysql` (o `pdo_sqlite`), `mbstring`, `openssl`, `gd` o `imagick`, `fileinfo`, `curl` |
| Composer | 2.x |
| Node.js | 20 o superior, con npm |
| Base de datos | MySQL 8 (producción y desarrollo) o SQLite (pruebas y arranque rápido) |

## Instalación local

```bash
git clone https://github.com/Kazuto1253/Empresa-de-Leche.git
cd Empresa-de-Leche
git checkout web-laravel-huata
cd laravelEcolacteosHuata
```

El comando `composer setup` instala dependencias, crea `.env`, genera la clave, migra la base y compila los estilos:

```bash
composer setup
```

> Por defecto `.env.example` usa SQLite. Si quieres MySQL, edita `.env` **antes** de migrar (ver más abajo) y ejecuta `php artisan migrate`.

### Configuración (`.env`)

| Variable | Valor típico | Descripción |
|---|---|---|
| `APP_NAME` | `Ecolácteos Huata` | Nombre que ve el usuario |
| `APP_URL` | `http://localhost:8000` | URL base; en producción, el dominio con `https://` |
| `APP_LOCALE` | `es` | Idioma |
| `APP_ENV` / `APP_DEBUG` | `local` / `true` | En producción: `production` / `false` |
| `DB_CONNECTION` | `mysql` o `sqlite` | Motor de base de datos |
| `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD` | según el servidor | Solo para MySQL |
| `SESSION_DRIVER`, `QUEUE_CONNECTION`, `CACHE_STORE` | `database` | Sesiones, colas y caché en la base |
| `MAIL_MAILER` | `log` | Los correos se escriben en el log; configura SMTP para enviarlos |

La zona horaria de negocio es `America/Lima` (clave `app.zona_horaria_local`) y la moneda son soles (`S/`).

### Imágenes de productos

Las fotos de las producciones se guardan en `storage/app/public`. Para que se vean en la tienda:

```bash
php artisan storage:link
```

### Datos de ejemplo

```bash
php artisan db:seed
```

Crea la empresa, el precio de la leche, turnos, rutas y usuarios de ejemplo con contraseña `1234`: `admin`, `planta`, `acopio`, `acopio2`, `proveedor`, `vendedor`, `produccion` y proveedores de muestra. **Solo para desarrollo.** En producción crea los usuarios reales desde el panel del administrador y no ejecutes el seeder (o cambia las contraseñas de inmediato).

### Ejecutar en desarrollo

```bash
composer dev      # servidor, cola y Vite en paralelo
# o por separado:
php artisan serve
npm run dev
```

Abre http://localhost:8000.

## Compilar estilos y scripts

```bash
npm run build
```

Genera los archivos de `public/build`. Hay dos hojas de entrada: `resources/css/app.css` (paneles) y `resources/css/landing.css` (página pública), además de los scripts 3D de la portada (`intro3d.js`, `hero3d.js`, `productos3d.js`, `interacciones.js`).

## Tareas programadas

El sistema registra una tarea cada cinco minutos (`asistencia:registrar-faltas-acopiadores`, marca falta a los acopiadores que no marcaron en un horario ya cerrado) y una diaria que borra invitaciones de equipo vencidas. En el servidor agrega una sola entrada de cron:

```cron
* * * * * cd /ruta/al/proyecto && php artisan schedule:run >> /dev/null 2>&1
```

Si usas colas (`QUEUE_CONNECTION=database`), mantén un trabajador activo (por ejemplo con Supervisor):

```bash
php artisan queue:work --tries=3
```

## Despliegue en un servidor Linux (VPS)

Pasos típicos en Ubuntu con Nginx y PHP-FPM:

1. Instala `nginx`, `php8.3-fpm` y las extensiones, `mysql-server`, `composer`, `nodejs`.
2. Crea la base y el usuario de MySQL.
3. Clona el repositorio y entra a `laravelEcolacteosHuata`.
4. Copia `.env.example` a `.env` y configura `APP_ENV=production`, `APP_DEBUG=false`, `APP_URL` y `DB_*`.
5. Ejecuta:

   ```bash
   composer install --no-dev --optimize-autoloader
   php artisan key:generate
   php artisan migrate --force
   php artisan storage:link
   npm ci && npm run build
   php artisan config:cache && php artisan route:cache && php artisan view:cache
   ```

6. Apunta Nginx a `public/` y envía todo a `index.php`:

   ```nginx
   server {
       server_name tu-dominio.pe;
       root /var/www/laravelEcolacteosHuata/public;
       index index.php;
       location / { try_files $uri $uri/ /index.php?$query_string; }
       location ~ \.php$ {
           include snippets/fastcgi-php.conf;
           fastcgi_pass unix:/run/php/php8.3-fpm.sock;
       }
   }
   ```

7. Activa HTTPS con Let's Encrypt (`certbot --nginx`).
8. Da permisos de escritura a `storage` y `bootstrap/cache` para el usuario del servidor web.

### Actualizar una versión

```bash
git pull
composer install --no-dev --optimize-autoloader
php artisan migrate --force
npm ci && npm run build
php artisan optimize:clear && php artisan config:cache && php artisan route:cache && php artisan view:cache
```

### Copias de seguridad

Respalda diariamente la base (`mysqldump`) y la carpeta `storage/app/public` (fotos y archivos de justificaciones).

## Lista de verificación antes de salir a producción

- [ ] `APP_DEBUG=false` y `APP_ENV=production`.
- [ ] HTTPS activo y `APP_URL` con `https://`.
- [ ] Contraseñas de ejemplo eliminadas o cambiadas.
- [ ] Teléfono de la empresa configurado (se usa para WhatsApp de los pedidos) en el panel del administrador, módulo Empresa.
- [ ] Cron del programador y copias de seguridad configurados.
- [ ] `php artisan test` en verde (ver [08](08-pruebas-y-calidad.md)).
