/**
 * utils.js – Shared utility helpers
 */

const Utils = (() => {

  /** Format a number as Indian Rupee string */
  function formatCurrency(amount) {
    return '₹ ' + Number(amount).toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  /** Format ISO date string to dd-MMM-yyyy */
  function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  /** Debounce helper */
  function debounce(fn, delay = 300) {
    let timer;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => fn(...args), delay);
    };
  }

  /** Get initials from name */
  function initials(name) {
    return (name || '?').split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();
  }

  /** Truncate long strings */
  function truncate(str, max = 80) {
    return str && str.length > max ? str.slice(0, max) + '…' : str;
  }

  /** Escape HTML to prevent XSS */
  function escapeHtml(str) {
    const d = document.createElement('div');
    d.textContent = str;
    return d.innerHTML;
  }

  /** Generate a simple random id */
  function uid() {
    return Math.random().toString(36).slice(2, 9);
  }

  /** Convert table data (array of objects) to CSV and trigger download */
  function downloadCSV(data, filename = 'export.csv') {
    if (!data || !data.length) return;
    const headers = Object.keys(data[0]);
    const rows = data.map(row => headers.map(h => JSON.stringify(row[h] ?? '')).join(','));
    const csv = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }

  /** Simple client-side pagination helper */
  function paginate(data, page, perPage = 10) {
    const start = (page - 1) * perPage;
    return {
      items: data.slice(start, start + perPage),
      total: data.length,
      pages: Math.ceil(data.length / perPage),
      page
    };
  }

  return { formatCurrency, formatDate, debounce, initials, truncate, escapeHtml, uid, downloadCSV, paginate };
})();
