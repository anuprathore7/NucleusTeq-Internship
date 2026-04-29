/**
 * ============================================
 *   Food Mania — Menu Page JS
 * ============================================
 *
 *  APIs USED:
 *  GET  /api/restaurants/{id}             → restaurant info
 *  GET  /api/restaurants/{id}/categories  → categories (public)
 *  GET  /api/restaurants/{id}/menu-items  → menu items (public)
 *  POST /api/cart/add                     → add item (token required)
 *    Body: { menuItemId, quantity }
 */

const FOOD_EMOJIS = ['🍕','🍔','🍜','🍣','🌮','🍛','🍱','🥗','🍗','🥪','🧆','🥘','🍲','🥙','🌯'];

let allCategories  = [];
let allMenuItems   = [];
let activeCategory = 'all';
let cartItemCount  = 0;

// ─────────────────────────────────────────
//   PAGE INIT
// ─────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    requireAuth();
    setupNavbar();
    loadCartCount();
    loadMenuData();

    document.addEventListener('click', (e) => {
        const menu = document.getElementById('userMenu');
        if (menu && !menu.contains(e.target)) {
            document.getElementById('userDropdown').classList.add('hidden');
        }
    });
});

// ─────────────────────────────────────────
//   NAVBAR
// ─────────────────────────────────────────
function setupNavbar() {
    const email = getEmail() || 'Account';
    document.getElementById('userEmailNav').textContent = email.split('@')[0];
    document.getElementById('userEmailDrop').textContent = email;
}
function toggleUserMenu() {
    document.getElementById('userDropdown').classList.toggle('hidden');
}

// ─────────────────────────────────────────
//   LOAD EXISTING CART COUNT
// ─────────────────────────────────────────
async function loadCartCount() {
    try {
        const res = await apiFetch('/api/cart');
        if (res.ok) {
            const cart = await res.json();
            if (cart.items && cart.items.length > 0) {
                const total = cart.items.reduce((s, i) => s + i.quantity, 0);
                updateCartBadge(total);
            }
        }
    } catch (e) { /* silent */ }
}

function updateCartBadge(count) {
    cartItemCount = count;
    const badge = document.getElementById('cartCount');
    if (!badge) return;
    if (count > 0) {
        badge.textContent = count;
        badge.classList.remove('hidden');
    } else {
        badge.classList.add('hidden');
    }
}

// ─────────────────────────────────────────
//   GET RESTAURANT ID
// ─────────────────────────────────────────
function getRestaurantId() {
    const params = new URLSearchParams(window.location.search);
    return params.get('restaurantId') || localStorage.getItem('fm_restaurant_id');
}

// ─────────────────────────────────────────
//   LOAD ALL DATA
// ─────────────────────────────────────────
async function loadMenuData() {
    const restaurantId = getRestaurantId();
    if (!restaurantId) {
        hideSkeleton();
        showError('No restaurant selected. Please go back and choose a restaurant.');
        return;
    }
    showSkeleton();
    try {
        const [restaurantRes, categoriesRes, menuRes] = await Promise.all([
            apiFetch(`/api/restaurants/${restaurantId}`),
            apiFetch(`/api/restaurants/${restaurantId}/categories`),
            apiFetch(`/api/restaurants/${restaurantId}/menu-items`)
        ]);
        if (!restaurantRes.ok) throw new Error(`Restaurant not found (${restaurantRes.status})`);
        if (!categoriesRes.ok) throw new Error(`Categories failed (${categoriesRes.status})`);
        if (!menuRes.ok)       throw new Error(`Menu failed (${menuRes.status})`);

        const restaurant = await restaurantRes.json();
        allCategories    = await categoriesRes.json();
        allMenuItems     = await menuRes.json();

        hideSkeleton();
        renderHero(restaurant);
        renderCategoryPills(allCategories);
        if (allMenuItems.length === 0) { showEmpty(); } else { renderMenu(allMenuItems, allCategories); }
    } catch (err) {
        console.error('Menu load error:', err);
        hideSkeleton();
        showError(err.message || 'Could not load menu. Please check your connection.');
    }
}

// ─────────────────────────────────────────
//   RENDER HERO
// ─────────────────────────────────────────
function renderHero(restaurant) {
    const emoji = FOOD_EMOJIS[restaurant.id % FOOD_EMOJIS.length];
    document.getElementById('heroEmoji').textContent         = emoji;
    document.getElementById('restaurantName').textContent    = restaurant.name;
    document.getElementById('restaurantAddress').textContent = '📍 ' + (restaurant.address || '--');
    document.getElementById('restaurantPhone').textContent   = '📞 ' + (restaurant.phone || '--');
    document.getElementById('restaurantDesc').textContent    = restaurant.description || '';
    document.title = `Food Mania — ${restaurant.name}`;
}

// ─────────────────────────────────────────
//   RENDER CATEGORY PILLS
// ─────────────────────────────────────────
function renderCategoryPills(categories) {
    const bar = document.getElementById('categoryBar');
    bar.innerHTML = '';
    const allPill = document.createElement('button');
    allPill.className = 'category-pill active';
    allPill.textContent = '🍽️ All Items';
    allPill.id = 'pill-all';
    allPill.onclick = () => filterByCategory('all');
    bar.appendChild(allPill);
    categories.forEach(cat => {
        const pill = document.createElement('button');
        pill.className = 'category-pill';
        pill.textContent = cat.name;
        pill.id = `pill-${cat.id}`;
        pill.onclick = () => filterByCategory(String(cat.id));
        bar.appendChild(pill);
    });
}

// ─────────────────────────────────────────
//   RENDER MENU
// ─────────────────────────────────────────
function renderMenu(items, categories) {
    const content = document.getElementById('menuContent');
    content.innerHTML = '';
    content.classList.remove('hidden');
    document.getElementById('emptyState').classList.add('hidden');

    const catMap = {};
    categories.forEach(c => { catMap[c.id] = c.name; });
    const groups = {};
    const uncategorized = [];
    items.forEach(item => {
        if (item.categoryId && catMap[item.categoryId]) {
            if (!groups[item.categoryId]) groups[item.categoryId] = [];
            groups[item.categoryId].push(item);
        } else { uncategorized.push(item); }
    });

    categories.forEach(cat => {
        const catItems = groups[cat.id];
        if (!catItems || catItems.length === 0) return;
        const section = document.createElement('div');
        section.id = `section-${cat.id}`;
        section.className = 'category-section';
        section.innerHTML = `
            <div class="category-section-title">
                <span>${getCategoryEmoji(cat.name)}</span>
                <span>${escapeHtml(cat.name)}</span>
                <span style="font-family:'DM Sans',sans-serif;font-size:13px;font-weight:400;color:var(--ash)">(${catItems.length} item${catItems.length !== 1 ? 's' : ''})</span>
            </div>
            <div class="space-y-3" id="items-${cat.id}"></div>`;
        content.appendChild(section);
        const container = document.getElementById(`items-${cat.id}`);
        catItems.forEach((item, idx) => container.appendChild(createMenuItemCard(item, idx)));
    });

    if (uncategorized.length > 0) {
        const section = document.createElement('div');
        section.id = 'section-uncategorized';
        section.className = 'category-section';
        section.innerHTML = `<div class="category-section-title"><span>🍴</span><span>More Items</span></div><div class="space-y-3" id="items-uncategorized"></div>`;
        content.appendChild(section);
        const container = document.getElementById('items-uncategorized');
        uncategorized.forEach((item, idx) => container.appendChild(createMenuItemCard(item, idx)));
    }
}

// ─────────────────────────────────────────
//   CREATE MENU ITEM CARD
// ─────────────────────────────────────────
function createMenuItemCard(item, index) {
    const emoji     = FOOD_EMOJIS[item.id % FOOD_EMOJIS.length];
    const available = item.available !== false;
    const card      = document.createElement('div');
    card.className  = `menu-item-card${available ? '' : ' item-unavailable'}`;
    card.style.animationDelay = `${index * 50}ms`;
    card.id = `menu-card-${item.id}`;
    card.innerHTML = `
        <div class="menu-item-emoji">${emoji}</div>
        <div class="menu-item-body">
            <div>
                <div class="item-name">${escapeHtml(item.name)}</div>
                <div class="item-desc">${escapeHtml(item.description || 'A delicious treat!')}</div>
            </div>
            <div class="item-footer">
                <div class="item-price">₹${Number(item.price).toFixed(2)}</div>
                ${available
        ? `<button class="btn-add" id="add-btn-${item.id}" onclick="addToCart(${item.id}, '${escapeHtml(item.name)}', ${item.price})">
                            <span>+</span> Add to Cart
                       </button>`
        : `<span class="unavailable-badge">Unavailable</span>`
    }
            </div>
        </div>`;
    return card;
}

// ─────────────────────────────────────────
//   ADD TO CART — real backend
//   POST /api/cart/add
//   Body: { menuItemId, quantity }
// ─────────────────────────────────────────
async function addToCart(itemId, itemName, price) {
    const btn = document.getElementById(`add-btn-${itemId}`);
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = `<span class="cart-spin"></span> Adding...`;
    }

    try {
        const res = await apiFetch('/api/cart/add', {
            method: 'POST',
            body: JSON.stringify({ menuItemId: itemId, quantity: 1 })
        });

        if (!res.ok) {
            let message = 'Something went wrong';

            try {
                const errJson = await res.json(); // ✅ parse JSON directly
                message = errJson.message || message;
            } catch {
                const text = await res.text();
                message = text || message;
            }

            //  CLEAN CUSTOM MESSAGE
            if (message.toLowerCase().includes('one restaurant')) {
                message = 'You can add food items from one restaurant only.';
            }

            if (res.status === 401 || res.status === 403) {
                showToast('error', '🔒', 'Session expired. Please log in again.');
                setTimeout(() => logout(), 1500);
            } else {
                showToast('error', '⚠️', message);
            }

            return;
        }

        const cart = await res.json();

        if (cart.items) {
            const total = cart.items.reduce((s, i) => s + i.quantity, 0);
            updateCartBadge(total);
        }

        showToast('success', '🛒', `"${itemName}" added to cart!`);

        const badge = document.getElementById('cartCount');
        if (badge) {
            badge.style.transform = 'scale(1.5)';
            setTimeout(() => badge.style.transform = '', 200);
        }

    } catch (err) {
        console.error('Add to cart error:', err);
        showToast('error', '❌', 'Could not connect to server.');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = `<span>+</span> Add to Cart`;
        }
    }
}

// ─────────────────────────────────────────
//   FILTER
// ─────────────────────────────────────────
function filterByCategory(categoryId) {
    activeCategory = categoryId;
    document.querySelectorAll('.category-pill').forEach(p => p.classList.remove('active'));
    const pill = document.getElementById(`pill-${categoryId}`);
    if (pill) pill.classList.add('active');
    if (categoryId === 'all') {
        document.querySelectorAll('.category-section').forEach(s => s.style.display = '');
    } else {
        document.querySelectorAll('.category-section').forEach(s => {
            s.style.display = s.id === `section-${categoryId}` ? '' : 'none';
        });
    }
}

// ─────────────────────────────────────────
//   TOAST
// ─────────────────────────────────────────
let toastTimer;
function showToast(type, icon, message) {
    clearTimeout(toastTimer);
    const toast = document.getElementById('toast');
    document.getElementById('toastIcon').textContent = icon;
    document.getElementById('toastMsg').textContent  = message;
    toast.className = `show ${type}`;
    toastTimer = setTimeout(() => { toast.className = type; }, 2800);
}

// ─────────────────────────────────────────
//   SKELETON / STATES
// ─────────────────────────────────────────
function showSkeleton() {
    document.getElementById('skeletonMenu').classList.remove('hidden');
    document.getElementById('menuContent').classList.add('hidden');
    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('errorState').classList.add('hidden');
}
function hideSkeleton() { document.getElementById('skeletonMenu').classList.add('hidden'); }
function showEmpty() {
    document.getElementById('emptyState').classList.remove('hidden');
    document.getElementById('menuContent').classList.add('hidden');
}
function showError(msg) {
    document.getElementById('errorState').classList.remove('hidden');
    document.getElementById('errorMsg').textContent = msg;
}

// ─────────────────────────────────────────
//   HELPERS
// ─────────────────────────────────────────
function getCategoryEmoji(name) {
    const n = (name || '').toLowerCase();
    if (n.includes('starter') || n.includes('appetizer')) return '🥗';
    if (n.includes('main') || n.includes('course'))        return '🍛';
    if (n.includes('veg'))                                  return '🥦';
    if (n.includes('non') || n.includes('chicken'))        return '🍗';
    if (n.includes('pizza'))   return '🍕';
    if (n.includes('burger'))  return '🍔';
    if (n.includes('noodle') || n.includes('pasta')) return '🍜';
    if (n.includes('dessert') || n.includes('sweet')) return '🍰';
    if (n.includes('drink') || n.includes('beverage')) return '🥤';
    if (n.includes('snack')) return '🍟';
    return '🍽️';
}
function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// Spinner CSS for add button
const s = document.createElement('style');
s.textContent = `
    @keyframes spin { to { transform: rotate(360deg); } }
    .cart-spin {
        display: inline-block; width: 13px; height: 13px;
        border: 2px solid rgba(255,255,255,.4);
        border-top-color: white; border-radius: 50%;
        animation: spin .6s linear infinite;
    }
`;
document.head.appendChild(s);