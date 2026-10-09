# Medicare Hub

Medicare Hub là hệ thống đặt lịch khám trực tuyến dành cho bệnh nhân, bác sĩ và quản trị viên. Hệ thống hỗ trợ đặt và quản lý lịch khám, hồ sơ bệnh án, đơn thuốc, nhắc lịch qua email, thống kê vận hành và chatbot tư vấn sức khỏe.

> Chatbot chỉ cung cấp thông tin tham khảo, không thay thế chẩn đoán hoặc tư vấn của bác sĩ. Trường hợp khẩn cấp cần liên hệ cơ sở y tế gần nhất.

## Chức năng hiện có

- Bệnh nhân: đăng ký, tạo hồ sơ, tìm bác sĩ, đặt lịch, xem lịch sử khám và tạo nhắc lịch/nhắc thuốc.
- Bác sĩ: xem lịch làm việc, danh sách bệnh nhân và lập hồ sơ khám.
- Quản trị viên: quản lý bác sĩ, bệnh nhân, chuyên khoa, lịch làm việc và dashboard thống kê.
- Hệ thống: phân quyền theo vai trò, email SMTP, chatbot và khôi phục mật khẩu có token hết hạn.

## Công nghệ

- Java 21, Spring Boot 3.4
- Spring MVC, Spring Security, Spring Data JPA
- Thymeleaf, JavaScript, CSS
- MySQL, Maven

## Chạy project

Yêu cầu: JDK 21 và MySQL 8+.

Thiết lập biến môi trường trong PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/medicarehubdb?createDatabaseIfNotExist=true"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-password"
$env:MAIL_USERNAME = "your-address@gmail.com"
$env:MAIL_PASSWORD = "your-gmail-app-password"
$env:BOOTSTRAP_ADMIN_EMAIL = "admin@your-domain.com"
$env:BOOTSTRAP_ADMIN_PASSWORD = "a-strong-initial-password"
```

Không commit mật khẩu thật vào `application.properties`. Với Gmail bật xác minh hai bước, sử dụng App Password.

Khởi động:

```powershell
.\mvnw.cmd spring-boot:run
```

Truy cập `http://localhost:8080`.

Tài khoản quản trị ban đầu chỉ được tạo khi cả hai biến `BOOTSTRAP_ADMIN_EMAIL` và `BOOTSTRAP_ADMIN_PASSWORD` được cung cấp. Dữ liệu bác sĩ/bệnh nhân mẫu bị tắt mặc định; để bật trong môi trường phát triển, chạy với profile `demo`:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Chạy kiểm thử:

```powershell
.\mvnw.cmd test
```

## Cấu trúc chính

```text
src/main/java/fit/se2/medicarehub/
├── controller/   MVC endpoints theo vai trò
├── model/        JPA entities và DTO
├── repository/   Truy cập dữ liệu
├── security/     UserDetails cho Spring Security
└── service/      Nghiệp vụ và tích hợp email

src/main/resources/
├── templates/    Giao diện Thymeleaf
└── static/       CSS, JavaScript và hình ảnh
```

## An toàn và quyền riêng tư

- CSRF được bật cho các thao tác thay đổi dữ liệu.
- Mật khẩu được băm bằng BCrypt; token đặt lại mật khẩu hết hạn sau 30 phút.
- Dữ liệu lịch hẹn và hồ sơ thuốc được kiểm tra quyền sở hữu trước khi truy cập hoặc thay đổi.
- Scheduler nhắc lịch sử dụng cơ chế claim nguyên tử, retry tối đa 5 lần và lưu lỗi gửi gần nhất để hạn chế gửi trùng.
- Khi triển khai thật, cần dùng HTTPS, database migration (Flyway/Liquibase), audit log, backup mã hóa và chính sách lưu/xóa dữ liệu y tế.

## Roadmap đề xuất

1. Bổ sung kiểm thử controller, phân quyền và các luồng đặt lịch cạnh tranh.
2. Đưa tác vụ email vào queue, có retry và theo dõi trạng thái gửi.
3. Thêm consent, audit log, mã hóa dữ liệu nhạy cảm và quy trình xuất/xóa dữ liệu bệnh nhân.
4. Đặt guardrail cho chatbot: cảnh báo cấp cứu, giới hạn phạm vi trả lời, ẩn PII và lưu vết đồng thuận.

## Lưu ý triển khai

`spring.jpa.hibernate.ddl-auto=update` phù hợp cho phát triển cục bộ nhưng không nên dùng để quản lý schema production. Hãy chuyển sang migration có phiên bản trước khi phát hành.
