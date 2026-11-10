// Empaquetador: arma el pedido, valida en cliente y envía el paquete a la API.
// El servidor vuelve a validar stock, roles y reglas de negocio.
(function () {
    'use strict';

    var form = document.getElementById('form-paquete');
    if (!form) {
        return;
    }

    var pedido = document.getElementById('numero-pedido');
    var boton = document.getElementById('btn-crear');
    var mensaje = document.getElementById('empaquetador-msg');
    var resumenProductos = document.getElementById('resumen-productos');
    var resumenUnidades = document.getElementById('resumen-unidades');
    var resumenTotal = document.getElementById('resumen-total');
    var filas = Array.prototype.slice.call(form.querySelectorAll('.package-product'));
    var hayProductos = filas.length > 0;

    var metaToken = document.querySelector('meta[name="_csrf"]');
    var metaHeader = document.querySelector('meta[name="_csrf_header"]');
    var csrfToken = metaToken ? metaToken.content : '';
    var csrfHeader = metaHeader ? metaHeader.content : '';

    var formatoMoneda = new Intl.NumberFormat('es-PE', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    function ocultarMensaje() {
        mensaje.hidden = true;
        mensaje.textContent = '';
    }

    function mostrarMensaje(titulo, detalle) {
        mensaje.hidden = false;
        mensaje.className = 'message message-error';
        mensaje.textContent = '';

        var strong = document.createElement('strong');
        strong.textContent = titulo;
        mensaje.appendChild(strong);

        if (detalle) {
            var p = document.createElement('p');
            p.textContent = detalle;
            mensaje.appendChild(p);
        }

        if (typeof mensaje.scrollIntoView === 'function') {
            mensaje.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    }

    function datosFila(fila) {
        var check = fila.querySelector('.package-product-check');
        var cantidad = fila.querySelector('.package-qty input');
        return { check: check, cantidad: cantidad };
    }

    function ajustarCantidad(fila) {
        var datos = datosFila(fila);
        if (!datos.check || !datos.cantidad) {
            return;
        }

        datos.cantidad.disabled = !datos.check.checked;
        if (!datos.check.checked) {
            return;
        }

        var max = parseInt(datos.check.getAttribute('data-stock'), 10);
        var valor = parseInt(datos.cantidad.value, 10);
        if (!Number.isFinite(valor) || valor < 1) {
            datos.cantidad.value = '1';
        } else if (Number.isFinite(max) && valor > max) {
            datos.cantidad.value = String(max);
        }
    }

    function actualizarResumen() {
        var productos = 0;
        var unidades = 0;
        var total = 0;

        filas.forEach(function (fila) {
            var datos = datosFila(fila);
            if (!datos.check || !datos.check.checked || !datos.cantidad) {
                return;
            }
            var cantidad = parseInt(datos.cantidad.value, 10);
            if (!Number.isFinite(cantidad) || cantidad < 1) {
                return;
            }
            var precio = parseFloat(datos.check.getAttribute('data-precio'));
            productos += 1;
            unidades += cantidad;
            total += (Number.isFinite(precio) ? precio : 0) * cantidad;
        });

        resumenProductos.textContent = productos + (productos === 1 ? ' producto' : ' productos');
        resumenUnidades.textContent = String(unidades);
        resumenTotal.textContent = 'S/ ' + formatoMoneda.format(total);
    }

    filas.forEach(function (fila) {
        var datos = datosFila(fila);
        if (!datos.check) {
            return;
        }
        datos.check.addEventListener('change', function () {
            ajustarCantidad(fila);
            actualizarResumen();
        });
        if (datos.cantidad) {
            datos.cantidad.addEventListener('input', function () {
                actualizarResumen();
            });
            datos.cantidad.addEventListener('change', function () {
                ajustarCantidad(fila);
                actualizarResumen();
            });
        }
    });

    function recolectarItems() {
        var items = [];
        for (var i = 0; i < filas.length; i += 1) {
            var datos = datosFila(filas[i]);
            if (!datos.check || !datos.check.checked || !datos.cantidad) {
                continue;
            }

            var cantidad = parseInt(datos.cantidad.value, 10);
            var stock = parseInt(datos.check.getAttribute('data-stock'), 10);
            if (!Number.isFinite(cantidad) || cantidad < 1) {
                mostrarMensaje('Cantidad inválida', 'Cada producto seleccionado debe tener una cantidad mayor a cero.');
                datos.cantidad.focus();
                return null;
            }
            if (Number.isFinite(stock) && cantidad > stock) {
                mostrarMensaje('Stock insuficiente', 'La cantidad solicitada supera las unidades disponibles.');
                datos.cantidad.focus();
                return null;
            }

            items.push({
                idProducto: parseInt(datos.check.value, 10),
                cantidad: cantidad
            });
        }

        if (!items.length) {
            mostrarMensaje('Selecciona al menos un producto', 'Marca los productos que formarán parte del paquete.');
            return null;
        }
        return items;
    }

    function leerJson(respuesta) {
        return respuesta.json().catch(function () {
            return null;
        });
    }

    form.addEventListener('submit', function (evento) {
        evento.preventDefault();
        ocultarMensaje();

        if (!form.reportValidity()) {
            return;
        }
        if (!csrfToken || !csrfHeader) {
            mostrarMensaje('No se pudo validar la sesión', 'Recarga la página e inténtalo de nuevo.');
            return;
        }

        var items = recolectarItems();
        if (!items) {
            return;
        }

        var numeroPedido = pedido.value.trim();
        boton.disabled = true;
        boton.setAttribute('aria-busy', 'true');
        boton.textContent = 'Generando paquete…';

        fetch(form.getAttribute('action'), {
            method: 'POST',
            credentials: 'same-origin',
            headers: (function () {
                var headers = {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                };
                headers[csrfHeader] = csrfToken;
                return headers;
            })(),
            body: JSON.stringify({ numeroPedido: numeroPedido, items: items })
        }).then(function (respuesta) {
            if (respuesta.status === 201) {
                return leerJson(respuesta).then(function (datos) {
                    if (datos && datos.idPaquete) {
                        window.location.assign('/paquete?creado=' + encodeURIComponent(datos.idPaquete));
                    } else {
                        mostrarMensaje('Paquete generado', 'El paquete se creó, pero no se pudo abrir el resumen. Revisa el historial.');
                    }
                });
            }

            if (respuesta.status === 401 || respuesta.status === 403) {
                mostrarMensaje('Sesión expirada', 'Vuelve a iniciar sesión para continuar empaquetando.');
                return null;
            }

            return leerJson(respuesta).then(function (datos) {
                var detalle = datos && datos.mensaje ? datos.mensaje : 'Revisa los datos e inténtalo de nuevo.';
                var titulo = respuesta.status === 409
                    ? 'No se pudo generar el paquete'
                    : (respuesta.status === 400 ? 'Revisa los datos del paquete' : 'No se pudo generar el paquete');
                mostrarMensaje(titulo, detalle);
            });
        }).catch(function () {
            mostrarMensaje('No se pudo conectar con el servidor', 'Revisa tu conexión e inténtalo de nuevo.');
        }).finally(function () {
            boton.disabled = !hayProductos;
            boton.removeAttribute('aria-busy');
            boton.textContent = 'Generar paquete y QR →';
        });
    });

    actualizarResumen();
})();
