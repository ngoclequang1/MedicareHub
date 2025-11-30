document.addEventListener("DOMContentLoaded", function () {
    let tests = [];
    let prescriptions = [];

    const testTableBody = document.getElementById("test-table-body");
    const prescriptionTableBody = document.getElementById("prescription-table-body");
    const medicalRecordForm = document.getElementById("medical-record-form");

    function displayTests() {
        testTableBody.innerHTML = "";
        tests.forEach((test, index) => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${test.serviceName}</td>
                <td>${test.instruction}</td>
                <td style="width: auto;"><button type="button" class="action-btn"><i class="fas fa-upload"></i></button></td>
            `;
            testTableBody.appendChild(row);
        });
    }

    function displayPrescriptions() {
        prescriptionTableBody.innerHTML = "";
        prescriptions.forEach((prescription, index) => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${prescription.medicineName}</td>
                <td>${prescription.instruction}</td>
                <td>${prescription.quantity}</td>
                <td>${prescription.price}</td>
            `;
            prescriptionTableBody.appendChild(row);
        });
    }

    // Hàm chuyển dữ liệu mảng thành các input ẩn với tên phù hợp với th:object của form
    function appendHiddenInputs() {
        // Xóa các input ẩn cũ nếu có
        document.querySelectorAll(".hidden-test").forEach(input => input.remove());
        document.querySelectorAll(".hidden-prescription").forEach(input => input.remove());

        tests.forEach((test, i) => {
            const inputServiceName = document.createElement("input");
            inputServiceName.type = "hidden";
            inputServiceName.name = `tests[${i}].serviceName`;
            inputServiceName.value = test.serviceName;
            inputServiceName.classList.add("hidden-test");

            const inputInstruction = document.createElement("input");
            inputInstruction.type = "hidden";
            inputInstruction.name = `tests[${i}].instruction`;
            inputInstruction.value = test.instruction;
            inputInstruction.classList.add("hidden-test");

            const inputNote = document.createElement("input");
            inputNote.type = "hidden";
            inputNote.name = `tests[${i}].note`;
            inputNote.value = test.note;
            inputNote.classList.add("hidden-test");

            medicalRecordForm.appendChild(inputServiceName);
            medicalRecordForm.appendChild(inputInstruction);
            medicalRecordForm.appendChild(inputNote);
        });

        prescriptions.forEach((prescription, i) => {
            const inputMedicineName = document.createElement("input");
            inputMedicineName.type = "hidden";
            inputMedicineName.name = `prescriptions[${i}].medicineName`;
            inputMedicineName.value = prescription.medicineName;
            inputMedicineName.classList.add("hidden-prescription");

            const inputPrescriptionInstruction = document.createElement("input");
            inputPrescriptionInstruction.type = "hidden";
            inputPrescriptionInstruction.name = `prescriptions[${i}].instruction`;
            inputPrescriptionInstruction.value = prescription.instruction;
            inputPrescriptionInstruction.classList.add("hidden-prescription");

            const inputQuantity = document.createElement("input");
            inputQuantity.type = "hidden";
            inputQuantity.name = `prescriptions[${i}].quantity`;
            inputQuantity.value = prescription.quantity;
            inputQuantity.classList.add("hidden-prescription");

            const inputPrice = document.createElement("input");
            inputPrice.type = "hidden";
            inputPrice.name = `prescriptions[${i}].price`;
            inputPrice.value = prescription.price;
            inputPrice.classList.add("hidden-prescription");

            medicalRecordForm.appendChild(inputMedicineName);
            medicalRecordForm.appendChild(inputPrescriptionInstruction);
            medicalRecordForm.appendChild(inputQuantity);
            medicalRecordForm.appendChild(inputPrice);
        });
    }

    // Xử lý nút "Thêm" trong phần "Thực hiện xét nghiệm"
    const testSection = document.querySelector("#test-section");
    const addTestButton = testSection.querySelector(".btn-add-test");
    addTestButton.addEventListener("click", function () {
        const testServiceName = document.getElementById("test-service-name");
        const testInstruction = document.getElementById("test-instruction");
        const testNote = document.getElementById("test-note");

        if (!testServiceName.value.trim() || !testInstruction.value.trim()) {
            alert("Vui lòng điền đầy đủ Tên dịch vụ và Chỉ định thực hiện!");
            return;
        }

        tests.push({
            serviceName: testServiceName.value.trim(),
            instruction: testInstruction.value.trim(),
            note: testNote.value.trim()
        });

        displayTests();

        testServiceName.value = "";
        testInstruction.value = "";
        testNote.value = "";
        testServiceName.focus();
    });

    // Xử lý nút "Thêm" trong phần "Kê đơn thuốc"
    const prescriptionSection = document.querySelector("#prescription-section");
    const addPrescriptionButton = prescriptionSection.querySelector(".btn-add-prescription");
    addPrescriptionButton.addEventListener("click", function () {
        const medicineName = document.getElementById("medicine-name");
        const medicineInstruction = document.getElementById("medicine-instruction");
        const medicineQuantity = document.getElementById("medicine-quantity");
        const medicinePrice = document.getElementById("medicine-price");

        if (!medicineName.value.trim() || !medicineInstruction.value.trim() ||
            !medicineQuantity.value.trim() || !medicinePrice.value.trim()) {
            alert("Vui lòng điền đầy đủ thông tin thuốc!");
            return;
        }

        prescriptions.push({
            medicineName: medicineName.value.trim(),
            instruction: medicineInstruction.value.trim(),
            quantity: medicineQuantity.value.trim(),
            price: medicinePrice.value.trim()
        });

        displayPrescriptions();

        medicineName.value = "";
        medicineInstruction.value = "";
        medicineQuantity.value = "";
        medicinePrice.value = "";
        medicineName.focus();
    });

    // Xử lý nút "Hủy" trong phần "Kê đơn thuốc"
    const cancelPrescriptionButton = prescriptionSection.querySelector(".btn-cancel");
    cancelPrescriptionButton.addEventListener("click", function () {
        const medicineName = document.getElementById("medicine-name");
        const medicineInstruction = document.getElementById("medicine-instruction");
        const medicineQuantity = document.getElementById("medicine-quantity");
        const medicinePrice = document.getElementById("medicine-price");

        medicineName.value = "";
        medicineInstruction.value = "";
        medicineQuantity.value = "";
        medicinePrice.value = "";
        medicineName.focus();
    });

    // Tích hợp Flatpickr cho trường "Ngày khám"
    const dateInput = document.querySelector("#examination-date");
    const calendarIcon = document.querySelector(".date-input i");
    const flatpickrInstance = flatpickr(dateInput, {
        dateFormat: "d/m/Y",
        defaultDate: "DD/MM/YY",
        position: "below",
        onOpen: function (selectedDates, dateStr, instance) {
            const iconRect = calendarIcon.getBoundingClientRect();
            const calendar = instance.calendarContainer;
            calendar.style.position = "absolute";
            calendar.style.top = `${iconRect.bottom + window.scrollY + 5}px`;
            calendar.style.left = `${iconRect.left + window.scrollX - 150}px`;
        },
        onChange: function (selectedDates, dateStr, instance) {
            dateInput.value = dateStr;
        }
    });

    calendarIcon.addEventListener("click", function () {
        flatpickrInstance.open();
    });

    dateInput.addEventListener("click", function () {
        flatpickrInstance.open();
    });

    // Hàm toggleSection có phạm vi toàn cục
    window.toggleSection = function (sectionId) {
        const section = document.getElementById(sectionId);
        const icon = section.parentElement.querySelector(".section-icon");
        if (section.style.display === "none" || section.style.display === "") {
            section.style.display = "block";
            if (icon) {
                icon.classList.add("active");
            }
        } else {
            section.style.display = "none";
            if (icon) {
                icon.classList.remove("active");
            }
        }
    };

    // Xử lý modal xác nhận
    const confirmationModal = document.getElementById("confirmationModal");
    const submitRecordBtn = document.getElementById("submitRecord");
    const cancelBtn = document.getElementById("cancelBtn");
    const confirmBtn = document.getElementById("confirmBtn");

    submitRecordBtn.addEventListener("click", function() {
        confirmationModal.style.display = "block";
    });

    cancelBtn.addEventListener("click", function() {
        confirmationModal.style.display = "none";
    });

    confirmBtn.addEventListener("click", function() {
        confirmationModal.style.display = "none";
        appendHiddenInputs(); // Chuyển dữ liệu trước khi submit form
        medicalRecordForm.submit();
    });
});
