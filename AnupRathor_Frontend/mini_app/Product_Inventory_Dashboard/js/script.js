let allProducts = [];
let currentPage = 1;
let currentCat = "all";
let lowStockOnly = false;
const PER_PAGE = 8;

const DEFAULT_PRODUCTS = [
  /* Electronics — 10 items */
  {
    id: 101,
    name: "Laptop Pro 15 inch",
    price: 75000,
    stock: 8,
    category: "electronics",
  },
  {
    id: 102,
    name: "Wireless Earbuds Pro",
    price: 3499,
    stock: 0,
    category: "electronics",
  },
  {
    id: 103,
    name: "Mechanical Keyboard RGB",
    price: 4200,
    stock: 3,
    category: "electronics",
  },
  {
    id: 104,
    name: "27-inch 4K Monitor",
    price: 32000,
    stock: 5,
    category: "electronics",
  },
  {
    id: 105,
    name: "Smartwatch Series X",
    price: 8999,
    stock: 1,
    category: "electronics",
  },
  {
    id: 106,
    name: "Noise Cancelling Headset",
    price: 6500,
    stock: 12,
    category: "electronics",
  },
  {
    id: 107,
    name: "USB-C Hub 7-in-1",
    price: 1899,
    stock: 0,
    category: "electronics",
  },
  {
    id: 108,
    name: "Wireless Charging Pad",
    price: 1200,
    stock: 20,
    category: "electronics",
  },
  {
    id: 109,
    name: "Portable Power Bank 20K",
    price: 2499,
    stock: 4,
    category: "electronics",
  },
  {
    id: 110,
    name: "Webcam HD 1080p",
    price: 3200,
    stock: 7,
    category: "electronics",
  },
  /* Clothing — 10 items */
  {
    id: 201,
    name: "Cotton Crew T-Shirt",
    price: 499,
    stock: 40,
    category: "clothing",
  },
  {
    id: 202,
    name: "Slim Fit Denim Jeans",
    price: 1899,
    stock: 15,
    category: "clothing",
  },
  {
    id: 203,
    name: "Hooded Sweatshirt",
    price: 1299,
    stock: 2,
    category: "clothing",
  },
  {
    id: 204,
    name: "Formal Blazer Navy",
    price: 3999,
    stock: 8,
    category: "clothing",
  },
  {
    id: 205,
    name: "Running Track Pants",
    price: 899,
    stock: 25,
    category: "clothing",
  },
  {
    id: 206,
    name: "Summer Floral Dress",
    price: 1499,
    stock: 0,
    category: "clothing",
  },
  {
    id: 207,
    name: "Wool Winter Coat",
    price: 5499,
    stock: 3,
    category: "clothing",
  },
  {
    id: 208,
    name: "Polo Shirt White",
    price: 699,
    stock: 30,
    category: "clothing",
  },
  {
    id: 209,
    name: "Athletic Sports Shorts",
    price: 599,
    stock: 18,
    category: "clothing",
  },
  {
    id: 210,
    name: "Puffer Jacket Black",
    price: 3299,
    stock: 1,
    category: "clothing",
  },
  /* Books — 9 items */
  {
    id: 301,
    name: "JavaScript: The Good Parts",
    price: 699,
    stock: 12,
    category: "books",
  },
  { id: 302, name: "Clean Code", price: 850, stock: 0, category: "books" },
  {
    id: 303,
    name: "The Pragmatic Programmer",
    price: 999,
    stock: 6,
    category: "books",
  },
  {
    id: 304,
    name: "You Don't Know JS",
    price: 749,
    stock: 9,
    category: "books",
  },
  {
    id: 305,
    name: "Eloquent JavaScript",
    price: 649,
    stock: 4,
    category: "books",
  },
  {
    id: 306,
    name: "Design Patterns",
    price: 1100,
    stock: 3,
    category: "books",
  },
  {
    id: 307,
    name: "CSS: The Definitive Guide",
    price: 799,
    stock: 0,
    category: "books",
  },
  {
    id: 308,
    name: "HTML & CSS by Jon Duckett",
    price: 599,
    stock: 15,
    category: "books",
  },
  {
    id: 309,
    name: "Web Application Hacker",
    price: 1250,
    stock: 2,
    category: "books",
  },
  /* Accessories — 9 items */
  {
    id: 401,
    name: "Leather Bifold Wallet",
    price: 999,
    stock: 4,
    category: "accessories",
  },
  {
    id: 402,
    name: "Canvas Backpack 30L",
    price: 2199,
    stock: 11,
    category: "accessories",
  },
  {
    id: 403,
    name: "Polarized Sunglasses",
    price: 1499,
    stock: 0,
    category: "accessories",
  },
  {
    id: 404,
    name: "Sports Water Bottle",
    price: 599,
    stock: 22,
    category: "accessories",
  },
  {
    id: 405,
    name: "Wireless Mouse Silent",
    price: 1299,
    stock: 1,
    category: "accessories",
  },
  {
    id: 406,
    name: "Desk Organiser Set",
    price: 899,
    stock: 7,
    category: "accessories",
  },
  {
    id: 407,
    name: "Phone Stand Adjustable",
    price: 399,
    stock: 3,
    category: "accessories",
  },
  {
    id: 408,
    name: "Laptop Sleeve 15 inch",
    price: 799,
    stock: 14,
    category: "accessories",
  },
  {
    id: 409,
    name: "Cable Management Kit",
    price: 349,
    stock: 0,
    category: "accessories",
  },
];

/* ====
   3. LOCAL STORAGE HELPERS
   localStorage stores data as text (strings).
   JSON.stringify converts array → text to save.
   JSON.parse converts text → array to read back.
   ===== */

/* Write allProducts to localStorage */
function saveToStorage() {
  localStorage.setItem("pid_products", JSON.stringify(allProducts));
  console.log("Saved to localStorage:", allProducts.length, "products");
}

/* Read from localStorage — returns null if nothing saved yet */
function loadFromStorage() {
  const raw = localStorage.getItem("pid_products");
  return raw ? JSON.parse(raw) : null;
}

/* ================================================
   4. LOADER HELPERS
   Update the loading overlay text and progress bar
   ================================================ */
function setLoaderText(msg) {
  const el = document.getElementById("loader-text");
  if (el) el.textContent = msg;
}

function animateProgress(pct) {
  const el = document.getElementById("prog-fill");
  if (el) el.style.width = pct + "%";
}
/* ================================================
   5. REAL API FETCH
   Uses fetch() — built-in browser HTTP function.
   async/await makes it easy to read and write.
   ================================================ */
async function fetchProductsFromAPI() {
  /* First check localStorage — no need to call API again */
  const saved = loadFromStorage();
  if (saved && saved.length > 0) {
    setLoaderText("Loading from saved data...");
    animateProgress(100);
    console.log("Loaded", saved.length, "products from localStorage");
    return saved;
  }

  /* localStorage empty — fetch from real API */
  try {
    setLoaderText("Connecting to dummyjson API...");
    animateProgress(20);

    /* fetch() sends a GET request — await waits for response */
    const response = await fetch("https://dummyjson.com/products?limit=100");

    animateProgress(55);
    setLoaderText("Parsing JSON response...");

    if (!response.ok) throw new Error("API error: " + response.status);

    /* .json() reads and parses the response body */
    const data = await response.json();

    animateProgress(80);
    setLoaderText("Mapping to inventory format...");

    const apiProducts = mapAPIProducts(data.products.slice(0, 40));

    animateProgress(100);
    setLoaderText("Ready!");

    console.log("API fetched:", apiProducts.length, "products");
    return [...apiProducts, ...DEFAULT_PRODUCTS];
  } catch (err) {
    /* If fetch fails — always fall back, never crash */
    console.warn("API failed, using fallback:", err.message);
    setLoaderText("Using offline data...");
    animateProgress(100);
    return DEFAULT_PRODUCTS;
  }
}

/* ================================================
   6. MAP API PRODUCTS
   Convert dummyjson format to our format.
   Price: USD × 83 = INR
   Category: remap their names to our 4 categories
   ================================================ */
function mapAPIProducts(items) {
  const catMap = {
    smartphones: "electronics",
    laptops: "electronics",
    tablets: "electronics",
    "mobile-accessories": "electronics",
    "mens-shirts": "clothing",
    "womens-dresses": "clothing",
    "mens-shoes": "clothing",
    "womens-shoes": "clothing",
    tops: "clothing",
    sunglasses: "accessories",
    "sports-accessories": "accessories",
    "skin-care": "accessories",
    fragrances: "accessories",
    "home-decoration": "accessories",
    beauty: "accessories",
  };

  return items.map((item, i) => ({
    id: 8000 + i,
    name: item.title,
    price: Math.round(item.price * 83),
    stock: item.stock,
    category: catMap[item.category] || "accessories",
  }));
}
/* 
   8. INIT — THE ENTRY POINT OF THE APP

   This is the function that starts EVERYTHING. Without calling init() at the bottom, the page just loads HTML and does nothing at all. */
async function init() {
  console.log("--- App starting ---");

  /* Fetch products from API or localStorage or fallback */
  allProducts = await fetchProductsFromAPI();

  /* Save to localStorage for next visit */
  saveToStorage();
  updateStats();
  renderProducts();

  /* Hide the loading spinner */
  const overlay = document.getElementById("loading-overlay");
  overlay.classList.add("gone");
  setTimeout(() => {
    overlay.style.display = "none";
  }, 500);

  console.log("--- App ready ---");
  console.log("Total products:", allProducts.length);
}
function updateStats() {
  const total = allProducts.length;
  const value = allProducts.reduce((s, p) => s + p.price * p.stock, 0);
  const out = allProducts.filter((p) => p.stock === 0).length;
  const low = allProducts.filter((p) => p.stock > 0 && p.stock < 5).length;
  document.getElementById("sb-total").textContent = total;
  document.getElementById("sb-value").textContent =
    value >= 100000
      ? (value / 100000).toFixed(1) + "L"
      : "\u20B9" + Math.round(value).toLocaleString("en-IN");
  document.getElementById("sb-out").textContent = out;
  document.getElementById("sb-low").textContent = low;
  document.getElementById("header-count").textContent = total;
  document.getElementById("header-low").textContent = low;
  document.getElementById("header-out").textContent = out;
}
init();

function fmt(n) {
  return "\u20B9" + Math.round(n).toLocaleString("en-IN");
}

function catBadgeClass(c) {
  return (
    {
      electronics: "cat-electronics",
      clothing: "cat-clothing",
      books: "cat-books",
      accessories: "cat-accessories",
    }[c] || "cat-accessories"
  );
}

function stripeClass(c) {
  return (
    {
      electronics: "stripe-electronics",
      clothing: "stripe-clothing",
      books: "stripe-books",
      accessories: "stripe-accessories",
    }[c] || "stripe-accessories"
  );
}

function stockBadge(s) {
  if (s === 0) return `<span class="stock-badge b-out">Out of Stock</span>`;
  if (s < 5) return `<span class="stock-badge b-low">Low Stock</span>`;
  return `<span class="stock-badge b-ok">In Stock</span>`;
}
function getFiltered() {
  const query = document
    .getElementById("search-input")
    .value.trim()
    .toLowerCase();
  const sort = document.getElementById("sort-sel").value;
  let list = [...allProducts];
  if (query) list = list.filter((p) => p.name.toLowerCase().includes(query));
  if (currentCat !== "all")
    list = list.filter((p) => p.category === currentCat);
  if (lowStockOnly) list = list.filter((p) => p.stock < 5);
  if (sort === "pa") list.sort((a, b) => a.price - b.price);
  if (sort === "pd") list.sort((a, b) => b.price - a.price);
  if (sort === "az") list.sort((a, b) => a.name.localeCompare(b.name));
  if (sort === "za") list.sort((a, b) => b.name.localeCompare(a.name));
  return list;
}
/* renderProducts — builds card HTML for the current page and injects it */
function renderProducts() {
  const filtered = getFiltered();
  const total = filtered.length;
  document.getElementById("item-count").textContent =
    total + " item" + (total !== 1 ? "s" : "");
  const titles = {
    all: "All Products",
    electronics: "Electronics",
    clothing: "Clothing",
    books: "Books",
    accessories: "Accessories",
  };
  document.getElementById("toolbar-title").textContent =
    titles[currentCat] || "Products";
  const grid = document.getElementById("product-grid");
  const empty = document.getElementById("empty-state");
  const pg = document.getElementById("pagination");
  if (total === 0) {
    grid.innerHTML = "";
    empty.style.display = "block";
    pg.innerHTML = "";
    return;
  }
  empty.style.display = "none";
  const start = (currentPage - 1) * PER_PAGE;
  const slice = filtered.slice(start, start + PER_PAGE);
  grid.innerHTML = slice
    .map(
      (p) => `
    <div class="product-card">
      <div class="card-stripe ${stripeClass(p.category)}"></div>
      <span class="cat-badge ${catBadgeClass(p.category)}">${p.category}</span>
      <p class="card-name">${p.name}</p>
      <p class="card-price">${fmt(p.price)}</p>
      <div class="card-footer">
        <span class="card-stock">${p.stock} units &nbsp;${stockBadge(p.stock)}</span>
        <button class="btn-del" onclick="deleteProduct(${p.id})">Delete</button>
      </div>
    </div>`,
    )
    .join("");
  renderPagination(total);
}
function renderPagination(total) {
  const pg = document.getElementById("pagination");
  const pages = Math.ceil(total / PER_PAGE);
  if (pages <= 1) {
    pg.innerHTML = "";
    return;
  }
  let h = `<button class="pg-btn" onclick="goPage(${currentPage - 1})" ${currentPage === 1 ? "disabled" : ""}>&larr; Prev</button>`;
  for (let i = 1; i <= pages; i++) {
    h += `<button class="pg-btn ${i === currentPage ? "active" : ""}" onclick="goPage(${i})">${i}</button>`;
  }
  h += `<button class="pg-btn" onclick="goPage(${currentPage + 1})" ${currentPage === pages ? "disabled" : ""}>Next &rarr;</button>`;
  pg.innerHTML = h;
}

function goPage(n) {
  currentPage = n;
  renderProducts();
  document.querySelector(".main-area").scrollIntoView({ behavior: "smooth" });
}

/* Tab switching — hides all panels, shows the chosen one */
function switchTab(tabName) {
  document
    .querySelectorAll(".tab-btn")
    .forEach((b) => b.classList.remove("active"));
  document
    .querySelectorAll(".tab-panel")
    .forEach((p) => p.classList.remove("active"));
  document.getElementById("tab-" + tabName).classList.add("active");
  document.getElementById("panel-" + tabName).classList.add("active");
  if (tabName === "analytics") renderAnalytics();
}

function setCategory(cat) {
  currentCat = cat;
  currentPage = 1;
  document.getElementById("search-cat-filter").value = cat;
  document
    .querySelectorAll(".csb")
    .forEach((b) => b.classList.remove("active"));
  const btn = document.getElementById("cs-" + cat);
  if (btn) btn.classList.add("active");
  renderProducts();
}

function toggleLow() {
  lowStockOnly = !lowStockOnly;
  currentPage = 1;
  const el = document.getElementById("low-tog");
  lowStockOnly ? el.classList.add("on") : el.classList.remove("on");
  renderProducts();
}

function onFilter() {
  const catDrop = document.getElementById("search-cat-filter").value;
  currentCat = catDrop;
  currentPage = 1;
  document
    .querySelectorAll(".csb")
    .forEach((b) => b.classList.remove("active"));
  const btn = document.getElementById("cs-" + catDrop);
  if (btn) btn.classList.add("active");
  renderProducts();
}
function deleteProduct(id) {
  allProducts = allProducts.filter(p => p.id !== id);
  saveToStorage();
  const f = getFiltered();
  const maxPage = Math.ceil(f.length / PER_PAGE) || 1;
  if (currentPage > maxPage) currentPage = maxPage;
  updateStats();
  renderProducts();
}
function showErr(errId, fgId, msg) {
  document.getElementById(errId).textContent = msg;
  document.getElementById(fgId)?.classList.add("has-err");
}
function clearErrs() {
  ["e-name","e-price","e-stock","e-cat"].forEach(id => { document.getElementById(id).textContent = ""; });
  ["fg-name","fg-price","fg-stock","fg-cat"].forEach(id => { document.getElementById(id)?.classList.remove("has-err"); });
}
 
function addProduct() {
  const name  = document.getElementById("f-name").value.trim();
  const price = parseFloat(document.getElementById("f-price").value);
  const stock = parseInt(document.getElementById("f-stock").value, 10);
  const cat   = document.getElementById("f-cat").value;
  clearErrs();
  let valid = true;
  if (!name)                      { showErr("e-name",  "fg-name",  "Name is required.");   valid=false; }
  if (isNaN(price) || price <= 0) { showErr("e-price", "fg-price", "Price must be > 0.");  valid=false; }
  if (document.getElementById("f-stock").value===""||isNaN(stock)||stock<0)
                                  { showErr("e-stock", "fg-stock", "Cannot be negative."); valid=false; }
  if (!cat)                       { showErr("e-cat",   "fg-cat",   "Please select one.");  valid=false; }
  if (!valid) return;
  allProducts.unshift({ id:Date.now(), name, price, stock, category:cat });
  saveToStorage();
  currentPage=1; currentCat="all"; lowStockOnly=false;
  document.getElementById("search-input").value      = "";
  document.getElementById("search-cat-filter").value = "all";
  document.getElementById("sort-sel").value          = "def";
  document.getElementById("low-tog").classList.remove("on");
  document.querySelectorAll(".csb").forEach(b=>b.classList.remove("active"));
  document.getElementById("cs-all")?.classList.add("active");
  document.getElementById("f-name").value=""; document.getElementById("f-price").value="";
  document.getElementById("f-stock").value=""; document.getElementById("f-cat").value="";
  clearErrs();
  updateStats(); renderProducts();
  const flash = document.getElementById("success-flash");
  flash.style.display = "flex";
  setTimeout(() => { flash.style.display = "none"; }, 4000);
}
 
function resetForm() {
  document.getElementById("f-name").value=""; document.getElementById("f-price").value="";
  document.getElementById("f-stock").value=""; document.getElementById("f-cat").value="";
  clearErrs();
  document.getElementById("success-flash").style.display = "none";
}

function renderAnalytics() {}


