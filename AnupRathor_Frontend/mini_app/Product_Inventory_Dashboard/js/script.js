/* loading overlay test */
window.addEventListener('load', () => {
  const overlay = document.getElementById('loading-overlay');
  setTimeout(() => { overlay.classList.add('gone'); }, 1500);
});


function setCategory(cat) {
  document.querySelectorAll(".csb").forEach(b => b.classList.remove("active"));
  const btn = document.getElementById("cs-" + cat);
  if (btn) btn.classList.add("active");
}

let allProducts = [], currentPage = 1, currentCat = "all", lowStockOnly = false;
const PER_PAGE = 8;
 
/* Tab switching — hides all panels, shows the chosen one */
function switchTab(tabName) {
  document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
  document.querySelectorAll(".tab-panel").forEach(p => p.classList.remove("active"));
  document.getElementById("tab-"   + tabName).classList.add("active");
  document.getElementById("panel-" + tabName).classList.add("active");
  if (tabName === "analytics") renderAnalytics();
}
 
function setCategory(cat) {
  currentCat = cat; currentPage = 1;
  document.getElementById("search-cat-filter").value = cat;
  document.querySelectorAll(".csb").forEach(b => b.classList.remove("active"));
  const btn = document.getElementById("cs-" + cat);
  if (btn) btn.classList.add("active");
}
 
function toggleLow() {
  lowStockOnly = !lowStockOnly; currentPage = 1;
  const el = document.getElementById("low-tog");
  lowStockOnly ? el.classList.add("on") : el.classList.remove("on");
}
 
function onFilter() {
  const catDrop = document.getElementById("search-cat-filter").value;
  currentCat = catDrop; currentPage = 1;
  document.querySelectorAll(".csb").forEach(b => b.classList.remove("active"));
  const btn = document.getElementById("cs-" + catDrop);
  if (btn) btn.classList.add("active");
}
 
function renderAnalytics() {}