/**
 * ============================================
 *   Food Mania — Restaurants Page JS
 * ============================================
 *
 */

// All restaurants from API stored here for filtering
let allRestaurants = [];

// Food emojis — assigned by restaurant id for consistency
const FOOD_EMOJIS = ['🍕', '🍔', '🍜', '🍣', '🌮', '🍛', '🍱', '🥗', '🍗', '🥪'];

// ─────────────────────────────────────────
//   PAGE INIT
// ─────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    // Step 1: Check authentication
    requireAuth();

    // Step 2: Set up navbar user info
    setupNavbar();

    // Step 3: Load restaurants
    loadRestaurants();

    // Step 4: Close dropdown on outside click
    document.addEventListener('click', (e) => {
        const menu = document.getElementById('userMenu');
        if (!menu.contains(e.target)) {
            document.getElementById('userDropdown').classList.add('hidden');
        }
    });
});

// ─────────────────────────────────────────
//   NAVBAR SETUP
// ─────────────────────────────────────────

function setupNavbar() {
    const email = getEmail() || 'Account';
    const shortEmail = email.split('@')[0]; // show just username part

    document.getElementById('userEmailNav').textContent = shortEmail;
    document.getElementById('userEmailDrop').textContent = email;
}

function toggleUserMenu() {
    document.getElementById('userDropdown').classList.toggle('hidden');
}

// ─────────────────────────────────────────
//   LOAD RESTAURANTS FROM BACKEND
// ─────────────────────────────────────────

async function loadRestaurants() {
    showSkeleton();

    try {
        /**
         *  GET /api/restaurants
         * Returns: [ { id, name, description, address, phone, ownerId }, ... ]
         * Needs JWT token — apiFetch adds it automatically
         */
        const response = await apiFetch('/api/restaurants');

        if (!response.ok) {
            throw new Error(`Server error: ${response.status}`);
        }

        const restaurants = await response.json();
        allRestaurants = restaurants;

        hideSkeleton();

        if (restaurants.length === 0) {
            showEmpty();
            return;
        }

        renderRestaurants(restaurants);

    } catch (err) {
        console.error('Failed to load restaurants:', err);
        hideSkeleton();
        showError('Could not load restaurants. Please check your connection.');
    }
}

// ─────────────────────────────────────────
//   RENDER RESTAURANT CARDS
// ─────────────────────────────────────────

function renderRestaurants(restaurants) {
    const grid = document.getElementById('restaurantGrid');
    grid.innerHTML = '';
    grid.classList.remove('hidden');

    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('errorState').classList.add('hidden');

    // Update count
    document.getElementById('restaurantCount').textContent =
        `${restaurants.length} restaurant${restaurants.length !== 1 ? 's' : ''} available`;

    restaurants.forEach((r, index) => {
        const card = createRestaurantCard(r, index);
        grid.appendChild(card);
    });
}

function createRestaurantCard(restaurant, index) {
    // Pick emoji based on restaurant id for consistency
    const emoji = FOOD_EMOJIS[restaurant.id % FOOD_EMOJIS.length];

    // Animation delay for staggered entrance
    const delay = index * 60;

    const card = document.createElement('div');
    card.className = 'restaurant-card';
    card.style.animationDelay = `${delay}ms`;
    card.style.animation = `fadeInUp .4s ease both ${delay}ms`;

    card.innerHTML = `
        <!-- Card Image / Emoji Area -->
        <div class="card-img">
            <span style="font-size:64px; z-index:1; position:relative;">${emoji}</span>
        </div>

        <!-- Card Body -->
        <div class="card-body">

            <!-- Name + Status -->
            <div class="flex items-start justify-between gap-2 mb-2">
                <h3 class="card-name">${escapeHtml(restaurant.name)}</h3>
                <span class="card-status status-open flex-shrink-0">
                    <span>●</span> Open
                </span>
            </div>

            <!-- Description -->
            <p class="card-desc">${escapeHtml(restaurant.description || 'Delicious food awaits!')}</p>

            <!-- Address + Phone -->
            <div class="card-meta">
                <div class="card-address">
                    <span>📍</span>
                    <span>${escapeHtml(restaurant.address)}</span>
                </div>
                <div class="flex items-center gap-1 text-xs text-ash">
                    <span>📞</span>
                    <span>${escapeHtml(restaurant.phone)}</span>
                </div>
            </div>

            <!-- Order Button -->
            <button
                class="btn-order"
                onclick="goToMenu(${restaurant.id}, '${escapeHtml(restaurant.name)}')"
            >
                View Menu & Order →
            </button>
        </div>
    `;

    return card;
}

// ─────────────────────────────────────────
//   NAVIGATE TO MENU
// ─────────────────────────────────────────

function goToMenu(restaurantId, restaurantName) {
    // Save restaurant info so menu page can use it
    localStorage.setItem('fm_restaurant_id', restaurantId);
    localStorage.setItem('fm_restaurant_name', restaurantName);
    window.location.href = `menu.html?restaurantId=${restaurantId}`;
}

// ─────────────────────────────────────────
//   SEARCH
// ─────────────────────────────────────────

function filterRestaurants() {
    const query = document.getElementById('searchInput').value.toLowerCase();
    applyFilter(query);
}

function filterRestaurantsMobile() {
    const query = document.getElementById('searchInputMobile').value.toLowerCase();
    applyFilter(query);
}

function applyFilter(query) {
    if (!query) {
        renderRestaurants(allRestaurants);
        return;
    }

    const filtered = allRestaurants.filter(r =>
        r.name.toLowerCase().includes(query) ||
        r.description?.toLowerCase().includes(query) ||
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

// ─────────────────────────────────────────
//   SORT
// ─────────────────────────────────────────

function sortRestaurants(value) {
    let sorted = [...allRestaurants];

    if (value === 'name') {
        sorted.sort((a, b) => a.name.localeCompare(b.name));
    } else if (value === 'name-desc') {
        sorted.sort((a, b) => b.name.localeCompare(a.name));
    }

    renderRestaurants(sorted);
}

// ─────────────────────────────────────────
//   SKELETON / ERROR / EMPTY STATES
// ─────────────────────────────────────────

function showSkeleton() {
    document.getElementById('skeletonGrid').classList.remove('hidden');
    document.getElementById('restaurantGrid').classList.add('hidden');
    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('errorState').classList.add('hidden');
}

function hideSkeleton() {
    document.getElementById('skeletonGrid').classList.add('hidden');
}

function showEmpty() {
    document.getElementById('emptyState').classList.remove('hidden');
    document.getElementById('restaurantCount').textContent = '0 restaurants available';
}

function showError(msg) {
    document.getElementById('errorState').classList.remove('hidden');
    document.getElementById('errorMsg').textContent = msg;
}

// ─────────────────────────────────────────
//   HELPER
// ─────────────────────────────────────────

// Prevent XSS — escape user data before putting in HTML
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// Card entrance animation
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeInUp {
        from { opacity:0; transform:translateY(20px); }
        to   { opacity:1; transform:translateY(0); }
    }
`;
document.head.appendChild(style);