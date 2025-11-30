package fit.se2.medicarehub.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.se2.medicarehub.model.*;
import fit.se2.medicarehub.repository.AppointmentRepository;
import fit.se2.medicarehub.repository.MedicalRecordRepository;
import fit.se2.medicarehub.repository.PatientRepository;
import fit.se2.medicarehub.repository.ScheduleRepository;
import fit.se2.medicarehub.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    DoctorService doctorService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private MedicalRecordRepository medicalrecordRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/home")
    public String homepage(Model model) {
        Doctor doctor = doctorService.getCurrentDoctor();
        Date today = new Date();
        Schedule currentSchedule = doctor.getSchedules().stream()
                .filter(schedule -> !today.before(schedule.getStartTime()) && !today.after(schedule.getEndTime()))
                .findFirst()
                .orElse(null);
        model.addAttribute("currentSchedule", currentSchedule);
        model.addAttribute("doctor", doctor);

        return "doctor/home";
    }

    @GetMapping("/patient-list")
    public String services(Model model,
                           @RequestParam(name = "ongoingexamination", required = false, defaultValue = "false") boolean ongoingexamination,
                           @RequestParam(name = "patientName", required = false) String patientName,
                           Pageable pageable) {
        Doctor doctor = doctorService.getCurrentDoctor();
        Page<Appointment> appointmentPage = doctorService.filterAndPageAppointments(doctor.getDoctorID(), ongoingexamination, patientName, pageable);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointmentPage", appointmentPage);

        Date today = new Date();
        Schedule currentSchedule = doctor.getSchedules().stream()
                .filter(schedule -> !today.before(schedule.getStartTime()) && !today.after(schedule.getEndTime()))
                .findFirst()
                .orElse(null);
        model.addAttribute("currentSchedule", currentSchedule);
        return "doctor/patient-list";
    }

    @GetMapping("/services/create-record")
    public String ongoingexamination(@RequestParam("patientId") Long patientId,
                                     @RequestParam("appointmentId") Long appointmentId,
                                     @ModelAttribute("recordDTO") MedicalRecordDTO recordDTO,
                                     Model model) {
        Doctor doctor = doctorService.getCurrentDoctor();
        model.addAttribute("doctor", doctor);
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        if (!(appointment.get().getStatus() == AppointmentStatus.CONFIRMED
                || appointment.get().getStatus() == AppointmentStatus.ONGOING)) {
            return "doctor/patient-list";
        }
        if (appointment.isPresent()) {
            appointment.get().setStatus(AppointmentStatus.ONGOING);
            appointmentRepository.save(appointment.get());
        }
        Patient patient = patientRepository.findById(patientId).get();

        Optional<MedicalRecord> record = medicalrecordRepository.findTopByPatientOrderByExaminationDateDesc(patient);
        model.addAttribute("record", record.orElse(null));
        model.addAttribute("patient", patient);
        model.addAttribute("appointment", appointment.get());
        model.addAttribute("doctor", doctor);
        model.addAttribute("recordDTO", recordDTO);

        Date today = new Date();
        Schedule currentSchedule = doctor.getSchedules().stream()
                .filter(schedule -> !today.before(schedule.getStartTime()) && !today.after(schedule.getEndTime()))
                .findFirst()
                .orElse(null);
        model.addAttribute("currentSchedule", currentSchedule);
        return "doctor/create-record";
    }

    @GetMapping("/patient-history/{patientId}")
    public String patientHistory(@PathVariable("patientId") Long patientId, Model model) {
        Doctor doctor = doctorService.getCurrentDoctor();
        Patient patient = patientRepository.findById(patientId).get();
        Optional<MedicalRecord> record = medicalrecordRepository.findTopByPatientOrderByExaminationDateDesc(patient);
        model.addAttribute("patient", patient);
        model.addAttribute("record", record.orElse(null));
        model.addAttribute("doctor", doctor);

        Date today = new Date();
        Schedule currentSchedule = doctor.getSchedules().stream()
                .filter(schedule -> !today.before(schedule.getStartTime()) && !today.after(schedule.getEndTime()))
                .findFirst()
                .orElse(null);
        model.addAttribute("currentSchedule", currentSchedule);
        return "doctor/patient-history";
    }

    @PostMapping("/services/create-record")
    public String createRecord(@ModelAttribute("recordDTO") MedicalRecordDTO recordDTO,
                               @RequestParam("patientId") Long patientId,
                               @RequestParam("appointmentId") Long appointmentId) {
        MedicalRecord record = new MedicalRecord();
        Doctor doctor = doctorService.getCurrentDoctor();
        Patient patient = patientRepository.findById(patientId).get();
        record.setDoctor(doctor);
        record.setPatient(patient);
        record.setExaminationDate(recordDTO.getExaminationDate());
        record.setSymptoms(recordDTO.getSymptoms());
        record.setDiagnosis(recordDTO.getDiagnosis());
        record.setReExamination(recordDTO.isReExamination());
        if (recordDTO.getTests() != null) {
            recordDTO.getTests().forEach(testDTO -> {
                TestRecord test = new TestRecord();
                test.setServiceName(testDTO.getServiceName());
                test.setInstruction(testDTO.getInstruction());
                test.setNote(testDTO.getNote());
                test.setRecord(record);
                record.getTests().add(test);
            });
        }
        if (recordDTO.getPrescriptions() != null) {
            recordDTO.getPrescriptions().forEach(prescriptionDTO -> {
                Prescription prescription = new Prescription();
                prescription.setMedicineName(prescriptionDTO.getMedicineName());
                prescription.setInstruction(prescriptionDTO.getInstruction());
                prescription.setQuantity(prescriptionDTO.getQuantity());
                prescription.setPrice(prescriptionDTO.getPrice());
                prescription.setRecord(record);
                record.getPrescriptions().add(prescription);
            });
        }
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        if (appointment.isPresent()) {
            appointment.get().setStatus(AppointmentStatus.COMPLETED);
            record.setAppoinmentDate(appointment.get().getAppointmentDate());
            appointmentRepository.save(appointment.get());
        }
        medicalrecordRepository.save(record);
        return "redirect:/doctor/patient-list";
    }

    @GetMapping("schedule")
    public String schedule(Model model) throws JsonProcessingException {
        Doctor doctor = doctorService.getCurrentDoctor();
        List<Schedule> schedules = scheduleRepository.findSchedulesByDoctorDoctorID(doctor.getDoctorID());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ScheduleDTO> scheduleDTOs = schedules.stream().map(schedule -> {
            ScheduleDTO dto = new ScheduleDTO();
            dto.setScheduleID(schedule.getScheduleID());
            dto.setDoctorID(schedule.getDoctor().getDoctorID());
            dto.setSeatCount(schedule.getSeatCount());
            dto.setService(schedule.getService());
            dto.setRoom(schedule.getRoom());
            dto.setStartDate(schedule.getStartTime() != null ? sdf.format(schedule.getStartTime()) : null);
            dto.setEndDate(schedule.getEndTime() != null ? sdf.format(schedule.getEndTime()) : null);
            return dto;
        }).toList();

        ObjectMapper mapper = new ObjectMapper();
        String schedulesJson = mapper.writeValueAsString(scheduleDTOs);
        model.addAttribute("schedulesJson", schedulesJson);
        System.out.println(schedulesJson);
        model.addAttribute("doctor", doctor);
        model.addAttribute("schedules", schedules);

        Date today = new Date();
        Schedule currentSchedule = doctor.getSchedules().stream()
                .filter(schedule -> !today.before(schedule.getStartTime()) && !today.after(schedule.getEndTime()))
                .findFirst()
                .orElse(null);
        model.addAttribute("currentSchedule", currentSchedule);
        return "doctor/schedule";
    }
}
