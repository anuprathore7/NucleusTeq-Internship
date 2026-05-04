/**
 * ============================================
 *   Food Mania — Cart Page JS
 * ============================================
 *
 */
/**
 * ============================================
 *   Food Mania — Cart JS
 *   Address selection modal added before order placement.
 *   All existing cart functions are unchanged below.
 * ============================================
 */

// ── ADDRESS MODAL STATE ────────────────────
let selectedAddressId = null;
let savedAddresses    = [];

// ── OPEN ADDRESS MODAL ─────────────────────
// Called when user clicks "Place Order"
// Loads saved addresses then shows the modal
async function openAddressModal() {
    selectedAddressId = null;
    document.getElementById('confirmOrderBtn').disabled = true;

    // Reset new address form
    document.getElementById('newAddrForm').classList.remove('open');
    ['newStreet','newCity','newState','newPincode'].forEach(id => {
        document.getElementById(id).value = '';
    });

    // Load saved addresses from backend
    await loadSavedAddresses();

    document.getElementById('modalAddress').classList.remove('hidden');
}

// ── CLOSE ADDRESS MODAL ────────────────────
function closeAddressModal() {
    document.getElementById('modalAddress').classList.add('hidden');
}

// ── LOAD SAVED ADDRESSES ───────────────────
// GET /api/addresses
async function loadSavedAddresses() {
    const listEl = document.getElementById('savedAddressList');
    listEl.innerHTML = `<div class="no-addr-msg">Loading addresses...</div>`;

    try {
        const res  = await apiFetch('/api/addresses');
        const data = await res.json();
        savedAddresses = Array.isArray(data) ? data : [];
        renderAddressList(savedAddresses);
    } catch (err) {
        listEl.innerHTML = `<div class="no-addr-msg" style="color:#c0392b">Failed to load addresses. Please try again.</div>`;
    }
}

// ── RENDER ADDRESS LIST ────────────────────
function renderAddressList(addresses) {
    const listEl = document.getElementById('savedAddressList');

    if (addresses.length === 0) {
        listEl.innerHTML = `<div class="no-addr-msg">No saved addresses yet. Add one below.</div>`;
        return;
    }

    listEl.innerHTML = '';
    addresses.forEach(addr => {
        const card = document.createElement('div');
        card.className = 'addr-card';
        card.id = `addr-card-${addr.id}`;
        card.onclick = () => selectAddress(addr.id);
        card.innerHTML = `
            <div class="addr-radio" id="addr-radio-${addr.id}"></div>
            <div class="addr-text">
                <div class="addr-street">${escHtml(addr.street)}</div>
                <div class="addr-detail">${escHtml(addr.city)}, ${escHtml(addr.state)} - ${escHtml(addr.pincode)}</div>
            </div>
            <button class="addr-delete-btn" onclick="deleteAddress(event, ${addr.id})" title="Remove address">✕</button>
        `;
        listEl.appendChild(card);
    });
}

// ── SELECT ADDRESS ─────────────────────────
function selectAddress(addressId) {
    selectedAddressId = addressId;

    // Update UI
    document.querySelectorAll('.addr-card').forEach(c => c.classList.remove('selected'));
    const card = document.getElementById(`addr-card-${addressId}`);
    if (card) card.classList.add('selected');

    document.getElementById('confirmOrderBtn').disabled = false;
}

// ── TOGGLE NEW ADDRESS FORM ────────────────
function toggleNewAddrForm() {
    document.getElementById('newAddrForm').classList.toggle('open');
}

// ── SAVE NEW ADDRESS ───────────────────────
// POST /api/addresses
async function saveNewAddress() {
    const street  = document.getElementById('newStreet').value.trim();
    const city    = document.getElementById('newCity').value.trim();
    const state   = document.getElementById('newState').value.trim();
    const pincode = document.getElementById('newPincode').value.trim();

    if (!street || !city || !state || !pincode) {
        showToast('error', '⚠️', 'Please fill all address fields.');
        return;
    }

    try {
        const res = await apiFetch('/api/addresses', {
            method: 'POST',
            body: JSON.stringify({ street, city, state, pincode })
        });

        if (!res.ok) {
            showToast('error', '❌', 'Failed to save address.');
            return;
        }

        const newAddr = await res.json();
        savedAddresses.push(newAddr);

        // Clear form and hide it
        ['newStreet','newCity','newState','newPincode'].forEach(id => {
            document.getElementById(id).value = '';
        });
        document.getElementById('newAddrForm').classList.remove('open');

        // Re-render and auto-select the new address
        renderAddressList(savedAddresses);
        selectAddress(newAddr.id);

        showToast('success', '✅', 'Address saved!');

    } catch (err) {
        showToast('error', '❌', 'Could not save address.');
    }
}

// ── DELETE ADDRESS ─────────────────────────
// DELETE /api/addresses/{id}
async function deleteAddress(event, addressId) {
    event.stopPropagation(); // prevent selecting the card
    if (!confirm('Remove this address?')) return;

    try {
        const res = await apiFetch(`/api/addresses/${addressId}`, { method: 'DELETE' });
        if (!res.ok) { showToast('error', '❌', 'Could not remove address.'); return; }

        savedAddresses = savedAddresses.filter(a => a.id !== addressId);

        if (selectedAddressId === addressId) {
            selectedAddressId = null;
            document.getElementById('confirmOrderBtn').disabled = true;
        }

        renderAddressList(savedAddresses);
        showToast('success', '✅', 'Address removed.');

    } catch (err) {
        showToast('error', '❌', 'Could not remove address.');
    }
}

// ── CONFIRM PLACE ORDER ────────────────────
// Called when user clicks "Confirm Order" in the modal
// POST /api/orders/place  with { deliveryAddressId }
async function confirmPlaceOrder() {
    if (!selectedAddressId) {
        showToast('error', '⚠️', 'Please select a delivery address.');
        return;
    }

    const btn = document.getElementById('confirmOrderBtn');
    btn.disabled = true;
    btn.textContent = 'Placing order...';

    try {
        const res = await apiFetch('/api/orders/place', {
            method: 'POST',
            body: JSON.stringify({ deliveryAddressId: selectedAddressId })
        });

        const data = await res.json();

        if (!res.ok) {
            showToast('error', '❌', data.message || 'Could not place order.');
            btn.disabled = false;
            btn.textContent = 'Confirm Order →';
            return;
        }

        // Close address modal, show success modal
        closeAddressModal();

        document.getElementById('successOrderId').textContent = `Order #${data.orderId}`;
        if (data.deliveryAddress) {
            document.getElementById('successAddress').textContent = 'Delivering to: ' + data.deliveryAddress;
        }
        document.getElementById('modalOrderSuccess').classList.remove('hidden');

    } catch (err) {
        showToast('error', '❌', 'Could not place order. Please try again.');
        btn.disabled = false;
        btn.textContent = 'Confirm Order →';
    }
}

// ── HELPER ────────────────────────────────
function escHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// ── GO TO ORDERS (used by success modal) ──
function goToOrders() {
    window.location.href = 'orders.html';
}

// ─────────────────────────────────────────────────────────────
//  ALL YOUR EXISTING CART FUNCTIONS BELOW — DO NOT TOUCH THESE
//  Just paste your existing cart.js content here below this line
// ─────────────────────────────────────────────────────────────

const FOOD_EMOJIS = ['🍕','🍔','🍜','🍣','🌮','🍛','🍱','🥗','🍗','🥪','🧆','🥘','🍲','🥙','🌯'];

let currentCart = null;

// ─────────────────────────────────────────
//   PAGE INIT
// ─────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    requireAuth();
    setupNavbar();
    loadCart();

    document.addEventListener('click', (e) => {
        const menu = document.getElementById('userMenu');
        if (menu && !menu.contains(e.target)) {
            const dd = document.getElementById('userDropdown');
            if (dd) dd.classList.add('hidden');
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
//   LOAD CART  — GET /api/cart
// ─────────────────────────────────────────
async function loadCart() {
    showSkeleton();
    try {
        const res = await apiFetch('/api/cart');

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || `Server error ${res.status}`);
        }

        const cart = await res.json();
        currentCart = cart;
        hideSkeleton();

        // Empty cart: items null or length 0
        if (!cart.items || cart.items.length === 0) {
            showEmpty();
            return;
        }

        renderCart(cart);

    } catch (err) {
        console.error('Cart load error:', err);
        hideSkeleton();
        showError(err.message || 'Could not load your cart.');
    }
}

// ─────────────────────────────────────────
//   RENDER CART
// ─────────────────────────────────────────
function renderCart(cart) {
    document.getElementById('cartContent').style.display = '';
    document.getElementById('emptyCart').style.display   = 'none';
    document.getElementById('errorCart').style.display   = 'none';

    document.getElementById('restaurantBadgeName').textContent = cart.restaurantName || 'Restaurant';

    const list = document.getElementById('cartItemsList');
    list.innerHTML = '';
    cart.items.forEach((item, idx) => list.appendChild(createCartCard(item, idx)));

    updateSummary(cart);
}

// ─────────────────────────────────────────
//   CREATE CART ITEM CARD
// ─────────────────────────────────────────
function createCartCard(item, index) {
    const emoji = FOOD_EMOJIS[item.menuItemId % FOOD_EMOJIS.length];

    const card = document.createElement('div');
    card.className = 'cart-card fade-in';
    card.id = `cart-item-${item.cartItemId}`;
    card.style.animationDelay = `${index * 60}ms`;

    card.innerHTML = `
        <div class="cc-emoji">${emoji}</div>
        <div class="cc-info">
            <div class="cc-name">${escapeHtml(item.menuItemName)}</div>
            <div class="cc-unit">₹${Number(item.price).toFixed(2)} each</div>
        </div>
        <div class="cc-qty" id="qty-wrap-${item.cartItemId}">
            <button class="cc-qty-btn" id="minus-${item.cartItemId}"
                onclick="changeQty(${item.cartItemId}, ${item.quantity}, -1)"
                ${item.quantity <= 1 ? 'disabled' : ''}>−</button>
            <div class="cc-qty-num" id="qty-${item.cartItemId}">${item.quantity}</div>
            <button class="cc-qty-btn" id="plus-${item.cartItemId}"
                onclick="changeQty(${item.cartItemId}, ${item.quantity}, 1)">+</button>
        </div>
        <div class="cc-subtotal" id="sub-${item.cartItemId}">₹${Number(item.subtotal).toFixed(2)}</div>
        <button class="cc-remove" onclick="removeItem(${item.cartItemId})" title="Remove">✕</button>
    `;
    return card;
}

// ─────────────────────────────────────────
//   UPDATE SUMMARY
// ─────────────────────────────────────────
function updateSummary(cart) {
    const subtotal   = cart.totalAmount || 0;
    const tax        = subtotal * 0.05;
    const grandTotal = subtotal + tax;
    const itemCount  = (cart.items || []).reduce((s, i) => s + i.quantity, 0);

    document.getElementById('summaryCount').textContent    = itemCount;
    document.getElementById('summarySubtotal').textContent = `₹${subtotal.toFixed(2)}`;
    document.getElementById('summaryTax').textContent      = `₹${tax.toFixed(2)}`;
    document.getElementById('summaryTotal').textContent    = `₹${grandTotal.toFixed(2)}`;
}

// ─────────────────────────────────────────
//   CHANGE QUANTITY
//   PUT /api/cart/update/{cartItemId}?quantity=N
//
//   KEY FIX: The onclick passes the CURRENT quantity
//   from the DOM, not from a stale variable.
//   After each update we REFRESH the onclick handlers
//   so the next click always has the latest qty.
// ─────────────────────────────────────────
async function changeQty(cartItemId, currentQty, delta) {
    const newQty = currentQty + delta;
    if (newQty < 1) return;

    // Disable both buttons immediately
    setQtyDisabled(cartItemId, true);

    try {
        /**
         * PUT /api/cart/update/{cartItemId}?quantity=N
         * NOTE: quantity is a @RequestParam — must be in URL, NOT body
         */
        const res = await apiFetch(`/api/cart/update/${cartItemId}?quantity=${newQty}`, {
            method: 'PUT'
        });

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err || `Error ${res.status}`);
        }

        const updatedCart = await res.json();
        currentCart = updatedCart;

        // Find updated item
        const updatedItem = updatedCart.items.find(i => i.cartItemId === cartItemId);
        if (updatedItem) {
            // Update displayed qty
            document.getElementById(`qty-${cartItemId}`).textContent = updatedItem.quantity;
            // Update subtotal
            document.getElementById(`sub-${cartItemId}`).textContent = `₹${Number(updatedItem.subtotal).toFixed(2)}`;

            // ── KEY FIX: Re-wire onclick with the NEW quantity ──
            // Without this, the next click would still pass the OLD qty
            const minusBtn = document.getElementById(`minus-${cartItemId}`);
            const plusBtn  = document.getElementById(`plus-${cartItemId}`);
            if (minusBtn) {
                minusBtn.disabled = updatedItem.quantity <= 1;
                minusBtn.onclick = () => changeQty(cartItemId, updatedItem.quantity, -1);
            }
            if (plusBtn) {
                plusBtn.onclick = () => changeQty(cartItemId, updatedItem.quantity, 1);
            }
        }

        updateSummary(updatedCart);

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to update quantity.');
    } finally {
        setQtyDisabled(cartItemId, false);
    }
}

function setQtyDisabled(cartItemId, disabled) {
    ['minus', 'plus'].forEach(prefix => {
        const btn = document.getElementById(`${prefix}-${cartItemId}`);
        if (btn) btn.disabled = disabled;
    });
}

// ─────────────────────────────────────────
//   REMOVE ITEM
//   DELETE /api/cart/remove/{cartItemId}
// ─────────────────────────────────────────
async function removeItem(cartItemId) {
    const card = document.getElementById(`cart-item-${cartItemId}`);
    if (card) {
        card.style.opacity   = '0';
        card.style.transform = 'translateX(30px)';
    }

    try {
        const res = await apiFetch(`/api/cart/remove/${cartItemId}`, { method: 'DELETE' });
        if (!res.ok) {
            if (card) { card.style.opacity = '1'; card.style.transform = ''; }
            const err = await res.text();
            throw new Error(err || `Error ${res.status}`);
        }

        const updatedCart = await res.json();
        currentCart = updatedCart;

        setTimeout(() => { if (card) card.remove(); }, 260);

        if (!updatedCart.items || updatedCart.items.length === 0) {
            setTimeout(() => {
                document.getElementById('cartContent').style.display = 'none';
                showEmpty();
            }, 300);
        } else {
            updateSummary(updatedCart);
        }

        showToast('info', '🗑️', 'Item removed.');

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to remove item.');
    }
}

// ─────────────────────────────────────────
//   CLEAR CART — DELETE /api/cart/clear
// ─────────────────────────────────────────
function openClearModal()  { document.getElementById('modalClear').classList.remove('hidden'); }
function closeClearModal() { document.getElementById('modalClear').classList.add('hidden'); }

async function clearCart() {
    closeClearModal();
    try {
        const res = await apiFetch('/api/cart/clear', { method: 'DELETE' });
        if (!res.ok) { const e = await res.text(); throw new Error(e || `Error ${res.status}`); }

        currentCart = null;
        document.getElementById('cartContent').style.display = 'none';
        showEmpty();
        showToast('info', '🗑️', 'Cart cleared.');
    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to clear cart.');
    }
}

// ─────────────────────────────────────────
//   PLACE ORDER
//   POST /api/orders/place
//   No body — backend reads cart from JWT token
// ─────────────────────────────────────────
async function placeOrder() {
    const btn = document.getElementById('checkoutBtn');
    btn.classList.add('loading');
    btn.innerHTML = `<span class="spinner"></span> Placing Order...`;

    try {
        /**
         * POST /api/orders/place
         * Header: Authorization: Bearer <token>
         * No body needed — backend reads cart from JWT user
         * Returns: OrderResponseDto { orderId, restaurantName, items, totalAmount, status, createdAt }
         */
        const res = await apiFetch('/api/orders/place', { method: 'POST' });

        if (!res.ok) {
            const errText = await res.text();
            // Parse JSON error if backend sends it
            let errMsg = errText;
            try {
                const errJson = JSON.parse(errText);
                errMsg = errJson.message || errJson.error || errText;
            } catch {}
            throw new Error(errMsg);
        }

        const order = await res.json();

        // Show success modal
        document.getElementById('successOrderId').textContent = `Order #${order.orderId}`;
        document.getElementById('modalOrderSuccess').classList.remove('hidden');

        // Hide cart content
        document.getElementById('cartContent').style.display = 'none';
        currentCart = null;

    } catch (err) {
        showToast('error', '❌', err.message || 'Failed to place order. Please try again.');
    } finally {
        btn.classList.remove('loading');
        btn.innerHTML = `Place Order →`;
    }
}

function goToOrders() {
    document.getElementById('modalOrderSuccess').classList.add('hidden');
    window.location.href = 'orders.html';
}

// ─────────────────────────────────────────
//   SKELETON / STATES
// ─────────────────────────────────────────
function showSkeleton() {
    document.getElementById('skeletonCart').style.display = '';
    document.getElementById('cartContent').style.display  = 'none';
    document.getElementById('emptyCart').style.display    = 'none';
    document.getElementById('errorCart').style.display    = 'none';
}
function hideSkeleton() { document.getElementById('skeletonCart').style.display = 'none'; }
function showEmpty() {
    document.getElementById('emptyCart').style.display   = '';
    document.getElementById('cartContent').style.display = 'none';
    document.getElementById('errorCart').style.display   = 'none';
}
function showError(msg) {
    document.getElementById('errorCart').style.display = '';
    document.getElementById('errorMsg').textContent = msg;
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
//   HELPER
// ─────────────────────────────────────────
function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}