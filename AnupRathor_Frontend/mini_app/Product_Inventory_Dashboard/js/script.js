let allProducts = [];

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

/* loading overlay test */
window.addEventListener("load", () => {
  const overlay = document.getElementById("loading-overlay");
  setTimeout(() => {
    overlay.classList.add("gone");
  }, 1500);
});

function setCategory(cat) {
  document
    .querySelectorAll(".csb")
    .forEach((b) => b.classList.remove("active"));
  const btn = document.getElementById("cs-" + cat);
  if (btn) btn.classList.add("active");
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
}

function toggleLow() {
  lowStockOnly = !lowStockOnly;
  currentPage = 1;
  const el = document.getElementById("low-tog");
  lowStockOnly ? el.classList.add("on") : el.classList.remove("on");
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
}

function renderAnalytics() {}
