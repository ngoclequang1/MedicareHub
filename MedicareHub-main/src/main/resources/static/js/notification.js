document.addEventListener("DOMContentLoaded", () => {
    const popup = document.getElementById("reminder-popup");
    const cancelBtn = document.getElementById("popup-cancel");
    const prescriptionInput = document.getElementById("popup-prescription-id");
    const reminderInput = document.getElementById("reminderTime");

    // Hàm set min và value cho input datetime-local
    function setReminderMinTime() {
        const now = new Date();
        const offset = now.getTimezoneOffset();
        const localISOTime = new Date(now.getTime() - offset * 60000).toISOString().slice(0, 16);
        reminderInput.min = localISOTime;
        reminderInput.value = localISOTime;
    }

    document.querySelectorAll(".reminder-btn").forEach(btn => {
        btn.addEventListener("click", e => {
            e.stopPropagation();
            prescriptionInput.value = btn.getAttribute("data-prescription-id");
            setReminderMinTime(); // Gọi khi mở popup
            popup.classList.remove("hidden");
        });
    });

    // Đóng popup khi click ra ngoài
    document.addEventListener("click", () => popup.classList.add("hidden"));

    // Không đóng popup khi click vào trong form
    popup.querySelector(".popup-content").addEventListener("click", e => e.stopPropagation());

    cancelBtn.addEventListener("click", () => popup.classList.add("hidden"));
});