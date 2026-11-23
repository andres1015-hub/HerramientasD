# Vista previa — 23/11/2026

Ejecuta desde la raíz: `python -m http.server 18081 --bind 127.0.0.1`.
Abre http://localhost:18081/vista-previa/demo-administrador.html.

Las siete páginas demo-*.html usan los estilos y operaciones.js reales; demo-datos.js solo proporciona datos ficticios, sin solicitudes al backend. Los filtros, diálogos y páginas permiten revisar el comportamiento. Guardar y descargar están deshabilitados. Las vistas anteriores se conservan.

Regenera CSS con `npm run build:css`; no edites app.css manualmente.
