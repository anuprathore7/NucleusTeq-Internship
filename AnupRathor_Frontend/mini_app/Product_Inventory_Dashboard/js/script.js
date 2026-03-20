/* loading overlay test */
window.addEventListener('load', () => {
  const overlay = document.getElementById('loading-overlay');
  setTimeout(() => { overlay.classList.add('gone'); }, 1500);
});