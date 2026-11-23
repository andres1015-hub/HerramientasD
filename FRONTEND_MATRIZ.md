# Matriz del frontend

## Arquitectura

Las interacciones se procesan en Java con Spring MVC y los servicios existentes. Thymeleaf genera las páginas HTML en el servidor; CSS define su presentación. No se ejecuta JavaScript en el navegador y no se requiere Node ni npm.

| Vista | Funciones | Lógica Java y acceso | Plantillas |
| --- | --- | --- | --- |
| Panel | Indicadores de inventario, usuarios y paquetes; exportación CSV | ReporteService; solo ADMINISTRADOR | operaciones.html, fragmentos/operaciones/panel.html |
| Inventario | Filtros, páginas, alta, edición, activación y movimientos de stock | InventarioService; lectura para sesión autenticada, cambios para ADMINISTRADOR | fragmentos/operaciones/productos.html |
| Usuarios | Búsqueda, páginas, alta, edición, restablecimiento y estado | UsuarioService; solo ADMINISTRADOR | fragmentos/operaciones/usuarios.html |
| Paquetes | Filtros, páginas, detalle, recepción y cancelación con motivo | PaqueteService; ADMINISTRADOR y USER | fragmentos/operaciones/paquetes.html |
| Empaquetador | Selección de productos y creación de pedidos | PaqueteService e InventarioService; ADMINISTRADOR y USER | fragmentos/operaciones/empaquetador.html |
| Movimientos | Historial filtrable y exportación CSV | MovimientoStockService y ReporteService; solo ADMINISTRADOR | fragmentos/operaciones/movimientos.html |
| Auditoría | Consulta filtrable, paginada y de solo lectura | AuditoriaService; solo ADMINISTRADOR | fragmentos/operaciones/auditoria.html |
| Cuenta | Edición de nombre y apellido; contraseña existente | UsuarioService; sesión autenticada | fragmentos/operaciones/cuenta.html |
| Consulta QR | Consulta manual y lectura de imagen PNG/JPEG mediante ZXing en Java | DetallePackageService; ADMINISTRADOR y USER | fragmentos/operaciones/qr.html |

Las escrituras son formularios POST con protección CSRF de Spring Security. Los mensajes de resultado se renderizan en el servidor. Las reglas de negocio, permisos y cálculos continúan en los servicios existentes.

La lectura QR desde una imagen funciona en Java. En móviles, el selector puede abrir la cámara para tomar una foto; no hay lector de video en vivo porque eso requeriría código en el navegador.
