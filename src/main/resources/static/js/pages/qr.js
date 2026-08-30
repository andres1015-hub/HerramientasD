import { api } from '../services/api.js';
import { emptyState, escapeHtml, icon, pageHeading, toast } from '../components/ui.js';
import { format } from '../utils/format.js';

export const renderQr = ({ state }) => `
    ${pageHeading({ eyebrow: 'Trazabilidad', title: 'Códigos QR', subtitle: 'Genera, descarga, imprime y lee etiquetas de producto.', actions: `<a class="button" href="#qr-scanner">${icon('scan')} Abrir lector</a>` })}
    <div class="toolbar">
        <div class="toolbar-group"><div class="field-control search-control">${icon('search')}<input id="qr-search" type="search" placeholder="Buscar producto para generar su QR…"></div></div>
        <span class="toolbar-count" id="qr-result-count">${state.products.length} códigos disponibles</span>
    </div>
    <section class="qr-layout">
        <div>
            <div class="qr-grid" id="qr-grid">${state.products.map(product => `
                <article class="qr-card" data-qr-card data-search="${escapeHtml(format.compact(`${product.name} ${product.code} ${product.category}`))}">
                    <div class="qr-image-wrap"><img loading="lazy" src="/api/products/${product.id}/qr" alt="Código QR de ${escapeHtml(product.name)}"></div>
                    <h3 title="${escapeHtml(product.name)}">${escapeHtml(product.name)}</h3><p>${escapeHtml(product.code)}</p>
                    <div class="qr-actions"><a class="button small" href="/api/products/${product.id}/qr?download=true">${icon('download')} Descargar</a><button class="button small" type="button" data-print-qr="${product.id}">${icon('print')} Imprimir</button></div>
                </article>`).join('')}</div>
            <div id="qr-no-results" hidden>${emptyState('No encontramos ese producto', 'Prueba buscando por nombre, código o categoría.')}</div>
        </div>
        <article class="card scanner-card" id="qr-scanner">
            <header class="card-header"><div><h2>Lector de códigos</h2><p>Sube una foto nítida de un QR</p></div><span class="stat-icon">${icon('scan')}</span></header>
            <label class="scanner-zone" id="scanner-zone">${icon('upload')}<strong>Selecciona una imagen</strong><span>PNG o JPG con el código visible</span><input id="qr-file" type="file" accept="image/png,image/jpeg,image/webp"></label>
            <div class="scan-result" id="scan-result"></div>
        </article>
    </section>`;

const printQr = product => {
    const popup = window.open('', '_blank', 'width=520,height=620');
    if (!popup) {
        toast('Ventana bloqueada', 'Permite ventanas emergentes para imprimir la etiqueta.', 'error');
        return;
    }
    popup.document.title = `QR ${product.code}`;
    const style = popup.document.createElement('style');
    style.textContent = 'body{margin:0;min-height:100vh;display:grid;place-items:center;font-family:Arial;color:#17202f}.label{text-align:center;border:1px solid #ddd;border-radius:18px;padding:28px;width:320px}.label img{width:280px}.label h1{font-size:20px;margin:12px 0 5px}.label p{font:14px monospace;margin:0;color:#677}.label small{display:block;margin-top:12px;color:#999}@media print{.label{border:0}}';
    const label = popup.document.createElement('div');
    label.className = 'label';
    const image = popup.document.createElement('img');
    image.src = `/api/products/${product.id}/qr`;
    image.alt = `QR ${product.code}`;
    const name = popup.document.createElement('h1');
    name.textContent = product.name;
    const code = popup.document.createElement('p');
    code.textContent = product.code;
    const brand = popup.document.createElement('small');
    brand.textContent = 'QR Stock · Inventario inteligente';
    label.append(image, name, code, brand);
    popup.document.head.append(style);
    popup.document.body.append(label);
    image.addEventListener('load', () => { popup.focus(); popup.print(); }, { once: true });
};

export const bindQr = ctx => {
    const search = document.querySelector('#qr-search');
    search?.addEventListener('input', () => {
        const query = format.compact(search.value);
        let visible = 0;
        document.querySelectorAll('[data-qr-card]').forEach(card => {
            card.hidden = Boolean(query) && !card.dataset.search.includes(query);
            if (!card.hidden) visible += 1;
        });
        document.querySelector('#qr-result-count').textContent = `${visible} código${visible === 1 ? '' : 's'} disponible${visible === 1 ? '' : 's'}`;
        document.querySelector('#qr-no-results').hidden = visible !== 0;
    });
    document.querySelectorAll('[data-print-qr]').forEach(button => button.addEventListener('click', () => {
        const product = ctx.state.products.find(item => item.id === Number(button.dataset.printQr));
        if (product) printQr(product);
    }));

    const fileInput = document.querySelector('#qr-file');
    const zone = document.querySelector('#scanner-zone');
    const result = document.querySelector('#scan-result');
    const scan = async file => {
        if (!file) return;
        result.className = 'scan-result visible';
        result.innerHTML = '<strong>Leyendo código…</strong><p>Estamos validando la etiqueta con el inventario.</p>';
        try {
            const product = await api.readQr(file);
            result.innerHTML = `<strong>${escapeHtml(product.name)}</strong><p>${escapeHtml(product.code)} · ${product.stock} unidades disponibles<br>${escapeHtml(product.category)}</p>`;
            toast('Código QR reconocido', `${product.name} fue encontrado en el inventario.`);
        } catch (error) {
            result.innerHTML = `<strong>No se pudo leer</strong><p>${escapeHtml(error.message)}</p>`;
            toast('Lectura sin resultado', error.message, 'error');
        }
    };
    fileInput?.addEventListener('change', () => scan(fileInput.files[0]));
    ['dragenter', 'dragover'].forEach(eventName => zone?.addEventListener(eventName, event => { event.preventDefault(); zone.classList.add('dragging'); }));
    ['dragleave', 'drop'].forEach(eventName => zone?.addEventListener(eventName, event => { event.preventDefault(); zone.classList.remove('dragging'); }));
    zone?.addEventListener('drop', event => scan(event.dataTransfer.files[0]));
};
