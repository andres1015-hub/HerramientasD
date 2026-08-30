import { badge, emptyState, escapeHtml, icon, pageHeading, toast } from '../components/ui.js';
import { downloadCsv, format } from '../utils/format.js';

const productRows = products => products.map(product => `<tr data-report-row data-search="${escapeHtml(format.compact(`${product.name} ${product.code} ${product.category}`))}"><td><strong>${escapeHtml(product.name)}</strong></td><td><span class="code-text">${escapeHtml(product.code)}</span></td><td>${escapeHtml(product.category)}</td><td>${format.money(product.price)}</td><td>${product.stock}</td><td>${product.minimumStock}</td><td>${badge(product.status)}</td></tr>`).join('');
const orderRows = orders => orders.map(order => `<tr data-report-row data-search="${escapeHtml(format.compact(`${order.code} ${order.customer}`))}" data-date="${format.dateInput(order.createdAt)}"><td><span class="order-code">${escapeHtml(order.code)}</span></td><td>${escapeHtml(order.customer)}</td><td>${format.date(order.createdAt)}</td><td>${order.items.reduce((sum, item) => sum + item.quantity, 0)}</td><td>${format.money(order.total)}</td><td>${badge(order.status)}</td></tr>`).join('');
const movementRows = movements => movements.map(movement => `<tr data-report-row data-search="${escapeHtml(format.compact(`${movement.productName} ${movement.productCode} ${movement.reference}`))}" data-product="${movement.productId}" data-type="${movement.type}" data-date="${format.dateInput(movement.createdAt)}"><td>${escapeHtml(movement.productName)}</td><td><span class="code-text">${escapeHtml(movement.productCode)}</span></td><td>${badge(movement.type)}</td><td>${movement.quantity}</td><td>${movement.resultingStock}</td><td>${escapeHtml(movement.reference)}</td><td>${format.dateTime(movement.createdAt)}</td></tr>`).join('');

const tableFor = (state, tab) => {
    if (tab === 'orders') return { headers: ['Pedido', 'Cliente', 'Fecha', 'Unidades', 'Total', 'Estado'], rows: orderRows(state.orders), total: state.orders.length, label: 'pedidos' };
    if (tab === 'movements') return { headers: ['Producto', 'Código', 'Tipo', 'Cantidad', 'Stock final', 'Referencia', 'Fecha'], rows: movementRows(state.movements), total: state.movements.length, label: 'movimientos' };
    return { headers: ['Producto', 'Código', 'Categoría', 'Precio', 'Stock', 'Mínimo', 'Estado'], rows: productRows(state.products), total: state.products.length, label: 'productos' };
};

export const renderReports = ({ state }) => {
    const tab = state.reportTab || 'products';
    const table = tableFor(state, tab);
    const filters = tab === 'movements' ? `
        <div class="field-control"><select id="report-product"><option value="">Todos los productos</option>${state.products.map(product => `<option value="${product.id}">${escapeHtml(product.name)}</option>`).join('')}</select></div>
        <div class="field-control"><select id="report-type"><option value="">Entradas y salidas</option><option value="ENTRADA">Entradas</option><option value="SALIDA">Salidas</option></select></div>` : '';
    return `
        ${pageHeading({ eyebrow: 'Análisis', title: 'Reportes', subtitle: 'Consulta y exporta la información operativa del sistema.', actions: `<button class="button primary" id="export-report" type="button">${icon('download')} Exportar CSV</button>` })}
        <div class="report-tabs" role="tablist"><button class="report-tab ${tab === 'products' ? 'active' : ''}" data-report-tab="products" type="button">Productos y stock</button><button class="report-tab ${tab === 'orders' ? 'active' : ''}" data-report-tab="orders" type="button">Pedidos</button><button class="report-tab ${tab === 'movements' ? 'active' : ''}" data-report-tab="movements" type="button">Movimientos</button></div>
        <div class="report-summary"><div><strong id="report-visible-total">${table.total}</strong><span>Registros en el reporte de ${table.label}</span></div><span class="report-mark">Actualizado · ${format.dateTime(new Date())}</span></div>
        <div class="toolbar">
            <div class="toolbar-group filters">
                <div class="field-control search-control">${icon('search')}<input id="report-search" type="search" placeholder="Buscar dentro del reporte…"></div>
                ${filters}
                ${tab !== 'products' ? '<div class="field-control"><input id="report-date" type="date" aria-label="Filtrar por fecha"></div>' : ''}
            </div>
            <span class="toolbar-count">Filtros combinables</span>
        </div>
        <section class="card table-card">
            ${table.total ? `<div class="table-wrap"><table class="data-table" id="report-table"><thead><tr>${table.headers.map(header => `<th>${header}</th>`).join('')}</tr></thead><tbody>${table.rows}</tbody></table></div><div class="pagination-note">El archivo CSV exportará solamente los registros visibles</div>` : emptyState('No hay datos para este reporte', 'Registra operaciones para comenzar a analizar la información.')}
            <div id="report-no-results" hidden>${emptyState('Sin resultados', 'Ajusta los filtros para ver otros registros.')}</div>
        </section>`;
};

export const bindReports = ctx => {
    document.querySelectorAll('[data-report-tab]').forEach(button => button.addEventListener('click', () => {
        ctx.state.reportTab = button.dataset.reportTab;
        ctx.renderCurrent();
    }));
    const controls = {
        search: document.querySelector('#report-search'),
        product: document.querySelector('#report-product'),
        type: document.querySelector('#report-type'),
        date: document.querySelector('#report-date')
    };
    const filter = () => {
        const query = format.compact(controls.search?.value);
        let visible = 0;
        document.querySelectorAll('[data-report-row]').forEach(row => {
            row.hidden = !((!query || row.dataset.search.includes(query)) && (!controls.product?.value || row.dataset.product === controls.product.value) && (!controls.type?.value || row.dataset.type === controls.type.value) && (!controls.date?.value || row.dataset.date === controls.date.value));
            if (!row.hidden) visible += 1;
        });
        const total = document.querySelector('#report-visible-total');
        if (total) total.textContent = visible;
        const empty = document.querySelector('#report-no-results');
        if (empty) empty.hidden = visible !== 0;
    };
    Object.values(controls).forEach(control => control?.addEventListener(control.type === 'search' ? 'input' : 'change', filter));
    document.querySelector('#export-report')?.addEventListener('click', () => {
        const table = document.querySelector('#report-table');
        if (!table) return;
        const headers = [...table.querySelectorAll('thead th')].map(cell => cell.textContent.trim());
        const rows = [...table.querySelectorAll('tbody tr:not([hidden])')].map(row => [...row.cells].map(cell => cell.textContent.trim()));
        if (!rows.length) {
            toast('No hay filas para exportar', 'Ajusta los filtros e inténtalo nuevamente.', 'error');
            return;
        }
        downloadCsv(`reporte-${ctx.state.reportTab || 'products'}-${format.dateInput(new Date())}.csv`, [headers, ...rows]);
        toast('Reporte exportado', `${rows.length} registros fueron incluidos en el CSV.`);
    });
};
