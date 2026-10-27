// Restringe el campo DNI a exactamente 8 dígitos numéricos:
// no permite letras, símbolos ni espacios mientras se escribe o se pega texto.
document.addEventListener('DOMContentLoaded', function () {
    const dni = document.getElementById('dni');
    if (!dni) {
        return;
    }

    dni.addEventListener('input', function () {
        this.value = this.value.replace(/\D/g, '').slice(0, 8);
    });
});
