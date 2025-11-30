document.addEventListener("DOMContentLoaded",  () => {
    // Nút xóa hồ sơ
    const deleteBtn = document.querySelector(".delete-btn");
    const deletePopup = document.getElementById("deletePopup");
    const closeDeleteBtn = document.getElementById("closeDelete");
    const cancelBtn = document.querySelector(".cancel-btn");
    const confirmDeleteBtn = document.querySelector(".confirm-btn");

    // Nút chi tiết
    const detailBtn = document.querySelector(".detail-btn");
    const detailPopup = document.getElementById("detailPopup");
    const closeDetailBtn = document.getElementById("closeDetail");

    // Mở popup khi bấm Xóa hồ sơ
    if (deleteBtn) {
        deleteBtn.addEventListener("click", () => {
            deletePopup.style.display = "flex";
        });
    }

    // Đóng popup Xóa hồ sơ
    if (closeDeleteBtn) {
        closeDeleteBtn.addEventListener("click", () => {
            deletePopup.style.display = "none";
        });
    }
    if (cancelBtn) {
        cancelBtn.addEventListener("click", () => {
            deletePopup.style.display = "none";
        });
    }

    // Xử lý nút "Có, hãy xóa đi"
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", () => {
            const patientId = deleteBtn.getAttribute("data-id");
            window.location.href = "/patient/delete-report?patientId=" + patientId;
        });
    }

    // Mở popup chi tiết
    if (detailBtn) {
        detailBtn.addEventListener("click", () => {
            detailPopup.style.display = "flex";
        });
    }

    // Đóng popup Chi tiết
    if (closeDetailBtn) {
        closeDetailBtn.addEventListener("click", () => {
            detailPopup.style.display = "none";
        });
    }
});
