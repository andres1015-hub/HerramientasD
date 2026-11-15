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
