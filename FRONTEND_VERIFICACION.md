# Entrega frontend — 15/11/2026

Rama: `frontend-vistas-nuevas`, creada desde `origin/main` (`7fbfaab`). Commits locales en español. Sin push ni merge.

## Cambios entregados

| Tarea | Resultado |
| --- | --- |
| Cuenta e inventario | Las rutas directas abren la página completa mediante redirección. El cambio de contraseña conserva los mensajes flash. |
| Inicio administrador | Accesos a empaquetador y escáner; últimos cuatro paquetes con pedido, estado, unidades y total. |
| Inicio por rol | Tarjetas `service-card` con iconos. USER mantiene tres herramientas; PROVEEDOR ve Catálogo y Mi cuenta. |
| Estados | Badges propios para paquetes y usuarios, con texto legible y colores semánticos. El escáner limpia la clase anterior al consultar otro QR. |
| Usuarios | Resumen, búsqueda por nombre/apellido/DNI, filtros combinados, limpieza y estado sin resultados. Búsqueda independiente de tildes. Tabla completa sin JavaScript y tarjetas por debajo de 35rem. |
| Impresión | Botón externo que llama a `window.print()`. CSS A4 con pedido, grilla QR, códigos de 40 mm y tarjetas sin cortes internos. |
| Error | Código destacado, icono del sprite, título, mensaje y acciones. Conserva todas las variables originales y contempla 401, 403, 404 y 500. |
| Login | Avisos por presencia de `error`, `logout`, `registrado` y `expirada`, incluidos parámetros sin valor. Conserva `exitoRegistro`. |
| Navegación | Menú móvil con `aria-expanded`, `aria-controls`, cierre con Escape y adaptación al cambiar de ancho. Sin JavaScript los enlaces permanecen accesibles. |
| Vista previa | Cinco páginas estáticas con datos de ejemplo, navegación relativa, QR local y acciones de escritura deshabilitadas. |

## Únicos cambios Java

- `UsuarioController.cuenta()` (`GET /miperfil`) devuelve `VISTA_CUENTA`, cuyo valor existente es `redirect:/?vista=cuenta`.
- `UsuarioController.cambiarPassword()` (`POST /miperfil/password`) devuelve esa misma constante después de establecer `exitoCuenta` o `errorCuenta`.
- `InventarioController.inventory()` (`GET /inventario`) devuelve `redirect:/?vista=inventario`.

No se modificaron servicios, repositorios, modelos, DTOs, seguridad, inicializadores ni dependencias. Se mantuvieron `apelllido`, `INNACTIVO`, los contratos API y los identificadores utilizados por los scripts.

## Verificación realizada

- `npm install`: sin vulnerabilidades reportadas.
- `npm run build:css`: correcto; `app.css` compilado incluido en los commits con cambios CSS.
- `mvnw.cmd -q compile`: correcto con JDK 21.
- `mvnw.cmd -q test`: 1 test, 0 fallos, 0 errores.
- 45 comprobaciones HTTP correctas sobre una base PostgreSQL temporal en `127.0.0.1:55432`, con la aplicación en el puerto 18080. No se usó la base habitual del proyecto.
- Roles ADMINISTRADOR, USER y PROVEEDOR: inicio, catálogo y cuenta; rutas operativas para USER/administrador y 403 de usuarios para USER/PROVEEDOR. Se comprobaron también las redirecciones directas de administrador y la página 404.
- Contraseña: éxito y error con mensajes flash visibles después de la redirección, usando exclusivamente una cuenta de prueba. Registro: aviso `exitoRegistro` correcto.
- Creación de paquete con JSON y CSRF; resumen de QR y descarga PNG correctos.
- Escáner en navegador: consultas manuales reales para Enviado, Recibido y Cancelado; clase de estado correcta después de cada consulta.
- Navegador a 390 y 1440 px: las cinco pantallas nuevas, sin desbordamientos horizontales ni imágenes rotas. Filtros combinados, búsqueda sin tildes, vacío y limpieza verificados en usuarios.
- Sin errores de CSP observados en consola durante la revisión. También se inspeccionaron todas las plantillas para descartar estilos/scripts inline y atributos de eventos.
- Enlaces y recursos relativos de las cinco vistas previas: correctos. Se revisaron mediante un servidor HTTP estático; el navegador integrado bloquea el protocolo `file:`.
- `node --check` para los cuatro scripts nuevos o modificados y `git diff --check`: correctos.

### Límites de la revisión

La cámara no se probó en un teléfono físico. El navegador integrado agotó el tiempo al invocar el diálogo nativo de impresión; no se verificaron una impresora real ni un PDF impreso. Los estilos de impresión y el handler están implementados. Se verificaron errores 403 y 404 reales; no se provocaron fallos 500 ni respuestas HTML 401 en la aplicación.

Docker estaba apagado. Se usó PostgreSQL instalado en el equipo con un clúster temporal, sin cambiar la configuración del proyecto. Los procesos auxiliares de la revisión se detuvieron al terminar.

## Archivos cambiados

Rutas relativas a la raíz del repositorio:

- Java: `src/main/java/com/example/ProyectoDesarrollo/Controllers/UsuarioController.java`, `InventarioController.java`.
- Plantillas en `src/main/resources/templates/`: `administrador.html`, `index.html`, `usuarios.html`, `empaquetador.html`, `error.html`, `Login.html` y `fragments/navegacion.html`.
- CSS en `src/main/resources/static/css/`: `workspace.css` y `app.css`.
- JavaScript en `src/main/resources/static/js/`: `empaquetador.js`, `escaner.js`, `usuarios.js` y `navegacion.js`.
- Vistas previas en `vista-previa/`: `administrador.html`, `empaquetador.html`, `escaner.html`, `usuarios.html`, `error.html`, `preview.css`, `assets/qr-demo.png` y `README.md`.
- Este informe: `FRONTEND_VERIFICACION.md`.

## Pendientes de backend

- Endpoint para cambiar el estado de un usuario. Se dejó `<!-- TODO backend: endpoint cambiar estado -->` y no se agregaron botones de activación.
- Endpoint para crear productos. El formulario de inventario continúa deshabilitado.
- Endpoints para movimientos de stock. Sus controles continúan deshabilitados.
- Endpoint para cambiar el estado de un paquete. Los nuevos badges solo presentan el estado actual.
