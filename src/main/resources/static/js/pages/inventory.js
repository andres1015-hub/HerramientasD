import { api } from '../services/api.js';
import { badge, closeModal, emptyState, escapeHtml, formDataObject, icon, openModal, pageHeading, setLoading, showFormError, toast } from '../components/ui.js';
import { format } from '../utils/format.js';

const movementForm = ctx => {
    const modal = openModal({
        title: 'Registrar movimiento',
        subtitle: 'Actualiza el stock con una entrada o salida manual',
        body: `
            <div class="form-error"></div>
            <form id="movement-form" class="form-grid">
                <div class="form-field full"><label for="movement-product">Producto <span>*</span></label><select id="movement-product" name="productId" required><option value="">Selecciona un producto</option>${ctx.state.products.filter(product => product.status === 'ACTIVO').map(product => `<option value="${product.id}">${escapeHtml(product.name)} · ${escapeHtml(product.code)} (${product.stock} un.)</option>`).join('')}</select></div>
                <div class="form-field"><label for="movement-type">Tipo <span>*</span></label><select id="movement-type" name="type" required><option value="ENTRADA">Entrada</option><option value="SALIDA">Salida</option></select></div>
                <div class="form-field"><label for="movement-quantity">Cantidad <span>*</span></label><input id="movement-quantity" name="quantity" type="number" min="1" step="1" value="1" required></div>
                <div class="form-field full"><label for="movement-reference">Referencia</label><input id="movement-reference" name="reference" maxlength="100" placeholder="Ej. OC-1045, ajuste por conteo"></div>
            </form>`,
        footer: `<button class="button" type="button" data-close-modal>Cancelar</button><button class="button primary" id="save-movement" type="submit" form="movement-form">${icon('check')} Registrar movimiento</button>`
    });
    modal.querySelector('#movement-form').addEventListener('submit', async event => {
        event.preventDefault();
        const values = formDataObject(event.currentTarget);
        const button = modal.querySelector('#save-movement');
        setLoading(button, true);
        try {
            await api.createMovement({ productId: Number(values.productId), type: values.type, quantity: Number(values.quantity), reference: values.reference });
            closeModal();
            await ctx.refreshData();
            ctx.renderCurrent();
            toast('Movimiento registrado', 'El stock del producto fue actualizado.');
        } catch (error) {
            showFormError(modal, error.message);
            setLoading(button, false);
        }
    });
};

export const renderInventory = ({ state }) => {
    const entries = state.movements.filter(movement => movement.type === 'ENTRADA').reduce((sum, movement) => sum + movement.quantity, 0);
    const outputs = state.movements.filter(movement => movement.type === 'SALIDA').reduce((sum, movement) => sum + movement.quantity, 0);
    const lowStock = state.products.filter(product => product.lowStock);
    return `
        ${pageHeading({ eyebrow: 'Control de existencias', title: 'Inventario', subtitle: 'Consulta el stock y registra entradas o salidas.', actions: `<button class="button primary" id="new-movement" type="button">${icon('plus')} Registrar movimiento</button>` })}
        <section class="inventory-summary">
            <article class="mini-summary"><span class="mini-summary-icon">${icon('plus')}</span><div><strong>${format.number(entries)}</strong><span>Unidades ingresadas</span></div></article>
            <article class="mini-summary"><span class="mini-summary-icon out">${icon('arrow')}</span><div><strong>${format.number(outputs)}</strong><span>Unidades despachadas</span></div></article>
            <article class="mini-summary"><span class="mini-summary-icon warn">${icon('alert')}</span><div><strong>${lowStock.length}</strong><span>Alertas de stock bajo</span></div></article>
        </section>
        <section class="content-grid">
            <article class="card">
                <header class="card-header"><div><h2>Stock actual</h2><p>${format.number(state.dashboard.totalStock)} unidades en ${state.products.length} productos</p></div><button class="text-link" type="button" data-nav="products">Gestionar productos ${icon('arrow')}</button></header>
                <div class="table-wrap"><table class="data-table"><thead><tr><th>Producto</th><th>Código</th><th>Disponible</th><th>Estado</th></tr></thead><tbody>${state.products.slice(0, 7).map(product => `<tr><td><strong>${escapeHtml(product.name)}</strong></td><td><span class="code-text">${escapeHtml(product.code)}</span></td><td>${product.stock} un.</td><td>${product.lowStock ? '<span class="badge warning">stock bajo</span>' : '<span class="badge success">correcto</span>'}</td></tr>`).join('')}</tbody></table></div>
            </article>
            <article class="card">
                <header class="card-header"><div><h2>Reposición prioritaria</h2><p>Artículos bajo su nivel mínimo</p></div></header>
                <div class="card-body alert-list">${lowStock.length ? lowStock.map(product => `<div class="alert-item"><span class="product-initial">${escapeHtml(format.initials(product.name))}</span><span class="alert-copy"><strong>${escapeHtml(product.name)}</strong><small>${escapeHtml(product.code)} · mínimo ${product.minimumStock}</small></span><span class="stock-count">${product.stock} un.</span></div>`).join('') : '<div class="empty-mini">No hay productos por reponer.</div>'}</div>
            </article>
        </section>
        <div class="toolbar" style="margin-top:20px">
            <div class="toolbar-group filters">
                <div class="field-control search-control">${icon('search')}<input id="movement-search" type="search" placeholder="Buscar producto o referencia…"></div>
                <div class="field-control"><select id="movement-product-filter"><option value="">Todos los productos</option>${state.products.map(product => `<option value="${product.id}">${escapeHtml(product.name)}</option>`).join('')}</select></div>
                <div class="field-control"><select id="movement-type-filter"><option value="">Entradas y salidas</option><option value="ENTRADA">Entradas</option><option value="SALIDA">Salidas</option></select></div>
                <div class="field-control"><input id="movement-date-filter" type="date" aria-label="Filtrar por fecha"></div>
            </div>
            <span class="toolbar-count" id="movement-result-count">${state.movements.length} movimientos</span>
        </div>
        <section class="card table-card">
            ${state.movements.length ? `<div class="table-wrap"><table class="data-table"><thead><tr><th>Producto</th><th>Tipo</th><th>Cantidad</th><th>Stock resultante</th><th>Referencia</th><th>Fecha</th></tr></thead><tbody>${state.movements.map(movement => `<tr data-movement-row data-search="${escapeHtml(format.compact(`${movement.productName} ${movement.productCode} ${movement.reference}`))}" data-product="${movement.productId}" data-type="${movement.type}" data-date="${format.dateInput(movement.createdAt)}"><td><div class="product-cell"><span class="product-initial">${escapeHtml(format.initials(movement.productName))}</span><span class="product-cell-copy"><strong>${escapeHtml(movement.productName)}</strong><small>${escapeHtml(movement.productCode)}</small></span></div></td><td>${badge(movement.type)}</td><td><strong>${movement.type === 'ENTRADA' ? '+' : '−'}${movement.quantity}</strong></td><td>${movement.resultingStock} un.</td><td>${escapeHtml(movement.reference)}</td><td>${format.dateTime(movement.createdAt)}</td></tr>`).join('')}</tbody></table></div>` : emptyState('Sin movimientos', 'Registra una entrada o salida para comenzar el historial.')}
            <div id="movement-no-results" hidden>${emptyState('Sin movimientos coincidentes', 'Cambia los filtros para ampliar la consulta.')}</div>
        </section>`;
};

export const bindInventory = ctx => {
    document.querySelector('#new-movement')?.addEventListener('click', () => movementForm(ctx));
    const controls = ['#movement-search', '#movement-product-filter', '#movement-type-filter', '#movement-date-filter'].map(selector => document.querySelector(selector));
    const filter = () => {
        const [search, product, type, date] = controls;
        const query = format.compact(search?.value);
        let visible = 0;
        document.querySelectorAll('[data-movement-row]').forEach(row => {
            row.hidden = !((!query || row.dataset.search.includes(query)) && (!product?.value || row.dataset.product === product.value) && (!type?.value || row.dataset.type === type.value) && (!date?.value || row.dataset.date === date.value));
            if (!row.hidden) visible += 1;
        });
        const count = document.querySelector('#movement-result-count');
        if (count) count.textContent = `${visible} movimiento${visible === 1 ? '' : 's'}`;
        const empty = document.querySelector('#movement-no-results');
        if (empty) empty.hidden = visible !== 0 || ctx.state.movements.length === 0;
    };
    controls.forEach(control => control?.addEventListener(control.tagName === 'INPUT' && control.type === 'search' ? 'input' : 'change', filter));
};
