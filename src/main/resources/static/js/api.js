/**
 * api.js – Centralised REST API communication layer.
 * All Fetch calls go through here so that base URL, auth headers,
 * error handling and loading state are managed in one place.
 */

const API = (() => {

  const BASE = '';   // Spring Boot serves on the same origin

  /** Show / hide the full-page spinner */
  function _setLoading(on) {
    const el = document.getElementById('spinner-overlay');
    if (el) el.classList.toggle('active', on);
  }

  /** Build headers including CSRF token if present */
  function _headers(extra = {}) {
    const headers = { 'Content-Type': 'application/json', ...extra };
    // Spring Security CSRF – read from meta tag if present
    const csrf = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    if (csrf && csrfHeader) {
      headers[csrfHeader.content] = csrf.content;
    }
    // JWT token from session storage
    const token = sessionStorage.getItem('shg_token');
    if (token) headers['Authorization'] = 'Bearer ' + token;
    return headers;
  }

  /** Generic request wrapper */
  async function request(method, path, body = null, showLoader = true) {
    if (showLoader) _setLoading(true);
    try {
      const opts = {
        method,
        headers: _headers(),
        credentials: 'same-origin'
      };
      if (body !== null) opts.body = JSON.stringify(body);

      const res = await fetch(BASE + path, opts);

      if (res.status === 401) {
        // Not authenticated – redirect to login
        sessionStorage.clear();
        window.location.href = '/';
        return null;
      }

      const contentType = res.headers.get('Content-Type') || '';
      const data = contentType.includes('application/json') ? await res.json() : await res.text();

      if (!res.ok) {
        const msg = (typeof data === 'object' && data.message) ? data.message : `Error ${res.status}`;
        throw new Error(msg);
      }
      return data;
    } finally {
      if (showLoader) _setLoading(false);
    }
  }

  const get    = (path, showLoader)       => request('GET',    path, null, showLoader);
  const post   = (path, body, showLoader) => request('POST',   path, body, showLoader);
  const put    = (path, body, showLoader) => request('PUT',    path, body, showLoader);
  const del    = (path, showLoader)       => request('DELETE', path, null, showLoader);

  return { get, post, put, del };
})();
