/**
 * dashboard.js – Dashboard page logic.
 * Loads SHG summary, member list, and quick stats widgets.
 */

document.addEventListener('DOMContentLoaded', async () => {
  Auth.requireAuth();

  // Populate header
  document.getElementById('welcome-name').textContent = Auth.displayName();

  // Load dashboard data
  await Promise.all([loadStats(), loadMembers()]);
});

async function loadStats() {
  try {
    const stats = await API.get('/api/dashboard/stats', false);
    if (!stats) return;

    _setText('stat-balance',      Utils.formatCurrency(stats.totalBalance  || 0));
    _setText('stat-members',      stats.memberCount     || 0);
    _setText('stat-loans',        Utils.formatCurrency(stats.totalLoans    || 0));
    _setText('stat-savings',      Utils.formatCurrency(stats.totalSavings  || 0));
    _setText('stat-group-name',   stats.groupName       || 'My SHG Group');
    _setText('stat-group-since',  stats.groupSince      || '—');
  } catch (err) {
    console.warn('Stats load failed (demo mode):', err.message);
    // Show demo values so the page isn't empty
    _setText('stat-balance',     Utils.formatCurrency(125000));
    _setText('stat-members',     24);
    _setText('stat-loans',       Utils.formatCurrency(48000));
    _setText('stat-savings',     Utils.formatCurrency(77000));
    _setText('stat-group-name',  'Mahila Shakti SHG');
    _setText('stat-group-since', 'Jan 2019');
  }
}

async function loadMembers() {
  const tbody = document.getElementById('member-table-body');
  if (!tbody) return;

  let members;
  try {
    members = await API.get('/api/members', false);
  } catch {
    members = _demoMembers();
  }

  if (!members || !members.length) {
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

// ── Helpers ──────────────────────────────────────────────────────────
function _setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

function _demoMembers() {
  return [
    { name: 'Priya Sharma',   role: 'President',  savings: 15000, loans: 0,     active: true  },
    { name: 'Sunita Devi',    role: 'Treasurer',  savings: 12000, loans: 5000,  active: true  },
    { name: 'Kavita Patel',   role: 'Member',     savings: 8000,  loans: 10000, active: true  },
    { name: 'Rekha Verma',    role: 'Member',     savings: 9500,  loans: 0,     active: true  },
    { name: 'Anjali Singh',   role: 'Member',     savings: 7200,  loans: 3000,  active: false },
    { name: 'Meera Gupta',    role: 'Member',     savings: 11000, loans: 8000,  active: true  },
    { name: 'Sita Yadav',     role: 'Member',     savings: 6800,  loans: 0,     active: true  },
    { name: 'Lata Mishra',    role: 'Member',     savings: 9000,  loans: 5000,  active: true  }
  ];
}
