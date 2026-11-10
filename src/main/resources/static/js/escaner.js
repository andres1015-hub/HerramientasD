// Escáner QR móvil: cámara trasera, lectura con jsQR y consulta segura a la API.
// Toda la información se pinta con textContent para no interpretar HTML ajeno.
(function () {
    'use strict';

    var video = document.getElementById('video');
    var canvas = document.getElementById('canvas');
    var stage = document.getElementById('scanner-stage');
    var mensaje = document.getElementById('scanner-msg');
    var botonIniciar = document.getElementById('btn-iniciar');
    var botonDetener = document.getElementById('btn-detener');
    var botonCambiar = document.getElementById('btn-cambiar');
    var botonOtro = document.getElementById('btn-otro');
    var resultado = document.getElementById('resultado');
    var formularioManual = document.getElementById('form-manual');
    var codigoManual = document.getElementById('codigo-manual');
    var archivoQr = document.getElementById('archivo-qr');

    if (!video || !canvas || !stage || !mensaje) {
        return;
    }

    var contexto = canvas.getContext('2d', { willReadFrequently: true });
    var metaToken = document.querySelector('meta[name="_csrf"]');
    var metaHeader = document.querySelector('meta[name="_csrf_header"]');
    var csrfToken = metaToken ? metaToken.content : '';
    var csrfHeader = metaHeader ? metaHeader.content : '';

    var stream = null;
    var escaneando = false;
    var procesando = false;
    var frameId = 0;
    var ultimoEscaneo = 0;
    var orientacion = 'environment';

    var formatoMoneda = new Intl.NumberFormat('es-PE', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    function esLocalhost() {
        return location.hostname === 'localhost' || location.hostname === '127.0.0.1' || location.hostname === '[::1]';
    }

    function setEstado(texto, estado) {
        mensaje.textContent = texto;
        mensaje.dataset.state = estado || 'idle';
    }

    function detenerCamara(mensajeFinal) {
        escaneando = false;
        if (frameId) {
            cancelAnimationFrame(frameId);
            frameId = 0;
        }
        if (stream) {
            stream.getTracks().forEach(function (pista) {
                pista.stop();
            });
            stream = null;
        }
        video.srcObject = null;
        stage.classList.remove('is-active');
        if (botonIniciar) {
            botonIniciar.hidden = false;
            botonIniciar.disabled = false;
        }
        if (botonDetener) {
            botonDetener.hidden = true;
        }
        if (botonCambiar) {
            botonCambiar.hidden = true;
        }
        if (mensajeFinal) {
            setEstado(mensajeFinal, 'idle');
        }
    }

    function describirError(error) {
        switch (error && error.name) {
            case 'NotAllowedError':
            case 'SecurityError':
                return 'Permiso de cámara denegado. Habilítalo desde la configuración del navegador y vuelve a intentarlo.';
            case 'NotFoundError':
            case 'DevicesNotFoundError':
                return 'No se encontró una cámara disponible en este dispositivo.';
            case 'NotReadableError':
            case 'TrackStartError':
                return 'La cámara está siendo usada por otra aplicación. Ciérrala e inténtalo de nuevo.';
            case 'OverconstrainedError':
                return 'No se pudo usar la cámara trasera. Prueba con «Cambiar cámara» o con otro navegador.';
            case 'AbortError':
                return 'La cámara se interrumpió. Vuelve a activarla.';
            default:
                return 'No se pudo abrir la cámara. Revisa los permisos del navegador.';
        }
    }

    function pedirCamara(constraints) {
        return navigator.mediaDevices.getUserMedia(constraints);
    }

    async function iniciarCamara() {
        if (typeof window.jsQR !== 'function') {
            setEstado('No se pudo cargar el lector de QR. Recarga la página.', 'error');
            return;
        }
        if (!window.isSecureContext && !esLocalhost()) {
            setEstado('La cámara requiere HTTPS o localhost. Abre la aplicación con una dirección segura.', 'error');
            return;
        }
        if (!navigator.mediaDevices || typeof navigator.mediaDevices.getUserMedia !== 'function') {
            setEstado('Este navegador no permite usar la cámara. Ingresa el código manualmente.', 'error');
            return;
        }

        detenerCamara('');
        resultado.hidden = true;
        botonIniciar.disabled = true;
        setEstado('Solicitando permiso para usar la cámara…', 'idle');

        var constraints = {
            audio: false,
            video: {
                facingMode: { ideal: orientacion },
                width: { ideal: 1280 },
                height: { ideal: 720 }
            }
        };

        try {
            try {
                stream = await pedirCamara(constraints);
            } catch (errorPrincipal) {
                if (errorPrincipal && errorPrincipal.name === 'OverconstrainedError') {
                    stream = await pedirCamara({ audio: false, video: true });
                } else {
                    throw errorPrincipal;
                }
            }
        } catch (error) {
            botonIniciar.disabled = false;
            setEstado(describirError(error), 'error');
            return;
        }

        video.srcObject = stream;
        try {
            await video.play();
        } catch (error) {
            detenerCamara('No se pudo iniciar la vista de la cámara.');
            return;
        }

        stage.classList.add('is-active');
        botonIniciar.hidden = true;
        botonIniciar.disabled = false;
        botonDetener.hidden = false;
        botonCambiar.hidden = false;
        escaneando = true;
        procesando = false;
        ultimoEscaneo = 0;
        setEstado('Apunta el código QR dentro del marco.', 'ok');
        frameId = requestAnimationFrame(leerFotograma);
    }

    function leerFotograma(tiempo) {
        if (!escaneando) {
            return;
        }
        if (tiempo - ultimoEscaneo >= 140 && video.readyState >= 2 && !procesando) {
            ultimoEscaneo = tiempo;
            var anchoVideo = video.videoWidth;
            var altoVideo = video.videoHeight;
            if (anchoVideo && altoVideo) {
                var escala = Math.min(1, 720 / Math.max(anchoVideo, altoVideo));
                canvas.width = Math.round(anchoVideo * escala);
                canvas.height = Math.round(altoVideo * escala);
                contexto.drawImage(video, 0, 0, canvas.width, canvas.height);
                var imagen = contexto.getImageData(0, 0, canvas.width, canvas.height);
                var qr = window.jsQR(imagen.data, imagen.width, imagen.height, { inversionAttempts: 'attemptBoth' });

                if (qr && qr.data) {
                    procesando = true;
                    escaneando = false;
                    if (navigator.vibrate) {
                        navigator.vibrate(90);
                    }
                    detenerCamara('');
                    consultar(qr.data);
                    return;
                }
            }
        }
        frameId = requestAnimationFrame(leerFotograma);
    }

    function extraerCodigo(texto) {
        var limpio = String(texto || '').trim();
        if (!limpio) {
            return '';
        }
        try {
            var url = new URL(limpio);
            var ruta = url.pathname.match(/\/qr\/ver\/([^/]+)\/?$/);
            if (ruta) {
                return decodeURIComponent(ruta[1]);
            }
            var parametro = url.searchParams.get('codigo') || url.searchParams.get('codigoQR');
            if (parametro) {
                return parametro.trim();
            }
        } catch (error) {
            // No era una URL: se usa el texto tal cual.
        }
        var coincidencia = limpio.match(/\/qr\/ver\/([^/?#\s]+)/);
        if (coincidencia) {
            return decodeURIComponent(coincidencia[1]);
        }
        return limpio;
    }

    function leerJson(respuesta) {
        return respuesta.json().catch(function () {
            return null;
        });
    }

    async function consultar(texto) {
        var codigo = extraerCodigo(texto);
        if (!codigo) {
            setEstado('El código está vacío. Inténtalo de nuevo.', 'error');
            procesando = false;
            return;
        }
        if (codigo.length > 100) {
            setEstado('El código no es válido: supera el largo permitido.', 'error');
            procesando = false;
            return;
        }

        setEstado('Consultando el paquete…', 'idle');

        try {
            var respuesta = await fetch('/api/detalle/qr', {
                method: 'POST',
                credentials: 'same-origin',
                headers: (function () {
                    var headers = {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    };
                    if (csrfToken && csrfHeader) {
                        headers[csrfHeader] = csrfToken;
                    }
                    return headers;
                })(),
                body: JSON.stringify({ codigoQR: codigo })
            });

            if (respuesta.ok) {
                var datos = await leerJson(respuesta);
                if (datos) {
                    mostrarResultado(datos);
                    setEstado('Código válido. Revisa el detalle del paquete.', 'ok');
                } else {
                    setEstado('El servidor devolvió una respuesta vacía. Inténtalo de nuevo.', 'error');
                }
                return;
            }

            if (respuesta.status === 404) {
                setEstado('QR no válido: no existe ningún paquete con ese código.', 'error');
                return;
            }
            if (respuesta.status === 401 || respuesta.status === 403) {
                setEstado('Tu sesión expiró o no tienes permisos. Vuelve a iniciar sesión.', 'error');
                return;
            }
            if (respuesta.status === 400) {
                var error = await leerJson(respuesta);
                setEstado((error && error.mensaje) || 'El código enviado no es válido.', 'error');
                return;
            }

            setEstado('No se pudo consultar el paquete (error ' + respuesta.status + ').', 'error');
        } catch (error) {
            setEstado('No se pudo conectar con el servidor. Revisa tu conexión.', 'error');
        } finally {
            procesando = false;
        }
    }

    function formatearFecha(valor) {
        if (!valor) {
            return '—';
        }
        var fecha = new Date(valor);
        if (Number.isNaN(fecha.getTime())) {
            return String(valor).replace('T', ' ').slice(0, 16);
        }
        try {
            return new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium', timeStyle: 'short' }).format(fecha);
        } catch (error) {
            return String(valor).replace('T', ' ').slice(0, 16);
        }
    }

    function setCampo(nombre, valor) {
        var elemento = resultado.querySelector('[data-campo="' + nombre + '"]');
        if (elemento) {
            elemento.textContent = valor === null || valor === undefined || valor === '' ? '—' : String(valor);
        }
    }

    function mostrarResultado(datos) {
        var responsable = ((datos.nombreUsuario || '') + ' ' + (datos.apellidoUsuario || '')).trim();
        setCampo('producto', datos.nombreProducto);
        setCampo('tipo', datos.tipoProducto);
        setCampo('pedido', datos.nPedido);
        setCampo('estado', datos.estadoPaquete);
        setCampo('cantidad', datos.cantidad !== undefined && datos.cantidad !== null
            ? datos.cantidad + (datos.cantidad === 1 ? ' unidad' : ' unidades')
            : null);
        setCampo('precio', datos.precioUnitario !== undefined && datos.precioUnitario !== null
            ? 'S/ ' + formatoMoneda.format(Number(datos.precioUnitario))
            : null);
        setCampo('responsable', responsable);
        setCampo('fecha', formatearFecha(datos.fechaEmision));
        setCampo('codigo', datos.codigoQR);

        resultado.hidden = false;
        resultado.setAttribute('tabindex', '-1');
        resultado.focus({ preventScroll: true });
    }

    function consultarManual(evento) {
        evento.preventDefault();
        var valor = codigoManual.value.trim();
        if (!valor) {
            codigoManual.focus();
            setEstado('Ingresa el código del QR para consultarlo.', 'error');
            return;
        }
        detenerCamara('');
        consultar(valor);
    }

    function leerImagen(archivo) {
        return new Promise(function (resolve, reject) {
            var lector = new FileReader();
            lector.onerror = function () {
                reject(new Error('No se pudo leer la imagen.'));
            };
            lector.onload = function () {
                var imagen = new Image();
                imagen.onload = function () {
                    resolve(imagen);
                };
                imagen.onerror = function () {
                    reject(new Error('El archivo no es una imagen válida.'));
                };
                imagen.src = lector.result;
            };
            lector.readAsDataURL(archivo);
        });
    }

    async function procesarArchivo() {
        var archivo = archivoQr.files && archivoQr.files[0];
        if (!archivo) {
            return;
        }

        detenerCamara('');
        resultado.hidden = true;

        if (!archivo.type || archivo.type.indexOf('image/') !== 0) {
            setEstado('Selecciona un archivo de imagen para analizarlo.', 'error');
            archivoQr.value = '';
            return;
        }
        if (typeof window.jsQR !== 'function') {
            setEstado('No se pudo cargar el lector de QR. Recarga la página.', 'error');
            archivoQr.value = '';
            return;
        }

        setEstado('Analizando la imagen del QR…', 'idle');

        try {
            var imagen = await leerImagen(archivo);
            var escala = Math.min(1, 1280 / Math.max(imagen.width, imagen.height));
            canvas.width = Math.max(1, Math.round(imagen.width * escala));
            canvas.height = Math.max(1, Math.round(imagen.height * escala));
            contexto.drawImage(imagen, 0, 0, canvas.width, canvas.height);

            var pixeles = contexto.getImageData(0, 0, canvas.width, canvas.height);
            var qr = window.jsQR(pixeles.data, pixeles.width, pixeles.height, { inversionAttempts: 'attemptBoth' });
            if (qr && qr.data) {
                consultar(qr.data);
            } else {
                setEstado('No se encontró un QR legible en la foto. Acércate más o ingresa el código.', 'error');
            }
        } catch (error) {
            setEstado(error.message || 'No se pudo analizar la imagen.', 'error');
        } finally {
            archivoQr.value = '';
        }
    }

    if (botonIniciar) {
        botonIniciar.addEventListener('click', iniciarCamara);
    }
    if (botonDetener) {
        botonDetener.addEventListener('click', function () {
            detenerCamara('Cámara detenida. Actívala de nuevo cuando quieras escanear.');
        });
    }
    if (botonCambiar) {
        botonCambiar.addEventListener('click', function () {
            orientacion = orientacion === 'environment' ? 'user' : 'environment';
            iniciarCamara();
        });
    }
    if (botonOtro) {
        botonOtro.addEventListener('click', function () {
            resultado.hidden = true;
            iniciarCamara();
        });
    }
    if (formularioManual) {
        formularioManual.addEventListener('submit', consultarManual);
    }
    if (archivoQr) {
        archivoQr.addEventListener('change', procesarArchivo);
    }
    window.addEventListener('pagehide', function () {
        detenerCamara('');
    });
    document.addEventListener('visibilitychange', function () {
        if (document.hidden) {
            detenerCamara('');
        }
    });
})();
