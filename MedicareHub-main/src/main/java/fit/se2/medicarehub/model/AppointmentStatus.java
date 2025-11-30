package fit.se2.medicarehub.model;

public enum AppointmentStatus {
    PENDING, // Chờ xác nhận
    CONFIRMED, // Đã xác nhận
    ONGOING, // Đang khám
    COMPLETED, // Đã khám
    CANCELED // Đã hủy
}
