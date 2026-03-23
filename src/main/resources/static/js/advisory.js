/**
 * advisory.js – Advisory module: investment plans, government schemes,
 *               recommendations pages.
 */

document.addEventListener('DOMContentLoaded', async () => {
  if (typeof Auth !== 'undefined') Auth.requireAuth();

  if (document.getElementById('investments-grid')) await loadInvestments();
  if (document.getElementById('schemes-grid'))    await loadSchemes();
  if (document.getElementById('recs-list'))       await loadRecommendations();
});

// ================================================================
// INVESTMENT PLANS
// ================================================================
let _plans = [];
const _planMap = new Map();

async function loadInvestments() {
  try {
    _plans = await API.get('/api/advisory/investment-plans') || [];
  } catch {
    _plans = _demoPlans();
  }
  renderPlans(_plans);
  bindPlanFilters();
}

function renderPlans(plans) {
  const grid = document.getElementById('investments-grid');
  if (!grid) return;
  if (!plans.length) {
    grid.innerHTML = '<div class="col-12 text-center text-muted py-4">No plans found.</div>';
    return;
  }
  plans.forEach((p, i) => _planMap.set(String(i), p));

  grid.innerHTML = plans.map((p, i) => `
    <div class="col-md-4 col-sm-6">
      <div class="plan-card">
        <div class="d-flex justify-content-between align-items-start mb-2">
          <h6 class="fw-bold mb-0">${Utils.escapeHtml(p.name)}</h6>
          <span class="badge-custom badge-${(p.risk||'').toLowerCase()}">${Utils.escapeHtml(p.risk||'—')}</span>
        </div>
        <div class="plan-rate">${Utils.escapeHtml(String(p.returnRate))}%</div>
        <div class="text-muted" style="font-size:.8rem">Annual return</div>
        <hr class="my-2">
        <div class="d-flex justify-content-between small text-muted">
          <span><i class="bi bi-building me-1"></i>${Utils.escapeHtml(p.provider||'—')}</span>
          <span><i class="bi bi-clock me-1"></i>${Utils.escapeHtml(p.tenure||'—')}</span>
        </div>
        <button class="btn-primary-custom w-100 mt-3 plan-detail-btn" data-plan-idx="${i}">
          View Details
        </button>
      </div>
    </div>`).join('');

  grid.querySelectorAll('.plan-detail-btn').forEach(btn => {
    btn.addEventListener('click', () => showPlanDetail(_planMap.get(btn.dataset.planIdx)));
  });
}

function bindPlanFilters() {
  ['plan-filter-type','plan-filter-risk','plan-search'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', filterPlans);
  });
}

function filterPlans() {
  const type   = document.getElementById('plan-filter-type')?.value  || '';
  const risk   = document.getElementById('plan-filter-risk')?.value  || '';
  const search = (document.getElementById('plan-search')?.value || '').toLowerCase();
  const filtered = _plans.filter(p => {
    if (type   && p.type !== type)     return false;
    if (risk   && p.risk !== risk)     return false;
    if (search && !(p.name||'').toLowerCase().includes(search)) return false;
    return true;
  });
  renderPlans(filtered);
}

function showPlanDetail(plan) {
  const body = document.getElementById('plan-modal-body');
  if (!body) return;
  body.innerHTML = `
    <dl class="row mb-0">
      <dt class="col-5">Plan Name</dt>   <dd class="col-7">${Utils.escapeHtml(plan.name)}</dd>
      <dt class="col-5">Provider</dt>    <dd class="col-7">${Utils.escapeHtml(plan.provider||'—')}</dd>
      <dt class="col-5">Return Rate</dt> <dd class="col-7"><strong>${plan.returnRate}%</strong> per annum</dd>
      <dt class="col-5">Risk Level</dt>  <dd class="col-7"><span class="badge-custom badge-${(plan.risk||'').toLowerCase()}">${Utils.escapeHtml(plan.risk||'—')}</span></dd>
      <dt class="col-5">Tenure</dt>      <dd class="col-7">${Utils.escapeHtml(plan.tenure||'—')}</dd>
      <dt class="col-5">Min. Amount</dt> <dd class="col-7">${Utils.formatCurrency(plan.minAmount||0)}</dd>
      <dt class="col-5">Description</dt> <dd class="col-7">${Utils.escapeHtml(plan.description||'—')}</dd>
    </dl>`;
  const modal = new bootstrap.Modal(document.getElementById('planDetailModal'));
  modal.show();
}

// ================================================================
// GOVERNMENT SCHEMES
// ================================================================
let _schemes = [];
const _schemeMap = new Map();

async function loadSchemes() {
  try {
    _schemes = await API.get('/api/advisory/govt-schemes') || [];
  } catch {
    _schemes = _demoSchemes();
  }
  renderSchemes(_schemes);
  bindSchemeFilters();
}

function renderSchemes(schemes) {
  const grid = document.getElementById('schemes-grid');
  if (!grid) return;
  if (!schemes.length) {
    grid.innerHTML = '<div class="col-12 text-center text-muted py-4">No schemes found.</div>';
    return;
  }
  schemes.forEach((s, i) => _schemeMap.set(String(i), s));

  grid.innerHTML = schemes.map((s, i) => `
    <div class="col-md-6">
      <div class="plan-card">
        <div class="d-flex justify-content-between align-items-start mb-1">
          <h6 class="fw-bold mb-0">${Utils.escapeHtml(s.name)}</h6>
          <span class="badge-custom badge-savings">${Utils.escapeHtml(s.type||'Scheme')}</span>
        </div>
        <p class="text-muted small mb-2">${Utils.escapeHtml(s.description||'')}</p>
        <div class="small mb-1"><strong>Eligibility:</strong> ${Utils.escapeHtml(s.eligibility||'—')}</div>
        <div class="small mb-3"><strong>Benefit:</strong> ${Utils.escapeHtml(s.benefit||'—')}</div>
        <button class="btn-primary-custom apply-scheme-btn" data-scheme-idx="${i}">
          <i class="bi bi-check2-circle"></i> Apply
        </button>
      </div>
    </div>`).join('');

  grid.querySelectorAll('.apply-scheme-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const s = _schemeMap.get(btn.dataset.schemeIdx);
      applyScheme(s ? (s.id || s.name) : '');
    });
  });
}

function bindSchemeFilters() {
  ['scheme-filter-type','scheme-search'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', filterSchemes);
  });
}

function filterSchemes() {
  const type   = document.getElementById('scheme-filter-type')?.value || '';
  const search = (document.getElementById('scheme-search')?.value || '').toLowerCase();
  const filtered = _schemes.filter(s => {
    if (type   && s.type !== type) return false;
    if (search && !(s.name||'').toLowerCase().includes(search) &&
                  !(s.description||'').toLowerCase().includes(search)) return false;
    return true;
  });
  renderSchemes(filtered);
}

function applyScheme(id) {
  showToast('Application submitted for scheme: ' + id, 'success');
}

// ================================================================
// RECOMMENDATIONS
// ================================================================
async function loadRecommendations() {
  let recs;
  try {
    recs = await API.get('/api/advisory/recommendations') || [];
  } catch {
    recs = _demoRecs();
  }
  renderRecs(recs);
}

function renderRecs(recs) {
  const list = document.getElementById('recs-list');
  if (!list) return;
  if (!recs.length) {
    list.innerHTML = '<div class="text-center text-muted py-4">No recommendations available.</div>';
    return;
  }
  list.innerHTML = recs.map(r => `
    <div class="content-card mb-3">
      <div class="d-flex align-items-start gap-3">
        <div class="avatar" style="background:var(--secondary)">${r.priority === 'High' ? '!' : '→'}</div>
        <div class="flex-grow-1">
          <div class="d-flex justify-content-between">
            <h6 class="fw-bold mb-1">${Utils.escapeHtml(r.title)}</h6>
            <span class="badge-custom badge-${(r.priority||'low').toLowerCase()}">${Utils.escapeHtml(r.priority||'Normal')}</span>
          </div>
          <p class="text-muted small mb-0">${Utils.escapeHtml(r.reason)}</p>
        </div>
      </div>
    </div>`).join('');
}

// ── Demo data ─────────────────────────────────────────────────────
function _demoPlans() {
  return [
    { name:'SBI Fixed Deposit',       provider:'State Bank of India', returnRate:8.5,  risk:'Low',    tenure:'1 Year',   minAmount:1000,  type:'FD',   description:'Safe fixed deposit with guaranteed returns.' },
    { name:'Post Office MIS',          provider:'India Post',          returnRate:7.4,  risk:'Low',    tenure:'5 Years',  minAmount:1000,  type:'MIS',  description:'Monthly income scheme with steady payouts.' },
    { name:'Sukanya Samriddhi Yojana', provider:'Government of India', returnRate:8.2,  risk:'Low',    tenure:'21 Years', minAmount:250,   type:'SIP',  description:'Girl child savings scheme with tax benefits.' },
    { name:'SHG Micro Finance',        provider:'Grameen Bank',        returnRate:12.0, risk:'Medium', tenure:'2 Years',  minAmount:5000,  type:'Loan', description:'Micro-finance for SHG business activities.' },
    { name:'PMJDY Savings',            provider:'IPPB',                returnRate:4.0,  risk:'Low',    tenure:'Ongoing',  minAmount:0,     type:'SIP',  description:'Zero-balance savings account with benefits.' },
    { name:'Mudra Loan – Shishu',      provider:'MUDRA Bank',          returnRate:10.5, risk:'Medium', tenure:'3 Years',  minAmount:10000, type:'Loan', description:'Business loan up to ₹50,000 for micro enterprises.' }
  ];
}

function _demoSchemes() {
  return [
    { id:'pmjdy', name:'PM Jan Dhan Yojana',        type:'Banking',    eligibility:'All citizens',      benefit:'Zero-balance bank account + insurance', description:'Financial inclusion scheme for all.' },
    { id:'pmmy',  name:'PM Mudra Yojana',            type:'Loan',       eligibility:'Small businesses',  benefit:'Loans up to ₹10 lakhs',                description:'Micro-enterprise loan scheme.' },
    { id:'nrlm',  name:'NRLM – Aajeevika',           type:'SHG',        eligibility:'Rural women SHGs',  benefit:'Subsidised credit & capacity building', description:'National rural livelihood mission.' },
    { id:'pmsby', name:'PM Suraksha Bima Yojana',    type:'Insurance',  eligibility:'Age 18-70, bank a/c', benefit:'₹2 lakh accident cover @ ₹20/yr',    description:'Accidental death & disability insurance.' },
    { id:'pmjjby',name:'PM Jeevan Jyoti Bima',       type:'Insurance',  eligibility:'Age 18-50',         benefit:'₹2 lakh life cover @ ₹436/yr',         description:'Life insurance for all.' },
    { id:'atal',  name:'Atal Pension Yojana',        type:'Pension',    eligibility:'Age 18-40',         benefit:'₹1000–₹5000 monthly pension',          description:'Government pension for unorganised sector.' }
  ];
}

function _demoRecs() {
  return [
    { title:'Increase Monthly Savings',    reason:'Group balance is below the 6-month target threshold.',   priority:'High' },
    { title:'Consider Fixed Deposit',      reason:'Idle funds can earn higher returns in SBI FD.',           priority:'Medium' },
    { title:'Apply for NRLM Subsidy',      reason:'Your group meets the eligibility criteria.',              priority:'High' },
    { title:'Review Loan Repayment',       reason:'3 members have overdue loan instalments.',                priority:'Medium' },
    { title:'Diversify to Mutual Funds',   reason:'Long-term growth potential with moderate risk.',          priority:'Low' }
  ];
}
