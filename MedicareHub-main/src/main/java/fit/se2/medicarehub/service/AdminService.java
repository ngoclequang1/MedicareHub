package fit.se2.medicarehub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.se2.medicarehub.model.*;
import fit.se2.medicarehub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private Dao adminDao;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private AppointmentStatsRepository appointmentStatsRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private SpecialtyRepository specialtyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;


    public Admin getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return adminRepository.findByUser_Email(email);
    }

    public void updateCurrentUser(Admin admin) {
        adminRepository.save(admin);
    }

    public AppointmentStats calculateAndSaveDailyStats(Date date) {
        AppointmentStats stats = appointmentStatsRepository.findByStatDate(date);
        if (stats == null) {
            stats = new AppointmentStats();
            stats.setStatDate(date);
        }

        Long totalAppointments = appointmentRepository.countByDate(date);
        Long totalPatients = appointmentRepository.countDistinctPatientByDate(date);
        List<Object[]> topSpecialties = appointmentRepository.findTopSpecialtiesByDate(
                date,
                PageRequest.of(0, 5)  // lấy 5 kết quả
        );
        List<Map<String, Object>> topSpecialtyList = new ArrayList<>();
        for (Object[] row : topSpecialties) {
            Long specialtyID = (Long) row[0];
            String specialtyName = (String) row[1];
            Long count = (Long) row[2];

            // Tìm bác sĩ top 1 trong chuyên khoa
            List<Object[]> topDoctor = appointmentRepository.findTopDoctorBySpecialtyAndDate(
                    date,
                    specialtyID,
                    PageRequest.of(0, 1)
            );
            String topDoctorName = "";
            if (!topDoctor.isEmpty()) {
                topDoctorName = (String) topDoctor.get(0)[0]; // doctorName
            }

            Map<String, Object> specialtyData = new HashMap<>();
            specialtyData.put("specialtyName", specialtyName);
            specialtyData.put("count", count);
            specialtyData.put("topDoctorName", topDoctorName);

            topSpecialtyList.add(specialtyData);
        }

        String topSpecialtiesJson = convertToJson(topSpecialtyList);
        stats.setTotalAppointment(totalAppointments);
        stats.setTotalPatient(totalPatients);
        stats.setTopSpecialtiesJson(topSpecialtiesJson);

        return appointmentStatsRepository.save(stats);
    }

    public void appointmentStats(Date startDate, Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        while (!cal.getTime().after(endDate)) {
            Date currentDate = cal.getTime();
            calculateAndSaveDailyStats(currentDate);
            cal.add(Calendar.DATE, 1);
        }
    }

    private String convertToJson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }
    // Quản lý Doctor
    public Page<Doctor> getAllDoctors(String fullName, DoctorDegree filterDegree, Long specialtyId, Boolean enabled, String sortField, String sortDir, Pageable pageable) {
        return adminDao.filterAndSortDoctors(fullName,filterDegree, specialtyId, enabled, sortField, sortDir, pageable);
    }

    public Doctor getDoctorById(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        return doctor.orElse(null);
    }

    public Doctor createDoctor(DoctorDTO doctorDTO, Doctor doctor, MultipartFile imageFile) {
        User user = doctor.getUser();
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = System.currentTimeMillis() + "_" + user.getUsername() + fileExtension;
        Path uploadPath = Paths.get("src/main/resources/static/uploads/cv");

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage());
        }

        user.setImage("/uploads/cv/" + fileName);
        user.setPassword(passwordEncoder.encode("123"));
        user.setEnabled(true);
        user.setCreatedAt(new Date());

        Role doctorRole = roleRepository.findByRoleName("ROLE_DOCTOR")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role bác sĩ"));
        user.setRoleID(doctorRole);
        User savedUser = userRepository.save(user);
        doctor.setDob(doctorDTO.getDob());
        doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        doctor.setClinicAddress(doctorDTO.getClinicAddress());
        doctor.setAcademicDegree(DoctorDegree.valueOf(doctorDTO.getAcademicDegree()));

        Specialty specialty =  specialtyRepository.findById(doctor.getSpecialty().getSpecialtyID())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"));
        doctor.setSpecialty(specialty);
        doctor.setUser(savedUser);

        List<Doctor> doctorList = specialty.getDoctors();
        if (doctorList == null) {
            specialty.setNumberOfDoctors(1);
        } else {
            specialty.setNumberOfDoctors(doctorList.size());
        }
        specialtyRepository.save(specialty);

        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Doctor doctor, MultipartFile imageFile) {
        Doctor existingDoctor = doctorRepository.findById(doctor.getDoctorID())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));
        existingDoctor.setSpecialty(
                specialtyRepository.findById(doctor.getSpecialty().getSpecialtyID())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa"))
        );

        existingDoctor.setDob(doctor.getDob());
        existingDoctor.setLicenseNumber(doctor.getLicenseNumber());
        existingDoctor.setClinicAddress(doctor.getClinicAddress());
        existingDoctor.setAcademicDegree(doctor.getAcademicDegree());

        User user = existingDoctor.getUser();
        user.setFullName(doctor.getUser().getFullName());
        user.setEmail(doctor.getUser().getEmail());
        user.setGender(doctor.getUser().getGender());
        user.setIdentityNumber(doctor.getUser().getIdentityNumber());
        user.setPhoneNumber(doctor.getUser().getPhoneNumber());
        user.setEnabled(doctor.getUser().isEnabled());

        if (imageFile != null && !imageFile.isEmpty()) {
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = System.currentTimeMillis() + "_" + user.getUsername() + fileExtension;
            Path uploadPath = Paths.get("src/main/resources/static/uploads/cv");
            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage());
            }
            user.setImage("/uploads/cv/" + fileName);
        }

        userRepository.save(user);

        return doctorRepository.save(existingDoctor);
    }


    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }


    // Quản lý Patient
    public Page<Patient> getAllPatients(String fullName, Boolean enabled, String sortField, String sortDir, Pageable pageable) {
        return adminDao.filterAndSortPatients(fullName, sortField, enabled, sortDir, pageable);
    }

    public Patient getPatientById(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        return patient.orElse(null);
    }

    public Patient updatePatient(Patient patient, MultipartFile imageFile) {
        Patient existingPatient = patientRepository.findById(patient.getPatientID())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân"));

        User user = existingPatient.getUser();
        user.setEnabled(patient.getUser().isEnabled());

        if (imageFile != null && !imageFile.isEmpty()) {
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = System.currentTimeMillis() + "_" + user.getUsername() + fileExtension;
            Path uploadPath = Paths.get("src/main/resources/static/uploads/ava");
            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage());
            }
            user.setImage("/uploads/ava/" + fileName);
        }

        userRepository.save(user);

        return patientRepository.save(existingPatient);
    }

    public Page<Specialty> getAllSpecialty(String specialtyName, Pageable pageable) {
        return adminDao.searchAndPage(specialtyName, pageable);
    }

    public Specialty getSpecialtyById(Long id) {
        Optional<Specialty> specialty = specialtyRepository.findById(id);
        return specialty.orElse(null);
    }

    public Specialty createSpecialty(SpecialtyDTO specialtyDTO, Specialty specialty) {
        specialty.setSpecialtyName(specialtyDTO.getSpecialtyName());
        specialty.setSpecialtyDescription(specialtyDTO.getSpecialtyDescription());

        return specialtyRepository.save(specialty);
    }
    public Specialty updateSpecialty(Specialty specialty) {
        return specialtyRepository.save(specialty);
    }

    public void updateSpecialtyEnabledStatus(Long specialtyId, boolean enabled) {
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new RuntimeException("Specialty not found"));
        specialty.setSpecialtyStatus(enabled);
        specialtyRepository.save(specialty);
    }


    public List<Schedule> getSchedulesByDoctorId(Long doctorId) {
        return scheduleRepository.findSchedulesByDoctorDoctorID(doctorId);
    }

    public Page<MedicalRecord> findMedicalRecords(Pageable pageable, String fullName) {
        return adminDao.findMedicalRecords(fullName, pageable);
    }

    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalRecordRepository.findById(id).orElse(null);
    }

    public void updateMedicalRecord(MedicalRecord medicalRecord) {
        medicalRecordRepository.save(medicalRecord);
    }

    public List<Appointment> findAppointmentByPatientDoctorAndDate (Long patientID, Long doctorID, Date appointmentDate) {
        return appointmentRepository.findAppointmentByPatientDoctorAndDate(patientID, doctorID, appointmentDate);
    }

    public List<MedicalRecord> searchMedicalRecordsByPatient(Patient patient) {
        return medicalRecordRepository.findAllByPatientOrderByExaminationDateDesc(patient);
    }

}
