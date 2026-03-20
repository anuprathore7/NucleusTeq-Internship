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