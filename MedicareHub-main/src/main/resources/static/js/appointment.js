document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const addParam = urlParams.get("create");
    if (addParam === "true") {
        showForm();
        history.replaceState(null, null, removeUrlParam("create"));
        hideForm();
    } else if (addParam === "false") {
        const modal = document.getElementById("create");
        if (modal) {
            modal.style.display = "flex";
        }
    }
});

// Show the modal form
function showForm() {
    const modal = document.getElementById("create");
    if (modal) {
        modal.style.display = "flex";
        updateUrlParam("create", "true");
        // Initialize the flatpickr after showing the form
        initializeFlatpickr();
    }
}

// Hide the modal form
function hideForm() {
    const modal = document.getElementById("create");
    if (modal) {
        modal.style.display = "none";
        history.replaceState(null, null, removeUrlParam("create"));
    }
    const form = modal.querySelector("form");
    if (form) {
        form.reset();
        clearErrors();
    }
}

// Update the URL parameter
function updateUrlParam(key, value) {
    const url = new URL(window.location);
    url.searchParams.set(key, value);
    window.history.pushState({}, "", url);
}

// Remove the URL parameter
function removeUrlParam(key) {
    const url = new URL(window.location);
    url.searchParams.delete(key);
    window.history.pushState({}, "", url);
}

// Clear errors
function clearErrors() {
    document.querySelectorAll(".error-message").forEach((el) => {
        el.innerHTML = "";
        el.style.display = "none";
    });
    document.querySelectorAll("#addForm input, #addForm textarea").forEach((el) => {
        el.classList.remove("error");
    });
}

// Close the modal when clicking outside
window.onclick = function (event) {
    const modal = document.getElementById("create");
    if (modal && event.target === modal) {
        hideForm();
    }
};

function selectDoctor(element) {
    const doctorID = element.getAttribute("data-doctor-id");

    // Update the doctor ID in the hidden field
    document.getElementById("doctorID").value = doctorID;

    const minDate = element.getAttribute("data-min-date");
    const maxDate = element.getAttribute("data-max-date");


    // Set the min and max date dynamically
    const appointmentDateInput = document.getElementById("appointmentDate");
    appointmentDateInput.setAttribute("min", minDate);
    appointmentDateInput.setAttribute("max", maxDate);

    // Show the modal form
    showForm();
}


// Initialize flatpickr with the updated min and max date
function initializeFlatpickr() {
    const appointmentDateInput = document.getElementById("appointmentDate");
    if (appointmentDateInput) {
        flatpickr(appointmentDateInput, {
            inline: true,
            minDate: appointmentDateInput.getAttribute("min"),
            maxDate: appointmentDateInput.getAttribute("max"),
            dateFormat: "Y-m-d", // Or any date format you prefer
        });
    }
}
