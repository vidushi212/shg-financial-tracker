/**
 * dashboard.js - Dashboard page logic.
 * Loads SHG summary, member list, and quick stats widgets from the database.
 */

document.addEventListener('DOMContentLoaded', async () => {
  Auth.requireAuth();

  document.getElementById('welcome-name').textContent = Auth.displayName();
  const role = Auth.role();
  const roleText = role ? Utils.titleCase(role) : 'Member';
  const roleWelcomeEl = document.getElementById('role-welcome-text');
  if (roleWelcomeEl) {
    if (role === 'government officer') {
      roleWelcomeEl.textContent = 'Your Government Officer dashboard focuses on advisory and schemes; finance transaction widgets are hidden for your role.';
    } else {
      roleWelcomeEl.textContent = `You are signed in as ${roleText}.`;
    }
  }

  await Promise.all([loadStats(), loadMembers()]);
});

async function loadStats() {
  try {
    const stats = await API.get('/api/dashboard/stats', false);
    if (!stats) throw new Error('Unable to load dashboard statistics.');

    _setText('stat-balance', Utils.formatCurrency(stats.totalBalance || 0));
    _setText('stat-members', stats.memberCount || 0);
    _setText('stat-loans', Utils.formatCurrency(stats.totalLoans || 0));
    _setText('stat-savings', Utils.formatCurrency(stats.totalSavings || 0));
    _setText('stat-group-name', stats.groupName || 'My SHG Group');
    _setText('stat-group-since', stats.groupSince || '-');
  } catch (err) {
    console.warn('Stats load failed:', err.message);
    showToast(err.message || 'Unable to load dashboard statistics.', 'error');
    _setText('stat-balance', Utils.formatCurrency(0));
    _setText('stat-members', 0);
    _setText('stat-loans', Utils.formatCurrency(0));
    _setText('stat-savings', Utils.formatCurrency(0));
    _setText('stat-group-name', 'Database data unavailable');
    _setText('stat-group-since', '-');
  }
}

async function loadMembers() {
  const tbody = document.getElementById('member-table-body');
  if (!tbody) return;

  let members = [];
  try {
    members = await API.get('/api/members', false) || [];
  } catch (err) {
    console.warn('Members load failed:', err.message);
    showToast(err.message || 'Unable to load members from the database.', 'error');
  }

  if (!members.length) {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">No members found.</td></tr>';
    return;
  }

  tbody.innerHTML = members.slice(0, 8).map(m => `
    <tr>
      <td><div class="d-flex align-items-center gap-2">
        <div class="avatar">${Utils.initials(m.name)}</div>
        <span>${Utils.escapeHtml(m.name)}</span>
      </div></td>
      <td><span class="badge-custom badge-savings">${Utils.escapeHtml(m.role || 'Member')}</span></td>
      <td>${Utils.formatCurrency(m.savings || 0)}</td>
      <td>${Utils.formatCurrency(m.loans || 0)}</td>
      <td><span class="badge-custom ${m.active ? 'badge-approved' : 'badge-rejected'}">${m.active ? 'Active' : 'Inactive'}</span></td>
    </tr>`).join('');
}

function _setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}
