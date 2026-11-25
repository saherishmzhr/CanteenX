document.addEventListener("DOMContentLoaded", function () {
    // Mobile nav toggle
    const toggleBtn = document.getElementById("navToggle");
    const navLinks = document.getElementById("navLinks");
    const navUser = document.getElementById("navUser");

    if (toggleBtn) {
        toggleBtn.addEventListener("click", function () {
            if (navLinks) navLinks.classList.toggle("open");
            if (navUser) navUser.classList.toggle("open");
        });
    }

    // Demo login quick-fill buttons on login page
    const demoButtons = document.querySelectorAll(".fill-demo-btn");
    demoButtons.forEach(function (btn) {
        btn.addEventListener("click", function () {
            const username = btn.getAttribute("data-username");
            const password = btn.getAttribute("data-password");
            const userInput = document.getElementById("username");
            const passInput = document.getElementById("password");
            if (userInput && username) userInput.value = username;
            if (passInput && password) passInput.value = password;
            if (userInput) userInput.focus();
        });
    });

    // Confirm destructive/cancel actions
    const confirmForms = document.querySelectorAll("form[data-confirm]");
    confirmForms.forEach(function (form) {
        form.addEventListener("submit", function (event) {
            const msg = form.getAttribute("data-confirm");
            if (msg && !window.confirm(msg)) {
                event.preventDefault();
            }
        });
    });
});
