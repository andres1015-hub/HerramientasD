// Muestra los permisos asociados al rol elegido en el formulario de registro.
// Los permisos reales se validan y aplican en el servidor (SecurityConfig + @PreAuthorize).
document.addEventListener('DOMContentLoaded', function () {
    // DNI del registro: solo dígitos numéricos, máximo 8.
    const dni = document.getElementById('registro-dni');
    if (dni) {
        dni.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 8);
        });
    }

    // Muestra los permisos asociados al rol elegido en el formulario de registro.
    // Los permisos reales se validan y aplican en el servidor (SecurityConfig + @PreAuthorize).
    const opciones = document.querySelectorAll('input[name="rol"]');
    const salida = document.getElementById('permisos-rol');
    if (!opciones.length || !salida) {
        return;
    }

    const permisos = {
        USER: 'Como usuario podrás usar el empaquetador, el escáner de QR y consultar el catálogo.',
        PROVEEDOR: 'Como proveedor podrás consultar el catálogo y gestionar tu cuenta; no tendrás acceso al empaquetador ni al escáner.'
    };

    function actualizar() {
        const elegido = document.querySelector('input[name="rol"]:checked');
        salida.textContent = elegido ? permisos[elegido.value] || '' : '';
    }

    opciones.forEach(function (opcion) {
        opcion.addEventListener('change', actualizar);
    });

    actualizar();
});
