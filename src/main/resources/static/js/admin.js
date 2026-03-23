/**
 * admin.js – Admin panel: broker verification, statistics, settings.
 */

document.addEventListener('DOMContentLoaded', async () => {
  if (typeof Auth !== 'undefined') {
    Auth.requireAuth();
    if (!['admin'].includes(Auth.role())) {
      showToast('Access denied. Admins only.', 'error');
      setTimeout(() => { window.location.href = '/dashboard'; }, 1500);
      return;
    }
  }

  if (document.getElementById('brokers-table-body'))  await loadBrokers();
  if (document.getElementById('admin-stats-section')) await loadAdminStats();
  if (document.getElementById('settings-form'))        loadSettings();
});

// ================================================================
// BROKER VERIFICATION
// ================================================================
let _brokers = [];

async function loadBrokers() {
  try {
    _brokers = await API.get('/api/admin/brokers/pending') || [];
  } catch {
    _brokers = _demoBrokers();
  }
  renderBrokers();
}

function renderBrokers() {
  const tbody = document.getElementById('brokers-table-body');
  if (!tbody) return;
  if (!_brokers.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-3">No pending broker applications.</td></tr>';
    return;
  }
  tbody.innerHTML = _brokers.map(b => `
    <tr id="broker-row-${b.id}">
      <td>${Utils.escapeHtml(b.name)}</td>
      <td>${Utils.escapeHtml(b.organisation||'—')}</td>
      <td>${Utils.escapeHtml(b.licenceNo||'—')}</td>
      <td>${Utils.formatDate(b.appliedAt)}</td>
      <td><span class="badge-custom badge-pending">Pending</span></td>
      <td>
        <button class="btn-primary-custom btn-sm me-1" onclick="verifyBroker(${b.id}, 'approve')">
          <i class="bi bi-check-lg"></i> Approve
        </button>
        <button class="btn btn-sm btn-outline-danger" data-bs-toggle="modal"
                data-bs-target="#rejectModal" onclick="setRejectTarget(${b.id})">
          <i class="bi bi-x-lg"></i> Reject
        </button>
        <button class="btn btn-sm btn-outline-secondary ms-1" onclick="showBrokerDetail(${b.id})">
          <i class="bi bi-eye"></i>
        </button>
      </td>
    </tr>`).join('');
}

async function verifyBroker(id, action) {
  const reason = action === 'reject'
    ? document.getElementById('reject-reason')?.value
    : null;
  try {
    await API.put('/api/admin/brokers/' + id + '/' + action, { reason });
    showToast('Broker ' + (action === 'approve' ? 'approved' : 'rejected') + ' successfully.', 'success');
    _brokers = _brokers.filter(b => b.id !== id);
    renderBrokers();
    bootstrap.Modal.getInstance(document.getElementById('rejectModal'))?.hide();
  } catch (err) {
    showToast(err.message || 'Action failed.', 'error');
  }
}

let _rejectTargetId = null;
function setRejectTarget(id) { _rejectTargetId = id; }

document.getElementById('confirm-reject-btn')?.addEventListener('click', () => {
  if (_rejectTargetId !== null) verifyBroker(_rejectTargetId, 'reject');
});

function showBrokerDetail(id) {
  const b = _brokers.find(x => x.id === id);
  if (!b) return;
  const body = document.getElementById('broker-modal-body');
  if (body) {
    body.innerHTML = `
      <dl class="row mb-0">
        <dt class="col-5">Full Name</dt>       <dd class="col-7">${Utils.escapeHtml(b.name)}</dd>
        <dt class="col-5">Organisation</dt>    <dd class="col-7">${Utils.escapeHtml(b.organisation||'—')}</dd>
        <dt class="col-5">Licence No.</dt>     <dd class="col-7">${Utils.escapeHtml(b.licenceNo||'—')}</dd>
        <dt class="col-5">Email</dt>           <dd class="col-7">${Utils.escapeHtml(b.email||'—')}</dd>
        <dt class="col-5">Phone</dt>           <dd class="col-7">${Utils.escapeHtml(b.phone||'—')}</dd>
        <dt class="col-5">Applied On</dt>      <dd class="col-7">${Utils.formatDate(b.appliedAt)}</dd>
        <dt class="col-5">Experience</dt>      <dd class="col-7">${Utils.escapeHtml(b.experience||'—')}</dd>
      </dl>`;
  }
  new bootstrap.Modal(document.getElementById('brokerDetailModal')).show();
}

// ================================================================
// ADMIN STATISTICS
// ================================================================
async function loadAdminStats() {
  let stats;
  try {
    stats = await API.get('/api/admin/statistics');
  } catch {
    stats = _demoAdminStats();
  }

  _setEl('stat-total-users',   stats.totalUsers   || 0);
  _setEl('stat-total-groups',  stats.totalGroups  || 0);
  _setEl('stat-total-tx',      stats.totalTx      || 0);
  _setEl('stat-total-brokers', stats.totalBrokers || 0);

  renderGrowthChart(stats.monthlyGrowth || []);
}

function renderGrowthChart(growth) {
  const canvas = document.getElementById('growth-chart');
  if (!canvas || typeof Chart === 'undefined') return;
  new Chart(canvas, {
    type: 'line',
    data: {
      labels: growth.map(g => g.month),
      datasets: [{
        label: 'New Members',
        data: growth.map(g => g.newMembers),
        fill: true,
        borderColor: '#1a6b3c',
        backgroundColor: 'rgba(26,107,60,.1)',
        tension: .3
      }]
    },
    options: { responsive: true, maintainAspectRatio: false,
                plugins: { legend: { position: 'top' } },
                scales: { y: { beginAtZero: true } } }
  });
}

// ================================================================
// SETTINGS
// ================================================================
function loadSettings() {
  // Load from localStorage or API
  const saved = JSON.parse(localStorage.getItem('shg_settings') || '{}');
  document.getElementById('setting-max-loan')?.setAttribute('value', saved.maxLoan || '50000');
  document.getElementById('setting-min-savings')?.setAttribute('value', saved.minSavings || '500');
  document.getElementById('setting-interest-rate')?.setAttribute('value', saved.interestRate || '12');

  document.getElementById('settings-form')?.addEventListener('submit', async e => {
    e.preventDefault();
    const payload = {
      maxLoan:      document.getElementById('setting-max-loan').value,
      minSavings:   document.getElementById('setting-min-savings').value,
      interestRate: document.getElementById('setting-interest-rate').value
    };
    try {
      await API.post('/api/admin/settings', payload);
    } catch {
      // store locally in demo mode
    }
    localStorage.setItem('shg_settings', JSON.stringify(payload));
    showToast('Settings saved successfully!', 'success');
  });
}

// ── Demo data ─────────────────────────────────────────────────────
function _demoBrokers() {
  return [
    { id:1, name:'Rajesh Kumar',  organisation:'SBI Insurance',    licenceNo:'IRDAI-2024-001', email:'rajesh@sbi.in',  phone:'9876543210', appliedAt:'2024-03-01', experience:'5 years in insurance.' },
    { id:2, name:'Meena Sharma',  organisation:'LIC of India',      licenceNo:'IRDAI-2024-002', email:'meena@lic.in',   phone:'9988776655', appliedAt:'2024-03-08', experience:'8 years in life insurance.' },
    { id:3, name:'Anil Gupta',    organisation:'HDFC Mutual Fund',  licenceNo:'SEBI-2024-045',  email:'anil@hdfc.in',   phone:'9123456780', appliedAt:'2024-03-12', experience:'3 years in mutual funds.' }
  ];
}

function _demoAdminStats() {
  return {
    totalUsers:  156, totalGroups: 12, totalTx: 3420, totalBrokers: 8,
    monthlyGrowth: [
      { month:'Oct', newMembers:8  }, { month:'Nov', newMembers:12 },
      { month:'Dec', newMembers:6  }, { month:'Jan', newMembers:15 },
      { month:'Feb', newMembers:10 }, { month:'Mar', newMembers:18 }
    ]
  };
}

function _setEl(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}
