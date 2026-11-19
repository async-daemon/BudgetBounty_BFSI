document.addEventListener("DOMContentLoaded", function () {
  // Sidebar toggle (mobile)
  const sidebar = document.getElementById("appSidebar");
  const backdrop = document.getElementById("sidebarBackdrop");
  const toggleBtn = document.getElementById("sidebarToggle");

  function openSidebar() {
    sidebar && sidebar.classList.add("show");
    backdrop && backdrop.classList.add("show");
  }
  function closeSidebar() {
    sidebar && sidebar.classList.remove("show");
    backdrop && backdrop.classList.remove("show");
  }
  toggleBtn && toggleBtn.addEventListener("click", openSidebar);
  backdrop && backdrop.addEventListener("click", closeSidebar);

  // Auto-dismiss flash alerts
  document.querySelectorAll(".alert-auto-dismiss").forEach(function (el) {
    setTimeout(function () {
      el.classList.remove("show");
      setTimeout(() => el.remove(), 300);
    }, 5000);
  });

  // Mark notifications as read when dropdown opened
  const notifBtn = document.getElementById("notifDropdownBtn");
  if (notifBtn) {
    notifBtn.addEventListener("click", function () {
      const dot = document.getElementById("notifDot");
      if (dot) dot.style.display = "none";
      fetch(notifBtn.dataset.markReadUrl, {
        method: "POST",
        headers: { "X-CSRFToken": window.CSRF_TOKEN || "" },
      }).catch(() => {});
    });
  }

  // Dropzone click-through to hidden file input + filename preview
  document.querySelectorAll("[data-dropzone]").forEach(function (zone) {
    const input = zone.querySelector("input[type=file]");
    const label = zone.querySelector("[data-dropzone-label]");
    if (!input) return;
    zone.addEventListener("click", () => input.click());
    ["dragenter", "dragover"].forEach((evt) =>
      zone.addEventListener(evt, (e) => { e.preventDefault(); zone.classList.add("drag-over"); })
    );
    ["dragleave", "drop"].forEach((evt) =>
      zone.addEventListener(evt, (e) => { e.preventDefault(); zone.classList.remove("drag-over"); })
    );
    zone.addEventListener("drop", (e) => {
      if (e.dataTransfer.files.length) {
        input.files = e.dataTransfer.files;
        if (label) label.textContent = e.dataTransfer.files[0].name;
      }
    });
    input.addEventListener("change", () => {
      if (input.files.length && label) label.textContent = input.files[0].name;
    });
  });

  // Show/hide password toggle
  document.querySelectorAll("[data-password-toggle]").forEach(function (btn) {
    const input = document.getElementById(btn.dataset.passwordToggle);
    if (!input) return;
    btn.addEventListener("click", function () {
      const show = input.type === "password";
      input.type = show ? "text" : "password";
      btn.querySelector("i").className = show ? "fa-solid fa-eye-slash" : "fa-solid fa-eye";
      btn.setAttribute("aria-label", show ? "Hide password" : "Show password");
    });
  });

  // Animated count-up for hero stat numbers
  document.querySelectorAll("[data-count-up]").forEach(function (el) {
    const target = parseFloat(el.dataset.countUp);
    if (isNaN(target)) return;
    const prefix = el.dataset.prefix || "";
    const suffix = el.dataset.suffix || "";
    const duration = 900;
    const start = performance.now();
    function tick(now) {
      const progress = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const value = target * eased;
      el.textContent = prefix + Math.round(value).toLocaleString("en-IN") + suffix;
      if (progress < 1) requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  });
});
