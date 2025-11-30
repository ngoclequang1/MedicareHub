package fit.se2.medicarehub.model;

import lombok.Data;

import java.util.Date;

@Data
public class AppointmentDTO {
    private String specialtyName;
    private String doctorName;
    private String doctorAcademicDegree;
    private Date appointmentDate;
    private int queueNumber;
    private String status;

    public void setStatusFromEnum(AppointmentStatus appointmentStatus) {
        switch (appointmentStatus) {
            case PENDING -> this.status = "Chờ xác nhận";
            case CONFIRMED -> this.status = "Chưa khám";
            case COMPLETED -> this.status = "Đã khám";
            case CANCELED -> this.status = "Đã hủy";
            default -> this.status = "Không xác định";
        }
    }
}
