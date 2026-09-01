const moneyFormatter = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' });
const numberFormatter = new Intl.NumberFormat('es-PE');
const dateFormatter = new Intl.DateTimeFormat('es-PE', { day: '2-digit', month: 'short', year: 'numeric' });
const dateTimeFormatter = new Intl.DateTimeFormat('es-PE', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });

const toDate = value => value ? new Date(value) : null;

export const format = {
    money: value => moneyFormatter.format(Number(value || 0)),
    number: value => numberFormatter.format(Number(value || 0)),
    date: value => {
        const date = toDate(value);
        return date && !Number.isNaN(date.getTime()) ? dateFormatter.format(date) : '—';
    },
    dateTime: value => {
        const date = toDate(value);
        return date && !Number.isNaN(date.getTime()) ? dateTimeFormatter.format(date) : '—';
    },
    dateInput: value => {
        const date = toDate(value);
        if (!date || Number.isNaN(date.getTime())) return '';
        const offset = date.getTimezoneOffset();
        return new Date(date.getTime() - offset * 60_000).toISOString().slice(0, 10);
    },
    initials: value => String(value || '?').trim().split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase(),
    compact: value => String(value || '').trim().toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, ''),
    title: value => String(value || '').toLowerCase().replace(/(^|\s)\S/g, letter => letter.toUpperCase())
};

export const downloadCsv = (filename, rows) => {
    const content = rows.map(row => row.map(cell => `"${String(cell ?? '').replaceAll('"', '""')}"`).join(',')).join('\r\n');
    const blob = new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
};
