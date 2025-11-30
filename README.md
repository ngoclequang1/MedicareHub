### 1. Clone repository

```bash
git clone https://github.com/AkihimaTomoya/MedicareHub.git medicarehub
cd medicarehub
```
### 2. Cấu hình application.properties
Mở file src/main/resources/application.properties và điền các thông tin còn thiếu:

```properties
# MYSQL
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

# EMAIL
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_GMAIL_APP_PASSWORD

# 📌 Lưu ý: Nếu tài khoản Gmail của bạn sử dụng xác minh 2 bước, hãy tạo mật khẩu ứng dụng riêng bằng cách truy cập https://myaccount.google.com/apppasswords.
