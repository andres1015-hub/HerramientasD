// Página de un código QR: imprimir y copiar el código.
// Sin JavaScript siguen disponibles la descarga y los enlaces.
(function () {
    'use strict';

    var imprimir = document.getElementById('btn-imprimir-qr');
    var copiar = document.getElementById('btn-copiar-codigo');
    var codigo = document.getElementById('qr-codigo');
    var aviso = document.getElementById('qr-aviso');

    function avisar(texto) {
        if (!aviso) { return; }
        aviso.textContent = texto;
        aviso.hidden = false;
    }

    if (imprimir) {
        imprimir.hidden = false;
        imprimir.addEventListener('click', function () { window.print(); });
    }

    if (copiar && codigo && navigator.clipboard && typeof navigator.clipboard.writeText === 'function') {
        copiar.hidden = false;
        copiar.addEventListener('click', function () {
            navigator.clipboard.writeText(codigo.textContent.trim()).then(function () {
                avisar('Código copiado al portapapeles.');
            }, function () {
                avisar('No se pudo copiar. Selecciona el código y cópialo manualmente.');
            });
        });
    }
})();