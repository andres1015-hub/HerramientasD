(function () {
    'use strict';
    var boton = document.getElementById('menu-toggle');
    var menu = document.getElementById('menu-principal');
    if (!boton || !menu) { return; }
    var movil = window.matchMedia('(max-width: 75rem)');
    function ajustar() {
        boton.hidden = !movil.matches;
        menu.hidden = movil.matches;
        boton.setAttribute('aria-expanded', String(!menu.hidden));
    }
    boton.addEventListener('click', function () {
        menu.hidden = !menu.hidden;
        boton.setAttribute('aria-expanded', String(!menu.hidden));
    });
    boton.closest('.workspace-header').addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape' && movil.matches) {
            menu.hidden = true;
            boton.setAttribute('aria-expanded', 'false');
            boton.focus();
        }
    });
    movil.addEventListener('change', ajustar);
    ajustar();
})();
