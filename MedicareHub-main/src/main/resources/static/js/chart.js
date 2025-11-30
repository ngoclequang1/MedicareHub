// chart.js
document.addEventListener("DOMContentLoaded", function() {
    // Lấy dữ liệu từ biến toàn cục window.chartStatsData
    var stats = window.chartStatsData || '[]';
    stats = JSON.parse(stats);
    console.log(stats);

    var labels = [];
    var values = [];

    stats.forEach(function(item) {
        var d = new Date(item.statDate);
        var day = String(d.getDate()).padStart(2, '0');
        var month = String(d.getMonth() + 1).padStart(2, '0');
        var year = d.getFullYear();
        labels.push(day + "/" + month + "/" + year);
        values.push(item.totalAppointment);
    });

    var ctx = document.getElementById('appointmentChart').getContext('2d');
    var appointmentChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Đặt khám',
                data: values,
                fill: false,
                backgroundColor: '#009688',
                borderColor: '#009688',
                borderWidth: 2,
                tension: 0.4,
                pointBackgroundColor: '#fff',
                pointBorderColor: '#009688',
                pointRadius: 5,
                pointHoverRadius: 7
            }]
        },
        options: {
            responsive: false,
            scales: {
                x: {
                    title: {
                        display: false,
                        text: 'Ngày'
                    }
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1,
                        callback: function (value) {
                            if (Number.isInteger(value)) {
                                return value;
                            }
                        }
                    },
                    title: {
                        display: true,
                        text: 'Số lượt đặt khám',
                        color: '#000',
                        font: {
                            size: 14,
                            style: 'bold',
                            family: 'Roboto'
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    display: true,
                    labels: {
                        font: {
                            size: 14,
                            style: 'bold',
                            family: 'Roboto'
                        },
                        color: '#000',
                        boxWidth: 15,
                        padding: 20
                    }
                }
            }
        }
    });
});
