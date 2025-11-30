document.addEventListener("DOMContentLoaded", function() {
    const userBtn = document.getElementById("user-btn");
    const userDropdown = document.getElementById("user-dropdown");

    userBtn.addEventListener("click", () => {
        userDropdown.style.display =
            userDropdown.style.display === "block" ? "none" : "block";
    });

    document.addEventListener("click", function (e) {
        if (!userBtn.contains(e.target) && !userDropdown.contains(e.target)) {
            userDropdown.style.display = "none";
        }
    });
});
