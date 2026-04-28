/**
 * ============================================
 *   Food Mania — Shared Utilities
 * ============================================
 */

const BASE_URL = 'http://localhost:8000';

function getToken()  { return localStorage.getItem('fm_token'); }
function getRole()   { return localStorage.getItem('fm_role');  }
function getEmail()  { return localStorage.getItem('fm_email'); }

function decodeJwt(token) {
    try { return JSON.parse(atob(token.split('.')[1])); }
    catch(e) { return null; }
}

function isLoggedIn() {
    const token = getToken();
    if (!token) return false;
    const p = decodeJwt(token);
    if (!p) return false;
    return Math.floor(Date.now()/1000) < p.exp;
}

function requireAuth() {
    if (!isLoggedIn()) { localStorage.clear(); window.location.href = '../pages/Auth.html'; }
}

function logout() {
    localStorage.clear();
    window.location.href = '../pages/Auth.html';
}

async function apiFetch(endpoint, options = {}) {
    const token = getToken();

    const headers = {
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        ...(options.headers || {})
    };

    // ❌ Only add JSON header if body is NOT FormData
    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    const res = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        headers
    });

    return res;
}