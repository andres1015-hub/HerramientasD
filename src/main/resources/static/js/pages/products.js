import { api } from '../services/api.js';
import { badge, closeModal, confirmAction, emptyState, escapeHtml, formDataObject, icon, openModal, pageHeading, setLoading, showFormError, toast } from '../components/ui.js';
import { format } from '../utils/format.js';

const productForm = (ctx, product = null) => {
    const editing = Boolean(product);
    const modal = openModal({
        title: editing ? 'Editar producto' : 'Registrar producto',
        subtitle: editing ? `Actualiza la información de ${product.code}` : 'Completa los datos para agregarlo al inventario',
        body: `
            <div class="form-error"></div>
            <form id="product-form" class="form-grid">
                <div class="form-field"><label for="product-code">Código <span>*</span></label><input id="product-code" name="code" required maxlength="30" value="${escapeHtml(product?.code || '')}" placeholder="PR-001"></div>
                <div class="form-field"><label for="product-status">Estado</label><select id="product-status" name="status"><option value="ACTIVO" ${product?.status !== 'INACTIVO' ? 'selected' : ''}>Activo</option><option value="INACTIVO" ${product?.status === 'INACTIVO' ? 'selected' : ''}>Inactivo</option></select></div>
                <div class="form-field full"><label for="product-name">Nombre <span>*</span></label><input id="product-name" name="name" required maxlength="100" value="${escapeHtml(product?.name || '')}" placeholder="Nombre del producto"></div>
                <div class="form-field full"><label for="product-description">Descripción</label><textarea id="product-description" name="description" maxlength="300" placeholder="Descripción breve del producto">${escapeHtml(product?.description || '')}</textarea></div>
                <div class="form-field"><label for="product-category">Categoría <span>*</span></label><input id="product-category" name="category" required maxlength="60" list="category-options" value="${escapeHtml(product?.category || '')}" placeholder="Equipos"><datalist id="category-options">${[...new Set(ctx.state.products.map(item => item.category))].map(category => `<option value="${escapeHtml(category)}"></option>`).join('')}</datalist></div>
                <div class="form-field"><label for="product-price">Precio (S/) <span>*</span></label><input id="product-price" name="price" type="number" required min="0" step="0.01" value="${product?.price ?? ''}" placeholder="0.00"></div>
                <div class="form-field"><label for="product-stock">Stock actual <span>*</span></label><input id="product-stock" name="stock" type="number" required min="0" step="1" value="${product?.stock ?? 0}"><small>Los cambios de stock generan un movimiento.</small></div>
                <div class="form-field"><label for="product-minimum">Stock mínimo <span>*</span></label><input id="product-minimum" name="minimumStock" type="number" required min="0" step="1" value="${product?.minimumStock ?? 0}"><small>Activa las alertas de reposición.</small></div>
            </form>`,
        footer: `<button class="button" type="button" data-close-modal>Cancelar</button><button class="button primary" type="submit" form="product-form" id="save-product">${icon('check')} ${editing ? 'Guardar cambios' : 'Registrar producto'}</button>`
    });

    modal.querySelector('#product-form').addEventListener('submit', async event => {
        event.preventDefault();
        const button = modal.querySelector('#save-product');
        const values = formDataObject(event.currentTarget);
        const payload = { ...values, price: Number(values.price), stock: Number.parseInt(values.stock, 10), minimumStock: Number.parseInt(values.minimumStock, 10) };
        setLoading(button, true);
        try {
            if (editing) await api.updateProduct(product.id, payload);
            else await api.createProduct(payload);
            closeModal();
            await ctx.refreshData();
            ctx.renderCurrent();
            toast(editing ? 'Producto actualizado' : 'Producto registrado', `${payload.name} quedó guardado correctamente.`);
        } catch (error) {
            showFormError(modal, error.message);
            setLoading(button, false);
        }
    });
};

const productDetail = product => openModal({
    title: product.name,
    subtitle: `${product.code} · ${product.category}`,
    className: 'wide',
    body: `
        <div class="product-detail-hero">
            <div>
                <div class="detail-grid">
                    <div class="detail-box"><span>Precio</span><strong>${format.money(product.price)}</strong></div>
                    <div class="detail-box"><span>Estado</span>${badge(product.status)}</div>
                    <div class="detail-box"><span>Stock disponible</span><strong>${product.stock} unidades</strong></div>
                    <div class="detail-box"><span>Stock mínimo</span><strong>${product.minimumStock} unidades</strong></div>
                </div>
                <p class="detail-description">${escapeHtml(product.description || 'Sin descripción registrada.')}</p>
                <div class="detail-box"><span>Fecha de registro</span><strong>${format.date(product.createdAt)}</strong></div>
            </div>
            <div class="product-qr-preview"><img src="/api/products/${product.id}/qr" alt="Código QR de ${escapeHtml(product.name)}"></div>
        </div>`,
    footer: `<button class="button" type="button" data-close-modal>Cerrar</button><a class="button primary" href="/api/products/${product.id}/qr?download=true">${icon('download')} Descargar QR</a>`
});

export const renderProducts = ({ state }) => {
    const categories = [...new Set(state.products.map(product => product.category))].sort();
    const initialSearch = state.globalSearch || '';
    const rows = state.products.map(product => {
        const searchable = format.compact(`${product.code} ${product.name} ${product.description}`);
        return `
            <tr data-product-row data-search="${escapeHtml(searchable)}" data-category="${escapeHtml(product.category)}" data-status="${product.status}" data-low="${product.lowStock}">
                <td><div class="product-cell"><span class="product-initial">${escapeHtml(format.initials(product.name))}</span><span class="product-cell-copy"><strong>${escapeHtml(product.name)}</strong><small>${escapeHtml(product.category)}</small></span></div></td>
                <td><span class="code-text">${escapeHtml(product.code)}</span></td>
                <td>${format.money(product.price)}</td>
                <td><div class="stock-cell"><strong>${product.stock} un.</strong><div class="stock-bar ${product.lowStock ? 'low' : ''}"><span style="width:${Math.min(100, Math.max(7, product.stock / Math.max(product.minimumStock * 2, 1) * 100))}%"></span></div></div></td>
                <td class="mobile-hide">${product.minimumStock} un.</td>
                <td>${badge(product.status)}</td>
                <td><div class="row-actions"><button class="row-action" type="button" data-view-product="${product.id}" title="Ver producto">${icon('eye')}</button><button class="row-action" type="button" data-edit-product="${product.id}" title="Editar producto">${icon('edit')}</button><button class="row-action danger" type="button" data-delete-product="${product.id}" title="Eliminar producto">${icon('trash')}</button></div></td>
            </tr>`;
    }).join('');

    return `
        ${pageHeading({ eyebrow: 'Catálogo', title: 'Productos', subtitle: 'Administra los artículos y sus niveles de stock.', actions: `<button class="button primary" id="new-product" type="button">${icon('plus')} Registrar producto</button>` })}
        <div class="toolbar">
            <div class="toolbar-group filters">
                <div class="field-control search-control">${icon('search')}<input id="product-search" type="search" value="${escapeHtml(initialSearch)}" placeholder="Buscar por nombre o código…"></div>
                <div class="field-control"><select id="product-category-filter"><option value="">Todas las categorías</option>${categories.map(category => `<option value="${escapeHtml(category)}">${escapeHtml(category)}</option>`).join('')}</select></div>
                <div class="field-control"><select id="product-status-filter"><option value="">Todos los estados</option><option value="ACTIVO">Activos</option><option value="INACTIVO">Inactivos</option><option value="LOW">Stock bajo</option></select></div>
            </div>
            <span class="toolbar-count" id="product-result-count">${state.products.length} productos</span>
        </div>
        <section class="card table-card">
            ${state.products.length ? `<div class="table-wrap"><table class="data-table"><thead><tr><th>Producto</th><th>Código</th><th>Precio</th><th>Stock</th><th class="mobile-hide">Mínimo</th><th>Estado</th><th aria-label="Acciones"></th></tr></thead><tbody>${rows}</tbody></table></div><div class="pagination-note">Mostrando el catálogo completo · Datos servidos por la API local</div>` : emptyState('Aún no hay productos', 'Registra el primer producto para comenzar a controlar el inventario.', `<button class="button primary" type="button" id="empty-new-product">${icon('plus')} Registrar producto</button>`)}
            <div id="product-no-results" hidden>${emptyState('Sin coincidencias', 'Prueba con otro texto o cambia los filtros seleccionados.')}</div>
        </section>`;
};

export const bindProducts = ctx => {
    const openNew = () => productForm(ctx);
    document.querySelector('#new-product')?.addEventListener('click', openNew);
    document.querySelector('#empty-new-product')?.addEventListener('click', openNew);

    const search = document.querySelector('#product-search');
    const category = document.querySelector('#product-category-filter');
    const status = document.querySelector('#product-status-filter');
    const filterRows = () => {
        const query = format.compact(search?.value);
        let visible = 0;
        document.querySelectorAll('[data-product-row]').forEach(row => {
            const matchesSearch = !query || row.dataset.search.includes(query);
            const matchesCategory = !category?.value || row.dataset.category === category.value;
            const matchesStatus = !status?.value || (status.value === 'LOW' ? row.dataset.low === 'true' : row.dataset.status === status.value);
            row.hidden = !(matchesSearch && matchesCategory && matchesStatus);
            if (!row.hidden) visible += 1;
        });
        const count = document.querySelector('#product-result-count');
        if (count) count.textContent = `${visible} producto${visible === 1 ? '' : 's'}`;
        const noResults = document.querySelector('#product-no-results');
        if (noResults) noResults.hidden = visible !== 0 || ctx.state.products.length === 0;
    };
    [search, category, status].forEach(control => control?.addEventListener(control.tagName === 'INPUT' ? 'input' : 'change', filterRows));
    filterRows();
    ctx.state.globalSearch = '';

    document.querySelectorAll('[data-view-product]').forEach(button => button.addEventListener('click', () => {
        const product = ctx.state.products.find(item => item.id === Number(button.dataset.viewProduct));
        if (product) productDetail(product);
    }));
    document.querySelectorAll('[data-edit-product]').forEach(button => button.addEventListener('click', () => {
        const product = ctx.state.products.find(item => item.id === Number(button.dataset.editProduct));
        if (product) productForm(ctx, product);
    }));
    document.querySelectorAll('[data-delete-product]').forEach(button => button.addEventListener('click', async () => {
        const product = ctx.state.products.find(item => item.id === Number(button.dataset.deleteProduct));
        if (!product) return;
        const confirmed = await confirmAction({ title: 'Eliminar producto', message: `¿Deseas eliminar <strong>${escapeHtml(product.name)}</strong>? Esta acción retirará el producto del catálogo actual.`, confirmText: 'Eliminar', danger: true });
        if (!confirmed) return;
        try {
            await api.deleteProduct(product.id);
            await ctx.refreshData();
            ctx.renderCurrent();
            toast('Producto eliminado', `${product.name} ya no aparece en el catálogo.`);
        } catch (error) {
            toast('No se pudo eliminar', error.message, 'error');
        }
    }));

    if (ctx.state.pendingAction === 'new-product') {
        ctx.state.pendingAction = null;
        window.setTimeout(openNew, 0);
    }
    if (ctx.state.pendingFilter === 'low') {
        ctx.state.pendingFilter = null;
        if (status) { status.value = 'LOW'; filterRows(); }
    }
};
