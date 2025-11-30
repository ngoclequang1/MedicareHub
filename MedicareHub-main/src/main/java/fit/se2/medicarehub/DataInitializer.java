package fit.se2.medicarehub;

import fit.se2.medicarehub.model.*;
import fit.se2.medicarehub.repository.AdminRepository;
import fit.se2.medicarehub.repository.AppointmentRepository;
import fit.se2.medicarehub.repository.DoctorRepository;
import fit.se2.medicarehub.repository.PatientRepository;
import fit.se2.medicarehub.repository.RoleRepository;
import fit.se2.medicarehub.repository.SpecialtyRepository;
import fit.se2.medicarehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.sql.Timestamp;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeRoles() {
        return args -> {
            // Khởi tạo role ADMIN
            Optional<Role> adminRoleOpt = roleRepository.findByRoleName("ROLE_ADMIN");
            if (adminRoleOpt.isEmpty()) {
                Role adminRole = new Role();
                adminRole.setRoleName("ROLE_ADMIN");
                roleRepository.save(adminRole);
                System.out.println("Role ADMIN created");
            }

            // Khởi tạo role DOCTOR
            Optional<Role> doctorRoleOpt = roleRepository.findByRoleName("ROLE_DOCTOR");
            if (doctorRoleOpt.isEmpty()) {
                Role doctorRole = new Role();
                doctorRole.setRoleName("ROLE_DOCTOR");
                roleRepository.save(doctorRole);
                System.out.println("Role DOCTOR created");
            }

            // Khởi tạo role PATIENT
            Optional<Role> patientRoleOpt = roleRepository.findByRoleName("ROLE_PATIENT");
            if (patientRoleOpt.isEmpty()) {
                Role patientRole = new Role();
                patientRole.setRoleName("ROLE_PATIENT");
                roleRepository.save(patientRole);
                System.out.println("Role PATIENT created");
            }
        };
    }

    @Bean
    public CommandLineRunner initializeAdminAccount() {
        return args -> {
            String adminEmail = "admin@example.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                Optional<Role> adminRoleOpt = roleRepository.findByRoleName("ROLE_ADMIN");
                Role adminRole;
                if (adminRoleOpt.isPresent()) {
                    adminRole = adminRoleOpt.get();
                } else {
                    adminRole = new Role();
                    adminRole.setRoleName("ROLE_ADMIN");
                    roleRepository.save(adminRole);
                }

                User admin = new User();
                admin.setUsername(adminEmail);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEnabled(true);
                admin.setRoleID(adminRole);
                admin.setCreatedAt(new Date());

                User adminUser = userRepository.save(admin);

                Admin adminEntity = new Admin();
                adminEntity.setUser(adminUser);
                adminRepository.save(adminEntity);
                System.out.println("Admin account created: " + adminEmail);
            } else {
                System.out.println("Admin account already exists: " + adminEmail);
            }
        };
    }

    @Bean
    public CommandLineRunner initializeDemoData() {
        return args -> {
            initializeRoles().run(args);
            // Khởi tạo chuyên khoa nếu chưa tồn tại
            String[] specialtyNames = {"Răng - Hàm - Mặt", "Nội tổng quát", "Ngoại tổng quát", "Da liễu", "Nhi khoa", "Tim mạch"};
            for (String name : specialtyNames) {
                Optional<Specialty> specOpt = specialtyRepository.findBySpecialtyName(name);
                if (specOpt.isEmpty()) {
                    Specialty spec = new Specialty();
                    spec.setSpecialtyName(name);
                    spec.setSpecialtyStatus(true);
                    spec.setNumberOfDoctors(0);
                    specialtyRepository.save(spec);
                    System.out.println("Specialty created: " + name);
                } else {
                    System.out.println("Specialty already exists: " + name);
                }
            }

            // Khởi tạo bác sĩ
            List<Map<String, Object>> doctorData = new ArrayList<>();

            Map<String, Object> d1 = new HashMap<>();
            d1.put("username", "nguyen.huy@example.com");
            d1.put("fullName", "Nguyễn Huy");
            d1.put("email", "nguyen.huy@example.com");
            d1.put("phoneNumber", "0912345678");
            d1.put("gender", "Nam");
            d1.put("identityNumber", "ID123456");
            d1.put("image", "images/cv2.png");
            d1.put("specialtyName", "Răng - Hàm - Mặt");
            d1.put("licenseNumber", "LIC123456");
            d1.put("clinicAddress", "12 Nguyễn Trãi");
            d1.put("academicDegree", "TS");
            d1.put("isHead", true);
            doctorData.add(d1);

            Map<String, Object> d2 = new HashMap<>();
            d2.put("username", "le.thao@example.com");
            d2.put("fullName", "Lê Thảo");
            d2.put("email", "le.thao@example.com");
            d2.put("phoneNumber", "0923456789");
            d2.put("gender", "Nữ");
            d2.put("identityNumber", "ID234567");
            d2.put("image", "images/cv1.png");
            d2.put("specialtyName", "Nội tổng quát");
            d2.put("licenseNumber", "LIC234567");
            d2.put("clinicAddress", "34 Lê Lợi");
            d2.put("academicDegree", "Ths");
            d2.put("isHead", false);
            doctorData.add(d2);

            Map<String, Object> d3 = new HashMap<>();
            d3.put("username", "tran.vu@example.com");
            d3.put("fullName", "Trần Vũ");
            d3.put("email", "tran.vu@example.com");
            d3.put("phoneNumber", "0934567890");
            d3.put("gender", "Nam");
            d3.put("identityNumber", "ID345678");
            d3.put("image", "images/cv2.png");
            d3.put("specialtyName", "Ngoại tổng quát");
            d3.put("licenseNumber", "LIC345678");
            d3.put("clinicAddress", "56 Trần Hưng Đạo");
            d3.put("academicDegree", "BS");
            d3.put("isHead", false);
            doctorData.add(d3);

            Map<String, Object> d4 = new HashMap<>();
            d4.put("username", "phuong.mai@example.com");
            d4.put("fullName", "Phương Mai");
            d4.put("email", "phuong.mai@example.com");
            d4.put("phoneNumber", "0945678901");
            d4.put("gender", "Nữ");
            d4.put("identityNumber", "ID456789");
            d4.put("image", "images/cv1.png");
            d4.put("specialtyName", "Da liễu");
            d4.put("licenseNumber", "LIC456789");
            d4.put("clinicAddress", "78 Hai Bà Trưng");
            d4.put("academicDegree", "TS");
            d4.put("isHead", true);
            doctorData.add(d4);

            Map<String, Object> d5 = new HashMap<>();
            d5.put("username", "hoang.nam@example.com");
            d5.put("fullName", "Hoàng Nam");
            d5.put("email", "hoang.nam@example.com");
            d5.put("phoneNumber", "0956789012");
            d5.put("gender", "Nam");
            d5.put("identityNumber", "ID567890");
            d5.put("image", "images/cv2.png");
            d5.put("specialtyName", "Nhi khoa");
            d5.put("licenseNumber", "LIC567890");
            d5.put("clinicAddress", "90 Pasteur");
            d5.put("academicDegree", "Ths");
            d5.put("isHead", false);
            doctorData.add(d5);

            Map<String, Object> d6 = new HashMap<>();
            d6.put("username", "bich.tram@example.com");
            d6.put("fullName", "Bích Trâm");
            d6.put("email", "bich.tram@example.com");
            d6.put("phoneNumber", "0967890123");
            d6.put("gender", "Nữ");
            d6.put("identityNumber", "ID678901");
            d6.put("image", "images/cv1.png");
            d6.put("specialtyName", "Tim mạch");
            d6.put("licenseNumber", "LIC678901");
            d6.put("clinicAddress", "102 Lý Tự Trọng");
            d6.put("academicDegree", "TS");
            d6.put("isHead", true);
            doctorData.add(d6);

            for (Map<String, Object> d : doctorData) {
                String username = (String) d.get("username");
                if (userRepository.findByUsername(username).isEmpty()) {
                    // Tạo user cho bác sĩ
                    User doctorUser = new User();
                    doctorUser.setUsername(username);
                    doctorUser.setFullName((String) d.get("fullName"));
                    doctorUser.setEmail((String) d.get("email"));
                    doctorUser.setPhoneNumber((String) d.get("phoneNumber"));
                    doctorUser.setGender((String) d.get("gender"));
                    doctorUser.setIdentityNumber((String) d.get("identityNumber"));
                    doctorUser.setImage((String) d.get("image"));
                    doctorUser.setPassword(passwordEncoder.encode("123"));
                    doctorUser.setEnabled(true);
                    doctorUser.setCreatedAt(new Date());

                    Role doctorRole = roleRepository.findByRoleName("ROLE_DOCTOR")
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy role DOCTOR"));
                    doctorUser.setRoleID(doctorRole);
                    User savedDoctorUser = userRepository.save(doctorUser);

                    // Tạo entity Doctor
                    Doctor doctor = new Doctor();
                    doctor.setUser(savedDoctorUser);
                    doctor.setLicenseNumber((String) d.get("licenseNumber"));
                    doctor.setClinicAddress((String) d.get("clinicAddress"));
                    doctor.setAcademicDegree(DoctorDegree.valueOf((String) d.get("academicDegree")));
                    doctor.setHead((Boolean) d.get("isHead"));

                    String specName = (String) d.get("specialtyName");
                    Specialty specialty = specialtyRepository.findBySpecialtyName(specName)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa: " + specName));
                    doctor.setSpecialty(specialty);

                    doctorRepository.save(doctor);

                    // Cập nhật số lượng bác sĩ trong chuyên khoa
                    List<Doctor> doctorsInSpec = specialty.getDoctors();
                    if (doctorsInSpec == null) {
                        specialty.setNumberOfDoctors(1);
                    } else {
                        Specialty s = specialtyRepository.findById(specialty.getSpecialtyID()).orElseThrow();
                        specialty.setNumberOfDoctors(s.getDoctors().size());
                    }
                    specialtyRepository.save(specialty);

                    System.out.println("Doctor created: " + username);
                } else {
                    System.out.println("Doctor already exists: " + username);
                }
            }

            // Khởi tạo bệnh nhân
            List<Map<String, Object>> patientData = new ArrayList<>();

            Map<String, Object> p1 = new HashMap<>();
            p1.put("username", "linh.nguyen@example.com");
            p1.put("fullName", "Linh Nguyễn");
            p1.put("email", "linh.nguyen@example.com");
            p1.put("phoneNumber", "0900000001");
            p1.put("gender", "Nữ");
            p1.put("identityNumber", "ID789012");
            p1.put("image", "images/cv1.png");
            p1.put("dob", "1990-05-15");
            p1.put("address", "123 Lý Thường Kiệt");
            p1.put("ethnicity", "Kinh");
            p1.put("code", "BN2346");
            patientData.add(p1);

            Map<String, Object> p2 = new HashMap<>();
            p2.put("username", "nam.tran@example.com");
            p2.put("fullName", "Nam Trần");
            p2.put("email", "nam.tran@example.com");
            p2.put("phoneNumber", "0900000002");
            p2.put("gender", "Nam");
            p2.put("identityNumber", "ID890123");
            p2.put("image", "images/cv2.png");
            p2.put("dob", "1985-07-20");
            p2.put("address", "456 Nguyễn Huệ");
            p2.put("ethnicity", "Kinh");
            p2.put("code", "BN2347");
            patientData.add(p2);

            Map<String, Object> p3 = new HashMap<>();
            p3.put("username", "hoang.anh@example.com");
            p3.put("fullName", "Hoàng Anh");
            p3.put("email", "hoang.anh@example.com");
            p3.put("phoneNumber", "0900000003");
            p3.put("gender", "Nam");
            p3.put("identityNumber", "ID901234");
            p3.put("image", "images/cv2.png");
            p3.put("dob", "2000-01-10");
            p3.put("address", "789 Trần Hưng Đạo");
            p3.put("ethnicity", "Kinh");
            p3.put("code", "BN2348");
            patientData.add(p3);

            Map<String, Object> p4 = new HashMap<>();
            p4.put("username", "thanh.thao@example.com");
            p4.put("fullName", "Thanh Thảo");
            p4.put("email", "thanh.thao@example.com");
            p4.put("phoneNumber", "0900000004");
            p4.put("gender", "Nữ");
            p4.put("identityNumber", "ID012345");
            p4.put("image", "images/cv1.png");
            p4.put("dob", "1995-09-25");
            p4.put("address", "321 Hai Bà Trưng");
            p4.put("ethnicity", "Hoa");
            p4.put("code", "BN2349");
            patientData.add(p4);

            for (Map<String, Object> p : patientData) {
                String username = (String) p.get("username");
                if (userRepository.findByUsername(username).isEmpty()) {
                    User patientUser = new User();
                    patientUser.setUsername(username);
                    patientUser.setFullName((String) p.get("fullName"));
                    patientUser.setEmail((String) p.get("email"));
                    patientUser.setPhoneNumber((String) p.get("phoneNumber"));
                    patientUser.setGender((String) p.get("gender"));
                    patientUser.setIdentityNumber((String) p.get("identityNumber"));
                    patientUser.setImage((String) p.get("image"));
                    patientUser.setPassword(passwordEncoder.encode("123"));
                    patientUser.setEnabled(true);
                    patientUser.setCreatedAt(new Date());

                    Role patientRole = roleRepository.findByRoleName("ROLE_PATIENT")
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy role PATIENT"));
                    patientUser.setRoleID(patientRole);
                    User savedPatientUser = userRepository.save(patientUser);

                    Patient patient = new Patient();
                    patient.setUser(savedPatientUser);
                    patient.setDob(java.sql.Date.valueOf((String) p.get("dob")));
                    patient.setAddress((String) p.get("address"));
                    patient.setEthnicity((String) p.get("ethnicity"));
                    patient.setPatientCode((String) p.get("code"));

                    patientRepository.save(patient);
                    System.out.println("Patient created: " + username);
                } else {
                    System.out.println("Patient already exists: " + username);
                }
            }

            // Khởi tạo lịch hẹn nếu chưa có
            if (appointmentRepository.count() == 0) {
                List<Map<String, Object>> appointmentData = new ArrayList<>();

                Map<String, Object> a1 = new HashMap<>();
                a1.put("doctorUsername", "nguyen.huy@example.com");
                a1.put("patientUsername", "linh.nguyen@example.com");
                a1.put("appointmentDate", "2025-04-18 09:00:00");
                a1.put("queueNumber", 1);
                a1.put("status", "CONFIRMED");
                appointmentData.add(a1);

                Map<String, Object> a2 = new HashMap<>();
                a2.put("doctorUsername", "le.thao@example.com");
                a2.put("patientUsername", "nam.tran@example.com");
                a2.put("appointmentDate", "2025-04-18 10:00:00");
                a2.put("queueNumber", 2);
                a2.put("status", "CONFIRMED");
                appointmentData.add(a2);

                Map<String, Object> a3 = new HashMap<>();
                a3.put("doctorUsername", "tran.vu@example.com");
                a3.put("patientUsername", "hoang.anh@example.com");
                a3.put("appointmentDate", "2025-04-18 14:30:00");
                a3.put("queueNumber", 3);
                a3.put("status", "CONFIRMED");
                appointmentData.add(a3);

                Map<String, Object> a4 = new HashMap<>();
                a4.put("doctorUsername", "phuong.mai@example.com");
                a4.put("patientUsername", "thanh.thao@example.com");
                a4.put("appointmentDate", "2025-04-18 08:45:00");
                a4.put("queueNumber", 4);
                a4.put("status", "CONFIRMED");
                appointmentData.add(a4);

                for (Map<String, Object> a : appointmentData) {
                    String doctorUsername = (String) a.get("doctorUsername");
                    String patientUsername = (String) a.get("patientUsername");

                    User doctorUser = userRepository.findByUsername(doctorUsername)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy doctor: " + doctorUsername));
                    User patientUser = userRepository.findByUsername(patientUsername)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy patient: " + patientUsername));

                    Doctor doctor = doctorRepository.findByUser_Email(doctorUser.getEmail());
                    if (doctor == null) {
                        throw new RuntimeException("Không tìm thấy doctor entity: " + doctorUsername);
                    }
                    Patient patient = patientRepository.findByUser_Email(patientUser.getEmail());
                    if (patient == null) {
                        throw new RuntimeException("Không tìm thấy patient entity: " + patientUsername);
                    }

                    Appointment appointment = new Appointment();
                    appointment.setDoctor(doctor);
                    appointment.setPatient(patient);
                    appointment.setAppointmentDate(Timestamp.valueOf((String) a.get("appointmentDate")));
                    appointment.setQueueNumber((Integer) a.get("queueNumber"));
                    appointment.setStatus(AppointmentStatus.valueOf((String) a.get("status")));
                    appointment.setCreatedAt(new Date());

                    appointmentRepository.save(appointment);
                    System.out.println("Appointment created: " + doctorUsername + " - " + patientUsername);
                }
            } else {
                System.out.println("Appointments already exist");
            }
        };
    }
}
