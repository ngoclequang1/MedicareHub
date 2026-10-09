package fit.se2.medicarehub.service.agent;

import fit.se2.medicarehub.model.Appointment;
import fit.se2.medicarehub.model.Doctor;
import fit.se2.medicarehub.model.MedicalRecord;
import fit.se2.medicarehub.model.Patient;
import fit.se2.medicarehub.repository.AppointmentRepository;
import fit.se2.medicarehub.repository.DoctorRepository;
import fit.se2.medicarehub.repository.MedicalRecordRepository;
import fit.se2.medicarehub.service.PatientService;
import fit.se2.medicarehub.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentToolService {

    private final PatientService patientService;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorService doctorService;

    public AgentToolService(PatientService patientService,
                            AppointmentRepository appointmentRepository,
                            DoctorRepository doctorRepository,
                            MedicalRecordRepository medicalRecordRepository,
                            DoctorService doctorService) {
        this.patientService = patientService;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.doctorService = doctorService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listMyAppointments() {
        Patient patient = requirePatient();
        return appointmentRepository.findAppointmentsByPatient(patient.getPatientID()).stream()
                .map(this::appointmentView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchDoctors(String normalizedMessage) {
        return doctorRepository.findAll().stream()
                .filter(doctor -> doctor.getSpecialty() != null && doctor.getSpecialty().isSpecialtyStatus())
                .filter(doctor -> matchesSpecialty(doctor, normalizedMessage))
                .limit(10)
                .map(this::doctorView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> summarizeMyMedicalHistory() {
        Patient patient = requirePatient();
        List<Map<String, Object>> result = new ArrayList<>();
        for (MedicalRecord record : medicalRecordRepository.findAllByPatientOrderByExaminationDateDesc(patient)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordId", record.getRecordID());
            item.put("examinationDate", record.getExaminationDate());
            item.put("diagnosis", record.getDiagnosis());
            item.put("symptoms", record.getSymptoms());
            item.put("doctor", record.getDoctor().getUser().getFullName());
            item.put("medications", record.getPrescriptions().stream()
                    .map(p -> Map.of("name", p.getMedicineName(), "instruction", p.getInstruction()))
                    .toList());
            result.add(item);
            if (result.size() == 5) {
                break;
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDoctorAppointments() {
        Doctor doctor = doctorService.getCurrentDoctor();
        if (doctor == null) {
            throw new IllegalStateException("Không tìm thấy hồ sơ bác sĩ.");
        }
        return appointmentRepository.findByDoctorDoctorIDOrderByAppointmentDateAsc(doctor.getDoctorID()).stream()
                .map(appointment -> {
                    Map<String, Object> item = appointmentView(appointment);
                    item.put("patient", appointment.getPatient().getUser().getFullName());
                    item.remove("doctor");
                    item.remove("specialty");
                    return item;
                })
                .toList();
    }

    private Patient requirePatient() {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            throw new IllegalStateException("Bạn cần tạo hồ sơ bệnh nhân trước khi sử dụng chức năng này.");
        }
        return patient;
    }

    private boolean matchesSpecialty(Doctor doctor, String message) {
        String specialty = doctor.getSpecialty().getSpecialtyName().toLowerCase();
        return message.contains("bac si") || message.contains("chuyen khoa")
                || message.contains(specialty)
                || (message.contains("da") && specialty.contains("da lieu"))
                || (message.contains("tim") && specialty.contains("tim mach"))
                || (message.contains("tre em") && specialty.contains("nhi"))
                || (message.contains("rang") && specialty.contains("rang"));
    }

    private Map<String, Object> appointmentView(Appointment appointment) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("appointmentId", appointment.getAppointmentID());
        item.put("doctor", appointment.getDoctor().getUser().getFullName());
        item.put("specialty", appointment.getDoctor().getSpecialty().getSpecialtyName());
        item.put("date", appointment.getAppointmentDate());
        item.put("status", appointment.getStatus().name());
        item.put("queueNumber", appointment.getQueueNumber());
        return item;
    }

    private Map<String, Object> doctorView(Doctor doctor) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("doctorId", doctor.getDoctorID());
        item.put("name", doctor.getUser().getFullName());
        item.put("degree", doctor.getAcademicDegree() == null ? "" : doctor.getAcademicDegree().name());
        item.put("specialty", doctor.getSpecialty().getSpecialtyName());
        item.put("clinicAddress", doctor.getClinicAddress());
        return item;
    }
}
