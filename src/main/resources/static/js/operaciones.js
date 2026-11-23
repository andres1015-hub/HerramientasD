/* HerramientasD: vistas operativas con los contratos existentes. Sin HTML de la API. */
(function () {
    'use strict';
    var serial = 0;
    var money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' });
    function el(tag, text, cls) {
        var n = document.createElement(tag);
        if (text !== undefined && text !== null) n.textContent = text;
        if (cls) n.className = cls;
        return n;
    }
    function button(text, action, primary) {
        var b = el('button', text, 'button ' + (primary ? 'button-primary' : 'button-secondary'));
        b.type = 'button'; b.addEventListener('click', action); return b;
    }
    function badge(text) {
        var good = ['ACTIVO', 'Recibido', 'Activo', 'ENTRADA', 'DEVOLUCION'].includes(text);
        return el('span', text, 'status-badge ' + (good ? 'status-success' : ['Cancelado', 'ELIMINADO', 'Inactivo'].includes(text) ? 'status-danger' : 'status-info'));
    }
    function date(value) {
        if (!value) return '—';
        var d = new Date(value);
        return Number.isNaN(d.getTime()) ? '—' : d.toLocaleString('es-PE', { dateStyle: 'medium', timeStyle: 'short' });
    }
    async function request(url, method, body) {
        if (window.HD_DEMO) return window.HD_DEMO(url, method || 'GET');
        var headers = { Accept: 'application/json' };
        if (method && method !== 'GET') {
            var token = document.querySelector('meta[name="_csrf"]');
            var header = document.querySelector('meta[name="_csrf_header"]');
            if (!token || !header || !token.content) throw new Error('Recarga la página para renovar la sesión.');
            headers[header.content] = token.content; headers['Content-Type'] = 'application/json';
        }
        var response = await fetch(url, { method: method || 'GET', headers: headers,
            credentials: 'same-origin', body: body === undefined ? undefined : JSON.stringify(body) });
        if (response.status === 204) return null;
        var json = (response.headers.get('content-type') || '').includes('application/json');
        var data = json ? await response.json() : null;
        if (response.redirected || response.status === 401) throw new Error('Tu sesión ha terminado. Inicia sesión de nuevo.');
        if (!response.ok) throw new Error((data && data.mensaje) || (response.status === 403 ? 'No tienes permiso para esta operación.' : 'No se pudo completar la operación.'));
        if (!json) throw new Error('La respuesta no es válida. Recarga la página o inicia sesión de nuevo.');
        return data;
    }
    function field(spec, value) {
        var wrap = el('div', null, 'field'); var id = 'operacion-' + (++serial);
        var label = el('label', spec.label); label.htmlFor = id;
        var input = el(spec.options ? 'select' : 'input'); input.id = id; input.name = spec.name;
        if (spec.options) spec.options.forEach(function (o) {
            var option = el('option', Array.isArray(o) ? o[1] : o);
            option.value = Array.isArray(o) ? o[0] : o; input.append(option);
        });
        else input.type = spec.type || 'text';
        ['min', 'max', 'step', 'minLength', 'maxLength', 'pattern', 'autocomplete'].forEach(function (key) { if (spec[key] !== undefined) input[key] = spec[key]; });
        if (spec.required) input.required = true;
        if (spec.readOnly) input.readOnly = true;
        if (value !== undefined && value !== null) input.value = value;
        else if (spec.value !== undefined) input.value = spec.value;
        else if (!spec.options) input.value = '';
        wrap.append(label, input); return wrap;
    }
    function read(form, specs) {
        var data = {};
        specs.forEach(function (s) { var v = form.elements[s.name].value.trim(); data[s.name] = s.number ? Number(v) : v; });
        return data;
    }
    function table(columns, rows) {
        var wrap = el('div', null, 'table-scroll'); wrap.tabIndex = 0; wrap.setAttribute('role', 'region'); wrap.setAttribute('aria-label', columns.map(function (c) { return c[0]; }).join(', '));
        var t = el('table', null, 'ops-table'); t.setAttribute('role', 'table'); var head = el('thead'); head.setAttribute('role', 'rowgroup'); var tr = el('tr'); tr.setAttribute('role', 'row');
        columns.forEach(function (c) { var th = el('th', c[0]); th.scope = 'col'; tr.append(th); }); head.append(tr); t.append(head);
        var body = el('tbody'); body.setAttribute('role', 'rowgroup');
        rows.forEach(function (r) { var row = el('tr'); row.setAttribute('role', 'row'); columns.forEach(function (c) {
            var td = el('td'); td.setAttribute('role', 'cell'); td.dataset.label = c[0]; var value = typeof c[1] === 'function' ? c[1](r) : r[c[1]];
            if (value instanceof Node) td.append(value); else td.textContent = value === null || value === undefined || value === '' ? '—' : value;
            row.append(td);
        }); body.append(row); });
        t.append(body); wrap.append(t); return wrap;
    }
    function dialog(title, specs, values, save, description) {
        var d = el('dialog', null, 'ops-dialog'); var form = el('form', null, 'ops-form');
        var h = el('h2', title); h.id = 'dialog-' + (++serial); d.setAttribute('aria-labelledby', h.id);
        var close = button('Cerrar', function () { d.close(); }); close.classList.add('dialog-close');
        form.append(close, h);
        if (description) form.append(el('p', description, 'field-help'));
        specs.forEach(function (s) { form.append(field(s, (values || {})[s.name])); });
        var msg = el('p', '', 'message message-error'); msg.hidden = true; msg.setAttribute('role', 'alert');
        var submit = el('button', 'Confirmar', 'button button-primary'); submit.type = 'submit';
        if (window.HD_DEMO) { submit.disabled = true; submit.textContent = 'Solo demostración · sin guardar'; }
        form.append(msg, submit); d.append(form); document.body.append(d);
        form.addEventListener('submit', async function (e) {
            e.preventDefault(); msg.hidden = true;
            if (!form.reportValidity()) return;
            submit.disabled = true; submit.textContent = 'Guardando…';
            try { await save(read(form, specs)); d.close(); }
            catch (err) { msg.textContent = err.message; msg.hidden = false; }
            finally { submit.disabled = false; submit.textContent = 'Confirmar'; }
        });
        d.addEventListener('close', function () { d.remove(); }); d.showModal();
        return form;
    }
    function actions(items) { var wrap = el('div', null, 'ops-actions'); items.filter(Boolean).forEach(function (i) { wrap.append(button(i[0], i[1])); }); return wrap; }
    function download(label, endpoint, params) {
        var a = el('a', label, 'button button-secondary'); a.href = endpoint + (params && params.toString() ? '?' + params : '');
        if (window.HD_DEMO) { a.removeAttribute('href'); a.setAttribute('aria-disabled', 'true'); a.title = 'Descarga disponible en la aplicación'; }
        else a.addEventListener('click', async function (event) {
            event.preventDefault(); if (a.getAttribute('aria-busy')) return;
            a.setAttribute('aria-busy', 'true');
            try {
                var response = await fetch(a.href, { credentials: 'same-origin', headers: { Accept: 'text/csv' } });
                if (!response.ok || !(response.headers.get('content-type') || '').includes('text/csv')) throw new Error('No se pudo descargar el archivo. Revisa tu sesión y permisos.');
                var url = URL.createObjectURL(await response.blob()); var link = el('a'); link.href = url; link.download = endpoint.split('/').pop(); document.body.append(link); link.click(); link.remove(); setTimeout(function () { URL.revokeObjectURL(url); }, 1000);
            } catch (err) { notice(a.closest('section') || document.querySelector('main'), err.message, true); }
            finally { a.removeAttribute('aria-busy'); }
        });
        return a;
    }
    function notice(root, text, bad) {
        var n = root.querySelector('.ops-notice');
        if (!n) { n = el('p'); root.prepend(n); }
        n.className = 'ops-notice message ' + (bad ? 'message-error' : 'message-success');
        n.setAttribute('role', bad ? 'alert' : 'status'); n.textContent = text;
    }
    function list(root, config) {
        var form = el('form', null, 'ops-filters');
        var initial = new URLSearchParams(location.search);
        config.filters.forEach(function (s) { form.append(field(s, initial.get(s.name) === null ? undefined : initial.get(s.name))); });
        if (initial.get('stock') === 'bajo' && form.elements.stockBajo) form.elements.stockBajo.value = 'true';
        var apply = el('button', 'Aplicar filtros', 'button button-primary'); apply.type = 'submit';
        var page = 0, generation = 0, totalPages = 0;
        var clear = button('Limpiar', function () { form.reset(); page = 0; load(); });
        form.append(apply, clear);
        var tools = el('div', null, 'ops-actions');
        var status = el('p', '', 'field-help'); status.setAttribute('role', 'status');
        var content = el('div'); var pager = el('nav', null, 'ops-pagination'); pager.setAttribute('aria-label', 'Paginación de ' + config.title);
        var info = el('span');
        var prev = button('← Anterior', function () { page--; load(true); });
        var next = button('Siguiente →', function () { page++; load(true); }); pager.append(prev, info, next);
        root.append(form, tools, status, content, pager);
        form.addEventListener('submit', function (e) { e.preventDefault(); if (!form.reportValidity()) return; page = 0; load(); });
        async function load(focus) {
            var gen = ++generation;
            var values = read(form, config.filters), params = new URLSearchParams();
            if (values.desde && values.hasta && values.desde > values.hasta) { status.textContent = 'La fecha inicial debe ser anterior a la final.'; return; }
            Object.keys(values).forEach(function (k) { if (values[k] !== '') params.set(k, values[k]); });
            var exportParams = new URLSearchParams(params);
            params.set('pagina', page); params.set('tamanio', '10');
            root.setAttribute('aria-busy', 'true'); status.textContent = 'Cargando…'; prev.disabled = next.disabled = true;
            try {
                var data = await request(config.endpoint + '?' + params);
                if (gen !== generation) return;
                totalPages = data.totalPaginas;
                if (page > 0 && page >= totalPages) { page = Math.max(0, totalPages - 1); return load(); }
                content.replaceChildren(); tools.replaceChildren();
                if (config.export) tools.append(download('Exportar CSV', config.export, exportParams));
                if (data.contenido.length) content.append(config.render(data.contenido, load));
                else { var empty = el('div', null, 'empty-state compact-empty'); empty.append(el('h3', 'No hay resultados'), el('p', 'Cambia los filtros o registra tu primera operación.')); content.append(empty); }
                status.textContent = data.totalElementos + ' resultados · ' + config.title;
                info.textContent = totalPages ? 'Página ' + (page + 1) + ' de ' + totalPages : 'Sin páginas';
                prev.disabled = page === 0; next.disabled = data.ultima || !totalPages;
                if (focus) { content.tabIndex = -1; content.focus(); }
            } catch (err) {
                if (gen !== generation) return;
                status.textContent = err.message; content.replaceChildren(button('Reintentar', function () { load(); })); info.textContent = '';
            } finally { if (gen === generation) root.removeAttribute('aria-busy'); }
        }
        load(); return { reload: load, form: form };
    }
    var range = [{ name: 'desde', label: 'Desde', type: 'date' }, { name: 'hasta', label: 'Hasta', type: 'date' }];
    var roles = ['USER', 'PROVEEDOR', 'ADMINISTRADOR'];
    var types = ['', 'INICIAL', 'ENTRADA', 'SALIDA', 'AJUSTE', 'PAQUETE', 'DEVOLUCION'];
    var productFields = [{ name: 'nombre', label: 'Nombre', required: true, maxLength: 50 }, { name: 'tipo', label: 'Tipo', required: true, maxLength: 30 }, { name: 'precio', label: 'Precio (S/)', type: 'number', min: 0.01, step: '0.01', required: true, number: true }, { name: 'stockMinimo', label: 'Stock mínimo', type: 'number', min: 0, max: 1000000, step: 1, required: true, number: true }];
    var personFields = [{ name: 'nombre', label: 'Nombre', required: true, maxLength: 30 }, { name: 'apellido', label: 'Apellido', required: true, maxLength: 30 }];
    function movements(root, productId) {
        var filters = (productId ? [] : [{ name: 'idProducto', label: 'ID del producto', type: 'number', min: 1 }]).concat([{ name: 'tipo', label: 'Movimiento', options: types }], range);
        list(root, { title: 'movimientos de stock', endpoint: productId ? '/api/productos/' + productId + '/movimientos' : '/api/movimientos', filters: filters,
            export: productId ? null : '/api/reportes/movimientos.csv', render: function (rows) { return table([
                ['Producto', 'nombreProducto'], ['Tipo', function (r) { return badge(r.tipo); }], ['Cantidad', 'cantidad'], ['Anterior', 'stockAnterior'], ['Nuevo', 'stockNuevo'], ['Referencia', 'referencia'], ['Usuario', 'usuarioDni'], ['Fecha', function (r) { return date(r.fecha); }]
            ], rows); } });
    }
    function inventory(root) {
        var admin = root.dataset.admin === 'true';
        var selected = new Set();
        var typeOptions = [['', 'Todos los tipos']];
        var initialType = new URLSearchParams(location.search).get('tipo');
        if (initialType) typeOptions.push(initialType);
        var filters = [{ name: 'nombre', label: 'Buscar por nombre', maxLength: 50 }, { name: 'tipo', label: 'Tipo', options: typeOptions }];
        if (admin) filters.push({ name: 'activo', label: 'Disponibilidad', options: [['', 'Todos'], ['true', 'Activos'], ['false', 'Inactivos']] });
        filters.push({ name: 'stockBajo', label: 'Existencias', options: [['false', 'Todas'], ['true', 'Bajo el mínimo']] });
        var state = list(root, { title: 'productos', endpoint: '/api/productos/paginado', filters: filters, render: function (rows, reload) {
            var grid = el('div', null, 'ops-product-grid');
            var selection = el('p', selected.size + ' productos seleccionados. Selección temporal; no modifica el inventario.', 'field-help'); selection.setAttribute('role', 'status');
            rows.forEach(function (p) {
                var card = el('article', null, 'panel ops-product');
                card.append(el('p', 'ART. ' + p.idProducto + ' / ' + p.tipo, 'eyebrow'), el('h3', p.nombre), el('p', money.format(p.precio), 'ops-price'), badge(p.activo ? 'Activo' : 'Inactivo'));
                card.append(el('p', p.stock + ' disponibles · Mínimo ' + p.stockMinimo, 'field-help'));
                if (p.stock <= p.stockMinimo) card.append(badge(p.stock === 0 ? 'Agotado' : 'Reponer existencias'));
                if (p.activo && p.stock > 0) {
                    var label = el('label', null, 'field-help'); var check = el('input'); check.type = 'checkbox'; check.checked = selected.has(p.idProducto);
                    check.addEventListener('change', function () { if (check.checked) selected.add(p.idProducto); else selected.delete(p.idProducto); selection.textContent = selected.size + ' productos seleccionados. Selección temporal; no modifica el inventario.'; });
                    label.append(check, document.createTextNode(' Seleccionar ' + p.nombre)); card.append(label);
                }
                if (admin) card.append(actions([
                    ['Editar', async function () { try { var current = await request('/api/productos/' + p.idProducto); dialog('Editar ' + p.nombre, productFields, current, async function (data) { await request('/api/productos/' + p.idProducto, 'PUT', data); notice(root, 'Producto actualizado.'); reload(); }, 'Las existencias se modifican desde movimientos.'); } catch (e) { notice(root, e.message, true); } }],
                    [p.activo ? 'Desactivar' : 'Activar', function () { dialog((p.activo ? 'Desactivar ' : 'Activar ') + p.nombre, [], {}, async function () { await request('/api/productos/' + p.idProducto + '/activo', 'PATCH', { activo: !p.activo }); notice(root, 'Disponibilidad actualizada.'); reload(); }, 'Se conserva el historial del producto.'); }],
                    ['Historial', function () { var form = dialog('Movimientos · ' + p.nombre, [], {}, function () {}); form.querySelector('[type="submit"]').remove(); var host = el('section'); form.parentElement.append(host); movements(host, p.idProducto); }]
                ]));
                grid.append(card);
            }); var wrap = el('div'); wrap.append(grid, selection, button('Limpiar selección', function () { selected.clear(); reload(); })); return wrap;
        } });
        request('/api/productos/tipos').then(function (data) { data.forEach(function (t) { if (t === initialType) return; var o = el('option', t); o.value = t; state.form.elements.tipo.append(o); }); }).catch(function (e) { notice(root, e.message, true); });
        if (admin) request('/api/productos/alertas').then(function (data) { notice(root, data.length + ' productos requieren reposición según su mínimo.'); }).catch(function (e) { notice(root, e.message, true); });
    }
    function users(root) {
        var listRoot = el('div');
        var roleField = { name: 'rol', label: 'Rol', options: roles, required: true };
        root.append(button('Registrar usuario +', function () {
            dialog('Registrar usuario', personFields.concat([{ name: 'dni', label: 'DNI (8 dígitos)', required: true, pattern: '[0-9]{8}', maxLength: 8 }, { name: 'password', label: 'Contraseña inicial', type: 'password', minLength: 8, maxLength: 100, required: true, autocomplete: 'new-password' }, roleField]), {}, async function (data) {
                await request('/api/usuarios', 'POST', data); notice(root, 'Usuario registrado.'); state.reload();
            });
        }, true), listRoot);
        var state = list(listRoot, { title: 'usuarios', endpoint: '/api/usuarios/pagina', filters: [{ name: 'busqueda', label: 'Nombre, apellido o DNI' }, { name: 'rol', label: 'Rol', options: [['', 'Todos los roles']].concat(roles) }, { name: 'estado', label: 'Estado', options: [['', 'Todos los estados'], 'ACTIVO', 'INNACTIVO', 'ELIMINADO'] }], render: function (rows, reload) {
            return table([['Persona', function (u) { return u.nombre + ' ' + u.apellido; }], ['DNI', 'dni'], ['Rol', 'rol'], ['Estado', function (u) { return badge(u.estado === 'INNACTIVO' ? 'Inactivo' : u.estado); }], ['Acciones', function (u) {
                if (u.estado === 'ELIMINADO') return 'Sin acciones';
                return actions([
                    ['Editar', async function () { try { var current = await request('/api/usuarios/' + u.idUsuario); dialog('Editar usuario', personFields.concat([roleField]), current, async function (data) { await request('/api/usuarios/' + u.idUsuario, 'PUT', data); notice(root, 'Usuario actualizado. Si cambió el rol, debe renovar su sesión.'); reload(); }); } catch (e) { notice(root, e.message, true); } }],
                    ['Restablecer contraseña', function () { dialog('Restablecer contraseña', [{ name: 'passwordNueva', label: 'Nueva contraseña', type: 'password', minLength: 8, maxLength: 100, required: true, autocomplete: 'new-password' }, { name: 'confirmacion', label: 'Repetir nueva contraseña', type: 'password', required: true, autocomplete: 'new-password' }], {}, async function (data) { if (data.passwordNueva !== data.confirmacion) throw new Error('Las contraseñas no coinciden.'); await request('/api/usuarios/' + u.idUsuario + '/password', 'POST', { passwordNueva: data.passwordNueva }); notice(root, 'Contraseña restablecida.'); }, 'Confirma una nueva contraseña para ' + u.nombre + '.'); }],
                    u.rol !== 'ADMINISTRADOR' && u.dni !== root.dataset.dni ? [u.estado === 'ACTIVO' ? 'Desactivar' : 'Activar', function () { dialog('Cambiar acceso de ' + u.nombre, [], {}, async function () { await request('/api/usuarios/' + u.idUsuario + '/estado', 'PATCH', { estado: u.estado === 'ACTIVO' ? 'INNACTIVO' : 'ACTIVO' }); notice(root, 'Acceso actualizado.'); reload(); }); }] : null
                ]);
            }]], rows);
        } });
    }
    function packages(root) {
        var admin = root.dataset.admin === 'true';
        list(root, { title: 'paquetes', endpoint: '/api/paquetes', filters: [{ name: 'pedido', label: 'Número de pedido' }, { name: 'estado', label: 'Estado', options: [['', 'Todos'], 'Enviado', 'Recibido', 'Cancelado'] }].concat(range), export: admin ? '/api/reportes/paquetes.csv' : null, render: function (rows, reload) {
            return table([['Pedido', 'nPedido'], ['Fecha', function (p) { return date(p.fechaCreacion); }], ['Persona', function (p) { return p.nombreUsuario + ' ' + p.apellidoUsuario; }], ['Unidades', 'totalUnidades'], ['Total', function (p) { return money.format(p.total); }], ['Estado', function (p) { return badge(p.estado); }], ['Acciones', function (p) {
                return actions([['Detalle', async function () {
                    try { var data = await request('/api/paquetes/' + p.idPaquete); var form = dialog('Pedido ' + data.nPedido, [], {}, function () {}); form.querySelector('[type="submit"]').remove();
                        form.append(el('p', 'Creado: ' + date(data.fechaCreacion) + ' · Estado: ' + data.estado), el('p', 'Último cambio: ' + date(data.fechaCambioEstado)), el('p', data.motivoCancelacion || ''), table([['Artículo', 'nombreProducto'], ['Cantidad', 'cantidad'], ['Precio', function (d) { return money.format(d.precioUnitario); }], ['QR', function (d) { var a = el('a', 'Ver QR', 'text-link'); a.href = window.HD_DEMO ? 'empaquetador.html' : '/paquete/qr/' + d.idDetalles; return a; }]], data.detalles));
                    } catch (e) { notice(root, e.message, true); }
                }], p.estado === 'Enviado' ? ['Marcar recibido', function () { dialog('Recibir pedido ' + p.nPedido, [], {}, async function () { await request('/api/paquetes/' + p.idPaquete + '/estado', 'PATCH', { estado: 'Recibido' }); notice(root, 'Pedido recibido.'); reload(); }, 'El cambio es definitivo.'); }] : null,
                p.estado === 'Enviado' ? ['Cancelar', function () { dialog('Cancelar pedido ' + p.nPedido, [{ name: 'motivo', label: 'Motivo de cancelación', required: true, minLength: 3, maxLength: 255 }], {}, async function (data) { await request('/api/paquetes/' + p.idPaquete + '/estado', 'PATCH', { estado: 'Cancelado', motivo: data.motivo }); notice(root, 'Pedido cancelado; el servidor ha devuelto las existencias.'); reload(); }, 'Esta acción es definitiva y devuelve las existencias al inventario.'); }] : null]);
            }]], rows);
        } });
    }
    function account(root) {
        root.append(el('h2', 'Datos personales'));
        var form = el('form', null, 'ops-form'); personFields.forEach(function (s) { form.append(field(s)); });
        var identity = el('p', '', 'field-help'); var msg = el('p', 'Cargando tus datos…', 'field-help'); msg.setAttribute('role', 'status');
        var save = el('button', 'Guardar datos', 'button button-primary'); save.type = 'submit'; save.disabled = true;
        form.append(identity, msg, save); root.append(form);
        async function load() { try { var data = await request('/miperfil/datos'); personFields.forEach(function (s) { form.elements[s.name].value = data[s.name]; }); identity.textContent = 'DNI: ' + data.dni + ' · Rol: ' + data.rol + ' (solo lectura)'; msg.textContent = ''; save.disabled = !!window.HD_DEMO; }
            catch (err) { msg.textContent = err.message; root.append(button('Reintentar', load)); } }
        form.addEventListener('submit', async function (e) { e.preventDefault(); if (!form.reportValidity()) return; save.disabled = true; msg.textContent = 'Guardando…'; try { await request('/miperfil/datos', 'PUT', read(form, personFields)); msg.textContent = 'Datos personales actualizados.'; } catch (err) { msg.textContent = err.message; } finally { save.disabled = false; } }); load();
    }
    function dashboard(root) {
        var status = el('p', 'Cargando indicadores…', 'field-help'); status.setAttribute('role', 'status'); root.append(status);
        var body = el('div'); root.append(body);
        async function load() { try { var data = await request('/api/reportes/resumen'); body.replaceChildren(); status.textContent = 'Resumen actual de la operación';
            var grid = el('div', null, 'ops-kpis');
            [['productosActivos', 'Productos activos'], ['productosInactivos', 'Inactivos'], ['productosSinStock', 'Agotados'], ['productosStockBajo', 'Stock bajo'], ['unidadesEnStock', 'Unidades disponibles'], ['valorInventario', 'Valor del inventario'], ['montoPaquetesNoCancelados', 'Monto de paquetes no cancelados']].forEach(function (item) { var card = el('article', null, 'panel'); card.append(el('p', item[1], 'eyebrow'), el('p', /valor|monto/.test(item[0]) ? money.format(data[item[0]]) : data[item[0]], 'ops-value')); grid.append(card); }); body.append(grid);
            var groups = el('div', null, 'ops-summary-grid');
            [['usuariosPorEstado', 'Usuarios por estado'], ['paquetesPorEstado', 'Paquetes por estado']].forEach(function (item) { var box = el('section', null, 'panel'); box.append(el('h3', item[1])); var dl = el('dl', null, 'ops-breakdown'); Object.keys(data[item[0]]).forEach(function (key) { dl.append(el('dt', key), el('dd', data[item[0]][key])); }); box.append(dl); groups.append(box); });
            var top = el('section', null, 'panel'); top.append(el('h3', 'Productos destacados'), data.topProductos.length ? table([['Producto', 'nombre'], ['Unidades', 'unidades']], data.topProductos) : el('p', 'Aún no hay productos destacados.')); groups.append(top); body.append(groups);
            body.append(download('Exportar productos CSV', '/api/reportes/productos.csv'));
        } catch (err) { status.textContent = err.message; body.replaceChildren(button('Reintentar', load)); } } load();
    }
    function audit(root) { list(root, { title: 'eventos de auditoría', endpoint: '/api/auditoria', filters: [{ name: 'accion', label: 'Acción' }, { name: 'entidad', label: 'Entidad' }, { name: 'usuario', label: 'Usuario (DNI)' }].concat(range), render: function (rows) { return table([['Fecha y hora', function (r) { return date(r.fecha); }], ['Usuario', 'usuarioDni'], ['Acción', 'accion'], ['Entidad', 'entidad'], ['ID', 'entidadId'], ['Detalle', 'detalle']], rows); } }); }
    var modules = { inventario: inventory, usuarios: users, historial: packages, movimientos: movements, cuenta: account, reportes: dashboard, auditoria: audit };
    document.querySelectorAll('[data-module]').forEach(function (root) {
        var init = modules[root.dataset.module]; if (init) { root.hidden = false; init(root); }
    });
    document.querySelectorAll('[data-ops-fallback]').forEach(function (n) { n.hidden = true; });
})();
