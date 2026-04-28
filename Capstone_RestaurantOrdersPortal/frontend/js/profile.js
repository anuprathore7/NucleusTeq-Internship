/**
 * Food Mania — Profile Page JS
 *

 */

document.addEventListener('DOMContentLoaded', () => {
    requireAuth();
    loadProfile();
});

// ── LOAD PROFILE ─────────────────────────
async function loadProfile() {
    try {
        // Fetch profile + orders in parallel
        const [profileRes, ordersRes] = await Promise.all([
            apiFetch('/api/users/profile'),
            apiFetch('/api/orders/my-orders')
        ]);

        if (!profileRes.ok) throw new Error('Could not load profile');

        const profile = await profileRes.json();
        const orders  = ordersRes.ok ? await ordersRes.json() : [];

        // Hide skeleton, show content
        document.getElementById('profileLoading').style.display = 'none';
        document.getElementById('profileContent').style.display = 'block';

        renderProfile(profile);
        renderStats(orders);
        renderRecentOrders(orders.slice(0, 5)); // show last 5

    } catch (err) {
        console.error('Profile load error:', err);
        document.getElementById('profileLoading').style.display = 'none';
        document.getElementById('profileError').style.display = 'block';
    }
}

// ── RENDER PROFILE ───────────────────────
function renderProfile(p) {
    const fullName  = `${p.firstName || ''} ${p.lastName || ''}`.trim() || 'User';
    const initials  = getInitials(p.firstName, p.lastName);
    const roleLabel = p.role === 'RESTAURANT_OWNER' ? '🏪 Restaurant Owner' : '🍽️ Customer';

    // Hero section
    document.getElementById('avatarInitials').textContent = initials;
    document.getElementById('heroName').textContent       = fullName;
    document.getElementById('heroEmail').textContent      = p.email || '';
    document.getElementById('heroRole').textContent       = roleLabel;

    // Info card
    document.getElementById('profileFullName').textContent = fullName;
    document.getElementById('profileEmail').textContent    = p.email || '--';
    document.getElementById('profilePhone').textContent    = p.phone || '--';
    document.getElementById('profileRole').textContent     = p.role === 'RESTAURANT_OWNER' ? 'Restaurant Owner' : 'Customer';

    // Wallet card
    const balance = p.walletBalance ?? 0;
    document.getElementById('profileWallet').textContent     = `₹${Number(balance).toFixed(2)}`;
    document.getElementById('profileRoleBadge').textContent  = roleLabel;

    // Cache for other pages
    localStorage.setItem('fm_wallet',    balance);
    localStorage.setItem('fm_firstName', p.firstName || '');
    localStorage.setItem('fm_role',      p.role || '');
}

// ── RENDER STATS ─────────────────────────
function renderStats(orders) {
    const total     = orders.length;
    const completed = orders.filter(o => o.status === 'COMPLETED').length;
    const cancelled = orders.filter(o => o.status === 'CANCELLED').length;
    const spent     = orders
        .filter(o => o.status !== 'CANCELLED')
        .reduce((sum, o) => sum + (o.totalAmount || 0), 0);

    document.getElementById('statTotalOrders').textContent = total;
    document.getElementById('statCompleted').textContent   = completed;
    document.getElementById('statCancelled').textContent   = cancelled;
    document.getElementById('statTotalSpent').textContent  = `₹${spent.toFixed(2)}`;
}

// ── RENDER RECENT ORDERS ─────────────────
function renderRecentOrders(orders) {
    const container = document.getElementById('recentOrdersList');

    if (!orders || orders.length === 0) {
        container.innerHTML = `
            <div style="text-align:center;padding:24px;color:var(--ash);font-size:14px;">
                <div style="font-size:36px;margin-bottom:10px;">📦</div>
                No orders yet. Start ordering!
            </div>
        `;
        return;
    }

    container.innerHTML = orders.map(order => `
        <div class="order-mini-card">
            <div style="flex:1;min-width:0;">
                <div style="font-family:'Playfair Display',serif;font-size:14px;font-weight:700;color:var(--ink);margin-bottom:3px;">
                    ${escapeHtml(order.restaurantName)}
                </div>
                <div style="font-size:12px;color:var(--ash);">
                    ${formatDate(order.createdAt)} · ${order.items?.length || 0} item${(order.items?.length || 0) !== 1 ? 's' : ''}
                </div>
            </div>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:6px;flex-shrink:0;">
                <span style="font-family:'Playfair Display',serif;font-size:15px;font-weight:700;color:var(--burgundy);">
                    ₹${Number(order.totalAmount).toFixed(2)}
                </span>
                <span class="status-pill status-${order.status}">
                    ${getStatusIcon(order.status)} ${order.status}
                </span>
            </div>
        </div>
    `).join('');
}

// ── HELPERS ──────────────────────────────
function getInitials(firstName, lastName) {
    const f = (firstName || '').charAt(0).toUpperCase();
    const l = (lastName  || '').charAt(0).toUpperCase();
    return (f + l) || '?';
}

function getStatusIcon(status) {
    const icons = {
        PLACED: '🔵', PENDING: '🟠',
        DELIVERED: '🟢', COMPLETED: '✅', CANCELLED: '🔴'
    };
    return icons[status] || '⚪';
}

function formatDate(dateStr) {
    if (!dateStr) return '--';
    try {
        return new Date(dateStr).toLocaleDateString('en-IN', {
            day: 'numeric', month: 'short', year: 'numeric'
        });
    } catch { return '--'; }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}