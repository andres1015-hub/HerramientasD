const request = async (url, options = {}) => {
    const config = { ...options, headers: { ...(options.headers || {}) } };
    if (config.body && !(config.body instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json';
        config.body = JSON.stringify(config.body);
    }

    const response = await fetch(url, config);
    if (response.status === 204) return null;

    const contentType = response.headers.get('content-type') || '';
    const payload = contentType.includes('application/json') ? await response.json() : await response.text();
    if (!response.ok) {
        throw new Error(payload?.message || payload || 'No se pudo completar la operación');
    }
    return payload;
};

export const api = {
    dashboard: () => request('/api/dashboard'),
    products: () => request('/api/products'),
    createProduct: product => request('/api/products', { method: 'POST', body: product }),
    updateProduct: (id, product) => request(`/api/products/${id}`, { method: 'PUT', body: product }),
    deleteProduct: id => request(`/api/products/${id}`, { method: 'DELETE' }),
    orders: () => request('/api/orders'),
    createOrder: order => request('/api/orders', { method: 'POST', body: order }),
    updateOrderStatus: (id, status) => request(`/api/orders/${id}/status`, { method: 'PATCH', body: { status } }),
    movements: () => request('/api/movements'),
    createMovement: movement => request('/api/movements', { method: 'POST', body: movement }),
    readQr: file => {
        const formData = new FormData();
        formData.append('file', file);
        return request('/api/qr/read', { method: 'POST', body: formData });
    }
};
