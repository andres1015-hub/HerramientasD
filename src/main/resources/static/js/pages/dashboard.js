import { badge, escapeHtml, icon, pageHeading } from '../components/ui.js';
import { format } from '../utils/format.js';

const statCard = ({ iconName, value, label, trend, warning = false, accent, tone }) => `
    <article class="stat-card" style="--accent:${accent || 'var(--brand)'};--tone:${tone || 'var(--brand-soft)'}">
        <div class="stat-top">
            <span class="stat-icon">${icon(iconName)}</span>
            <span class="stat-trend ${warning ? 'warning' : ''}">${escapeHtml(trend)}</span>
        </div>
        <strong class="value">${escapeHtml(value)}</strong>
        <p>${escapeHtml(label)}</p>
    </article>`;

export const renderDashboard = ({ state }) => {
    const data = state.dashboard;
    const categoryStock = state.products.reduce((accumulator, product) => {
        accumulator[product.category] = (accumulator[product.category] || 0) + product.stock;
        return accumulator;
    }, {});
    const maxStock = Math.max(1, ...Object.values(categoryStock));
    const categories = Object.entries(categoryStock).sort((a, b) => b[1] - a[1]);

    return `
        ${pageHeading({
            eyebrow: 'Resumen operativo',
            title: 'Hola, Rodrigo',
            subtitle: `Así se encuentra tu inventario hoy, ${format.date(new Date())}.`,
            actions: `<button class="button" type="button" data-nav="reports">${icon('chart')} Ver reportes</button><button class="button primary" type="button" data-nav="products" data-action="new-product">${icon('plus')} Nuevo producto</button>`
        })}
        <section class="stat-grid" aria-label="Indicadores del inventario">
            ${statCard({ iconName: 'box', value: format.number(data.totalProducts), label: 'Productos registrados', trend: 'Catálogo activo' })}
            ${statCard({ iconName: 'inventory', value: format.number(data.totalStock), label: 'Unidades disponibles', trend: 'Stock total', accent: 'var(--blue)', tone: 'var(--blue-soft)' })}
            ${statCard({ iconName: 'alert', value: format.number(data.lowStockProducts), label: 'Productos con stock bajo', trend: 'Requieren atención', warning: true, accent: 'var(--amber)', tone: 'var(--amber-soft)' })}
            ${statCard({ iconName: 'orders', value: format.number(data.totalOrders), label: 'Pedidos registrados', trend: `${data.openOrders} en curso`, accent: 'var(--purple)', tone: '#f1edff' })}
        </section>

        <section class="content-grid">
            <article class="card chart-card">
                <header class="card-header"><div><h2>Stock por categoría</h2><p>Distribución actual de unidades disponibles</p></div><button class="text-link" type="button" data-nav="inventory">Ver inventario ${icon('arrow')}</button></header>
                <div class="card-body">
                    <div class="bar-chart" role="img" aria-label="Gráfico de stock por categoría">
                        ${categories.map(([category, stock]) => `
                            <div class="bar-column" title="${escapeHtml(category)}: ${stock}">
                                <div class="bar-track"><span class="bar-fill" style="height:${Math.max(6, Math.round(stock / maxStock * 100))}%"></span></div>
                                <span>${escapeHtml(category)}</span>
                            </div>`).join('')}
                    </div>
                </div>
            </article>
            <article class="card">
                <header class="card-header"><div><h2>Alertas de stock</h2><p>Productos en o debajo del mínimo</p></div><button class="text-link" type="button" data-nav="products" data-filter="low">Gestionar ${icon('arrow')}</button></header>
                <div class="card-body alert-list">
                    ${data.lowStockAlerts.length ? data.lowStockAlerts.slice(0, 5).map(product => `
                        <div class="alert-item">
                            <span class="product-initial">${escapeHtml(format.initials(product.name))}</span>
                            <span class="alert-copy"><strong>${escapeHtml(product.name)}</strong><small>Mínimo: ${product.minimumStock} unidades</small></span>
                            <span class="stock-count">${product.stock} un.</span>
                        </div>`).join('') : '<div class="empty-mini">Todo el inventario tiene stock suficiente.</div>'}
                </div>
            </article>
        </section>

        <section class="content-grid">
            <article class="card">
                <header class="card-header"><div><h2>Movimientos recientes</h2><p>Últimas entradas y salidas registradas</p></div><button class="text-link" type="button" data-nav="inventory">Ver todos ${icon('arrow')}</button></header>
                <div class="card-body activity-list">
                    ${data.recentMovements.map(movement => `
                        <div class="activity-item">
                            <span class="activity-icon ${movement.type === 'ENTRADA' ? 'in' : 'out'}">${icon(movement.type === 'ENTRADA' ? 'plus' : 'arrow')}</span>
                            <span class="activity-copy"><strong>${escapeHtml(movement.productName)}</strong><p>${escapeHtml(movement.reference)} · ${escapeHtml(movement.productCode)}</p></span>
                            <span class="activity-meta"><strong>${movement.type === 'ENTRADA' ? '+' : '−'}${movement.quantity}</strong><time>${format.dateTime(movement.createdAt)}</time></span>
                        </div>`).join('')}
                </div>
            </article>
            <article class="card">
                <header class="card-header"><div><h2>Pedidos recientes</h2><p>Actividad comercial más reciente</p></div><button class="text-link" type="button" data-nav="orders">Ver pedidos ${icon('arrow')}</button></header>
                <div class="card-body order-list">
                    ${data.recentOrders.map(order => `
                        <div class="alert-item">
                            <span class="product-initial">${escapeHtml(format.initials(order.customer))}</span>
                            <span class="alert-copy"><strong>${escapeHtml(order.customer)}</strong><small>${escapeHtml(order.code)} · ${format.money(order.total)}</small></span>
                            ${badge(order.status)}
                        </div>`).join('')}
                </div>
            </article>
        </section>`;
};

export const bindDashboard = () => {};
