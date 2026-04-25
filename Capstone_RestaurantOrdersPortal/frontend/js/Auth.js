
/**
 * ============================================
 *   Food Mania — Auth Page JS
 * ============================================
 */

const BASE_URL = 'http://localhost:8000';

// ─────────────────────────────────────────
//   PAGE INIT
// ─────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    // If already logged in and token is valid → redirect
    const token = localStorage.getItem('fm_token');
    if (token && isTokenValid(token)) {
        const role = localStorage.getItem('fm_role');
        redirectByRole(role);
        return;
    }

    // Set up tab indicator initial position
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
    if (tab === 'register') {
        indicator.classList.add('right');
    } else {
        indicator.classList.remove('right');
    }
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

    // Basic validation
    if (!email || !password) {
        showAlert('error', '⚠️', 'Please enter both email and password.');
        return;
    }

    if (!isValidEmail(email)) {
        showAlert('error', '⚠️', 'Please enter a valid email address.');
        return;
    }

    setLoading('loginBtn', true);
    clearAlert();

    try {
        /**
         * POST /api/auth/login
         * Body: { email, password }
         * Response: { token }
         */
        const response = await fetch(`${BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            // 401 → wrong credentials, 500 → server error
            if (response.status === 401 || response.status === 403) {
                showAlert('error', '❌', 'Invalid email or password. Please try again.');
            } else {
                showAlert('error', '❌', `Server error (${response.status}). Please try again.`);
            }
            return;
        }

        const data = await response.json();
        const token = data.token;

        if (!token) {
            showAlert('error', '❌', 'Login failed. No token received.');
            return;
        }

        // Decode JWT to extract role and email
        const payload = decodeJwt(token);
        if (!payload) {
            showAlert('error', '❌', 'Invalid token received. Please try again.');
            return;
        }

        // Save to localStorage
        // JWT payload has: sub (email), role, exp
        localStorage.setItem('fm_token', token);
        localStorage.setItem('fm_email', payload.sub || email);
        localStorage.setItem('fm_role',  payload.role || '');

        // If payload has userId, save it too
        if (payload.userId) {
            localStorage.setItem('fm_user_id', payload.userId);
        }

        showAlert('success', '✅', 'Login successful! Redirecting...');

        // Redirect after short delay
        setTimeout(() => {
            redirectByRole(payload.role);
        }, 800);

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

    // Validation
    if (!firstName || !lastName || !email || !phone || !password) {
        showAlert('error', '⚠️', 'Please fill in all fields.');
        return;
    }

    if (!isValidEmail(email)) {
        showAlert('error', '⚠️', 'Please enter a valid email address.');
        return;
    }

    if (password.length < 6) {
        showAlert('error', '⚠️', 'Password must be at least 6 characters.');
        return;
    }

    if (!/^\d{10}$/.test(phone)) {
        showAlert('error', '⚠️', 'Please enter a valid 10-digit phone number.');
        return;
    }

    setLoading('registerBtn', true);
    clearAlert();

    try {
        /**
         * POST /api/auth/register
         * Body: { firstName, lastName, email, password, phone, role }
         * Response: "User registered successfully" (string)
         */
        const response = await fetch(`${BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ firstName, lastName, email, password, phone, role })
        });

        if (!response.ok) {
            const errorText = await response.text();
            if (response.status === 409 || errorText.toLowerCase().includes('exist')) {
                showAlert('error', '❌', 'An account with this email already exists.');
            } else {
                showAlert('error', '❌', errorText || `Registration failed (${response.status}).`);
            }
            return;
        }

        // Success — backend returns a string message
        showAlert('success', '✅', 'Account created! You can now sign in.');

        // Clear fields
        document.getElementById('regFirstName').value = '';
        document.getElementById('regLastName').value  = '';
        document.getElementById('regEmail').value     = '';
        document.getElementById('regPhone').value     = '';
        document.getElementById('regPassword').value  = '';

        // Switch to login tab after 1.5s
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
        // USER or anything else
        window.location.href = 'restaurants.html';
    }
}

// ─────────────────────────────────────────
//   ALERT HELPERS
// ─────────────────────────────────────────

function showAlert(type, icon, message) {
    const box  = document.getElementById('alertBox');
    const ico  = document.getElementById('alertIcon');
    const msg  = document.getElementById('alertMsg');

    box.className = `alert ${type}`;
    ico.textContent = icon;
    msg.textContent = message;
}

function clearAlert() {
    const box = document.getElementById('alertBox');
    box.className = 'alert hidden';
}

// ─────────────────────────────────────────
//   LOADING STATE
// ─────────────────────────────────────────

function setLoading(btnId, loading) {
    const btn    = document.getElementById(btnId);
    const text   = btn.querySelector('.btn-text');
    const icon   = btn.querySelector('.btn-icon');
    const loader = btn.querySelector('.btn-loader');

    if (loading) {
        btn.classList.add('loading');
        text.style.opacity = '0.6';
        icon.classList.add('hidden');
        loader.classList.remove('hidden');
    } else {
        btn.classList.remove('loading');
        text.style.opacity = '1';
        icon.classList.remove('hidden');
        loader.classList.add('hidden');
    }
}

// ─────────────────────────────────────────
//   UTILITIES
// ─────────────────────────────────────────

function decodeJwt(token) {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
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