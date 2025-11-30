package fit.se2.medicarehub.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SpecialtyDTO {

    @NotBlank(message = "Tên khoa không được để trống")
    @Pattern(regexp = "^[\\p{Lu}][\\p{L}0-9\\- ]*$", message = "Tên khoa phải bắt đầu bằng chữ in hoa, có thể chứa chữ, số, dấu gạch ngang và khoảng trắng")
    private String specialtyName;

    @Pattern(regexp = "^(|[\\p{Lu}][\\p{L}0-9,;\\s]*\\.)$",
            message = "Mô tả phải bắt đầu bằng chữ in hoa, có thể chứa chữ, số, dấu phẩy, dấu chấm phẩy và phải kết thúc bằng dấu chấm hoặc để trống.")
    private String specialtyDescription;

    private Long doctorHeadId;
}
