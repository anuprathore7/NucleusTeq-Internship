/**
 * Food Mania — Restaurants Page JS
 * FIXED: Wallet balance fetches from /api/users/profile
 */

let allRestaurants = [];
const FOOD_EMOJIS  = ['🍕','🍔','🍜','🍣','🌮','🍛','🍱','🥗','🍗','🥪'];

document.addEventListener('DOMContentLoaded', () => {
    requireAuth();

    // Owner redirect
    if (getRole() === 'RESTAURANT_OWNER') {
        window.location.replace('owner-dashboard.html');
        return;
    }

    setupNavbar();
    loadRestaurants();
    loadWalletBalance(); // ← fetch and show wallet

    document.addEventListener('click', (e) => {
        const menu = document.getElementById('userMenu');
        if (menu && !menu.contains(e.target)) {
            document.getElementById('userDropdown')?.classList.add('hidden');
        }
    });
});

// ── NAVBAR ──────────────────────────────
function setupNavbar() {
    const email = getEmail() || 'Account';
    document.getElementById('userEmailNav').textContent  = email.split('@')[0];
    document.getElementById('userEmailDrop').textContent = email;
}

function toggleUserMenu() {
    document.getElementById('userDropdown').classList.toggle('hidden');
}

// ── WALLET BALANCE ───────────────────────
/**
 * Calls GET /api/users/profile
 * Gets walletBalance from response
 * Updates the wallet badge in navbar
 */
async function loadWalletBalance() {
    try {
        const res = await apiFetch('/api/users/profile');
        if (!res.ok) return;

        const data = await res.json();
        const balance = data.walletBalance ?? 0;

        // Update wallet badge
        const walletEl = document.getElementById('walletBadge');
        if (walletEl) {
            walletEl.textContent = `₹${Number(balance).toFixed(0)}`;
        }

        // Cache in localStorage for other pages
        localStorage.setItem('fm_wallet',     balance);
        localStorage.setItem('fm_firstName',  data.firstName || '');
        localStorage.setItem('fm_lastName',   data.lastName || '');
        localStorage.setItem('fm_role',       data.role || getRole());

    } catch (err) {
        console.warn('Could not load wallet balance:', err);
        // Show cached value if available
        const cached = localStorage.getItem('fm_wallet');
        const walletEl = document.getElementById('walletBadge');
        if (cached && walletEl) {
            walletEl.textContent = `₹${Number(cached).toFixed(0)}`;
        }
    }
}

// ── LOAD RESTAURANTS ─────────────────────
async function loadRestaurants() {
    showSkeleton();
    try {
        const res = await apiFetch('/api/restaurants');
        if (!res.ok) throw new Error(`Server error: ${res.status}`);

        const restaurants = await res.json();
        allRestaurants = restaurants;
        hideSkeleton();

        if (restaurants.length === 0) { showEmpty(); return; }
        renderRestaurants(restaurants);

    } catch (err) {
        console.error('Failed to load restaurants:', err);
        hideSkeleton();
        showError('Could not load restaurants. Please check your connection.');
    }
}

// ── RENDER ───────────────────────────────
function renderRestaurants(restaurants) {
    const grid = document.getElementById('restaurantGrid');
    grid.innerHTML = '';
    grid.classList.remove('hidden');

    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('errorState').classList.add('hidden');
    document.getElementById('restaurantCount').textContent =
        `${restaurants.length} restaurant${restaurants.length !== 1 ? 's' : ''} available`;

    restaurants.forEach((r, index) => {
        grid.appendChild(createRestaurantCard(r, index));
    });
}

function createRestaurantCard(restaurant, index) {
    const emoji = FOOD_EMOJIS[restaurant.id % FOOD_EMOJIS.length];

    const card = document.createElement('div');
    card.className = 'restaurant-card';
    card.style.animation = `fadeInUp .4s ease both ${index * 60}ms`;

    const imageUrl = restaurant.imagePath
        ? `http://localhost:8000${restaurant.imagePath}`
        : null;

    card.innerHTML = `
        <div class="card-img" style="position:relative;overflow:hidden;border-radius:12px 12px 0 0;height:160px;background:#f5f5f5;display:flex;align-items:center;justify-content:center;">
            
            ${
        imageUrl
            ? `<img 
                        src="${imageUrl}" 
                        alt="${escapeHtml(restaurant.name)}"
                        style="width:100%;height:100%;object-fit:cover;"
                        onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"
                   />`
            : ''
    }

            <!-- Emoji fallback -->
            <div style="
                display:${imageUrl ? 'none' : 'flex'};
                align-items:center;
                justify-content:center;
                width:100%;
                height:100%;
                font-size:64px;
            ">
                ${emoji}
            </div>

        </div>

        <div class="card-body">
            <div class="flex items-start justify-between gap-2 mb-2">
                <h3 class="card-name">${escapeHtml(restaurant.name)}</h3>
                <span class="card-status status-open flex-shrink-0">● Open</span>
            </div>

            <p class="card-desc">
                ${escapeHtml(restaurant.description || 'Delicious food awaits!')}
            </p>

            <div class="card-meta">
                <div class="card-address">
                    <span>📍</span>
                    <span>${escapeHtml(restaurant.address)}</span>
                </div>

                <div style="display:flex;align-items:center;gap:4px;font-size:12px;color:var(--ash)">
                    <span>📞</span>
                    <span>${escapeHtml(restaurant.phone)}</span>
                </div>
            </div>

            <button class="btn-order"
                onclick="goToMenu(${restaurant.id}, '${escapeHtml(restaurant.name)}')">
                View Menu & Order →
            </button>
        </div>
    `;

    return card;
}

function goToMenu(restaurantId, restaurantName) {
    localStorage.setItem('fm_restaurant_id',   restaurantId);
    localStorage.setItem('fm_restaurant_name', restaurantName);
    window.location.href = `menu.html?restaurantId=${restaurantId}`;
}

// ── SEARCH ───────────────────────────────
function filterRestaurants() {
    applyFilter(document.getElementById('searchInput').value.toLowerCase());
}
function filterRestaurantsMobile() {
    applyFilter(document.getElementById('searchInputMobile')?.value.toLowerCase() || '');
}
function applyFilter(query) {
    if (!query) { renderRestaurants(allRestaurants); return; }
    const filtered = allRestaurants.filter(r =>
        r.name.toLowerCase().includes(query) ||
        (r.description || '').toLowerCase().includes(query) ||
        r.address.toLowerCase().includes(query)
    );
    if (filtered.length === 0) {
        document.getElementById('restaurantGrid').classList.add('hidden');
        document.getElementById('emptyState').classList.remove('hidden');
        document.getElementById('restaurantCount').textContent = '0 restaurants found';
    } else {
        document.getElementById('emptyState').classList.add('hidden');
        renderRestaurants(filtered);
    }
}

// ── SORT ─────────────────────────────────
function sortRestaurants(value) {
    let sorted = [...allRestaurants];
    if (value === 'name')      sorted.sort((a, b) => a.name.localeCompare(b.name));
    if (value === 'name-desc') sorted.sort((a, b) => b.name.localeCompare(a.name));
    renderRestaurants(sorted);
}

// ── STATES ───────────────────────────────
function showSkeleton() {
    document.getElementById('skeletonGrid').classList.remove('hidden');
    document.getElementById('restaurantGrid').classList.add('hidden');
    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('errorState').classList.add('hidden');
}
function hideSkeleton() { document.getElementById('skeletonGrid').classList.add('hidden'); }
function showEmpty() {
    document.getElementById('emptyState').classList.remove('hidden');
    document.getElementById('restaurantCount').textContent = '0 restaurants available';
}
function showError(msg) {
    document.getElementById('errorState').classList.remove('hidden');
    document.getElementById('errorMsg').textContent = msg;
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// Fade in animation
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeInUp {
        from { opacity:0; transform:translateY(20px); }
        to   { opacity:1; transform:translateY(0); }
    }
`;
document.head.appendChild(style);