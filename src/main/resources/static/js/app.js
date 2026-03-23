/**
 * app.js – Bootstrap global UI elements (toasts, spinner, sidebar
 *           toggle, dark-mode, user profile, active nav links).
 * Loaded on every page.
 */

document.addEventListener('DOMContentLoaded', () => {

  // ── Toast container ──────────────────────────────────────────────────
  if (!document.getElementById('toast-container')) {
    const tc = document.createElement('div');
    tc.id = 'toast-container';
    document.body.appendChild(tc);
  }

  // ── Spinner overlay ──────────────────────────────────────────────────
  if (!document.getElementById('spinner-overlay')) {
    const sp = document.createElement('div');
    sp.id = 'spinner-overlay';
    sp.className = 'spinner-overlay';
    sp.innerHTML = '<div class="spinner-border text-light" role="status"><span class="visually-hidden">Loading…</span></div>';
    document.body.appendChild(sp);
  }

  // ── Sidebar overlay (mobile) ─────────────────────────────────────────
  if (!document.getElementById('sidebar-overlay')) {
    const so = document.createElement('div');
    so.id = 'sidebar-overlay';
    document.body.appendChild(so);
    so.addEventListener('click', closeSidebar);
  }

  // ── Sidebar toggle button (mobile) ──────────────────────────────────
  const toggleBtn = document.getElementById('sidebar-toggle');
  if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
      document.getElementById('sidebar')?.classList.toggle('open');
      document.getElementById('sidebar-overlay')?.classList.toggle('active');
    });
  }

  // ── Populate user profile in navbar ─────────────────────────────────
  const userNameEl  = document.getElementById('nav-user-name');
  const userRoleEl  = document.getElementById('nav-user-role');
  const userInitEl  = document.getElementById('nav-user-initials');
  if (userNameEl && typeof Auth !== 'undefined') {
    userNameEl.textContent  = Auth.displayName();
    if (userRoleEl) userRoleEl.textContent = Auth.role();
    if (userInitEl) userInitEl.textContent = Utils.initials(Auth.displayName());
  }

  // ── Logout buttons ───────────────────────────────────────────────────
  document.querySelectorAll('[data-action="logout"]').forEach(el => {
    el.addEventListener('click', e => {
      e.preventDefault();
      if (typeof Auth !== 'undefined') Auth.logout();
    });
  });

  // ── Dark mode toggle ─────────────────────────────────────────────────
  const saved = localStorage.getItem('shg_theme') || 'light';
  document.documentElement.setAttribute('data-theme', saved);

  document.querySelectorAll('[data-action="toggle-dark"]').forEach(el => {
    el.addEventListener('click', () => {
      const next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      localStorage.setItem('shg_theme', next);
    });
  });

  // ── Mark active nav link ─────────────────────────────────────────────
  const path = window.location.pathname;
  document.querySelectorAll('.sidebar .nav-link').forEach(link => {
    if (link.getAttribute('href') === path) link.classList.add('active');
  });

  // ── Role-based nav visibility ────────────────────────────────────────
  if (typeof Auth !== 'undefined') {
    const userRole = Auth.role();
    document.querySelectorAll('[data-role-requires]').forEach(el => {
      const required = el.dataset.roleRequires.split(',').map(r => r.trim());
      if (!required.includes(userRole)) el.style.display = 'none';
    });
  }
});

// ── Global helpers ────────────────────────────────────────────────────

/** Show a toast notification. type = 'success' | 'error' | 'info' | 'warning' */
function showToast(message, type = 'info', duration = 4000) {
  const icons = { success: 'bi-check-circle-fill', error: 'bi-x-circle-fill',
                  warning: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill' };
  const id = 'toast-' + Date.now();
  const html = `<div id="${id}" class="toast-msg ${type}">
    <i class="bi ${icons[type] || icons.info}" style="flex-shrink:0"></i>
    <span>${Utils.escapeHtml(message)}</span>
    <button type="button" onclick="document.getElementById('${id}').remove()"
            style="margin-left:auto;background:none;border:none;cursor:pointer;font-size:1rem;">
      <i class="bi bi-x"></i>
    </button>
  </div>`;
  const tc = document.getElementById('toast-container');
  if (tc) {
    tc.insertAdjacentHTML('beforeend', html);
    setTimeout(() => document.getElementById(id)?.remove(), duration);
  }
}

/** Close the mobile sidebar */
function closeSidebar() {
  document.getElementById('sidebar')?.classList.remove('open');
  document.getElementById('sidebar-overlay')?.classList.remove('active');
}

/** Render Bootstrap-style pagination buttons into containerEl */
function renderPagination(containerEl, current, total, onPage) {
  if (!containerEl) return;
  containerEl.innerHTML = '';
  if (total <= 1) return;

  const mkBtn = (label, page, active = false, disabled = false) => {
    const b = document.createElement('button');
    b.innerHTML = label;
    b.className = active ? 'active' : '';
    b.disabled = disabled;
    if (!disabled) b.addEventListener('click', () => onPage(page));
    return b;
  };

  const wrapper = document.createElement('div');
  wrapper.className = 'pagination-custom';
  wrapper.appendChild(mkBtn('&laquo;', current - 1, false, current === 1));
  for (let i = 1; i <= total; i++) {
    wrapper.appendChild(mkBtn(i, i, i === current));
  }
  wrapper.appendChild(mkBtn('&raquo;', current + 1, false, current === total));
  containerEl.appendChild(wrapper);
}
