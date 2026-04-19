/**
 * finance.js - Finance module: transactions page and reports page.
 */

let _allTx = [];
let _txPage = 1;
const TX_PER_PAGE = 10;

document.addEventListener('DOMContentLoaded', async () => {
  if (typeof Auth !== 'undefined') Auth.requireAuth();

  if (document.getElementById('tx-table-body')) {
    await loadTransactions();
    bindTransactionFilters();
    bindAddTransaction();
  }

  if (document.getElementById('report-summary-body')) {
    await loadReports();
  }
});

async function loadTransactions() {
  try {
    _allTx = await API.get('/api/transactions') || [];
  } catch (err) {
    _allTx = [];
    showToast(err.message || 'Unable to load transactions from the database.', 'error');
  }
  renderTransactions();
  updateBalanceSummary();
}

function renderTransactions() {
  const searchVal = (document.getElementById('tx-search')?.value || '').toLowerCase();
  const typeVal = document.getElementById('tx-filter-type')?.value || '';
  const fromVal = document.getElementById('tx-filter-from')?.value || '';
  const toVal = document.getElementById('tx-filter-to')?.value || '';

  const data = _allTx.filter(t => {
    if (typeVal && t.type !== typeVal) return false;
    if (fromVal && t.date < fromVal) return false;
    if (toVal && t.date > toVal) return false;
    if (searchVal
      && !(t.description || '').toLowerCase().includes(searchVal)
      && !(t.type || '').toLowerCase().includes(searchVal)) {
      return false;
    }
    return true;
  });

  const paged = Utils.paginate(data, _txPage, TX_PER_PAGE);
  const tbody = document.getElementById('tx-table-body');
  if (!tbody) return;

  if (!paged.items.length) {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-3">No transactions found.</td></tr>';
  } else {
    tbody.innerHTML = paged.items.map(t => `
      <tr>
        <td>${Utils.formatDate(t.date)}</td>
        <td><span class="badge-custom badge-${(t.type || '').toLowerCase()}">${Utils.escapeHtml(t.type)}</span></td>
        <td>${Utils.formatCurrency(t.amount)}</td>
        <td>${Utils.escapeHtml(t.description || '-')}</td>
        <td>${Utils.escapeHtml(t.member || '-')}</td>
      </tr>`).join('');
  }

  renderPagination(document.getElementById('tx-pagination'), paged.page, paged.pages, p => {
    _txPage = p;
    renderTransactions();
  });
}

function updateBalanceSummary() {
  const savings = _allTx.filter(t => t.type === 'Savings').reduce((s, t) => s + Number(t.amount), 0);
  const loans = _allTx.filter(t => t.type === 'Loan').reduce((s, t) => s + Number(t.amount), 0);
  const repayment = _allTx.filter(t => t.type === 'Repayment').reduce((s, t) => s + Number(t.amount), 0);
  const expense = _allTx.filter(t => t.type === 'Expense').reduce((s, t) => s + Number(t.amount), 0);

  _setEl('balance-savings', Utils.formatCurrency(savings));
  _setEl('balance-loans', Utils.formatCurrency(loans - repayment));
  _setEl('balance-expense', Utils.formatCurrency(expense));
  _setEl('balance-net', Utils.formatCurrency(savings + repayment - loans - expense));
}

function bindTransactionFilters() {
  ['tx-search', 'tx-filter-type', 'tx-filter-from', 'tx-filter-to'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', () => {
      _txPage = 1;
      renderTransactions();
    });
  });

  document.getElementById('btn-export-csv')?.addEventListener('click', () => {
    Utils.downloadCSV(_allTx, 'transactions.csv');
  });
}

function bindAddTransaction() {
  const form = document.getElementById('add-tx-form');
  if (!form) return;

  form.addEventListener('submit', async e => {
    e.preventDefault();
    if (!form.checkValidity()) {
      form.classList.add('was-validated');
      return;
    }

    const payload = {
      type: document.getElementById('tx-type').value,
      amount: document.getElementById('tx-amount').value,
      date: document.getElementById('tx-date').value,
      description: document.getElementById('tx-desc').value,
      member: document.getElementById('tx-member')?.value || ''
    };

    try {
      await API.post('/api/transactions', payload);
      showToast('Transaction recorded successfully!', 'success');
      form.reset();
      form.classList.remove('was-validated');
      bootstrap.Modal.getInstance(document.getElementById('addTxModal'))?.hide();
      await loadTransactions();
    } catch (err) {
      showToast(err.message || 'Failed to save transaction.', 'error');
    }
  });
}

async function loadReports() {
  let report;
  try {
    report = await API.get('/api/reports/monthly');
  } catch (err) {
    showToast(err.message || 'Unable to load reports from the database.', 'error');
    report = { months: [] };
  }
  renderReportTable(report);
  renderReportChart(report);
}

function renderReportTable(report) {
  const tbody = document.getElementById('report-summary-body');
  if (!tbody || !report) return;
  tbody.innerHTML = (report.months || []).map(m => `
    <tr>
      <td>${Utils.escapeHtml(m.month)}</td>
      <td>${Utils.formatCurrency(m.savings)}</td>
      <td>${Utils.formatCurrency(m.loans)}</td>
      <td>${Utils.formatCurrency(m.expenses)}</td>
      <td>${Utils.formatCurrency(m.savings - m.loans - m.expenses)}</td>
    </tr>`).join('');
}

function renderReportChart(report) {
  const canvas = document.getElementById('report-chart');
  if (!canvas || !report || typeof Chart === 'undefined') return;

  const months = (report.months || []).map(m => m.month);
  const savings = (report.months || []).map(m => m.savings);
  const expenses = (report.months || []).map(m => m.expenses);

  new Chart(canvas, {
    type: 'bar',
    data: {
      labels: months,
      datasets: [
        { label: 'Savings', data: savings, backgroundColor: '#16a34a' },
        { label: 'Expenses', data: expenses, backgroundColor: '#dc2626' }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: 'top' } },
      scales: { y: { beginAtZero: true } }
    }
  });
}

function _setEl(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}
