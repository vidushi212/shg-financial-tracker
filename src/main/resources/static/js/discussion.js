/**
 * discussion.js – Discussion list & detail pages.
 */

let _discussions = [];
let _discPage = 1;
const DISC_PER_PAGE = 8;

document.addEventListener('DOMContentLoaded', async () => {
  if (typeof Auth !== 'undefined') Auth.requireAuth();

  if (document.getElementById('disc-list-container')) await loadDiscussions();
  if (document.getElementById('disc-detail-body'))    await loadDiscussionDetail();
});

// ================================================================
// DISCUSSION LIST
// ================================================================
async function loadDiscussions() {
  try {
    _discussions = await API.get('/api/discussions') || [];
  } catch {
    _discussions = _demoDiscussions();
  }
  renderDiscussions();
  bindDiscussionFilters();
}

function renderDiscussions() {
  const search   = (document.getElementById('disc-search')?.value  || '').toLowerCase();
  const category = document.getElementById('disc-filter-cat')?.value || '';

  let data = _discussions.filter(d => {
    if (category && d.category !== category) return false;
    if (search && !(d.title||'').toLowerCase().includes(search) &&
                  !(d.preview||'').toLowerCase().includes(search)) return false;
    return true;
  });

  const paged = Utils.paginate(data, _discPage, DISC_PER_PAGE);
  const container = document.getElementById('disc-list-container');
  if (!container) return;

  if (!paged.items.length) {
    container.innerHTML = '<div class="text-center text-muted py-4">No discussions found.</div>';
  } else {
    container.innerHTML = paged.items.map(d => `
      <div class="discussion-card" onclick="openDiscussion(${d.id})">
        <div class="d-flex align-items-start gap-3">
          <div class="avatar">${Utils.initials(d.author)}</div>
          <div class="flex-grow-1">
            <div class="d-flex justify-content-between align-items-start">
              <h6 class="fw-bold mb-1">${Utils.escapeHtml(d.title)}</h6>
              <span class="badge-custom badge-savings ms-2" style="white-space:nowrap">${Utils.escapeHtml(d.category||'General')}</span>
            </div>
            <p class="text-muted small mb-1">${Utils.escapeHtml(Utils.truncate(d.preview||'', 100))}</p>
            <div class="d-flex gap-3 small text-muted">
              <span><i class="bi bi-person me-1"></i>${Utils.escapeHtml(d.author)}</span>
              <span><i class="bi bi-chat me-1"></i>${d.commentCount||0} comments</span>
              <span><i class="bi bi-clock me-1"></i>${Utils.formatDate(d.createdAt)}</span>
            </div>
          </div>
        </div>
      </div>`).join('');
  }

  renderPagination(document.getElementById('disc-pagination'), paged.page, paged.pages, p => {
    _discPage = p;
    renderDiscussions();
  });
}

function bindDiscussionFilters() {
  ['disc-search','disc-filter-cat'].forEach(id => {
    document.getElementById(id)?.addEventListener('input', () => { _discPage = 1; renderDiscussions(); });
  });
}

function openDiscussion(id) {
  window.location.href = '/discussion/detail?id=' + id;
}

// ── Create discussion form ────────────────────────────────────────
const newDiscForm = document.getElementById('new-disc-form');
if (newDiscForm) {
  newDiscForm.addEventListener('submit', async e => {
    e.preventDefault();
    if (!newDiscForm.checkValidity()) { newDiscForm.classList.add('was-validated'); return; }

    const payload = {
      title:    document.getElementById('disc-title').value,
      content:  document.getElementById('disc-content').value,
      category: document.getElementById('disc-category').value
    };

    try {
      await API.post('/api/discussions', payload);
      showToast('Discussion created!', 'success');
      newDiscForm.reset();
      bootstrap.Modal.getInstance(document.getElementById('newDiscModal'))?.hide();
      await loadDiscussions();
    } catch (err) {
      showToast(err.message || 'Failed to create discussion.', 'error');
    }
  });
}

// ================================================================
// DISCUSSION DETAIL
// ================================================================
async function loadDiscussionDetail() {
  const params = new URLSearchParams(window.location.search);
  const id     = params.get('id');

  let discussion, comments;
  try {
    discussion = await API.get('/api/discussions/' + id);
    comments   = await API.get('/api/discussions/' + id + '/comments');
  } catch {
    discussion = _demoDiscussions().find(d => d.id == id) || _demoDiscussions()[0];
    comments   = _demoComments();
  }

  // Render discussion header
  const header = document.getElementById('disc-detail-header');
  if (header && discussion) {
    document.getElementById('disc-detail-title').textContent   = discussion.title;
    document.getElementById('disc-detail-author').textContent  = discussion.author;
    document.getElementById('disc-detail-date').textContent    = Utils.formatDate(discussion.createdAt);
    document.getElementById('disc-detail-cat').textContent     = discussion.category || 'General';
    document.getElementById('disc-detail-body').textContent    = discussion.content  || discussion.preview || '';
  }

  // Render comments
  renderComments(comments || []);

  // Bind add-comment form
  const commentForm = document.getElementById('add-comment-form');
  if (commentForm) {
    commentForm.addEventListener('submit', async e => {
      e.preventDefault();
      const text = document.getElementById('comment-text').value.trim();
      if (!text) return;
      try {
        await API.post('/api/discussions/' + id + '/comments', { content: text });
        showToast('Comment posted!', 'success');
        commentForm.reset();
        const comments2 = await API.get('/api/discussions/' + id + '/comments');
        renderComments(comments2);
      } catch {
        // Demo mode – just append locally
        const localComments = _demoComments();
        localComments.unshift({ author: Auth.displayName(), content: text, createdAt: new Date().toISOString() });
        renderComments(localComments);
        showToast('Comment posted (demo mode).', 'info');
        commentForm.reset();
      }
    });
  }
}

function renderComments(comments) {
  const container = document.getElementById('comments-container');
  if (!container) return;
  if (!comments.length) {
    container.innerHTML = '<div class="text-muted text-center py-3">No comments yet. Be the first to comment!</div>';
    return;
  }
  container.innerHTML = comments.map(c => `
    <div class="comment-item">
      <div class="d-flex align-items-center gap-2 mb-1">
        <div class="avatar" style="width:28px;height:28px;font-size:.75rem">${Utils.initials(c.author)}</div>
        <span class="fw-bold small">${Utils.escapeHtml(c.author)}</span>
        <span class="text-muted" style="font-size:.75rem">${Utils.formatDate(c.createdAt)}</span>
      </div>
      <p class="mb-0 small">${Utils.escapeHtml(c.content)}</p>
    </div>`).join('');
}

// ── Demo data ─────────────────────────────────────────────────────
function _demoDiscussions() {
  return [
    { id:1, title:'How to increase monthly savings?', author:'Priya Sharma',   category:'Finance',     commentCount:5, preview:'We are trying to motivate members to save more each month…', createdAt:'2024-03-01', content:'We are trying to motivate all 24 members to increase their monthly savings contribution from ₹500 to ₹700. Please share your ideas and experiences.' },
    { id:2, title:'Best investment for group funds',   author:'Sunita Devi',    category:'Investment',  commentCount:3, preview:'Our group has ₹50,000 idle. Which scheme should we invest in?', createdAt:'2024-03-05', content:'Our group has accumulated ₹50,000 in idle funds. We are considering FD vs mutual funds. Please share your opinions.' },
    { id:3, title:'NRLM Subsidy Application Process', author:'Kavita Patel',   category:'Government',  commentCount:7, preview:'Can anyone guide on the NRLM subsidy application steps?', createdAt:'2024-03-08', content:'I would like to know the step-by-step process for applying for NRLM Aajeevika subsidy. Has anyone done it?' },
    { id:4, title:'Loan repayment issues – March',    author:'Rekha Verma',    category:'Loan',        commentCount:2, preview:'Three members are behind on their loan instalments…', createdAt:'2024-03-12', content:'We have three members who have missed their March instalments. How should we handle this as a group?' },
    { id:5, title:'Group meeting agenda – April',      author:'Admin',          category:'General',     commentCount:9, preview:'Proposed agenda for our April monthly meeting…', createdAt:'2024-03-15', content:'Please review the proposed agenda for the April meeting: 1) Savings review 2) Loan approvals 3) Investment decision 4) Any other business.' }
  ];
}

function _demoComments() {
  return [
    { author:'Sunita Devi',  content:'Great idea! We should set a fixed date each month.',    createdAt:'2024-03-02' },
    { author:'Kavita Patel', content:'I agree. Maybe we can do auto-debit from accounts.',    createdAt:'2024-03-03' },
    { author:'Rekha Verma',  content:'We tried this in our previous group. Works very well!', createdAt:'2024-03-04' }
  ];
}
