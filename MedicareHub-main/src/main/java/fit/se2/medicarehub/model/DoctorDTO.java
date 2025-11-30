package fit.se2.medicarehub.model;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DoctorDTO {
    @NotBlank(message = "Giấy phép hành nghề là bắt buộc")
    @Size(min = 6, max = 20, message = "Giấy phép hành nghề phải từ 6 đến 20 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Giấy phép hành nghề chỉ được chứa chữ cái và số")
    private String licenseNumber;

    @NotBlank(message = "Địa chỉ phòng khám là bắt buộc")
    private String clinicAddress;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @NotBlank(message = "Học vị bác sĩ là bắt buộc")
    private String academicDegree;

    private Doctor doctor;

}
