# Frontend listo para revisión — 23/11/2026

Rama frontend-diseno-operaciones, HEAD 8e3e294 desde develop-2. Cambios locales sin commit, push, PR ni merge durante la implementación. La rama remota se creó en el análisis anterior.
Worktree: C:/Users/2305498/Downloads/Codex/HerramientasDesarrollo/HerramientasD-frontend-diseno.
La carpeta original HerramientasD conserva su cambio pendiente en app.css.

## Archivos y módulos

| Archivos | Resultado | Verificación |
| --- | --- | --- |
| templates/inventario.html, static/js/operaciones.js | Catálogo paginado, filtros, tipos, alertas, edición, activación confirmada, historial individual y selección temporal. Alta y movimientos existentes conservados. | Alta y edición en navegador; movimientos, activación e historial por API de prueba. |
| templates/usuarios.html, operaciones.js | Alta, edición, contraseña con repetición, estados, filtros y páginas. | Alta en navegador; edición, reset 204 y estados por API. |
| templates/historial.html, operaciones.js | Pedidos filtrados, detalle y QR, recepción y cancelación con motivo. | Detalle y transiciones desde navegador. |
| templates/movimientos.html, operaciones.js | Historial por ID/tipo/fechas, páginas y CSV. | Consulta general e individual. |
| templates/cuenta.html, operaciones.js | Edición de nombre/apellido; DNI/rol de lectura; contraseña previa conservada. | GET/PUT propio, USER y PROVEEDOR. |
| templates/administrador.html, operaciones.js | Campos reales de ResumenGeneralResponse, destacados y CSV productos. | API y descarga CSV desde navegador. |
| templates/auditoria.html, operaciones.js | Consulta de eventos filtrada y paginada, sin edición. | Eventos reales en base temporal. |
| templates/index.html, fragments/navegacion.html | Vistas integradas y navegación según permisos. | HTTP 200 y roles revisados. |
| static/css/workspace.css, app.css | Componentes comunes, tablas móviles, diálogos nativos, misma paleta y CSS compilado. | 390/768/1024/1440 px. |
| vista-previa/demo-*.html, demo-datos.js, README.md | Siete demos sin solicitudes al backend ni escrituras. | 28 combinaciones de vista/ancho; paginación y vacío. |
| FRONTEND_MATRIZ.md, FRONTEND_VERIFICACION.md, este informe | Contratos, roles, pruebas y límites actualizados. | Sin cambios Java. |

## Comprobaciones

- npm ci sin vulnerabilidades reportadas; npm run build:css correcto.
- node --check de todos los scripts del proyecto y demo: correcto.
- git diff --check: correcto.
- Maven con JDK 22 y PostgreSQL temporal: 1 prueba, 0 fallos, 0 errores. El primer intento predeterminado falló por conexión a PostgreSQL; se repitió con base aislada y pasó.
- Chromium mediante Playwright: siete pantallas reales y siete demos a cuatro anchos; sin desbordamiento horizontal ni errores JavaScript en las revisiones.
- Formularios de producto/usuario, detalle y transiciones de paquetes comprobados en navegador. Otras escrituras comprobadas mediante fetch autenticado contra la API temporal.
- Paginación, vacío, Escape, menús móviles y recuperación de HTTP 500 comprobados.
- CSRF en escrituras, credentials same-origin, textContent y scripts externos. Sin nuevas dependencias frontend.
- CSV mediante fetch y Blob para respetar el filtro de acceso directo a /api. productos.csv descargado correctamente.
- PROVEEDOR conserva catálogo Thymeleaf: Spring excluye ese rol de /api/**. Su perfil usa /miperfil/datos.

## Requisitos y arranque

El pom declara Java 21. El Java predeterminado del equipo es 17: se usó JDK 22 instalado. Se requieren PostgreSQL y Maven Wrapper; Node/npm para regenerar CSS.
La revisión usa una base temporal frontend_review en 127.0.0.1:55433, aplicación en 18080 y demo en 18081. No se cambió application.properties ni la base habitual.

Con la base temporal activa, para reiniciar la aplicación:

```powershell
Set-Location 'C:\Users\2305498\Downloads\Codex\HerramientasDesarrollo\HerramientasD-frontend-diseno'
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-22'
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://127.0.0.1:55433/frontend_review'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$env:SPRING_DATASOURCE_PASSWORD = ''
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=18080 --spring.thymeleaf.cache=false'
```

Si PostgreSQL temporal está detenido:

```powershell
& 'C:\Program Files\PostgreSQL\17\bin\pg_ctl.exe' -D (Join-Path $env:TEMP 'herramientasd-frontend-20261123') -l (Join-Path $env:TEMP 'herramientasd-frontend-20261123/postgres.log') -o '-p 55433 -h 127.0.0.1' start
```

Para una base propia, créala y configura las variables SPRING_DATASOURCE de acuerdo con tu instalación. Sin variables el proyecto apunta a localhost:5432/Proyecto_HDesarrollo.

Vista previa sin Java ni base de datos:

```powershell
Set-Location 'C:\Users\2305498\Downloads\Codex\HerramientasDesarrollo\HerramientasD-frontend-diseno'
python -m http.server 18081 --bind 127.0.0.1
```

## URLs

| Pantalla | Aplicación | Demo |
| --- | --- | --- |
| Panel | http://localhost:18080/ | http://localhost:18081/vista-previa/demo-administrador.html |
| Inventario | http://localhost:18080/?vista=inventario | http://localhost:18081/vista-previa/demo-inventario.html |
| Usuarios | http://localhost:18080/usuarios | http://localhost:18081/vista-previa/demo-usuarios.html |
| Paquetes | http://localhost:18080/?vista=historial | http://localhost:18081/vista-previa/demo-historial.html |
| Movimientos | http://localhost:18080/?vista=movimientos | http://localhost:18081/vista-previa/demo-movimientos.html |
| Auditoría | http://localhost:18080/?vista=auditoria | http://localhost:18081/vista-previa/demo-auditoria.html |
| Cuenta | http://localhost:18080/?vista=cuenta | http://localhost:18081/vista-previa/demo-cuenta.html |
| Empaquetador | http://localhost:18080/paquete | Vista anterior conservada |
| Escáner | http://localhost:18080/escaner | Vista anterior conservada |

## Límites y pendientes

- Esperando aprobación visual antes de commit, push y PR. No se fusionó a main.
- No se expone eliminación de usuarios. Se respetan las restricciones administrativas del servicio.
- Cámara en teléfono físico, impresora/PDF real, lector de pantalla y recorrido exhaustivo de teclado no verificados. Se revisaron etiquetas, foco nativo y Escape.
- productos.csv, paquetes.csv y movimientos.csv descargados desde el navegador. No se comparó cada celda exportada con la base de datos.
- No se modificaron controladores, DTO, servicios, repositorios, modelos, seguridad ni dependencias Java.
- Los datos de pruebas permanecen solo en la base temporal.

## Estado de Git al entregar

`git status --short --branch`: rama frontend-diseno-operaciones siguiendo origin/frontend-diseno-operaciones, sin commits nuevos; 10 archivos modificados y 14 nuevos sin seguimiento.

`git diff --stat`: 10 archivos existentes, 83 inserciones y 82 eliminaciones. Este comando no incluye los 14 archivos nuevos; están descritos en la tabla de módulos. No hay cambios en src/main/java. El CSS pendiente de la carpeta original sigue en su rama original.
