document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const addParam = urlParams.get("add");
    const editParam = urlParams.get("edit");

    const addModal = document.getElementById("add");
    const editModal = document.getElementById("edit");

    // Nếu có tham số "add" trong URL (dù là true hay false), hiển thị modal
    if (addParam !== null && addModal) {
        addModal.style.display = "flex";
    }

    // Nếu có tham số "edit" trong URL, hiển thị modal edit
    if (editParam !== null && editModal) {
        editModal.style.display = "flex";
    }

    // Nếu không có cả tham số "add" lẫn "edit", ẩn modal (nếu cần)
    if (addParam === null && editParam === null) {
        hideForm();
    }
});

// Mở modal và cập nhật URL với ?add=true hoặc ?edit=true
function showForm(formtype) {
    if (formtype === 'add' || formtype === 'schedule') {
        const modal = document.getElementById("add");
        if (modal) {
            modal.style.display = "flex";
            updateUrlParam("add", "true");
        }
    } else if (formtype === 'edit') {
        const modal = document.getElementById("edit");
        if (modal) {
            modal.style.display = "flex";
            updateUrlParam("edit", "true");
        }
    }
}

// Đóng modal và xóa ?add hoặc ?edit khỏi URL
function hideForm() {
    const addModal = document.getElementById("add");
    const editModal = document.getElementById("edit");

    if (addModal) {
        addModal.style.display = "none";
        history.replaceState(null, null, removeUrlParam("add"));
        // Nếu cần thiết, reset form trong modal add
        const form = addModal.querySelector("form");
        if (form) {
            form.reset();
            clearErrors();
        }
    }
    if (editModal) {
        editModal.style.display = "none";
        history.replaceState(null, null, removeUrlParam("edit"));
    }
}

// Cập nhật tham số URL
function updateUrlParam(key, value) {
    const url = new URL(window.location);
    url.searchParams.set(key, value);
    window.history.pushState({}, "", url);
}

// Xóa tham số khỏi URL
function removeUrlParam(key) {
    const url = new URL(window.location);
    url.searchParams.delete(key);
    return url.toString();
}

// Xóa tất cả lỗi hiển thị
function clearErrors() {
    document.querySelectorAll(".error-message").forEach((el) => {
        el.innerHTML = "";
        el.style.display = "none";
    });
    document.querySelectorAll("#addForm input, #addForm textarea").forEach((el) => {
        el.classList.remove("error");
    });
}

// Đóng modal khi bấm ngoài vùng modal
window.onclick = function (event) {
    const addModal = document.getElementById("add");
    const editModal = document.getElementById("edit");
    if (
        (addModal && !addModal.contains(event.target)) &&
        (editModal && !editModal.contains(event.target))
    ) {
        hideForm();
    }
};

// Phần cấu hình Flatpickr (không thay đổi)
document.addEventListener("DOMContentLoaded", function () {
    const scheduleDateInput = document.getElementById("startDate");
    const scheduleEndDateInput = document.getElementById("endDate");
    const startDateContainer = document.getElementById("startDateContainer");
    const endDateContainer = document.getElementById("endDateContainer");
    const startDateSpan = document.getElementById("startDateSpan");
    const endDateSpan = document.getElementById("endDateSpan");

    let isEndDateEnabled = false; // Kiểm tra xem flatpickr của ngày kết thúc đã được khởi tạo hay chưa

    startDateSpan.classList.add("active");
    startDateContainer.style.display = "block";
    endDateContainer.style.display = "none";

    flatpickr("#startDate", {
        inline: true,
        dateFormat: "Y-m-d",
        defaultDate: scheduleDateInput.value || new Date().toISOString().split("T")[0],
        onChange: function(selectedDates, dateStr) {
            scheduleDateInput.value = dateStr;
            if (scheduleEndDateInput._flatpickr) {
                scheduleEndDateInput._flatpickr.set('minDate', dateStr);
            }
            if (!isEndDateEnabled) {
                isEndDateEnabled = true;
                startDateSpan.classList.remove("active");
                endDateSpan.classList.add("active");
                startDateContainer.style.display = "none";
                endDateContainer.style.display = "block";
                if (!scheduleEndDateInput._flatpickr) {
                    flatpickr("#endDate", {
                        inline: true,
                        dateFormat: "Y-m-d",
                        defaultDate: dateStr,
                        minDate: dateStr,
                        onChange: function(selectedDates, dateStr) {
                            const startDate = new Date(scheduleDateInput.value);
                            const endDate = new Date(dateStr);
                            if (endDate < startDate) {
                                scheduleEndDateInput.value = scheduleDateInput.value;
                            } else {
                                scheduleEndDateInput.value = dateStr;
                            }
                        }
                    });
                }
            }
        }
    });

    endDateSpan.addEventListener("click", function () {
        if (!isEndDateEnabled) return;
        startDateContainer.style.display = "none";
        endDateContainer.style.display = "block";
        startDateSpan.classList.remove("active");
        endDateSpan.classList.add("active");
    });

    startDateSpan.addEventListener("click", function () {
        if (!isEndDateEnabled) return;
        startDateContainer.style.display = "block";
        endDateContainer.style.display = "none";
        startDateSpan.classList.add("active");
        endDateSpan.classList.remove("active");
    });
});

function selectDoctor(element) {
    const doctorID = element.getAttribute("data-doctor-id");
    const doctorName = element.getAttribute("data-doctor-name");
    document.getElementById("doctorID").value = doctorID;
    document.getElementById("doctorName").value = doctorName;
    showForm('schedule');
}
