function renderStats(statsJson) {
    // Chuyển chuỗi JSON thành đối tượng
    var statsArray = JSON.parse(statsJson);

    // Tính tổng các appointment và patient từ tất cả các stat
    var totalAppointments = 0;
    var totalPatients = 0;
    statsArray.forEach(function(stat) {
        totalAppointments += stat.totalAppointment;
        totalPatients += stat.totalPatient;
    });

    // Cập nhật tổng bệnh nhân (giả sử phần tử có class .patient-count đã tồn tại)
    var patientCountEl = document.querySelector('.patient-count');
    if (patientCountEl) {
        patientCountEl.innerText = totalPatients;
    }

    // Gom dữ liệu topSpecialties từ tất cả stat
    var specialtiesMap = {};
    statsArray.forEach(function(stat) {
        if (stat.topSpecialtiesJson && stat.topSpecialtiesJson !== "[]") {
            var specialties = JSON.parse(stat.topSpecialtiesJson);
            specialties.forEach(function(specialty) {
                if (specialtiesMap[specialty.specialtyName]) {
                    specialtiesMap[specialty.specialtyName].count += specialty.count;
                } else {
                    specialtiesMap[specialty.specialtyName] = {
                        specialtyName: specialty.specialtyName,
                        topDoctorName: specialty.topDoctorName,
                        count: specialty.count
                    };
                }
            });
        }
    });

    // Chuyển map thành mảng và sắp xếp giảm dần theo count
    var combinedSpecialties = Object.values(specialtiesMap);
    combinedSpecialties.sort(function(a, b) {
        return b.count - a.count;
    });

    // Render danh sách topSpecialties vào phần tử có id "topSpecialtiesList"
    var listEl = document.getElementById("topSpecialtiesList");
    if (listEl) {
        listEl.innerHTML = ""; // Clear danh sách cũ
        if (combinedSpecialties.length > 0) {
            combinedSpecialties.forEach(function(specialty, index) {
                var li = document.createElement("li");
                li.innerHTML =
                    "<div class='left'>" +
                    "<strong>" + specialty.specialtyName + "</strong>" +
                    "<div class='doctor-name'>" + specialty.topDoctorName + "</div>" +
                    "</div>" +
                    "<span class='middle'><strong class='count'>" + specialty.count + "</strong> Lượt khám</span>" +
                    "<strong class='right'>#" + (index + 1) + "</strong>";
                listEl.appendChild(li);
            });
        } else {
            listEl.innerHTML = "<li>Không có dữ liệu cuộc hẹn.</li>";
        }
    }
}

// Khi DOM đã sẵn sàng, bạn có thể gọi hàm renderStats
document.addEventListener("DOMContentLoaded", function() {
    // Giả sử dữ liệu JSON được lưu trong biến global statsData
    if (typeof statsData !== 'undefined') {
        renderStats(statsData);
    }
});
