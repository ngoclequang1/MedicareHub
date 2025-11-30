package fit.se2.medicarehub.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleDTO {

    private Long scheduleID;
    private Long doctorID;

    private String service;

    private String room;
    @NotNull(message = "Số lượng chỗ không được để trống")
    @Min(value = 1, message = "Số lượng chỗ có tối thiểu là 1")
    @Max(value = 30, message = "Số lượng chỗ có tối đa là 30")
    private Integer seatCount;

    private String startDate;

    private String endDate;
}
