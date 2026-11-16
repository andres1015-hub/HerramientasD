(function () {
    'use strict';

    var formProducto = document.getElementById('form-producto');
    var formMovimiento = document.getElementById('form-movimiento');
    var avisoGeneral = document.getElementById('inventario-msg');
    var CLAVE_AVISO = 'inventario-aviso';

    var metaToken = document.querySelector('meta[name="_csrf"]');
    var metaHeader = document.querySelector('meta[name="_csrf_header"]');
    var csrfToken = metaToken ? metaToken.content : '';
    var csrfHeader = metaHeader ? metaHeader.content : '';

    if (avisoGeneral) {
        try {
            var guardado = sessionStorage.getItem(CLAVE_AVISO);
            if (guardado) {
                sessionStorage.removeItem(CLAVE_AVISO);
                avisoGeneral.textContent = guardado;
                avisoGeneral.hidden = false;
                if (typeof avisoGeneral.scrollIntoView === 'function') {
                    avisoGeneral.scrollIntoView({ block: 'nearest' });
                }
            }
        } catch (e) { /* sin almacenamiento: no se muestra el aviso */ }
    }

    function mostrarError(caja, texto) {
        caja.textContent = texto;
        caja.hidden = false;
    }

    function ocultar(caja) {
        caja.hidden = true;
        caja.textContent = '';
    }

    function textoError(respuesta, datos) {
        if (respuesta.status === 401 || respuesta.status === 403) {
            return 'Tu sesión expiró o no tienes permisos para esta acción.';
        }
        if (datos && datos.erroresCampo && datos.erroresCampo.length) {
            return datos.erroresCampo[0].mensaje || datos.mensaje;
        }
        return (datos && datos.mensaje) || 'No se pudo completar la operación. Inténtalo de nuevo.';
    }

    function enviar(metodo, url, cuerpo, caja, boton, textoBoton, mensajeExito) {
        if (!csrfToken || !csrfHeader) {
            mostrarError(caja, 'No se pudo validar la sesión. Recarga la página.');
            return;
        }
        boton.disabled = true;
        boton.setAttribute('aria-busy', 'true');
        boton.textContent = 'Guardando…';

        var cabeceras = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
        cabeceras[csrfHeader] = csrfToken;

        fetch(url, {
            method: metodo,
            credentials: 'same-origin',
            headers: cabeceras,
            body: JSON.stringify(cuerpo)
        }).then(function (respuesta) {
            return respuesta.json().catch(function () { return null; }).then(function (datos) {
                if (respuesta.ok) {
                    try { sessionStorage.setItem(CLAVE_AVISO, mensajeExito(datos)); } catch (e) { /* opcional */ }
                    window.location.reload();
                    return;
                }
                mostrarError(caja, textoError(respuesta, datos));
            });
        }).catch(function () {
            mostrarError(caja, 'No se pudo conectar con el servidor. Revisa tu conexión.');
        }).finally(function () {
            boton.disabled = false;
            boton.removeAttribute('aria-busy');
            boton.textContent = textoBoton;
        });
    }

    function entero(valor) {
        return /^\d+$/.test(String(valor).trim()) ? parseInt(valor, 10) : NaN;
    }

    if (formProducto) {
        var cajaProducto = document.getElementById('producto-msg');
        var botonProducto = document.getElementById('btn-producto');

        formProducto.addEventListener('submit', function (evento) {
            evento.preventDefault();
            ocultar(cajaProducto);

            var nombre = document.getElementById('producto-nombre').value.trim();
            var tipo = document.getElementById('producto-tipo').value.trim();
            var precio = parseFloat(document.getElementById('producto-precio').value);
            var stock = entero(document.getElementById('producto-stock').value);

            if (!nombre || !tipo) {
                mostrarError(cajaProducto, 'El nombre y el tipo son obligatorios.');
                return;
            }
            if (!Number.isFinite(precio) || precio <= 0) {
                mostrarError(cajaProducto, 'El precio debe ser mayor a cero.');
                return;
            }
            if (!Number.isFinite(stock) || stock < 0) {
                mostrarError(cajaProducto, 'El stock inicial debe ser un número entero desde cero.');
                return;
            }

            enviar('POST', '/api/productos',
                { nombre: nombre, tipo: tipo, precio: precio, stock: stock },
                cajaProducto, botonProducto, 'Guardar producto',
                function (datos) {
                    return 'Producto «' + (datos && datos.nombre ? datos.nombre : nombre) + '» agregado al catálogo.';
                });
        });
    }

    if (formMovimiento) {
        var cajaMovimiento = document.getElementById('movimiento-msg');
        var botonMovimiento = document.getElementById('btn-movimiento');

        formMovimiento.addEventListener('submit', function (evento) {
            evento.preventDefault();
            ocultar(cajaMovimiento);

            var selector = document.getElementById('movimiento-producto');
            var idProducto = entero(selector.value);
            var marcado = formMovimiento.querySelector('input[name="operacion"]:checked');
            var operacion = marcado ? marcado.value : 'entrada';
            var cantidad = entero(document.getElementById('movimiento-cantidad').value);

            if (!Number.isFinite(idProducto)) {
                mostrarError(cajaMovimiento, 'Selecciona un producto.');
                return;
            }
            if (!Number.isFinite(cantidad) || cantidad < 0) {
                mostrarError(cajaMovimiento, 'Ingresa una cantidad entera válida.');
                return;
            }
            if (operacion !== 'ajuste' && cantidad < 1) {
                mostrarError(cajaMovimiento, 'La cantidad de una entrada o salida debe ser mayor a cero.');
                return;
            }

            var url = '/api/productos/' + idProducto + (operacion === 'ajuste' ? '/stock' : '/' + operacion);
            var metodo = operacion === 'ajuste' ? 'PUT' : 'POST';
            var etiqueta = { entrada: 'Entrada', salida: 'Salida', ajuste: 'Ajuste' }[operacion];

            enviar(metodo, url, { cantidad: cantidad }, cajaMovimiento, botonMovimiento,
                'Registrar movimiento', function (datos) {
                    return etiqueta + ' registrada. Stock actual: ' + (datos ? datos.stock : cantidad) + '.';
                });
        });
    }
})();