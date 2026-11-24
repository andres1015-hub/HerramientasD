# Entrega frontend Java — 23/11/2026

## Qué se cambió

El flujo de interfaz se pasó a Spring MVC: Java recibe filtros y formularios, reutiliza los servicios del proyecto y Thymeleaf renderiza las vistas. Se quitaron los scripts JavaScript y la compilación npm. CSS conserva la identidad visual, adapta las tablas a móvil y define los componentes de navegación, tarjetas, formularios y estados.

Se agregaron el panel, inventario, usuarios, paquetes, movimientos, auditoría, cuenta, empaquetador y consulta QR como vistas del módulo /operaciones. Las operaciones de escritura se realizan con formularios POST, validación del lado servidor, protección CSRF y mensajes accesibles.

El lector QR procesa en Java una imagen PNG/JPEG. En un teléfono, el control de carga puede abrir la cámara para tomar una foto; la lectura de video en vivo requiere JavaScript y no forma parte de esta versión.

## Para visualizar

La aplicación de revisión está disponible en http://localhost:18082. Inicia sesión con una cuenta de la base local.

| Pantalla | Dirección local |
| --- | --- |
| Inicio / panel | http://localhost:18082/ |
| Inventario | http://localhost:18082/operaciones/productos |
| Usuarios | http://localhost:18082/operaciones/usuarios |
| Paquetes | http://localhost:18082/operaciones/paquetes |
| Movimientos | http://localhost:18082/operaciones/movimientos |
| Auditoría | http://localhost:18082/operaciones/auditoria |
| Mi cuenta | http://localhost:18082/operaciones/cuenta |
| Empaquetador | http://localhost:18082/operaciones/empaquetador |
| Consulta QR | http://localhost:18082/operaciones/qr |

Las vistas protegidas redirigen al inicio de sesión si no hay una sesión activa. Las pantallas de administración requieren el rol ADMINISTRADOR; el empaquetador y paquetes requieren ADMINISTRADOR o USER.

## Arranque local

El proyecto declara Java 21. En esta máquina se dispone de JDK 22, que puede compilar y ejecutar el proyecto. También se requiere PostgreSQL y Maven Wrapper; no se requiere Node ni npm.

Con la base local de revisión ya iniciada en el puerto 55433:

    Set-Location 'C:\Users\2305498\Downloads\Codex\HerramientasDesarrollo\HerramientasD-frontend-diseno'
    $env:JAVA_HOME = 'C:\Program Files\Java\jdk-22'
    $env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://127.0.0.1:55433/frontend_review'
    $env:SPRING_DATASOURCE_USERNAME = 'postgres'
    $env:SPRING_DATASOURCE_PASSWORD = ''
    .\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=18082 --spring.thymeleaf.cache=false'

Para usar otra base, configura las variables SPRING_DATASOURCE con los datos de tu PostgreSQL. No uses la base temporal para producción.

## Verificaciones y límites

- La aplicación Java se inició con una base PostgreSQL temporal aislada.
- Nueve vistas renderizadas con JavaScript desactivado respondieron correctamente; se recorrieron operaciones principales de productos, usuarios, paquetes y cuenta.
- La última suite Maven ejecutada en la revisión terminó con 1 prueba aprobada y sin fallos.
- La cámara de video en vivo, impresora física, lector de pantalla y revisión completa de teclado quedan pendientes.
- No se ha fusionado el PR a main. La revisión visual del usuario está pendiente.

## Archivos

- Nuevo controlador MVC OperacionesWebController y plantillas Thymeleaf bajo templates/operaciones y templates/fragments/operaciones.
- Navegación y alias de rutas adaptados a las vistas nuevas.
- CSS refinado en static/css/workspace.css; JavaScript y npm retirados del flujo.
- Matriz de módulos en FRONTEND_MATRIZ.md y evidencia en FRONTEND_VERIFICACION.md.
