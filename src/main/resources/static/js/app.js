import { api } from './services/api.js';
import { escapeHtml, icon, toast } from './components/ui.js';
import { renderDashboard, bindDashboard } from './pages/dashboard.js';
import { renderProducts, bindProducts } from './pages/products.js';
import { renderOrders, bindOrders } from './pages/orders.js';
import { renderInventory, bindInventory } from './pages/inventory.js';
import { renderQr, bindQr } from './pages/qr.js';
import { renderReports, bindReports } from './pages/reports.js';

const pageContent = document.querySelector('#page-content');
const pages = {
    dashboard: { render: renderDashboard, bind: bindDashboard },
    products: { render: renderProducts, bind: bindProducts },
    orders: { render: renderOrders, bind: bindOrders },
    inventory: { render: renderInventory, bind: bindInventory },
    qr: { render: renderQr, bind: bindQr },
    reports: { render: renderReports, bind: bindReports }
};

const state = {
    currentPage: 'dashboard', products: [], orders: [], movements: [], dashboard: null,
    globalSearch: '', pendingAction: null, pendingFilter: null, reportTab: 'products'
};

const updateChrome = () => {
    document.querySelector('#products-count').textContent = state.products.length;
    const alerts = state.products.filter(product => product.lowStock).length;
    document.querySelector('#notification-badge').textContent = alerts;
    document.querySelector('#notification-badge').hidden = alerts === 0;
    const openOrders = state.orders.some(order => ['PENDIENTE', 'PROCESANDO'].includes(order.status));
    document.querySelector('#orders-dot').hidden = !openOrders;
};

const refreshData = async () => {
    const [dashboard, products, orders, movements] = await Promise.all([api.dashboard(), api.products(), api.orders(), api.movements()]);
    Object.assign(state, { dashboard, products, orders, movements });
    updateChrome();
};

const context = () => ({ state, refreshData, renderCurrent, navigate });

function renderCurrent() {
    const page = pages[state.currentPage] || pages.dashboard;
    pageContent.innerHTML = page.render(context());
    page.bind(context());
    document.querySelectorAll('.nav-item').forEach(item => item.classList.toggle('active', item.dataset.page === state.currentPage));
    pageContent.focus({ preventScroll: true });
}

function closeSidebar() {
    document.querySelector('#sidebar').classList.remove('open');
    document.querySelector('#sidebar-overlay').classList.remove('open');
}

function navigate(page, updateHash = true) {
    if (!pages[page]) page = 'dashboard';
    state.currentPage = page;
    if (updateHash && window.location.hash !== `#/${page}`) history.pushState(null, '', `#/${page}`);
    closeSidebar();
    renderCurrent();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.querySelectorAll('.nav-item').forEach(item => item.addEventListener('click', () => navigate(item.dataset.page)));
document.addEventListener('click', event => {
    const target = event.target.closest('[data-nav]');
    if (!target) return;
    state.pendingAction = target.dataset.action || null;
    state.pendingFilter = target.dataset.filter || null;
    navigate(target.dataset.nav);
});
document.querySelector('#mobile-menu').addEventListener('click', () => {
    document.querySelector('#sidebar').classList.add('open');
    document.querySelector('#sidebar-overlay').classList.add('open');
});
document.querySelector('#sidebar-overlay').addEventListener('click', closeSidebar);

const profileButton = document.querySelector('#profile-button');
const profileMenu = document.querySelector('#profile-menu');
profileButton.addEventListener('click', () => {
    const open = profileMenu.classList.toggle('open');
    profileButton.setAttribute('aria-expanded', String(open));
});
document.querySelector('#logout-demo').addEventListener('click', () => profileMenu.classList.remove('open'));
document.querySelector('#session-info').addEventListener('click', () => toast('Sesión local activa', 'El módulo de usuarios aún no existe en el backend del proyecto.'));
document.addEventListener('click', event => {
    if (!event.target.closest('.profile-wrap')) profileMenu.classList.remove('open');
});

const globalSearch = document.querySelector('#global-search');
const globalSearchInput = document.querySelector('#global-search-input');
globalSearch.addEventListener('submit', event => {
    event.preventDefault();
    state.globalSearch = globalSearchInput.value.trim();
    navigate('products');
});
document.addEventListener('keydown', event => {
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        globalSearchInput.focus();
        globalSearchInput.select();
    }
    if (event.key === 'Escape') closeSidebar();
});
document.querySelector('#notification-button').addEventListener('click', () => {
    state.pendingFilter = 'low';
    navigate('products');
});
window.addEventListener('hashchange', () => navigate(window.location.hash.replace('#/', '') || 'dashboard', false));

const showStartupError = error => {
    pageContent.innerHTML = `
        <div class="empty-state" style="margin-top:12vh">
            <span class="empty-icon">${icon('alert')}</span>
            <h3>No pudimos cargar el inventario</h3>
            <p>${escapeHtml(error.message)}. Comprueba que la API de Spring Boot esté disponible.</p>
            <button class="button primary" type="button" id="retry-load">Reintentar</button>
        </div>`;
    document.querySelector('#retry-load').addEventListener('click', start);
};

async function start() {
    try {
        pageContent.innerHTML = `<div class="page-loader"><span class="loader-mark">${icon('qr')}</span><p>Preparando tu inventario…</p></div>`;
        await refreshData();
        state.currentPage = pages[window.location.hash.replace('#/', '')] ? window.location.hash.replace('#/', '') : 'dashboard';
        renderCurrent();
    } catch (error) {
        showStartupError(error);
    }
}

start();
