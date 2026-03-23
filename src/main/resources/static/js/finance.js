/**
 * finance.js – Finance module: transactions page & reports page.
 */

// ================================================================
// TRANSACTIONS PAGE
// ================================================================

let _allTx = [];
let _txPage = 1;
const TX_PER_PAGE = 10;

document.addEventListener('DOMContentLoaded', async () => {
  if (typeof Auth !== 'undefined') Auth.requireAuth();

  // Transactions page
  if (document.getElementById('tx-table-body')) {
    await loadTransactions();
    bindTransactionFilters();
    bindAddTransaction();
  }

  // Reports page
  if (document.getElementById('report-summary-body')) {
    await loadReports();
  }
});

// ── Load & render transactions ────────────────────────────────────
async function loadTransactions() {
  try {
    _allTx = await API.get('/api/transactions') || [];
  } catch {
    _allTx = _demoTransactions();
  }
  renderTransactions();
  updateBalanceSummary();
}

function renderTransactions() {
  const searchVal = (document.getElementById('tx-search')?.value || '').toLowerCase();
  const typeVal   = document.getElementById('tx-filter-type')?.value  || '';
  const fromVal   = document.getElementById('tx-filter-from')?.value  || '';
  const toVal     = document.getElementById('tx-filter-to')?.value    || '';

  let data = _allTx.filter(t => {
    if (typeVal && t.type !== typeVal) return false;
    if (fromVal && t.date < fromVal)   return false;
    if (toVal   && t.date > toVal)     return false;
    if (searchVal && !(t.description || '').toLowerCase().includes(searchVal) &&
        !(t.type || '').toLowerCase().includes(searchVal)) return false;
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
        <td><span class="badge-custom badge-${(t.type||'').toLowerCase()}">${Utils.escapeHtml(t.type)}</span></td>
        <td>${Utils.formatCurrency(t.amount)}</td>
        <td>${Utils.escapeHtml(t.description || '—')}</td>
        <td>${Utils.escapeHtml(t.member || '—')}</td>
      </tr>`).join('');
  }

  renderPagination(document.getElementById('tx-pagination'), paged.page, paged.pages, p => {
    _txPage = p;
    renderTransactions();
  });
}

function updateBalanceSummary() {
  const savings = _allTx.filter(t => t.type === 'Savings').reduce((s, t) => s + Number(t.amount), 0);
  const loans   = _allTx.filter(t => t.type === 'Loan')   .reduce((s, t) => s + Number(t.amount), 0);
  const expense = _allTx.filter(t => t.type === 'Expense').reduce((s, t) => s + Number(t.amount), 0);

  _setEl('balance-savings', Utils.formatCurrency(savings));
  _setEl('balance-loans',   Utils.formatCurrency(loans));
  _setEl('balance-expense', Utils.formatCurrency(expense));
  _setEl('balance-net',     Utils.formatCurrency(savings - loans - expense));
}

function bindTransactionFilters() {
  ['tx-search','tx-filter-type','tx-filter-from','tx-filter-to'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', () => { _txPage = 1; renderTransactions(); });
  });

  document.getElementById('btn-export-csv')?.addEventListener('click', () => {
    Utils.downloadCSV(_allTx, 'transactions.csv');
  });
}

// ── Add transaction form ──────────────────────────────────────────
function bindAddTransaction() {
  const form = document.getElementById('add-tx-form');
  if (!form) return;

  form.addEventListener('submit', async e => {
    e.preventDefault();
    if (!form.checkValidity()) { form.classList.add('was-validated'); return; }

    const payload = {
      type:        document.getElementById('tx-type').value,
      amount:      document.getElementById('tx-amount').value,
      date:        document.getElementById('tx-date').value,
      description: document.getElementById('tx-desc').value,
      member:      document.getElementById('tx-member')?.value || ''
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

// ── Reports page ──────────────────────────────────────────────────
async function loadReports() {
  let report;
  try {
    report = await API.get('/api/reports/monthly');
  } catch {
    report = _demoReport();
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

  const months   = (report.months || []).map(m => m.month);
  const savings  = (report.months || []).map(m => m.savings);
  const expenses = (report.months || []).map(m => m.expenses);

  new Chart(canvas, {
    type: 'bar',
    data: {
      labels: months,
      datasets: [
        { label: 'Savings',  data: savings,  backgroundColor: '#16a34a' },
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

// ── Demo data ─────────────────────────────────────────────────────
function _demoTransactions() {
  return [
    { date:'2024-03-01', type:'Savings', amount:5000,  description:'Monthly savings', member:'Priya Sharma' },
    { date:'2024-03-05', type:'Loan',    amount:10000, description:'Business loan – Priya', member:'Priya Sharma' },
    { date:'2024-03-10', type:'Expense', amount:1200,  description:'Meeting expenses', member:'Admin' },
    { date:'2024-03-15', type:'Savings', amount:5000,  description:'Monthly savings', member:'Sunita Devi' },
    { date:'2024-02-01', type:'Savings', amount:5000,  description:'Monthly savings', member:'Kavita Patel' },
    { date:'2024-02-20', type:'Loan',    amount:8000,  description:'Medical loan', member:'Rekha Verma' },
    { date:'2024-01-10', type:'Savings', amount:4500,  description:'Monthly savings', member:'Anjali Singh' },
    { date:'2024-01-25', type:'Expense', amount:800,   description:'Office supplies', member:'Admin' }
  ];
}

function _demoReport() {
  return {
    months: [
      { month:'Jan 2024', savings:22000, loans:12000, expenses:3000 },
      { month:'Feb 2024', savings:24000, loans:8000,  expenses:2500 },
      { month:'Mar 2024', savings:25000, loans:10000, expenses:4500 },
      { month:'Apr 2024', savings:23000, loans:6000,  expenses:2000 },
      { month:'May 2024', savings:27000, loans:9000,  expenses:3500 },
      { month:'Jun 2024', savings:26000, loans:11000, expenses:4000 }
    ]
  };
}

function _setEl(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}
