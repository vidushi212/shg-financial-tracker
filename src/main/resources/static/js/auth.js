/**
 * auth.js – Authentication helpers (login, register, logout,
 *            session management via sessionStorage).
 */

const Auth = (() => {

  const STORAGE_KEY = 'shg_user';

  /** Persist logged-in user info */
  function saveUser(user) {
    const storedUser = { ...user };
    if (storedUser.role) storedUser.role = storedUser.role.toLowerCase();
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(storedUser));
    if (storedUser.token) sessionStorage.setItem('shg_token', storedUser.token);
  }

  /** Retrieve stored user (or null) */
  function getUser() {
    try {
      return JSON.parse(sessionStorage.getItem(STORAGE_KEY));
    } catch {
      return null;
    }
  }

  /** Clear session and redirect to login */
  function logout() {
    sessionStorage.clear();
    window.location.href = '/';
  }

  /** Returns true if a user session exists */
  function isLoggedIn() {
    return !!getUser();
  }

  /** Guard – call at the top of every protected page */
  function requireAuth() {
    if (!isLoggedIn()) {
      window.location.href = '/';
    }
  }

  /** Redirect already-authenticated users away from login page */
  function redirectIfLoggedIn() {
    if (isLoggedIn()) {
      window.location.href = '/dashboard';
    }
  }

  /**
   * Send login request.
   * On success stores the returned user object and navigates to dashboard.
   */
  async function login(username, password) {
    const data = await API.post('/api/auth/login', { username, password });
    if (data) {
      saveUser(data);
      window.location.href = '/dashboard';
    }
  }

  /**
   * Send registration request.
   * On success automatically logs in.
   */
  async function register(payload) {
    const data = await API.post('/api/auth/register', payload);
    if (data) {
      saveUser(data);
      window.location.href = '/dashboard';
    }
  }

  /**
   * Create a demo login session for the given role.
   */
  function demoLogin(role) {
    const normalizedRole = role.toLowerCase();
    const username = `${normalizedRole.replace(/\s+/g, '.')}-demo`;
    const fullName = `${role.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ')} Demo`;
    saveUser({ username, fullName, role: normalizedRole, token: `demo-${normalizedRole.replace(/\s+/g, '_')}` });
    window.location.href = '/dashboard';
  }

  /**
   * Return the current user's role formatted for display.
   */
  function displayRole() {
    const u = getUser();
    return u ? Utils.titleCase(u.role || '') : '';
  }

  /** Helper used by dashboard/navbar to render the user's display name */
  function displayName() {
    const u = getUser();
    return u ? (u.fullName || u.username || 'User') : 'Guest';
  }

  /** Returns the current user's role string (lowercase) */
  function role() {
    const u = getUser();
    return u ? (u.role || '').toLowerCase() : '';
  }

  return { saveUser, getUser, logout, isLoggedIn, requireAuth, redirectIfLoggedIn,
           login, register, demoLogin, displayName, role, displayRole };
})();
