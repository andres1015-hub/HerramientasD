import { api } from '../services/api.js';
import { badge, closeModal, emptyState, escapeHtml, formDataObject, icon, openModal, pageHeading, setLoading, showFormError, toast } from '../components/ui.js';
import { format } from '../utils/format.js';

const productOptions = products => products
    .filter(product => product.status === 'ACTIVO' && product.stock > 0)
    .map(product => `<option value="${product.id}">${escapeHtml(product.name)} · ${escapeHtml(product.code)} (${product.stock} disp.)</option>`).join('');

const orderForm = ctx => {
    const options = productOptions(ctx.state.products);
    const modal = openModal({
        title: 'Registrar pedido',
        subtitle: 'Selecciona los productos y cantidades solicitadas',
        className: 'wide',
        body: `
            <div class="form-error"></div>
            <form id="order-form" class="form-grid">
                <div class="form-field"><label for="order-customer">Cliente o solicitante <span>*</span></label><input id="order-customer" name="customer" required maxlength="100" placeholder="Nombre del cliente"></div>
                <div class="form-field"><label for="order-status">Estado inicial</label><select id="order-status" name="status"><option value="PENDIENTE">Pendiente</option><option value="PROCESANDO">Procesando</option><option value="COMPLETADO">Completado</option></select></div>
                <div class="form-field full"><label>Productos <span>*</span></label><div class="line-items" id="order-lines"></div><button class="button small add-line" id="add-order-line" type="button">${icon('plus')} Agregar producto</button></div>
            </form>`,
        footer: `<button class="button" type="button" data-close-modal>Cancelar</button><button class="button primary" id="save-order" type="submit" form="order-form">${icon('check')} Registrar pedido</button>`
    });

    const lines = modal.querySelector('#order-lines');
    const addLine = () => {
        const line = document.createElement('div');
        line.className = 'line-item';
        line.innerHTML = `<select name="productId" required><option value="">Selecciona un producto</option>${options}</select><input name="quantity" type="number" min="1" step="1" value="1" aria-label="Cantidad" required><button type="button" aria-label="Quitar producto">${icon('trash')}</button>`;
        line.querySelector('button').addEventListener('click', () => { if (lines.children.length > 1) line.remove(); });
        lines.append(line);
    };
    addLine();
    modal.querySelector('#add-order-line').addEventListener('click', addLine);
    modal.querySelector('#order-form').addEventListener('submit', async event => {
        event.preventDefault();
        const values = formDataObject(event.currentTarget);
        const items = [...lines.querySelectorAll('.line-item')].map(line => ({
            productId: Number(line.querySelector('[name="productId"]').value),
            quantity: Number(line.querySelector('[name="quantity"]').value)
        }));
        const button = modal.querySelector('#save-order');
        setLoading(button, true, 'Registrando…');
        try {
            const order = await api.createOrder({ customer: values.customer, status: values.status, items });
            closeModal();
            await ctx.refreshData();
            ctx.renderCurrent();
            toast('Pedido registrado', `${order.code} fue creado y el stock quedó actualizado.`);
        } catch (error) {
            showFormError(modal, error.message);
            setLoading(button, false);
        }
    });
};

const orderDetail = order => openModal({
    title: `Pedido ${order.code}`,
    subtitle: `${order.customer} · ${format.dateTime(order.createdAt)}`,
    className: 'wide',
    body: `
        <div class="detail-grid">
            <div class="detail-box"><span>Cliente</span><strong>${escapeHtml(order.customer)}</strong></div>
            <div class="detail-box"><span>Estado</span>${badge(order.status)}</div>
            <div class="detail-box"><span>Productos</span><strong>${order.items.reduce((sum, item) => sum + item.quantity, 0)} unidades</strong></div>
            <div class="detail-box"><span>Total</span><strong>${format.money(order.total)}</strong></div>
        </div>
        <div class="table-wrap" style="margin-top:16px"><table class="data-table"><thead><tr><th>Producto</th><th>Código</th><th>Cantidad</th><th>Precio</th><th>Subtotal</th></tr></thead><tbody>${order.items.map(item => `<tr><td><strong>${escapeHtml(item.productName)}</strong></td><td><span class="code-text">${escapeHtml(item.productCode)}</span></td><td>${item.quantity}</td><td>${format.money(item.unitPrice)}</td><td class="order-total">${format.money(item.subtotal)}</td></tr>`).join('')}</tbody></table></div>`,
    footer: `<button class="button primary" type="button" data-close-modal>Cerrar detalle</button>`
});

export const renderOrders = ({ state }) => `
    ${pageHeading({ eyebrow: 'Operaciones', title: 'Pedidos', subtitle: 'Registra solicitudes y consulta su estado.', actions: `<button class="button primary" id="new-order" type="button">${icon('plus')} Nuevo pedido</button>` })}
    <div class="toolbar">
        <div class="toolbar-group filters">
            <div class="field-control search-control">${icon('search')}<input id="order-search" type="search" placeholder="Buscar pedido o cliente…"></div>
            <div class="field-control"><select id="order-filter"><option value="">Todos los estados</option><option value="PENDIENTE">Pendientes</option><option value="PROCESANDO">Procesando</option><option value="COMPLETADO">Completados</option><option value="CANCELADO">Cancelados</option></select></div>
        </div>
        <span class="toolbar-count" id="order-result-count">${state.orders.length} pedidos</span>
    </div>
    <section class="card table-card">
        ${state.orders.length ? `<div class="table-wrap"><table class="data-table"><thead><tr><th>Pedido</th><th>Cliente</th><th>Fecha</th><th>Productos</th><th>Total</th><th>Estado</th><th></th></tr></thead><tbody>${state.orders.map(order => `
            <tr data-order-row data-search="${escapeHtml(format.compact(`${order.code} ${order.customer}`))}" data-status="${order.status}">
                <td><span class="order-code">${escapeHtml(order.code)}</span></td>
                <td><div class="product-cell"><span class="product-initial">${escapeHtml(format.initials(order.customer))}</span><span class="product-cell-copy"><strong>${escapeHtml(order.customer)}</strong><small>${order.items.length} referencia${order.items.length === 1 ? '' : 's'}</small></span></div></td>
                <td>${format.date(order.createdAt)}</td><td>${order.items.reduce((sum, item) => sum + item.quantity, 0)} un.</td><td class="order-total">${format.money(order.total)}</td>
                <td><select class="status-select" data-order-status="${order.id}" aria-label="Estado del pedido"><option value="PENDIENTE" ${order.status === 'PENDIENTE' ? 'selected' : ''}>Pendiente</option><option value="PROCESANDO" ${order.status === 'PROCESANDO' ? 'selected' : ''}>Procesando</option><option value="COMPLETADO" ${order.status === 'COMPLETADO' ? 'selected' : ''}>Completado</option><option value="CANCELADO" ${order.status === 'CANCELADO' ? 'selected' : ''}>Cancelado</option></select></td>
                <td><div class="row-actions"><button class="row-action" type="button" data-view-order="${order.id}" title="Ver detalle">${icon('eye')}</button></div></td>
            </tr>`).join('')}</tbody></table></div><div class="pagination-note">Los pedidos nuevos descuentan automáticamente sus unidades del inventario</div>` : emptyState('No hay pedidos registrados', 'Crea el primer pedido y selecciona productos disponibles.', `<button class="button primary" id="empty-new-order" type="button">${icon('plus')} Nuevo pedido</button>`)}
        <div id="order-no-results" hidden>${emptyState('Sin pedidos coincidentes', 'Cambia la búsqueda o el filtro de estado.')}</div>
    </section>`;

export const bindOrders = ctx => {
    const openNew = () => orderForm(ctx);
    document.querySelector('#new-order')?.addEventListener('click', openNew);
    document.querySelector('#empty-new-order')?.addEventListener('click', openNew);
    document.querySelectorAll('[data-view-order]').forEach(button => button.addEventListener('click', () => {
        const order = ctx.state.orders.find(item => item.id === Number(button.dataset.viewOrder));
        if (order) orderDetail(order);
    }));
    document.querySelectorAll('[data-order-status]').forEach(select => select.addEventListener('change', async () => {
        select.disabled = true;
        try {
            await api.updateOrderStatus(Number(select.dataset.orderStatus), select.value);
            await ctx.refreshData();
            toast('Estado actualizado', `El pedido ahora está ${select.value.toLowerCase()}.`);
        } catch (error) {
            toast('No se pudo actualizar', error.message, 'error');
            ctx.renderCurrent();
        } finally { select.disabled = false; }
    }));
    const search = document.querySelector('#order-search');
    const status = document.querySelector('#order-filter');
    const filter = () => {
        const query = format.compact(search?.value);
        let visible = 0;
        document.querySelectorAll('[data-order-row]').forEach(row => {
            row.hidden = !((!query || row.dataset.search.includes(query)) && (!status?.value || row.dataset.status === status.value));
            if (!row.hidden) visible += 1;
        });
        document.querySelector('#order-result-count').textContent = `${visible} pedido${visible === 1 ? '' : 's'}`;
        const empty = document.querySelector('#order-no-results');
        if (empty) empty.hidden = visible !== 0 || ctx.state.orders.length === 0;
    };
    search?.addEventListener('input', filter);
    status?.addEventListener('change', filter);
};
