export const icon = name => `<svg aria-hidden="true"><use href="#icon-${name}"></use></svg>`;

export const escapeHtml = value => String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');

const statusTone = status => ({
    ACTIVO: 'success', INACTIVO: 'neutral',
    PENDIENTE: 'warning', PROCESANDO: 'info', COMPLETADO: 'success', CANCELADO: 'danger',
    ENTRADA: 'success', SALIDA: 'danger'
}[status] || 'neutral');

export const badge = status => `<span class="badge ${statusTone(status)}">${escapeHtml(String(status || '').replace('_', ' ').toLowerCase())}</span>`;

export const pageHeading = ({ eyebrow, title, subtitle, actions = '' }) => `
    <header class="page-heading">
        <div>
            <p class="eyebrow">${escapeHtml(eyebrow)}</p>
            <h1>${escapeHtml(title)}</h1>
            <p class="subtitle">${escapeHtml(subtitle)}</p>
        </div>
        ${actions ? `<div class="heading-actions">${actions}</div>` : ''}
    </header>`;

export const emptyState = (title, copy, action = '') => `
    <div class="empty-state">
        <span class="empty-icon">${icon('box')}</span>
        <h3>${escapeHtml(title)}</h3>
        <p>${escapeHtml(copy)}</p>
        ${action}
    </div>`;

export const toast = (title, message = '', type = 'success') => {
    const region = document.querySelector('#toast-region');
    const element = document.createElement('div');
    element.className = `toast ${type}`;
    element.innerHTML = `${icon(type === 'error' ? 'alert' : 'check')}<div><strong>${escapeHtml(title)}</strong>${message ? `<p>${escapeHtml(message)}</p>` : ''}</div>`;
    region.append(element);
    window.setTimeout(() => element.remove(), 4200);
};

const layer = () => document.querySelector('#modal-layer');

export const closeModal = () => {
    const target = layer();
    target.classList.remove('open');
    target.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
    window.setTimeout(() => { if (!target.classList.contains('open')) target.innerHTML = ''; }, 190);
};

export const openModal = ({ title, subtitle = '', body, footer = '', className = '', onOpen }) => {
    const target = layer();
    target.innerHTML = `
        <section class="modal ${className}" role="dialog" aria-modal="true" aria-labelledby="modal-title">
            <header class="modal-header">
                <div><h2 id="modal-title">${escapeHtml(title)}</h2>${subtitle ? `<p>${escapeHtml(subtitle)}</p>` : ''}</div>
                <button class="modal-close" type="button" data-close-modal aria-label="Cerrar">${icon('x')}</button>
            </header>
            <div class="modal-body">${body}</div>
            ${footer ? `<footer class="modal-footer">${footer}</footer>` : ''}
        </section>`;
    target.classList.add('open');
    target.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    target.querySelectorAll('[data-close-modal]').forEach(button => button.addEventListener('click', closeModal));
    target.addEventListener('mousedown', event => { if (event.target === target) closeModal(); }, { once: true });
    target.querySelector('input, select, textarea, button')?.focus();
    onOpen?.(target.querySelector('.modal'));
    return target.querySelector('.modal');
};

export const showFormError = (modal, message) => {
    const target = modal.querySelector('.form-error');
    if (!target) return;
    target.textContent = message;
    target.classList.add('visible');
};

export const confirmAction = ({ title, message, confirmText = 'Confirmar', danger = false }) => new Promise(resolve => {
    const modal = openModal({
        title,
        className: 'compact',
        body: `<p class="confirm-copy">${message}</p>`,
        footer: `<button class="button" type="button" data-close-modal>Cancelar</button><button class="button ${danger ? 'danger' : 'primary'}" type="button" id="confirm-action">${escapeHtml(confirmText)}</button>`
    });
    modal.querySelector('#confirm-action').addEventListener('click', () => { closeModal(); resolve(true); });
    modal.querySelectorAll('[data-close-modal]').forEach(button => button.addEventListener('click', () => resolve(false), { once: true }));
});

export const formDataObject = form => Object.fromEntries(new FormData(form).entries());

export const setLoading = (button, loading, label = 'Guardando…') => {
    if (!button) return;
    if (loading) {
        button.dataset.original = button.innerHTML;
        button.innerHTML = label;
        button.disabled = true;
    } else {
        button.innerHTML = button.dataset.original || button.innerHTML;
        button.disabled = false;
    }
};
