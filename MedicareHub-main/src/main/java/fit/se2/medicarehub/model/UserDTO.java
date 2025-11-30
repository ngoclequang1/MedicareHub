package fit.se2.medicarehub.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserDTO {

    @NotBlank(message = "Tên là bắt buộc")
    private String firstName;

    @NotBlank(message = "Họ là bắt buộc")
    private String lastName;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải ít nhất 8 kí tự")
    private String password;

    private String confirmPassword;
}
