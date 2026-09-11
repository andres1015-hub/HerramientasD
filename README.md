# Inventario QR · Spring Boot

Aplicación de inventario con Java 21, Spring Boot MVC, Thymeleaf y PostgreSQL. Las páginas, búsquedas, validaciones, formularios y códigos QR se procesan en el servidor. La aplicación no contiene JavaScript propio ni depende de un frontend JavaScript.

## Funcionalidades

- Dashboard con productos, unidades disponibles y alertas de stock bajo.
- Catálogo con búsqueda, creación, consulta, edición y confirmación de eliminación.
- Persistencia de productos en PostgreSQL mediante Spring Data JPA.
- Inventario y reportes básicos calculados a partir de los productos almacenados.
- Generación y descarga de códigos QR en PNG desde el servidor.
- Consulta de usuarios y paquetes existentes, sin mostrar contraseñas ni DNI.
- Interfaz responsive con Thymeleaf y CSS.

Se mantienen las entidades y tablas aportadas por el equipo. Los pedidos muestran los paquetes guardados en modo consulta; el flujo completo de pedidos, movimientos, lectura de QR, administración de usuarios y autenticación quedan pendientes. Los datos de productos ya no se reinician al apagar la aplicación y no se sobrescriben con datos de ejemplo al arrancar.

El catálogo usa la tabla `producto` y conserva sus columnas, tipos y relaciones con paquetes. Solo se añaden campos opcionales para el código QR, descripción, stock mínimo, estado y fecha de alta. Los registros existentes sin código aparecen como `AUTO-<id_producto>`, sin cambiar sus identificadores ni rellenar sus datos automáticamente.

## PostgreSQL y Docker del equipo

Se conserva la organización de `upstream/main`: servicios `postgres-db`, `backend` y `pgadmin`, base `Proyecto_HDesarrollo`, y volúmenes `postgres_data`/`pgadmin_data`. El Dockerfile usa Java 21, ejecuta las pruebas durante la compilación y corre el JAR con un usuario sin privilegios. Los puertos publicados se restringen a `127.0.0.1` para desarrollo local.

Antes de iniciar sobre una instalación existente, comprueba los volúmenes y su nombre de proyecto Compose:

```powershell
docker volume ls
docker volume inspect NOMBRE_DEL_VOLUMEN_EXISTENTE
```

Consulta la etiqueta `com.docker.compose.project` del volumen PostgreSQL y usa ese valor como `COMPOSE_PROJECT_NAME` en `.env`. El valor predeterminado es `herramientasd`; **si el proyecto anterior utilizó otro nombre, debes conservarlo**. Ejecutar desde otra carpeta con otro nombre de proyecto puede crear volúmenes vacíos y aparentar pérdida de datos, aunque el volumen original siga existiendo. También puedes indicar el proyecto explícitamente con `docker compose -p NOMBRE_ORIGINAL ...` en todos los comandos. No cambies la versión mayor de PostgreSQL de un volumen existente sin una migración planificada.

1. Copia `.env.example` a `.env`.
2. Completa `POSTGRES_PASSWORD` y `PGADMIN_DEFAULT_PASSWORD` localmente. Para una instalación existente, utiliza las credenciales originales; no publiques `.env`.
3. Conserva los nombres del proyecto y de la base que ya usa el equipo.
4. Valida e inicia:

```powershell
docker compose config --quiet
docker compose up --build -d
docker compose ps
docker compose logs backend
```

La aplicación queda en `http://localhost:8080` y pgAdmin en `http://localhost:5050`. En pgAdmin, entra con `PGADMIN_DEFAULT_EMAIL` y su contraseña. Registra un servidor con un nombre descriptivo y estos datos de **Connection**:

| Campo | Valor predeterminado |
| --- | --- |
| Host name/address | `postgres-db` |
| Port | `5432` |
| Maintenance database | `Proyecto_HDesarrollo` |
| Username | `postgres` |
| Password | El valor local de `POSTGRES_PASSWORD` |

El host `postgres-db` funciona desde pgAdmin dentro de Compose. Desde una herramienta instalada en Windows usa `localhost` y el puerto publicado. Si el volumen pgAdmin ya fue inicializado, conserva su cuenta existente: cambiar variables de inicialización no reemplaza sus usuarios.

Las variables `POSTGRES_DB`, `POSTGRES_USER` y `POSTGRES_PASSWORD` inicializan una base nueva; cambiarlas en `.env` **no modifica** una base ni sus contraseñas ya almacenadas. Para detener el proyecto conservando los datos, usa `docker compose stop`. No ejecutes `docker compose down -v`, borres volúmenes ni configures Hibernate con `create` o `create-drop` sobre datos del equipo. Antes de aplicar actualizaciones de esquema sobre datos importantes, realiza una copia de seguridad.

## Ejecución local con Java 21

Requiere PostgreSQL en ejecución y la base `Proyecto_HDesarrollo`. Puedes iniciar solo la infraestructura Docker con `docker compose up -d postgres-db pgadmin`; el backend local se conecta a `localhost`, no a `postgres-db`.

En PowerShell, configura las credenciales mediante variables de entorno. Spring Boot no lee automáticamente el `.env` usado por Docker Compose:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.3.9-hotspot'
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/Proyecto_HDesarrollo'
$env:SPRING_DATASOURCE_USERNAME='postgres'
$env:SPRING_DATASOURCE_PASSWORD=Read-Host 'Contrasena de PostgreSQL' -MaskInput
.\mvnw.cmd clean verify
& "$env:JAVA_HOME\bin\java.exe" -jar target\app.jar
```

Adapta `JAVA_HOME` a tu instalación JDK 21. `-MaskInput` requiere PowerShell 7; en Windows PowerShell 5.1 puedes definir la variable desde la configuración de ejecución del IDE. Si ya está disponible Maven 3.9+, puedes usar `mvn clean verify` en lugar del wrapper.

## Pruebas y límites

Las pruebas automatizadas usan H2 en modo PostgreSQL exclusivamente en el entorno de test y no se conectan a los datos del equipo. El JAR de la aplicación utiliza PostgreSQL. H2 permite probar lógica y persistencia sin infraestructura, pero no sustituye validar el arranque y la conservación de datos con PostgreSQL real.

La ausencia de JavaScript no elimina por sí sola las vulnerabilidades. La aplicación valida los formularios en el servidor, conserva el escape de Thymeleaf e incorpora protección CSRF y una política de contenido que bloquea scripts. Este avance está orientado a desarrollo local; antes de exponerlo se requiere autenticación, autorización, gestión de credenciales y revisión de seguridad del despliegue. pgAdmin es una herramienta externa con su propia interfaz y dependencias.
