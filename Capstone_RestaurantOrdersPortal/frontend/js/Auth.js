/**
 * ============================================
 *   Food Mania — Auth Page JS
 * ============================================
 */

const BASE_URL = 'http://localhost:8000';

// ─────────────────────────────────────────
//   HELPER: Extract message from backend response
//   Your GlobalExceptionHandler always returns { "message": "..." }
//   This reads it safely, with a fallback.
// ─────────────────────────────────────────

async function extractErrorMessage(response, fallback) {
    try {
        const data = await response.json();
        return data.message || fallback;
    } catch {
        return fallback;
    }
}

// ─────────────────────────────────────────
//   PAGE INIT
// ─────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('fm_token');
    if (token && isTokenValid(token)) {
        const role = localStorage.getItem('fm_role');
        redirectByRole(role);
        return;
    }
    updateTabIndicator('login');
});

// ─────────────────────────────────────────
//   TAB SWITCHING
// ─────────────────────────────────────────

function switchTab(tab) {
    const loginForm    = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const tabLogin     = document.getElementById('tabLogin');
    const tabRegister  = document.getElementById('tabRegister');

    clearAlert();

    if (tab === 'login') {
        loginForm.classList.add('active');
        registerForm.classList.remove('active');
        tabLogin.classList.add('active');
        tabRegister.classList.remove('active');
        updateTabIndicator('login');
    } else {
        registerForm.classList.add('active');
        loginForm.classList.remove('active');
        tabRegister.classList.add('active');
        tabLogin.classList.remove('active');
        updateTabIndicator('register');
    }
}

function updateTabIndicator(tab) {
    const indicator = document.getElementById('tabIndicator');
    if (!indicator) return;
    indicator.classList.toggle('right', tab === 'register');
}

// ─────────────────────────────────────────
//   PASSWORD TOGGLE
// ─────────────────────────────────────────

function togglePw(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    if (input.type === 'password') {
        input.type = 'text';
        btn.textContent = '🙈';
    } else {
        input.type = 'password';
        btn.textContent = '👁️';
    }
}

// ─────────────────────────────────────────
//   LOGIN
// ─────────────────────────────────────────

async function handleLogin() {
    const email    = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    // ── Client-side checks (matches your ValidationUtil) ──
    if (!email) {
        showAlert('error', '⚠️', 'Email is required.');
        return;
    }
    if (!isValidEmail(email)) {
        showAlert('error', '⚠️', 'Please enter a valid email address.');
        return;
    }
    if (!password) {
        showAlert('error', '⚠️', 'Password is required.');
        return;
    }

    setLoading('loginBtn', true);
    clearAlert();

    try {
        const response = await fetch(`${BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            // ── Read { "message": "..." } from your GlobalExceptionHandler ──
            const message = await extractErrorMessage(
                response,
                response.status >= 500
                    ? 'Something went wrong on our end. Please try again.'
                    : 'Invalid email or password. Please try again.'
            );
            showAlert('error', '❌', message);
            return;
        }

        const data  = await response.json();
        const token = data.token;

        if (!token) {
            showAlert('error', '❌', 'Login failed. No token received.');
            return;
        }

        const payload = decodeJwt(token);
        if (!payload) {
            showAlert('error', '❌', 'Invalid token received. Please try again.');
            return;
        }

        localStorage.setItem('fm_token',  token);
        localStorage.setItem('fm_email',  payload.sub || email);
        localStorage.setItem('fm_role',   payload.role || '');
        if (payload.userId) {
            localStorage.setItem('fm_user_id', payload.userId);
        }

        showAlert('success', '✅', 'Login successful! Redirecting...');
        setTimeout(() => redirectByRole(payload.role), 800);

    } catch (err) {
        console.error('Login error:', err);
        showAlert('error', '❌', 'Cannot connect to server. Is the backend running?');
    } finally {
        setLoading('loginBtn', false);
    }
}

// ─────────────────────────────────────────
//   REGISTER
// ─────────────────────────────────────────

async function handleRegister() {
    const firstName = document.getElementById('regFirstName').value.trim();
    const lastName  = document.getElementById('regLastName').value.trim();
    const email     = document.getElementById('regEmail').value.trim();
    const phone     = document.getElementById('regPhone').value.trim();
    const password  = document.getElementById('regPassword').value.trim();
    const roleInput = document.querySelector('input[name="role"]:checked');
    const role      = roleInput ? roleInput.value : 'USER';

    // ── Client-side checks (mirrors your ValidationUtil exactly) ──
    if (!firstName) {
        showAlert('error', '⚠️', 'First name is required.');
        return;
    }
    if (!lastName) {
        showAlert('error', '⚠️', 'Last name is required.');
        return;
    }
    if (!email) {
        showAlert('error', '⚠️', 'Email is required.');
        return;
    }
    if (!isValidEmail(email)) {
        showAlert('error', '⚠️', 'Please enter a valid email address.');
        return;
    }
    if (!phone) {
        showAlert('error', '⚠️', 'Phone number is required.');
        return;
    }
    if (!/^\d{10}$/.test(phone)) {
        showAlert('error', '⚠️', 'Phone number must be 10 digits.');
        return;
    }
    if (!password) {
        showAlert('error', '⚠️', 'Password is required.');
        return;
    }
    if (password.length < 6) {
        showAlert('error', '⚠️', 'Password must be at least 6 characters.');
        return;
    }

    setLoading('registerBtn', true);
    clearAlert();

    try {
        const response = await fetch(`${BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ firstName, lastName, email, password, phone, role })
        });

        if (!response.ok) {
            // ── Read { "message": "..." } from your GlobalExceptionHandler ──
            const message = await extractErrorMessage(
                response,
                response.status >= 500
                    ? 'Something went wrong on our end. Please try again.'
                    : 'Registration failed. Please check your details.'
            );
            showAlert('error', '❌', message);
            return;
        }

        // Success — backend returns string "User registered successfully"
        // (not JSON, so we don't call response.json() here)
        showAlert('success', '✅', 'Account created! You can now sign in.');

        document.getElementById('regFirstName').value = '';
        document.getElementById('regLastName').value  = '';
        document.getElementById('regEmail').value     = '';
        document.getElementById('regPhone').value     = '';
        document.getElementById('regPassword').value  = '';

        setTimeout(() => {
            switchTab('login');
            document.getElementById('loginEmail').value = email;
        }, 1500);

    } catch (err) {
        console.error('Register error:', err);
        showAlert('error', '❌', 'Cannot connect to server. Is the backend running?');
    } finally {
        setLoading('registerBtn', false);
    }
}

// ─────────────────────────────────────────
//   REDIRECT BY ROLE
// ─────────────────────────────────────────

function redirectByRole(role) {
    if (role === 'RESTAURANT_OWNER') {
        window.location.href = 'owner-dashboard.html';
    } else {
        window.location.href = 'restaurants.html';
    }
}

// ─────────────────────────────────────────
//   ALERT HELPERS
// ─────────────────────────────────────────

function showAlert(type, icon, message) {
    const box = document.getElementById('alertBox');
    const ico = document.getElementById('alertIcon');
    const msg = document.getElementById('alertMsg');
    box.className   = `alert ${type}`;
    ico.textContent = icon;
    msg.textContent = message;
}

function clearAlert() {
    document.getElementById('alertBox').className = 'alert hidden';
}

// ─────────────────────────────────────────
//   LOADING STATE
// ─────────────────────────────────────────

function setLoading(btnId, loading) {
    const btn    = document.getElementById(btnId);
    const text   = btn.querySelector('.btn-text');
    const icon   = btn.querySelector('.btn-icon');
    const loader = btn.querySelector('.btn-loader');

    btn.classList.toggle('loading', loading);
    text.style.opacity = loading ? '0.6' : '1';
    icon.classList.toggle('hidden', loading);
    loader.classList.toggle('hidden', !loading);
}

// ─────────────────────────────────────────
//   UTILITIES
// ─────────────────────────────────────────

function decodeJwt(token) {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch {
        return null;
    }
}

function isTokenValid(token) {
    const payload = decodeJwt(token);
    if (!payload) return false;
    return Math.floor(Date.now() / 1000) < payload.exp;
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}