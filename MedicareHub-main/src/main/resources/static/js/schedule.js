document.addEventListener("DOMContentLoaded", function () {
    // Lấy dữ liệu lịch trình từ Thymeleaf
    var scheduleList = JSON.parse(document.getElementById('scheduleData').textContent);

    // Lấy ngày hiện tại
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth(); // 0-11
    const currentDay = currentDate.getDate();

    // Cập nhật tiêu đề lịch
    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    document.querySelector(".calendar-title").textContent = `${monthNames[currentMonth]} ${currentYear}`;

    // Tạo lịch và hiển thị
    generateCalendar(currentYear, currentMonth);

    // Hàm cập nhật sidebar (được tối ưu hóa và tích hợp dữ liệu lịch trình)
    function updateSidebar(selectedDate) {
        const day = selectedDate.getDate();
        const month = selectedDate.getMonth() + 1;
        const year = selectedDate.getFullYear();
        const dateKey = formatDate(selectedDate);

        let scheduleForDate = null;
        for (const schedule of scheduleList) {
            const startDate = new Date(schedule.startDate);
            const endDate = new Date(schedule.endDate);

            // Tạo Date objects chỉ chứa ngày để so sánh (bỏ qua thời gian)
            const startDateOnly = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
            const endDateOnly = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());
            const selectedDateOnly = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), selectedDate.getDate());

            if (selectedDateOnly >= startDateOnly && selectedDateOnly <= endDateOnly) { // So sánh với DateOnly
                scheduleForDate = schedule;
                console.log("Schedule found for date:", dateKey, schedule);
                break;
            }
        }

        let room = "Chưa xếp lịch";
        let activity = "Khám tổng quát";
        let morningSchedule = null;
        let afternoonSchedule = null;
        let noSchedule = false;

        if (scheduleForDate) {
            room = scheduleForDate.room || room;
            activity = scheduleForDate.service || activity;

            const startDateSchedule = new Date(scheduleForDate.startDate);
            const endDateSchedule = new Date(scheduleForDate.endDate);

            const isStartDate = selectedDate.getFullYear() === startDateSchedule.getFullYear() &&
                selectedDate.getMonth() === startDateSchedule.getMonth() &&
                selectedDate.getDate() === startDateSchedule.getDate();

            const isEndDate = selectedDate.getFullYear() === endDateSchedule.getFullYear() &&
                selectedDate.getMonth() === endDateSchedule.getMonth() &&
                selectedDate.getDate() === endDateSchedule.getDate();

            const morningEndTimeHour = 12;
            const afternoonStartTimeHour = 13;

            const startDateHour = startDateSchedule.getHours();
            const endDateHour = endDateSchedule.getHours();

            if (startDateHour < morningEndTimeHour && endDateHour >= afternoonStartTimeHour) {
                // Full day schedule
                morningSchedule = {
                    startTime: isStartDate ? startDateSchedule.toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit'
                    }) : "08:00",
                    endTime: "12:00"
                };
                afternoonSchedule = {
                    startTime: "13:00",
                    endTime: isEndDate ? endDateSchedule.toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit'
                    }) : "17:00"
                };
            } else if (endDateHour < afternoonStartTimeHour) {
                // Morning only schedule
                morningSchedule = {
                    startTime: isStartDate ? startDateSchedule.toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit'
                    }) : "08:00",
                    endTime: isEndDate ? endDateSchedule.toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit'
                    }) : "12:00" // Or endDateSchedule.toLocaleTimeString if more precise
                };
            } else if (startDateHour >= afternoonStartTimeHour) {
                // Afternoon only schedule
                afternoonSchedule = {
                    startTime: isStartDate ? startDateSchedule.toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit'
                    }) : "13:00",
                    endTime: isEndDate ? endDateSchedule.toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit'
                    }) : "17:00"
                };
            } else {
                // Fallback or handle other cases if needed - default to full day with standard times
                morningSchedule = {startTime: "08:00", endTime: "12:00"};
                afternoonSchedule = {startTime: "13:00", endTime: "17:00"};
            }
        } else {
            noSchedule = true;
        }
        let scheduleDetailsHTML = '';

        if (noSchedule) {
            scheduleDetailsHTML += `
            <div class="schedule-section">
                <h4>Buổi sáng</h4>
                <p>Chưa xếp lịch</p>
                <p><strong>Phòng:</strong> Chưa xếp lịch</p>
                <p><strong>Dịch vụ:</strong> Chưa xếp lịch</p>
                <h4>Buổi chiều</h4>
                <p>Chưa xếp lịch</p>
                <p><strong>Phòng:</strong> Chưa xếp lịch</p>
                <p><strong>Dịch vụ:</strong> Chưa xếp lịch</p>
            </div>
        `;
        } else {
            if (morningSchedule) {
                scheduleDetailsHTML += `
            <div class="schedule-section">
                <h4>Buổi sáng</h4>
                <p>${morningSchedule.startTime} - ${morningSchedule.endTime}</p>
                <p><strong>Phòng:</strong> ${room}</p>
                <p><strong>Dịch vụ:</strong> ${activity}</p>
            </div>
        `;
            }

            if (afternoonSchedule) {
                scheduleDetailsHTML += `
            <div class="schedule-section">
                <h4>Buổi chiều</h4>
                <p>${afternoonSchedule.startTime} - ${afternoonSchedule.endTime}</p>
                <p><strong>Phòng:</strong> ${room}</p>
                <p><strong>Dịch vụ:</strong> ${activity}</p>
            </div>
        `;
            }
        }

        document.querySelector(".info-box").innerHTML = `
        <div class="highlight-day-large">${day}</div>
        <div class="info-month">Tháng ${month}</div>
        <div class="schedule-details">
            ${scheduleDetailsHTML}
        </div>
        <div class="info-note">Click vào ngày khác để xem lịch trực</div>
    `;
    }

    // Xử lý click vào ngày
    document.querySelector(".days").addEventListener("click", function (e) {
        const dayElement = e.target.closest(".day");
        if (!dayElement || dayElement.classList.contains("empty")) return;

        const dayNumber = parseInt(dayElement.querySelector(".day-number").textContent);
        const selectedDate = new Date(currentYear, currentMonth, dayNumber);

        updateSidebar(selectedDate);

        // Đánh dấu ngày được chọn (tùy chọn, nếu bạn muốn highlight ngày click)
        document.querySelectorAll(".day").forEach(d => d.classList.remove("selected")); // Bỏ chọn ngày cũ
        dayElement.classList.add("selected"); // Chọn ngày mới
    });

    // Hàm định dạng ngày (giữ nguyên)
    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    }

    // Hiển thị thông tin ngày hiện tại ban đầu
    updateSidebar(currentDate);

    // Hàm tạo phần tử ngày (giữ nguyên)
    function createDayElement(day) {
        const dayElement = document.createElement("div");
        dayElement.className = day ? "day" : "day empty";

        if (day) {
            const dayNumber = document.createElement("span");
            dayNumber.className = "day-number";
            dayNumber.textContent = day;
            dayElement.appendChild(dayNumber);
        }
        return dayElement;
    }

    // Hàm tạo lịch (tối ưu hóa đánh dấu ngày hiện tại)
    function generateCalendar(year, month) {
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        const startingDay = firstDay.getDay(); // 0-6 (Chủ nhật - Thứ 7)

        const daysContainer = document.querySelector(".days");
        daysContainer.innerHTML = "";

        // Thêm các ô trống cho những ngày đầu tháng
        for (let i = 0; i < startingDay; i++) {
            daysContainer.appendChild(createDayElement(""));
        }

        // Thêm các ngày trong tháng
        for (let day = 1; day <= daysInMonth; day++) {
            const dayElement = createDayElement(day);
            if (day === currentDay && month === currentMonth && year === currentYear) {
                dayElement.classList.add("today");
            }
            daysContainer.appendChild(dayElement);
        }

        // Điền đủ 42 ô (6 tuần) để giữ layout ổn định
        const totalCells = startingDay + daysInMonth;
        const remainingCells = 42 - totalCells;
        for (let i = 0; i < remainingCells; i++) {
            daysContainer.appendChild(createDayElement(""));
        }
    }
});