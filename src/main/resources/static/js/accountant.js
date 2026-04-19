let accountantMembers = [];
let accountantOverview = null;
let accountantReports = null;

document.addEventListener('DOMContentLoaded', async () => {
  if (!Auth.requireRole(['accountant', 'president', 'treasurer'])) {
    showToast('This workspace is limited to accountant-level finance roles.', 'warning');
    return;
  }

  const today = new Date().toISOString().slice(0, 10);
  _setValue('loan-date', today);
  _setValue('repayment-date', today);

  await loadAccountantDesk();
  bindAccountantFilters();
  bindAccountantActions();
});

async function loadAccountantDesk() {
  try {
    const [overview, members, reports] = await Promise.all([
      API.get('/api/accountant/overview'),
      API.get('/api/accountant/member-accounts'),
      API.get('/api/accountant/reports')
    ]);

    accountantOverview = overview;
    accountantMembers = members || [];
    accountantReports = reports || { monthly: [], topSavers: [], topBorrowers: [] };
  } catch (error) {
    accountantOverview = null;
    accountantMembers = [];
    accountantReports = { monthly: [], topSavers: [], topBorrowers: [] };
    showToast(error.message || 'Unable to load accountant data from the database.', 'error');
  }

  renderOverview();
  renderMembers();
  renderReports();
  populateMemberSelects();
}

function renderOverview() {
  const overview = accountantOverview || {
    groupBalance: 0,
    outstandingLoans: 0,
    overdueMembers: 0,
    pendingTransactions: 0,
    portfolioHealth: 'Stable',
    membersWithLoans: 0,
    collectionsThisMonth: 0,
    highRiskMembers: 0,
    alerts: [],
    recentTransactions: []
  };

  _setText('acc-group-balance', Utils.formatCurrency(overview.groupBalance || 0));
  _setText('acc-outstanding', Utils.formatCurrency(overview.outstandingLoans || 0));
  _setText('acc-overdue', overview.overdueMembers || 0);
  _setText('acc-pending', overview.pendingTransactions || 0);
  _setText('portfolio-health', overview.portfolioHealth || 'Stable');
  _setText('loan-members-count', overview.membersWithLoans || 0);
  _setText('loan-collections', Utils.formatCurrency(overview.collectionsThisMonth || 0));
  _setText('loan-high-risk', overview.highRiskMembers || 0);
  _setText('loan-portfolio-health', overview.portfolioHealth || 'Stable');

  const alertsEl = document.getElementById('accountant-alerts');
  const alerts = overview.alerts || [];
  alertsEl.innerHTML = alerts.length ? alerts.map(alert => `
    <div class="activity-item ${alert.severity || 'medium'}">
      <div class="activity-title">${Utils.escapeHtml(alert.title || 'Alert')}</div>
      <div class="activity-meta">${Utils.escapeHtml(alert.detail || '')}</div>
    </div>
  `).join('') : '<div class="text-muted small">No urgent alerts right now.</div>';

  const recentEl = document.getElementById('recent-activity-list');
  const recent = overview.recentTransactions || [];
  recentEl.innerHTML = recent.length ? recent.map(item => `
    <div class="activity-item">
      <div class="d-flex justify-content-between gap-2">
        <div class="activity-title">${Utils.escapeHtml(item.member || 'Member')} - ${Utils.escapeHtml(item.type || '')}</div>
        <strong>${Utils.formatCurrency(item.amount || 0)}</strong>
      </div>
      <div class="activity-meta">${Utils.escapeHtml(item.date || '')} | ${Utils.escapeHtml(item.state || '')}</div>
      <div class="small text-muted">${Utils.escapeHtml(item.description || '')}</div>
    </div>
  `).join('') : '<div class="text-muted small">No recent ledger activity.</div>';
}

function renderMembers() {
  const search = (document.getElementById('account-search')?.value || '').trim().toLowerCase();
  const loanFilter = document.getElementById('account-loan-filter')?.value || '';
  const healthFilter = document.getElementById('account-health-filter')?.value || '';
  const tbody = document.getElementById('accountant-member-body');
  if (!tbody) return;

  const rows = (accountantMembers || []).filter(member => {
    const haystack = `${member.name || ''} ${member.role || ''}`.toLowerCase();
    if (search && !haystack.includes(search)) return false;
    if (loanFilter && member.loanStatus !== loanFilter) return false;
    if (healthFilter && member.health !== healthFilter) return false;
    return true;
  });

  if (!rows.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-3">No matching member accounts found.</td></tr>';
    return;
  }

  tbody.innerHTML = rows.map(member => `
    <tr>
      <td>
        <div class="d-flex align-items-center gap-2">
          <div class="avatar">${Utils.initials(member.name || 'M')}</div>
          <div>
            <div class="fw-semibold">${Utils.escapeHtml(member.name || '')}</div>
            <div class="small text-muted">${Utils.escapeHtml(member.role || '')}</div>
          </div>
        </div>
      </td>
      <td>${Utils.formatCurrency(member.savingsAmount || 0)}</td>
      <td>${Utils.formatCurrency(member.loanAmount || 0)}</td>
      <td>${Utils.formatCurrency(member.netPosition || 0)}</td>
      <td>
        <span class="badge-custom ${loanBadgeClass(member.loanStatus)}">${Utils.escapeHtml(member.loanStatus || '-')}</span>
        <div class="small text-muted mt-1">Health: ${Utils.escapeHtml(member.health || '-')}</div>
      </td>
      <td>
        <div>${Utils.escapeHtml(member.lastTransactionDate || '-')}</div>
        <div class="small text-muted">Repayment: ${Utils.escapeHtml(member.lastRepaymentDate || '-')}</div>
      </td>
    </tr>
  `).join('');
}

function renderReports() {
  const tbody = document.getElementById('accountant-report-body');
  const reports = accountantReports || { monthly: [], topSavers: [], topBorrowers: [] };
  const monthly = reports.monthly || [];
  tbody.innerHTML = monthly.length ? monthly.map(item => `
    <tr>
      <td>${Utils.escapeHtml(item.month || '')}</td>
      <td>${Utils.formatCurrency(item.savings || 0)}</td>
      <td>${Utils.formatCurrency(item.loans || 0)}</td>
      <td>${Utils.formatCurrency(item.repayments || 0)}</td>
      <td>${Utils.formatCurrency(item.expenses || 0)}</td>
    </tr>
  `).join('') : '<tr><td colspan="5" class="text-center text-muted py-3">No report data available.</td></tr>';

  _renderStackList('top-savers-list', reports.topSavers, item =>
    `<div class="activity-item"><div class="d-flex justify-content-between gap-2"><span>${Utils.escapeHtml(item.name || '')}</span><strong>${Utils.formatCurrency(item.savingsAmount || 0)}</strong></div></div>`
  );
  _renderStackList('top-borrowers-list', reports.topBorrowers, item =>
    `<div class="activity-item"><div class="d-flex justify-content-between gap-2"><span>${Utils.escapeHtml(item.name || '')}</span><strong>${Utils.formatCurrency(item.loanAmount || 0)}</strong></div><div class="activity-meta">Health: ${Utils.escapeHtml(item.health || '-')}</div></div>`
  );
}

function populateMemberSelects() {
  const options = ['loan-member', 'repayment-member'].map(id => document.getElementById(id)).filter(Boolean);
  const filteredMembers = (accountantMembers || []).filter(member => (member.role || '').toLowerCase() !== 'admin');
  const html = ['<option value="">Select member...</option>']
    .concat(filteredMembers.map(member => `<option value="${member.id}">${Utils.escapeHtml(member.name)} (${Utils.escapeHtml(member.loanStatus || 'No Active Loan')})</option>`))
    .join('');
  options.forEach(select => {
    select.innerHTML = html;
  });
}

function bindAccountantFilters() {
  ['account-search', 'account-loan-filter', 'account-health-filter'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', renderMembers);
    document.getElementById(id)?.addEventListener('change', renderMembers);
  });
}

function bindAccountantActions() {
  document.getElementById('accountant-export-btn')?.addEventListener('click', () => {
    Utils.downloadCSV(accountantMembers || [], 'accountant-member-accounts.csv');
  });

  document.getElementById('report-export-btn')?.addEventListener('click', () => {
    Utils.downloadCSV((accountantReports && accountantReports.monthly) || [], 'accountant-report-snapshot.csv');
  });

  document.getElementById('loan-form')?.addEventListener('submit', async event => {
    event.preventDefault();
    const form = event.target;
    form.classList.add('was-validated');
    if (!form.checkValidity()) return;

    const payload = {
      memberId: document.getElementById('loan-member').value,
      amount: document.getElementById('loan-amount').value,
      date: document.getElementById('loan-date').value,
      description: document.getElementById('loan-description').value,
      recordedBy: Auth.displayName()
    };

    try {
      await API.post('/api/accountant/loans', payload);
      bootstrap.Modal.getInstance(document.getElementById('loanModal'))?.hide();
      form.reset();
      form.classList.remove('was-validated');
      _setValue('loan-date', new Date().toISOString().slice(0, 10));
      showToast('Loan issued and ledger updated.', 'success');
      await loadAccountantDesk();
    } catch (error) {
      showToast(error.message || 'Unable to issue loan.', 'error');
    }
  });

  document.getElementById('repayment-form')?.addEventListener('submit', async event => {
    event.preventDefault();
    const form = event.target;
    form.classList.add('was-validated');
    if (!form.checkValidity()) return;

    const payload = {
      memberId: document.getElementById('repayment-member').value,
      amount: document.getElementById('repayment-amount').value,
      date: document.getElementById('repayment-date').value,
      description: document.getElementById('repayment-description').value,
      recordedBy: Auth.displayName()
    };

    try {
      await API.post('/api/accountant/repayments', payload);
      bootstrap.Modal.getInstance(document.getElementById('repaymentModal'))?.hide();
      form.reset();
      form.classList.remove('was-validated');
      _setValue('repayment-date', new Date().toISOString().slice(0, 10));
      showToast('Repayment recorded successfully.', 'success');
      await loadAccountantDesk();
    } catch (error) {
      showToast(error.message || 'Unable to record repayment.', 'error');
    }
  });
}

function loanBadgeClass(status) {
  if (status === 'Overdue') return 'badge-rejected';
  if (status === 'Monitoring') return 'badge-pending';
  if (status === 'On Track') return 'badge-approved';
  return 'badge-savings';
}

function _renderStackList(id, items, renderFn) {
  const el = document.getElementById(id);
  if (!el) return;
  el.innerHTML = items && items.length ? items.map(renderFn).join('') : '<div class="text-muted small">No data available.</div>';
}

function _setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

function _setValue(id, value) {
  const el = document.getElementById(id);
  if (el) el.value = value;
}
