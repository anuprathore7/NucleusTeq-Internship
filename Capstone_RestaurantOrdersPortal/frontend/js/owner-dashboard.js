
/**
 * ============================================
 *   Food Mania — Owner Dashboard JS (UPDATED)
 *   Added: Orders panel with Accept / status flow
 * ============================================
 */

// ─────────────────────────────────────────
//   STATE
// ─────────────────────────────────────────
let myRestaurants      = [];
let selectedRestaurant = null;
let allCategories      = [];
let allMenuItems       = [];
let allOrders          = [];         // ← NEW
let editingCategoryId  = null;
let editingMenuItemId  = null;
let confirmCallback    = null;
let ordersAutoRefresh  = null;       // ← NEW: interval handle

// ─────────────────────────────────────────
//   STATUS LABELS (matches OrderStatus enum)
// ─────────────────────────────────────────
const STATUS_LABELS = {
    PLACED:           { label: 'Placed',          color: '#2563EB', bg: '#EFF4FF', border: '#BFDBFE' },
    PENDING:          { label: 'Pending',          color: '#C2410C', bg: '#FFF7ED', border: '#FED7AA' },
    ACCEPTED:         { label: 'Accepted',         color: '#15803D', bg: '#F0FDF4', border: '#BBF7D0' },
    OUT_FOR_DELIVERY: { label: 'Out for Delivery', color: '#4338CA', bg: '#EEF2FF', border: '#C7D2FE' },
    COMPLETED:        { label: 'Completed',        color: '#166534', bg: '#F0FDF4', border: '#BBF7D0' },
    CANCELLED:        { label: 'Cancelled',        color: '#C0392B', bg: '#FDF0EF', border: '#F5C6C3' },
};

// What button to show for each status (owner action)
const NEXT_ACTION = {
    PLACED:           { label: '✅ Accept Order',        next: 'PENDING'          },
    PENDING:          { label: '🍳 Start Preparing',     next: 'ACCEPTED'         },
    ACCEPTED:         { label: '🛵 Out for Delivery',    next: 'OUT_FOR_DELIVERY' },
    OUT_FOR_DELIVERY: { label: '🎉 Mark Delivered',      next: 'COMPLETED'        },
};

// ─────────────────────────────────────────
//   PAGE INIT
// ─────────────────────────────────────────
history.pushState(null, '', window.location.href);
window.addEventListener('popstate', () => history.pushState(null, '', window.location.href));

document.addEventListener('DOMContentLoaded', () => {
    requireAuth();

    const role = getRole();
    if (role !== 'RESTAURANT_OWNER') {
        alert('Access denied. This page is for restaurant owners only.');
        window.location.href = 'restaurants.html';
        return;
    }

    setupNavbar();
    loadMyRestaurants();

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
    document.getElementById('userEmailNav').textContent  = email.split('@')[0];
    document.getElementById('userEmailDrop').textContent = email;
}

function toggleUserMenu() {
    document.getElementById('userDropdown').classList.toggle('hidden');
}

// ─────────────────────────────────────────
//   OWNER ID
// ─────────────────────────────────────────
function getOwnerId() {
    const token = getToken();
    if (!token) return null;
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.sub;
    } catch { return null; }
}

// ─────────────────────────────────────────
//   LOAD MY RESTAURANTS
// ─────────────────────────────────────────
async function loadMyRestaurants() {
    try {
        const ownerId = getOwnerId();
        if (!ownerId) {
            document.getElementById('sidebarRestaurantList').innerHTML =
                `<p class="text-xs text-red-500 px-2">Session error. Please login again.</p>`;
            return;
        }

        const res = await apiFetch(`/api/restaurants/owner`);
        if (!res.ok) throw new Error(`Server error ${res.status}`);
        const data = await res.json();
        myRestaurants = Array.isArray(data) ? data : [];
        renderSidebarRestaurants(myRestaurants);

    } catch (err) {
        console.error('Failed to load restaurants:', err);
        document.getElementById('sidebarRestaurantList').innerHTML =
            `<p class="text-xs text-red-500 px-2">Failed to load restaurants</p>`;
    }
}

// ─────────────────────────────────────────
//   SIDEBAR RESTAURANT LIST
// ─────────────────────────────────────────
function renderSidebarRestaurants(restaurants) {
    const list = document.getElementById('sidebarRestaurantList');
    list.innerHTML = '';

    if (restaurants.length === 0) {
        list.innerHTML = `<p class="text-xs text-ash px-2 mb-2">No restaurants yet.</p>`;
        return;
    }

    restaurants.forEach(r => {
        const card = document.createElement('div');
        card.className = `restaurant-card-side${selectedRestaurant?.id === r.id ? ' selected' : ''}`;
        card.id = `rest-card-${r.id}`;
        card.onclick = () => selectRestaurant(r);
        card.innerHTML = `
            <div class="font-dm font-semibold text-sm text-ink truncate">${escapeHtml(r.name)}</div>
            <div class="text-xs text-ash truncate mt-0.5">📍 ${escapeHtml(r.address)}</div>
        `;
        list.appendChild(card);
    });
}

// ─────────────────────────────────────────
//   SELECT RESTAURANT
// ─────────────────────────────────────────
async function selectRestaurant(restaurant) {
    selectedRestaurant = restaurant;

    document.querySelectorAll('.restaurant-card-side').forEach(c => c.classList.remove('selected'));
    const card = document.getElementById(`rest-card-${restaurant.id}`);
    if (card) card.classList.add('selected');

    document.getElementById('sidebarNav').classList.remove('hidden');
    document.getElementById('panelSelectPrompt').classList.add('hidden');

    document.getElementById('editRestName').value    = restaurant.name || '';
    document.getElementById('editRestDesc').value    = restaurant.description || '';
    document.getElementById('editRestAddress').value = restaurant.address || '';
    document.getElementById('editRestPhone').value   = restaurant.phone || '';

    await Promise.all([loadCategories(), loadMenuItems(), loadOrders()]);
    switchPanel('overview');

    // Auto refresh orders every 20s when a restaurant is selected
    if (ordersAutoRefresh) clearInterval(ordersAutoRefresh);
    ordersAutoRefresh = setInterval(() => {
        if (selectedRestaurant) loadOrders(true); // silent refresh
    }, 20000);
}

// ─────────────────────────────────────────
//   LOAD CATEGORIES
// ─────────────────────────────────────────
async function loadCategories() {
    if (!selectedRestaurant) return;
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}/categories`);
        if (!res.ok) throw new Error(res.status);
        allCategories = await res.json();
        renderCategoriesTable(allCategories);
        updateStats();
    } catch (err) { console.error('Failed to load categories:', err); }
}

// ─────────────────────────────────────────
//   LOAD MENU ITEMS
// ─────────────────────────────────────────
async function loadMenuItems() {
    if (!selectedRestaurant) return;
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}/menu-items`);
        if (!res.ok) throw new Error(res.status);
        allMenuItems = await res.json();
        renderMenuItemsTable(allMenuItems);
        updateStats();
    } catch (err) { console.error('Failed to load menu items:', err); }
}

// ─────────────────────────────────────────
//   LOAD ORDERS  ← NEW
//   GET /api/orders/restaurant/{id}
// ─────────────────────────────────────────
async function loadOrders(silent = false) {
    if (!selectedRestaurant) return;
    try {
        const res = await apiFetch(`/api/orders/restaurant/${selectedRestaurant.id}`);
        if (!res.ok) throw new Error(res.status);
        allOrders = await res.json();
        renderOrdersPanel(allOrders);
        updateStats();
        if (!silent) updateOrdersBadge();
    } catch (err) { console.error('Failed to load orders:', err); }
}

// ─────────────────────────────────────────
//   RENDER ORDERS PANEL  ← NEW
// ─────────────────────────────────────────
function renderOrdersPanel(orders) {
    const wrap = document.getElementById('ordersTableWrap');
    if (!wrap) return;

    // Count active orders for badge
    const activeCount = orders.filter(o =>
        ['PLACED','PENDING','ACCEPTED','OUT_FOR_DELIVERY'].includes(o.status)
    ).length;

    if (orders.length === 0) {
        wrap.innerHTML = `
            <div class="text-center py-12">
                <div class="text-5xl mb-3">📋</div>
                <p class="font-playfair text-lg font-bold text-ink mb-1">No orders yet</p>
                <p class="text-ash text-sm font-dm">Orders from customers will appear here</p>
            </div>`;
        return;
    }

    // Group: active first, then history
    const active  = orders.filter(o => !['COMPLETED','CANCELLED'].includes(o.status));
    const history = orders.filter(o =>  ['COMPLETED','CANCELLED'].includes(o.status));

    wrap.innerHTML = '';

    if (active.length > 0) {
        const section = document.createElement('div');
        section.innerHTML = `
            <div class="flex items-center gap-3 mb-4">
                <h3 class="font-dm font-bold text-ink text-sm uppercase tracking-wider">Active Orders</h3>
                <span class="bg-red-100 text-red-700 text-xs font-bold px-2 py-0.5 rounded-full">${active.length}</span>
            </div>`;
        active.forEach(order => section.appendChild(buildOrderRow(order)));
        wrap.appendChild(section);
    }

    if (history.length > 0) {
        const section = document.createElement('div');
        section.className = active.length > 0 ? 'mt-8' : '';
        section.innerHTML = `
            <div class="flex items-center gap-3 mb-4">
                <h3 class="font-dm font-bold text-ash text-sm uppercase tracking-wider">Order History</h3>
                <span class="bg-gray-100 text-gray-600 text-xs font-bold px-2 py-0.5 rounded-full">${history.length}</span>
            </div>`;
        history.forEach(order => section.appendChild(buildOrderRow(order)));
        wrap.appendChild(section);
    }
}

function buildOrderRow(order) {
    const s = STATUS_LABELS[order.status] || { label: order.status, color: '#888', bg: '#f3f4f6', border: '#e5e7eb' };
    const action = NEXT_ACTION[order.status];
    const date = formatDate(order.createdAt);

    const itemsSummary = (order.items || [])
        .map(i => `${escapeHtml(i.itemName)} ×${i.quantity}`)
        .join(', ');

    const card = document.createElement('div');
    card.className = 'border border-gray-100 rounded-2xl p-4 mb-3 bg-white hover:shadow-md transition-shadow';
    card.id = `owner-order-${order.orderId}`;

    card.innerHTML = `
        <div class="flex items-start justify-between gap-3 flex-wrap">
            <div class="flex-1 min-w-0">
                <div class="flex items-center gap-3 mb-1 flex-wrap">
                    <span class="font-playfair font-bold text-ink text-base">Order #${order.orderId}</span>
                    <span style="background:${s.bg};color:${s.color};border:1px solid ${s.border}"
                          class="text-xs font-bold px-3 py-0.5 rounded-full font-dm">
                        ${s.label}
                    </span>
                </div>
                <p class="text-xs text-ash font-dm mb-1">🕐 ${date}</p>
                <p class="text-sm text-ink font-dm truncate">${itemsSummary}</p>
            </div>
            <div class="text-right flex-shrink-0">
                <p class="font-playfair font-bold text-burgundy text-lg">₹${order.totalAmount.toFixed(2)}</p>
                <div class="flex gap-2 mt-2 justify-end flex-wrap">
                    ${action ? `
                    <button onclick="advanceOrderStatus(${order.orderId}, '${action.next}')"
                            class="text-xs font-dm font-semibold px-3 py-1.5 rounded-full bg-burgundy text-white hover:bg-burgundy-dk transition whitespace-nowrap">
                        ${action.label}
                    </button>` : ''}
                    ${order.status !== 'COMPLETED' && order.status !== 'CANCELLED' ? `
                    <button onclick="cancelOrderAsOwner(${order.orderId})"
                            class="text-xs font-dm font-semibold px-3 py-1.5 rounded-full border border-red-300 text-red-600 hover:bg-red-50 transition whitespace-nowrap">
                        ✕ Cancel
                    </button>` : ''}
                </div>
            </div>
        </div>

        <!-- Items detail (collapsed) -->
        <div class="mt-3 pt-3 border-t border-gray-50">
            ${(order.items || []).map(item => `
            <div class="flex justify-between text-xs font-dm text-ash py-0.5">
                <span>${escapeHtml(item.itemName)} <span class="text-gray-400">×${item.quantity}</span></span>
                <span>₹${item.subtotal.toFixed(2)}</span>
            </div>`).join('')}
        </div>
    `;

    return card;
}

// ─────────────────────────────────────────
//   ADVANCE ORDER STATUS  ← NEW
//   PATCH /api/orders/status/{orderId}?status=XXX
// ─────────────────────────────────────────
async function advanceOrderStatus(orderId, newStatus) {
    try {
        const res = await apiFetch(`/api/orders/status/${orderId}?status=${newStatus}`, {
            method: 'PATCH'
        });

        if (!res.ok) {
            const err = await res.json();
            showToast('error', '❌', err.message || 'Failed to update order status');
            return;
        }

        const STATUS_TOASTS = {
            PENDING:          '⏳ Order marked as Pending!',
            ACCEPTED:         '✅ Order Accepted!',
            OUT_FOR_DELIVERY: '🛵 Order is Out for Delivery!',
            COMPLETED:        '🎉 Order marked as Delivered!',
        };

        showToast('success', '✅', STATUS_TOASTS[newStatus] || 'Status updated!');
        await loadOrders(true);
        updateStats();

    } catch (err) {
        showToast('error', '❌', 'Failed to update order status');
    }
}

// ─────────────────────────────────────────
//   CANCEL ORDER AS OWNER  ← NEW
// ─────────────────────────────────────────
async function cancelOrderAsOwner(orderId) {
    if (!confirm('Cancel this order? The customer will be refunded.')) return;
    await advanceOrderStatus(orderId, 'CANCELLED');
}

// ─────────────────────────────────────────
//   ORDERS BADGE  ← NEW
// ─────────────────────────────────────────
function updateOrdersBadge() {
    const badge = document.getElementById('ordersNavBadge');
    if (!badge) return;
    const active = allOrders.filter(o =>
        ['PLACED','PENDING','ACCEPTED','OUT_FOR_DELIVERY'].includes(o.status)
    ).length;
    badge.textContent = active;
    badge.style.display = active > 0 ? 'flex' : 'none';
}

// ─────────────────────────────────────────
//   UPDATE STATS
// ─────────────────────────────────────────
function updateStats() {
    document.getElementById('overviewRestName').textContent = selectedRestaurant?.name || '--';
    document.getElementById('statCategories').textContent   = allCategories.length;
    document.getElementById('statMenuItems').textContent    = allMenuItems.length;
    document.getElementById('statAvailable').textContent    = allMenuItems.filter(i => i.available !== false).length;

    // Orders stat
    const statOrdersEl = document.getElementById('statOrders');
    if (statOrdersEl) {
        const active = allOrders.filter(o => ['PLACED','PENDING','ACCEPTED','OUT_FOR_DELIVERY'].includes(o.status)).length;
        statOrdersEl.textContent = active;
    }
}

// ─────────────────────────────────────────
//   PANEL SWITCHING (updated to include orders)
// ─────────────────────────────────────────
function switchPanel(panelName) {
    ['SelectPrompt','Overview','Categories','Menu','Restaurant','Orders'].forEach(p => {
        const el = document.getElementById(`panel${p}`);
        if (el) { el.classList.add('hidden'); el.classList.remove('active'); }
    });

    const el = document.getElementById(`panel${capitalize(panelName)}`);
    if (el) { el.classList.remove('hidden'); el.classList.add('active'); }

    ['Overview','Categories','Menu','Restaurant','Orders'].forEach(p => {
        const btn = document.getElementById(`nav${p}`);
        if (btn) btn.classList.remove('active');
    });
    const activeBtn = document.getElementById(`nav${capitalize(panelName)}`);
    if (activeBtn) activeBtn.classList.add('active');

    // Reload orders fresh when switching to orders panel
    if (panelName === 'orders') loadOrders();
}

function capitalize(str) { return str.charAt(0).toUpperCase() + str.slice(1); }

// ─────────────────────────────────────────
//   ALL EXISTING FUNCTIONS BELOW (unchanged)
// ─────────────────────────────────────────

function renderCategoriesTable(categories) {
    const wrap = document.getElementById('categoriesTableWrap');
    if (categories.length === 0) {
        wrap.innerHTML = `<div class="text-center py-10"><div class="text-5xl mb-3">🏷️</div><p class="font-playfair text-lg font-bold text-ink mb-1">No categories yet</p><p class="text-ash text-sm font-dm">Add categories to organise your menu</p></div>`;
        return;
    }
    wrap.innerHTML = `<table class="data-table"><thead><tr><th>Name</th><th>ID</th><th>Actions</th></tr></thead><tbody id="categoriesTbody"></tbody></table>`;
    const tbody = document.getElementById('categoriesTbody');
    categories.forEach(cat => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td class="font-semibold">${escapeHtml(cat.name)}</td><td class="text-ash text-xs">#${cat.id}</td><td><div class="flex items-center gap-2"><button onclick="openEditCategoryModal(${cat.id}, '${escapeHtml(cat.name)}')" class="btn-secondary-sm">✏️ Edit</button><button onclick="confirmDeleteCategory(${cat.id}, '${escapeHtml(cat.name)}')" class="btn-danger-sm">🗑️</button></div></td>`;
        tbody.appendChild(tr);
    });
}

function renderMenuItemsTable(items) {
    const wrap = document.getElementById('menuTableWrap');
    if (items.length === 0) {
        wrap.innerHTML = `<div class="text-center py-10"><div class="text-5xl mb-3">🍽️</div><p class="font-playfair text-lg font-bold text-ink mb-1">No menu items yet</p><p class="text-ash text-sm font-dm">Add your first food item</p></div>`;
        return;
    }
    const catMap = {};
    allCategories.forEach(c => { catMap[c.id] = c.name; });
    wrap.innerHTML = `<table class="data-table"><thead><tr><th>Item</th><th>Category</th><th>Price</th><th>Status</th><th>Actions</th></tr></thead><tbody id="menuTbody"></tbody></table>`;
    const tbody = document.getElementById('menuTbody');
    items.forEach(item => {
        const catName = item.categoryId ? (catMap[item.categoryId] || `#${item.categoryId}`) : '—';
        const available = item.available !== false;
        const tr = document.createElement('tr');
        tr.innerHTML = `<td><div class="font-semibold text-ink">${escapeHtml(item.name)}</div><div class="text-xs text-ash mt-0.5">${escapeHtml(item.description || '')}</div></td><td class="text-ash text-sm">${escapeHtml(catName)}</td><td class="font-semibold text-burgundy font-dm">₹${Number(item.price).toFixed(2)}</td><td><span class="${available ? 'badge-avail' : 'badge-unavail'}">${available ? '✅ Available' : '🚫 Unavailable'}</span></td><td><div class="flex items-center gap-2"><button onclick="openEditMenuItemModal(${item.id})" class="btn-secondary-sm">✏️ Edit</button><button onclick="confirmDeleteMenuItem(${item.id}, '${escapeHtml(item.name)}')" class="btn-danger-sm">🗑️</button></div></td>`;
        tbody.appendChild(tr);
    });
}

function openAddRestaurantModal() {
    document.getElementById('modalRestaurantTitle').textContent = 'Add Restaurant';
    ['mRestName','mRestDesc','mRestAddress','mRestPhone'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('mRestImage').value = '';
    openModal('modalRestaurant');
}

async function submitRestaurant() {
    const name = document.getElementById('mRestName').value.trim();
    const description = document.getElementById('mRestDesc').value.trim();
    const address = document.getElementById('mRestAddress').value.trim();
    const phone = document.getElementById('mRestPhone').value.trim();
    const imageFile = document.getElementById('mRestImage').files[0];
    if (!name || !address || !phone) { showToast('error', '⚠️', 'Name, address and phone are required.'); return; }
    try {
        const res = await apiFetch('/api/restaurants', { method: 'POST', body: JSON.stringify({ name, description, address, phone }) });
        if (!res.ok) throw new Error((await res.text()) || `Error ${res.status}`);
        let newRestaurant = await res.json();
        if (imageFile) {
            showToast('info', '⏳', 'Uploading image...');
            const formData = new FormData();
            formData.append('file', imageFile);
            const imgRes = await fetch(`http://localhost:8000/api/restaurants/${newRestaurant.id}/image`, { method: 'POST', headers: { 'Authorization': `Bearer ${getToken()}` }, body: formData });
            if (!imgRes.ok) { showToast('error', '⚠️', 'Restaurant saved, but image upload failed.'); }
            else { newRestaurant = await imgRes.json(); showToast('success', '✅', `"${name}" added with image!`); }
        }
        closeModal('modalRestaurant');
        await loadMyRestaurants();
        selectRestaurant(newRestaurant);
    } catch (err) { showToast('error', '❌', err.message || 'Failed to add restaurant.'); }
}

async function updateRestaurant() {
    if (!selectedRestaurant) return;
    const name = document.getElementById('editRestName').value.trim();
    const description = document.getElementById('editRestDesc').value.trim();
    const address = document.getElementById('editRestAddress').value.trim();
    const phone = document.getElementById('editRestPhone').value.trim();
    if (!name || !address || !phone) { showToast('error', '⚠️', 'Name, address and phone are required.'); return; }
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}`, { method: 'PUT', body: JSON.stringify({ name, description, address, phone }) });
        if (!res.ok) throw new Error((await res.text()) || `Error ${res.status}`);
        selectedRestaurant = await res.json();
        showToast('success', '✅', 'Restaurant updated successfully!');
        await loadMyRestaurants();
        renderSidebarRestaurants(myRestaurants);
    } catch (err) { showToast('error', '❌', err.message || 'Failed to update restaurant.'); }
}

function confirmDeleteRestaurant() {
    if (!selectedRestaurant) return;
    document.getElementById('confirmMsg').textContent = `Are you sure you want to delete "${selectedRestaurant.name}"?`;
    confirmCallback = deleteRestaurant;
    openModal('modalConfirm');
}

async function deleteRestaurant() {
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error(`Error ${res.status}`);
        showToast('success', '✅', 'Restaurant deleted.');
        closeModal('modalConfirm');
        selectedRestaurant = null; allCategories = []; allMenuItems = []; allOrders = [];
        if (ordersAutoRefresh) clearInterval(ordersAutoRefresh);
        await loadMyRestaurants();
        document.getElementById('sidebarNav').classList.add('hidden');
        switchPanel('SelectPrompt');
    } catch (err) { showToast('error', '❌', err.message || 'Failed to delete restaurant.'); }
}

function openAddCategoryModal() {
    editingCategoryId = null;
    document.getElementById('modalCategoryTitle').textContent = 'Add Category';
    document.getElementById('mCatName').value = '';
    openModal('modalCategory');
}

function openEditCategoryModal(id, name) {
    editingCategoryId = id;
    document.getElementById('modalCategoryTitle').textContent = 'Edit Category';
    document.getElementById('mCatName').value = name;
    openModal('modalCategory');
}

async function submitCategory() {
    const name = document.getElementById('mCatName').value.trim();
    if (!name) { showToast('error', '⚠️', 'Category name is required.'); return; }
    if (!selectedRestaurant) return;
    const rid = selectedRestaurant.id;
    try {
        const url = editingCategoryId ? `/api/restaurants/${rid}/categories/${editingCategoryId}` : `/api/restaurants/${rid}/categories`;
        const method = editingCategoryId ? 'PUT' : 'POST';
        const res = await apiFetch(url, { method, body: JSON.stringify({ name }) });
        if (!res.ok) throw new Error((await res.text()) || `Error ${res.status}`);
        closeModal('modalCategory');
        showToast('success', '✅', editingCategoryId ? 'Category updated!' : 'Category added!');
        await loadCategories();
    } catch (err) { showToast('error', '❌', err.message || 'Failed to save category.'); }
}

function confirmDeleteCategory(id, name) {
    document.getElementById('confirmMsg').textContent = `Delete category "${name}"?`;
    confirmCallback = () => deleteCategory(id);
    openModal('modalConfirm');
}

async function deleteCategory(categoryId) {
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}/categories/${categoryId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error(`Error ${res.status}`);
        closeModal('modalConfirm');
        showToast('success', '✅', 'Category deleted.');
        await loadCategories();
    } catch (err) { showToast('error', '❌', err.message || 'Failed to delete category.'); }
}

function populateCategoryDropdown(selectedCatId) {
    const select = document.getElementById('mItemCategory');
    select.innerHTML = '<option value="">-- No Category --</option>';
    allCategories.forEach(cat => {
        const opt = document.createElement('option');
        opt.value = cat.id; opt.textContent = cat.name;
        if (selectedCatId && String(cat.id) === String(selectedCatId)) opt.selected = true;
        select.appendChild(opt);
    });
}

function openAddMenuItemModal() {
    editingMenuItemId = null;
    document.getElementById('modalMenuItemTitle').textContent = 'Add Menu Item';
    ['mItemName','mItemDesc','mItemPrice'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('mItemAvailable').value = 'true';
    populateCategoryDropdown(null);
    openModal('modalMenuItem');
}

function openEditMenuItemModal(itemId) {
    const item = allMenuItems.find(i => i.id === itemId);
    if (!item) return;
    editingMenuItemId = itemId;
    document.getElementById('modalMenuItemTitle').textContent = 'Edit Menu Item';
    document.getElementById('mItemName').value      = item.name || '';
    document.getElementById('mItemDesc').value      = item.description || '';
    document.getElementById('mItemPrice').value     = item.price || '';
    document.getElementById('mItemAvailable').value = item.available !== false ? 'true' : 'false';
    populateCategoryDropdown(item.categoryId);
    openModal('modalMenuItem');
}

async function submitMenuItem() {
    const name = document.getElementById('mItemName').value.trim();
    const description = document.getElementById('mItemDesc').value.trim();
    const price = parseFloat(document.getElementById('mItemPrice').value);
    const categoryId = document.getElementById('mItemCategory').value || null;
    const available = document.getElementById('mItemAvailable').value === 'true';
    if (!name || isNaN(price) || price < 0) { showToast('error', '⚠️', 'Name and a valid price are required.'); return; }
    if (!selectedRestaurant) return;
    const rid = selectedRestaurant.id;
    const body = { name, description, price, available, categoryId: categoryId ? parseInt(categoryId) : null };
    try {
        const url = editingMenuItemId ? `/api/restaurants/${rid}/menu-items/${editingMenuItemId}` : `/api/restaurants/${rid}/menu-items`;
        const method = editingMenuItemId ? 'PUT' : 'POST';
        const res = await apiFetch(url, { method, body: JSON.stringify(body) });
        if (!res.ok) throw new Error((await res.text()) || `Error ${res.status}`);
        closeModal('modalMenuItem');
        showToast('success', '✅', editingMenuItemId ? 'Item updated!' : 'Item added!');
        await loadMenuItems();
    } catch (err) { showToast('error', '❌', err.message || 'Failed to save menu item.'); }
}

function confirmDeleteMenuItem(id, name) {
    document.getElementById('confirmMsg').textContent = `Delete menu item "${name}"?`;
    confirmCallback = () => deleteMenuItem(id);
    openModal('modalConfirm');
}

async function deleteMenuItem(menuItemId) {
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}/menu-items/${menuItemId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error(`Error ${res.status}`);
        closeModal('modalConfirm');
        showToast('success', '✅', 'Menu item deleted.');
        await loadMenuItems();
    } catch (err) { showToast('error', '❌', err.message || 'Failed to delete menu item.'); }
}

function executeConfirm() {
    if (confirmCallback) { confirmCallback(); confirmCallback = null; }
}

function openModal(id)  { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

document.addEventListener('click', (e) => {
    ['modalRestaurant','modalCategory','modalMenuItem','modalConfirm'].forEach(id => {
        const modal = document.getElementById(id);
        if (modal && e.target === modal) closeModal(id);
    });
});

let toastTimer;
function showToast(type, icon, message) {
    clearTimeout(toastTimer);
    const toast = document.getElementById('toast');
    document.getElementById('toastIcon').textContent = icon;
    document.getElementById('toastMsg').textContent  = message;
    toast.className = `show ${type}`;
    toastTimer = setTimeout(() => { toast.className = type; }, 2800);
}

function formatDate(dateStr) {
    if (!dateStr) return '--';
    try {
        return new Date(dateStr).toLocaleDateString('en-IN', {
            day: 'numeric', month: 'short', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch { return dateStr; }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}