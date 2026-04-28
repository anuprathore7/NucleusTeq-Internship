/**
 * ============================================
 *   Food Mania — Owner Dashboard JS
 * ============================================
 *
*/

// ─────────────────────────────────────────
//   STATE
// ─────────────────────────────────────────

let myRestaurants      = [];
let selectedRestaurant = null;
let allCategories      = [];
let allMenuItems       = [];
let editingCategoryId  = null;
let editingMenuItemId  = null;
let confirmCallback    = null;

// ─────────────────────────────────────────
//   PAGE INIT
// ─────────────────────────────────────────

// Prevent browser back button from going to restaurants page
history.pushState(null, '', window.location.href);
window.addEventListener('popstate', () => {
    history.pushState(null, '', window.location.href);
});
document.addEventListener('DOMContentLoaded', () => {
    requireAuth();

    // Owner only guard
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
    document.getElementById('userEmailNav').textContent = email.split('@')[0];
    document.getElementById('userEmailDrop').textContent = email;
}

function toggleUserMenu() {
    document.getElementById('userDropdown').classList.toggle('hidden');
}

// ─────────────────────────────────────────
//   GET OWNER ID FROM TOKEN
// ─────────────────────────────────────────

function getOwnerId() {
    const token = getToken();
    if (!token) return null;

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.sub; //  THIS IS THE CORRECT FIELD (standard JWT)
    } catch {
        return null;
    }
}

// ─────────────────────────────────────────
//   LOAD MY RESTAURANTS
//   GET /api/restaurants/owner/{ownerId}
// ─────────────────────────────────────────

async function loadMyRestaurants() {
    try {
        const ownerId = getOwnerId();

        if (!ownerId) {
            console.error("Owner ID missing in token");
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
//   RENDER SIDEBAR RESTAURANT LIST
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

    // Update sidebar selection styling
    document.querySelectorAll('.restaurant-card-side').forEach(c => c.classList.remove('selected'));
    const card = document.getElementById(`rest-card-${restaurant.id}`);
    if (card) card.classList.add('selected');

    // Show nav
    document.getElementById('sidebarNav').classList.remove('hidden');

    // Hide prompt
    document.getElementById('panelSelectPrompt').classList.add('hidden');

    // Populate edit form
    document.getElementById('editRestName').value    = restaurant.name || '';
    document.getElementById('editRestDesc').value    = restaurant.description || '';
    document.getElementById('editRestAddress').value = restaurant.address || '';
    document.getElementById('editRestPhone').value   = restaurant.phone || '';

    // Load data and switch to overview
    await Promise.all([loadCategories(), loadMenuItems()]);
    switchPanel('overview');
}

// ─────────────────────────────────────────
//   LOAD CATEGORIES
//   GET /api/restaurants/{id}/categories
// ─────────────────────────────────────────

async function loadCategories() {
    if (!selectedRestaurant) return;
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}/categories`);
        if (!res.ok) throw new Error(res.status);
        allCategories = await res.json();
        renderCategoriesTable(allCategories);
        updateStats();
    } catch (err) {
        console.error('Failed to load categories:', err);
    }
}

// ─────────────────────────────────────────
//   LOAD MENU ITEMS
//   GET /api/restaurants/{id}/menu-items
// ─────────────────────────────────────────

async function loadMenuItems() {
    if (!selectedRestaurant) return;
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}/menu-items`);
        if (!res.ok) throw new Error(res.status);
        allMenuItems = await res.json();
        renderMenuItemsTable(allMenuItems);
        updateStats();
    } catch (err) {
        console.error('Failed to load menu items:', err);
    }
}

// ─────────────────────────────────────────
//   UPDATE STATS (overview panel)
// ─────────────────────────────────────────

function updateStats() {
    document.getElementById('overviewRestName').textContent   = selectedRestaurant?.name || '--';
    document.getElementById('statCategories').textContent     = allCategories.length;
    document.getElementById('statMenuItems').textContent      = allMenuItems.length;
    const avail = allMenuItems.filter(i => i.available !== false).length;
    document.getElementById('statAvailable').textContent      = avail;
}

// ─────────────────────────────────────────
//   PANEL SWITCHING
// ─────────────────────────────────────────

function switchPanel(panelName) {
    // Hide all panels
    ['SelectPrompt','Overview','Categories','Menu','Restaurant'].forEach(p => {
        document.getElementById(`panel${p}`).classList.add('hidden');
        document.getElementById(`panel${p}`).classList.remove('active');
    });

    // Show selected
    const el = document.getElementById(`panel${capitalize(panelName)}`);
    if (el) { el.classList.remove('hidden'); el.classList.add('active'); }

    // Update sidebar button states
    ['Overview','Categories','Menu','Restaurant'].forEach(p => {
        const btn = document.getElementById(`nav${p}`);
        if (btn) btn.classList.remove('active');
    });
    const activeBtn = document.getElementById(`nav${capitalize(panelName)}`);
    if (activeBtn) activeBtn.classList.add('active');
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

// ─────────────────────────────────────────
//   CATEGORIES TABLE
// ─────────────────────────────────────────

function renderCategoriesTable(categories) {
    const wrap = document.getElementById('categoriesTableWrap');

    if (categories.length === 0) {
        wrap.innerHTML = `
            <div class="text-center py-10">
                <div class="text-5xl mb-3">🏷️</div>
                <p class="font-playfair text-lg font-bold text-ink mb-1">No categories yet</p>
                <p class="text-ash text-sm font-dm">Add categories to organise your menu</p>
            </div>`;
        return;
    }

    wrap.innerHTML = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>ID</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody id="categoriesTbody"></tbody>
        </table>`;

    const tbody = document.getElementById('categoriesTbody');
    categories.forEach(cat => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="font-semibold">${escapeHtml(cat.name)}</td>
            <td class="text-ash text-xs">#${cat.id}</td>
            <td>
                <div class="flex items-center gap-2">
                    <button onclick="openEditCategoryModal(${cat.id}, '${escapeHtml(cat.name)}')" class="btn-secondary-sm">✏️ Edit</button>
                    <button onclick="confirmDeleteCategory(${cat.id}, '${escapeHtml(cat.name)}')" class="btn-danger-sm">🗑️</button>
                </div>
            </td>`;
        tbody.appendChild(tr);
    });
}

// ─────────────────────────────────────────
//   MENU ITEMS TABLE
// ─────────────────────────────────────────

function renderMenuItemsTable(items) {
    const wrap = document.getElementById('menuTableWrap');

    if (items.length === 0) {
        wrap.innerHTML = `
            <div class="text-center py-10">
                <div class="text-5xl mb-3">🍽️</div>
                <p class="font-playfair text-lg font-bold text-ink mb-1">No menu items yet</p>
                <p class="text-ash text-sm font-dm">Add your first food item</p>
            </div>`;
        return;
    }

    // Build category lookup
    const catMap = {};
    allCategories.forEach(c => { catMap[c.id] = c.name; });

    wrap.innerHTML = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>Item</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody id="menuTbody"></tbody>
        </table>`;

    const tbody = document.getElementById('menuTbody');
    items.forEach(item => {
        const catName = item.categoryId ? (catMap[item.categoryId] || `#${item.categoryId}`) : '—';
        const available = item.available !== false;

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div class="font-semibold text-ink">${escapeHtml(item.name)}</div>
                <div class="text-xs text-ash mt-0.5">${escapeHtml(item.description || '')}</div>
            </td>
            <td class="text-ash text-sm">${escapeHtml(catName)}</td>
            <td class="font-semibold text-burgundy font-dm">₹${Number(item.price).toFixed(2)}</td>
            <td>
                <span class="${available ? 'badge-avail' : 'badge-unavail'}">
                    ${available ? '✅ Available' : '🚫 Unavailable'}
                </span>
            </td>
            <td>
                <div class="flex items-center gap-2">
                    <button onclick="openEditMenuItemModal(${item.id})" class="btn-secondary-sm">✏️ Edit</button>
                    <button onclick="confirmDeleteMenuItem(${item.id}, '${escapeHtml(item.name)}')" class="btn-danger-sm">🗑️</button>
                </div>
            </td>`;
        tbody.appendChild(tr);
    });
}

// ─────────────────────────────────────────
//   RESTAURANT MODALS
// ─────────────────────────────────────────
function openAddRestaurantModal() {
    document.getElementById('modalRestaurantTitle').textContent = 'Add Restaurant';
    document.getElementById('mRestName').value    = '';
    document.getElementById('mRestDesc').value    = '';
    document.getElementById('mRestAddress').value = '';
    document.getElementById('mRestPhone').value   = '';
    document.getElementById('mRestImage').value   = '';  // ← clear file picker
    openModal('modalRestaurant');
}

async function submitRestaurant() {
    const name        = document.getElementById('mRestName').value.trim();
    const description = document.getElementById('mRestDesc').value.trim();
    const address     = document.getElementById('mRestAddress').value.trim();
    const phone       = document.getElementById('mRestPhone').value.trim();
    const imageFile   = document.getElementById('mRestImage').files[0]; // undefined if not picked

    if (!name || !address || !phone) {
        showToast('error', '⚠️', 'Name, address and phone are required.');
        return;
    }

    try {
        // ── STEP 1: Create the restaurant with JSON ───────────────────
        const res = await apiFetch('/api/restaurants', {
            method: 'POST',
            body: JSON.stringify({ name, description, address, phone })
        });

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || `Error ${res.status}`);
        }

        let newRestaurant = await res.json();
        // At this point newRestaurant.imagePath is null — that's expected

        // ── STEP 2: Upload image if user selected one ─────────────────
        if (imageFile) {
            showToast('info', '⏳', 'Uploading image...');

            const formData = new FormData();
            formData.append('file', imageFile);

            const imgRes = await fetch(`http://localhost:8000/api/restaurants/${newRestaurant.id}/image`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                    // ❌ DO NOT add Content-Type here
                },
                body: formData
            });

            if (!imgRes.ok) {
                console.error('Image upload failed:', imgRes.status);
                showToast('error', '⚠️', 'Restaurant saved, but image upload failed.');
            } else {
                newRestaurant = await imgRes.json();
                showToast('success', '✅', `"${name}" added with image!`);
            }
        }

        closeModal('modalRestaurant');
        await loadMyRestaurants();
        selectRestaurant(newRestaurant);

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to add restaurant.');
    }
}

/**
 * PUT /api/restaurants/{id}
 * Body: { name, description, address, phone }
 * Header: Authorization: Bearer <token>
 */
async function updateRestaurant() {
    if (!selectedRestaurant) return;

    const name        = document.getElementById('editRestName').value.trim();
    const description = document.getElementById('editRestDesc').value.trim();
    const address     = document.getElementById('editRestAddress').value.trim();
    const phone       = document.getElementById('editRestPhone').value.trim();

    if (!name || !address || !phone) {
        showToast('error', '⚠️', 'Name, address and phone are required.');
        return;
    }

    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}`, {
            method: 'PUT',
            body: JSON.stringify({ name, description, address, phone })
        });

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || `Error ${res.status}`);
        }

        const updated = await res.json();
        selectedRestaurant = updated;
        showToast('success', '✅', 'Restaurant updated successfully!');

        // Refresh sidebar
        await loadMyRestaurants();
        renderSidebarRestaurants(myRestaurants);

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to update restaurant.');
    }
}

/**
 * DELETE /api/restaurants/{id}
 */
function confirmDeleteRestaurant() {
    if (!selectedRestaurant) return;
    document.getElementById('confirmMsg').textContent =
        `Are you sure you want to delete "${selectedRestaurant.name}"? This will remove all its categories and menu items.`;
    confirmCallback = deleteRestaurant;
    openModal('modalConfirm');
}

async function deleteRestaurant() {
    try {
        const res = await apiFetch(`/api/restaurants/${selectedRestaurant.id}`, {
            method: 'DELETE'
        });

        if (!res.ok) throw new Error(`Error ${res.status}`);

        showToast('success', '✅', 'Restaurant deleted.');
        closeModal('modalConfirm');

        selectedRestaurant = null;
        allCategories = [];
        allMenuItems  = [];

        await loadMyRestaurants();

        // Show prompt
        document.getElementById('sidebarNav').classList.add('hidden');
        switchPanel('SelectPrompt');

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to delete restaurant.');
    }
}

// ─────────────────────────────────────────
//   CATEGORY MODALS
// ─────────────────────────────────────────

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
        let res;
        if (editingCategoryId) {
            /**
             * PUT /api/restaurants/{id}/categories/{cId}
             * Body: { name }
             * Header: Authorization
             */
            res = await apiFetch(`/api/restaurants/${rid}/categories/${editingCategoryId}`, {
                method: 'PUT',
                body: JSON.stringify({ name })
            });
        } else {
            /**
             * POST /api/restaurants/{id}/categories
             * Body: { name }
             * Header: Authorization
             */
            res = await apiFetch(`/api/restaurants/${rid}/categories`, {
                method: 'POST',
                body: JSON.stringify({ name })
            });
        }

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || `Error ${res.status}`);
        }

        closeModal('modalCategory');
        showToast('success', '✅', editingCategoryId ? 'Category updated!' : 'Category added!');
        await loadCategories();

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to save category.');
    }
}

function confirmDeleteCategory(id, name) {
    document.getElementById('confirmMsg').textContent = `Delete category "${name}"?`;
    confirmCallback = () => deleteCategory(id);
    openModal('modalConfirm');
}

/**
 * DELETE /api/restaurants/{id}/categories/{cId}
 */
async function deleteCategory(categoryId) {
    try {
        const res = await apiFetch(
            `/api/restaurants/${selectedRestaurant.id}/categories/${categoryId}`,
            { method: 'DELETE' }
        );
        if (!res.ok) throw new Error(`Error ${res.status}`);

        closeModal('modalConfirm');
        showToast('success', '✅', 'Category deleted.');
        await loadCategories();

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to delete category.');
    }
}

// ─────────────────────────────────────────
//   MENU ITEM MODALS
// ─────────────────────────────────────────

function populateCategoryDropdown(selectedCatId) {
    const select = document.getElementById('mItemCategory');
    select.innerHTML = '<option value="">-- No Category --</option>';
    allCategories.forEach(cat => {
        const opt = document.createElement('option');
        opt.value = cat.id;
        opt.textContent = cat.name;
        if (selectedCatId && String(cat.id) === String(selectedCatId)) opt.selected = true;
        select.appendChild(opt);
    });
}

function openAddMenuItemModal() {
    editingMenuItemId = null;
    document.getElementById('modalMenuItemTitle').textContent = 'Add Menu Item';
    document.getElementById('mItemName').value      = '';
    document.getElementById('mItemDesc').value      = '';
    document.getElementById('mItemPrice').value     = '';
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
    const name        = document.getElementById('mItemName').value.trim();
    const description = document.getElementById('mItemDesc').value.trim();
    const price       = parseFloat(document.getElementById('mItemPrice').value);
    const categoryId  = document.getElementById('mItemCategory').value || null;
    const available   = document.getElementById('mItemAvailable').value === 'true';

    if (!name || isNaN(price) || price < 0) {
        showToast('error', '⚠️', 'Name and a valid price are required.');
        return;
    }

    if (!selectedRestaurant) return;
    const rid = selectedRestaurant.id;

    const body = {
        name,
        description,
        price,
        available,
        categoryId: categoryId ? parseInt(categoryId) : null
    };

    try {
        let res;
        if (editingMenuItemId) {
            /**
             * PUT /api/restaurants/{rid}/menu-items/{mId}
             * Body: { name, description, price, available, categoryId }
             * Header: Authorization
             */
            res = await apiFetch(`/api/restaurants/${rid}/menu-items/${editingMenuItemId}`, {
                method: 'PUT',
                body: JSON.stringify(body)
            });
        } else {
            /**
             * POST /api/restaurants/{rid}/menu-items
             * Body: { name, description, price, available, categoryId }
             * Header: Authorization
             */
            res = await apiFetch(`/api/restaurants/${rid}/menu-items`, {
                method: 'POST',
                body: JSON.stringify(body)
            });
        }

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || `Error ${res.status}`);
        }

        closeModal('modalMenuItem');
        showToast('success', '✅', editingMenuItemId ? 'Item updated!' : 'Item added!');
        await loadMenuItems();

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to save menu item.');
    }
}

function confirmDeleteMenuItem(id, name) {
    document.getElementById('confirmMsg').textContent = `Delete menu item "${name}"?`;
    confirmCallback = () => deleteMenuItem(id);
    openModal('modalConfirm');
}

/**
 * DELETE /api/restaurants/{id}/menu-items/{mId}
 */
async function deleteMenuItem(menuItemId) {
    try {
        const res = await apiFetch(
            `/api/restaurants/${selectedRestaurant.id}/menu-items/${menuItemId}`,
            { method: 'DELETE' }
        );
        if (!res.ok) throw new Error(`Error ${res.status}`);

        closeModal('modalConfirm');
        showToast('success', '✅', 'Menu item deleted.');
        await loadMenuItems();

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to delete menu item.');
    }
}

// ─────────────────────────────────────────
//   CONFIRM MODAL
// ─────────────────────────────────────────

function executeConfirm() {
    if (confirmCallback) {
        confirmCallback();
        confirmCallback = null;
    }
}

// ─────────────────────────────────────────
//   MODAL HELPERS
// ─────────────────────────────────────────

function openModal(id) {
    document.getElementById(id).classList.remove('hidden');
}

function closeModal(id) {
    document.getElementById(id).classList.add('hidden');
}

// Close modal on overlay click
document.addEventListener('click', (e) => {
    ['modalRestaurant','modalCategory','modalMenuItem','modalConfirm'].forEach(id => {
        const modal = document.getElementById(id);
        if (modal && e.target === modal) closeModal(id);
    });
});

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