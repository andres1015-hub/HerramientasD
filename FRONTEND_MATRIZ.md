# Alcance frontend — 23/11/2026

Base: develop-2, 8e3e294. Trabajo en frontend-diseno-operaciones.

| Vista | Funcionalidad | Endpoint | Rol | Archivos |
| --- | --- | --- | --- | --- |
| Inventario | Página, filtros, tipos, alertas | GET /api/productos/paginado, /tipos, /alertas | ADMINISTRADOR, USER | inventario.html, operaciones.js |
| Inventario | Editar y activar | GET/PUT /api/productos/{id}, PATCH /{id}/activo | ADMINISTRADOR | inventario.html, operaciones.js |
| Inventario | Alta y movimientos existentes | POST /api/productos, POST /{id}/entrada, /salida, PUT /{id}/stock | ADMINISTRADOR en UI | inventario.js (conservado) |
| Movimientos | Historial y CSV filtrado | GET /api/movimientos, /api/productos/{id}/movimientos, /api/reportes/movimientos.csv | ADMINISTRADOR | movimientos.html, operaciones.js |
| Usuarios | Página, alta, edición, contraseña, estado | /api/usuarios/pagina, /{id}, POST /, PUT /{id}, POST /{id}/password, PATCH /{id}/estado | ADMINISTRADOR | usuarios.html, operaciones.js |
| Paquetes | Historial, detalle, recepción, cancelación | GET /api/paquetes, /{id}, PATCH /{id}/estado | ADMINISTRADOR, USER | historial.html, operaciones.js |
| Cuenta | Datos propios y edición | GET/PUT /miperfil/datos | Cualquier sesión | cuenta.html, operaciones.js |
| Panel | KPIs reales y CSV | GET /api/reportes/resumen, /productos.csv, /paquetes.csv | ADMINISTRADOR | administrador.html, operaciones.js |
| Auditoría | Bitácora filtrada, sin edición | GET /api/auditoria | ADMINISTRADOR | auditoria.html, operaciones.js |
| Integración | Rutas por vista y navegación | /?vista=… | Según vista | index.html, fragments/navegacion.html |

PROVEEDOR conserva catálogo renderizado por Thymeleaf; /api/** lo excluye en SecurityConfig. Cuenta propia usa /miperfil/datos. No se modifica Java. No se incorpora eliminación de cuentas.
