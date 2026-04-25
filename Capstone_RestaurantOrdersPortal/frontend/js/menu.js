/**
 * ============================================
 *   Food Mania — Menu Page JS
 * ============================================
 *
 */
/**  These emojis just for testing purpose after that i will change with images **/
const FOOD_EMOJIS   = ['🍕','🍔','🍜','🍣','🌮','🍛','🍱','🥗','🍗','🥪','🧆','🥘','🍲','🥙','🌯'];
const UNAVAIL_EMOJI = '🚫';

let allCategories = [];
let allMenuItems  = [];
let activeCategory = 'all'; // 'all' or category id as string

// ─────────────────────────────────────────
//   PAGE INIT
// ─────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    requireAuth();
    setupNavbar();
    loadMenuData();

    // Close dropdown on outside click
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
//   GET RESTAURANT ID
// ─────────────────────────────────────────

function getRestaurantId() {
    // Try URL param first: menu.html?restaurantId=5
    const params = new URLSearchParams(window.location.search);
    const fromUrl = params.get('restaurantId');
    if (fromUrl) return fromUrl;

    // Fallback to localStorage (set by restaurants page)
    return localStorage.getItem('fm_restaurant_id');
}

// ─────────────────────────────────────────
//   LOAD ALL DATA
// ─────────────────────────────────────────

async function loadMenuData() {
    const restaurantId = getRestaurantId();

    if (!restaurantId) {
        showError('No restaurant selected. Please go back and choose a restaurant.');
        hideSkeleton();
        return;
    }

    showSkeleton();

    try {
        /**
         * Parallel fetch:
         * GET /api/restaurants/{id}
         * GET /api/restaurants/{id}/categories  (public — no token needed)
         * GET /api/restaurants/{id}/menu-items  (public — no token needed)
         */
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

        // Render hero
        renderHero(restaurant);

        // Render category pills
        renderCategoryPills(allCategories);

        // Render menu items
        if (allMenuItems.length === 0) {
            showEmpty();
        } else {
            renderMenu(allMenuItems, allCategories);
        }

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
    document.getElementById('heroEmoji').textContent        = emoji;
    document.getElementById('restaurantName').textContent   = restaurant.name;
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

    // "All" pill
    const allPill = document.createElement('button');
    allPill.className = 'category-pill active';
    allPill.textContent = '🍽️ All Items';
    allPill.onclick = () => filterByCategory('all');
    allPill.id = 'pill-all';
    bar.appendChild(allPill);

    // One pill per category
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
//   Groups items by category, shows sections
// ─────────────────────────────────────────

function renderMenu(items, categories) {
    const content = document.getElementById('menuContent');
    content.innerHTML = '';
    content.classList.remove('hidden');
    document.getElementById('emptyState').classList.add('hidden');

    // Build a map: categoryId → category name
    const catMap = {};
    categories.forEach(c => { catMap[c.id] = c.name; });

    // Group items by categoryId
    const groups = {};
    const uncategorized = [];

    items.forEach(item => {
        if (item.categoryId && catMap[item.categoryId]) {
            if (!groups[item.categoryId]) groups[item.categoryId] = [];
            groups[item.categoryId].push(item);
        } else {
            uncategorized.push(item);
        }
    });

    // Render each category section
    categories.forEach((cat, idx) => {
        const catItems = groups[cat.id];
        if (!catItems || catItems.length === 0) return;

        const section = document.createElement('div');
        section.id = `section-${cat.id}`;
        section.className = 'category-section';

        section.innerHTML = `
            <div class="category-section-title">
                <span>${getCategoryEmoji(cat.name)}</span>
                <span>${escapeHtml(cat.name)}</span>
                <span class="text-sm font-dm font-normal text-ash">(${catItems.length} item${catItems.length !== 1 ? 's' : ''})</span>
            </div>
            <div class="space-y-3" id="items-${cat.id}"></div>
        `;

        content.appendChild(section);

        const itemsContainer = document.getElementById(`items-${cat.id}`);
        catItems.forEach((item, itemIdx) => {
            const card = createMenuItemCard(item, itemIdx);
            itemsContainer.appendChild(card);
        });
    });

    // Uncategorized items
    if (uncategorized.length > 0) {
        const section = document.createElement('div');
        section.id = 'section-uncategorized';
        section.className = 'category-section';
        section.innerHTML = `
            <div class="category-section-title">
                <span>🍴</span>
                <span>More Items</span>
            </div>
            <div class="space-y-3" id="items-uncategorized"></div>
        `;
        content.appendChild(section);
        const container = document.getElementById('items-uncategorized');
        uncategorized.forEach((item, idx) => {
            container.appendChild(createMenuItemCard(item, idx));
        });
    }
}

// ─────────────────────────────────────────
//   CREATE MENU ITEM CARD
// ─────────────────────────────────────────

function createMenuItemCard(item, index) {
    const emoji = FOOD_EMOJIS[item.id % FOOD_EMOJIS.length];
    const delay = index * 50;
    const available = item.available !== false; // default to true if null

    const card = document.createElement('div');
    card.className = `menu-item-card${available ? '' : ' item-unavailable'}`;
    card.style.animationDelay = `${delay}ms`;

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
        ? `<button class="btn-add" onclick="addToCart(${item.id}, '${escapeHtml(item.name)}', ${item.price})">
                            <span>+</span> Add to Cart
                       </button>`
        : `<span class="unavailable-badge">Unavailable</span>`
    }
            </div>
        </div>
    `;
    return card;
}

// ─────────────────────────────────────────
//   FILTER BY CATEGORY
// ─────────────────────────────────────────

function filterByCategory(categoryId) {
    activeCategory = categoryId;

    // Update pill styles
    document.querySelectorAll('.category-pill').forEach(p => p.classList.remove('active'));
    const activePill = document.getElementById(`pill-${categoryId}`);
    if (activePill) activePill.classList.add('active');

    // Show/hide sections
    if (categoryId === 'all') {
        document.querySelectorAll('.category-section').forEach(s => s.style.display = '');
    } else {
        document.querySelectorAll('.category-section').forEach(s => {
            s.style.display = s.id === `section-${categoryId}` ? '' : 'none';
        });
    }
}

// ─────────────────────────────────────────
//   ADD TO CART (toast only — backend later)
// ─────────────────────────────────────────

function addToCart(itemId, itemName, price) {
    // Show toast — cart backend will be connected later
    showToast('success', '🛒', `"${itemName}" added to cart!`);

    // Animate cart count (UI only)
    const cartCount = document.getElementById('cartCount');
    cartCount.classList.remove('hidden');
    const current = parseInt(cartCount.textContent) || 0;
    cartCount.textContent = current + 1;
    cartCount.style.transform = 'scale(1.4)';
    setTimeout(() => cartCount.style.transform = '', 200);
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

    toastTimer = setTimeout(() => {
        toast.className = type;
    }, 2500);
}

// ─────────────────────────────────────────
//   CATEGORY EMOJI HELPER
// ─────────────────────────────────────────

function getCategoryEmoji(name) {
    const n = (name || '').toLowerCase();
    if (n.includes('starter') || n.includes('appetizer')) return '🥗';
    if (n.includes('main') || n.includes('course'))       return '🍛';
    if (n.includes('veg'))                                 return '🥦';
    if (n.includes('non') || n.includes('chicken') || n.includes('meat')) return '🍗';
    if (n.includes('pizza'))   return '🍕';
    if (n.includes('burger'))  return '🍔';
    if (n.includes('noodle') || n.includes('pasta')) return '🍜';
    if (n.includes('dessert') || n.includes('sweet')) return '🍰';
    if (n.includes('drink') || n.includes('beverage')) return '🥤';
    return '🍽️';
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

function hideSkeleton() {
    document.getElementById('skeletonMenu').classList.add('hidden');
}

function showEmpty() {
    document.getElementById('emptyState').classList.remove('hidden');
    document.getElementById('menuContent').classList.add('hidden');
}

function showError(msg) {
    document.getElementById('errorState').classList.remove('hidden');
    document.getElementById('errorMsg').textContent = msg;
}

// ─────────────────────────────────────────
//   HELPER
// ─────────────────────────────────────────

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}