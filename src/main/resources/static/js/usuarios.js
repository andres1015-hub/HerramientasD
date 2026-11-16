// Mejora progresiva: sin JavaScript se conserva la tabla completa.
(function () {
    'use strict';
    var tabla = document.getElementById('usuarios-tabla');
    if (!tabla) { return; }
    var busqueda = document.getElementById('usuarios-busqueda');
    var rol = document.getElementById('usuarios-rol');
    var estado = document.getElementById('usuarios-estado');
    var conteo = document.getElementById('usuarios-conteo');
    var vacio = document.getElementById('usuarios-sin-resultados');
    var filas = Array.prototype.slice.call(tabla.tBodies[0].rows);
    var aviso = document.getElementById('usuarios-aviso');
    var metaToken = document.querySelector('meta[name="_csrf"]');
    var metaHeader = document.querySelector('meta[name="_csrf_header"]');
    var csrfToken = metaToken ? metaToken.content : '';
    var csrfHeader = metaHeader ? metaHeader.content : '';
    var ETIQUETAS = { ACTIVO: 'Activo', INNACTIVO: 'Inactivo', ELIMINADO: 'Eliminado' };
    var CLASES = { ACTIVO: 'status-success', INNACTIVO: 'status-warning', ELIMINADO: 'status-danger' };

    function mostrarAviso(texto, tipo) {
        aviso.className = 'message ' + (tipo === 'ok' ? 'message-success' : 'message-error');
        aviso.setAttribute('role', tipo === 'ok' ? 'status' : 'alert');
        aviso.textContent = texto;
        aviso.hidden = false;
    }

    // Refleja en la fila el estado confirmado por el servidor.
    function pintarEstado(fila, estado) {
        fila.dataset.estado = estado;
        var badge = fila.querySelector('.status-badge');
        badge.className = 'status-badge ' + CLASES[estado];
        badge.textContent = ETIQUETAS[estado];
        var boton = fila.querySelector('.user-state-btn');
        var persona = fila.cells[0].textContent.trim();
        boton.textContent = estado === 'ACTIVO' ? 'Desactivar' : 'Activar';
        boton.setAttribute('aria-label', boton.textContent + ' a ' + persona);
    }

    function cambiarEstado(fila, boton) {
        var nuevo = fila.dataset.estado === 'ACTIVO' ? 'INNACTIVO' : 'ACTIVO';
        var persona = fila.cells[0].textContent.trim();
        if (!window.confirm((nuevo === 'ACTIVO' ? 'Activar' : 'Desactivar') + ' la cuenta de ' + persona + '?')) {
            return;
        }
        if (!csrfToken || !csrfHeader) {
            mostrarAviso('No se pudo validar la sesión. Recarga la página.', 'error');
            return;
        }
        var cabeceras = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
        cabeceras[csrfHeader] = csrfToken;
        boton.disabled = true;

        fetch('/api/usuarios/' + encodeURIComponent(fila.dataset.id) + '/estado', {
            method: 'PATCH',
            credentials: 'same-origin',
            headers: cabeceras,
            body: JSON.stringify({ estado: nuevo })
        }).then(function (respuesta) {
            return respuesta.json().catch(function () { return null; }).then(function (datos) {
                if (respuesta.ok && datos && datos.estado) {
                    pintarEstado(fila, datos.estado);
                    mostrarAviso('La cuenta de ' + persona + ' ahora está ' + ETIQUETAS[datos.estado].toLowerCase() + '.', 'ok');
                } else if (respuesta.status === 401 || respuesta.status === 403) {
                    mostrarAviso('Tu sesión expiró o no tienes permisos para esta acción.', 'error');
                } else {
                    mostrarAviso((datos && datos.mensaje) || 'No se pudo cambiar el estado. Inténtalo de nuevo.', 'error');
                }
            });
        }).catch(function () {
            mostrarAviso('No se pudo conectar con el servidor. Revisa tu conexión.', 'error');
        }).finally(function () {
            boton.disabled = false;
        });
    }

    filas.forEach(function (fila) {
        var boton = fila.querySelector('.user-state-btn');
        if (boton) {
            boton.addEventListener('click', function () { cambiarEstado(fila, boton); });
        }
    });


    function normalizar(texto) {
        return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('es').trim();
    }
    function filtrar() {
        var texto = normalizar(busqueda.value);
        var visibles = 0;
        filas.forEach(function (fila) {
            var persona = fila.cells[0].textContent + ' ' + fila.cells[1].textContent;
            var coincide = normalizar(persona).indexOf(texto) !== -1
                && (!rol.value || fila.dataset.rol === rol.value)
                && (!estado.value || fila.dataset.estado === estado.value);
            fila.hidden = !coincide;
            if (coincide) { visibles += 1; }
        });
        conteo.textContent = 'Mostrando ' + visibles + ' de ' + filas.length + ' usuarios.';
        vacio.hidden = visibles !== 0;
        tabla.closest('.table-scroll').hidden = visibles === 0;
    }
    busqueda.addEventListener('input', filtrar);
    rol.addEventListener('change', filtrar);
    estado.addEventListener('change', filtrar);
    document.getElementById('usuarios-limpiar').addEventListener('click', function () {
        busqueda.value = ''; rol.value = ''; estado.value = '';
        filtrar(); busqueda.focus();
    });
    document.getElementById('usuarios-filtros').hidden = false;
    conteo.hidden = false;
    filtrar();
})();