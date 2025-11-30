package fit.se2.medicarehub.controller;

import fit.se2.medicarehub.model.*;
import fit.se2.medicarehub.repository.AppointmentRepository;
import fit.se2.medicarehub.repository.ScheduleRepository;
import fit.se2.medicarehub.repository.SpecialtyRepository;
import fit.se2.medicarehub.service.AdminService;
import fit.se2.medicarehub.service.EmailService;
import fit.se2.medicarehub.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/patient")
public class PatientController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String route() {
        return "redirect:patient/home";
    }

    @GetMapping("/home")
    public String homepage(Model model) {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            patient = new Patient();
            patient.setUser(new User());

        }
        model.addAttribute("patient", patient);
        return "patient/home";
    }

    @GetMapping("/report")
    public String report(Model model) {
        Patient patient = patientService.getCurrentPatient();
        model.addAttribute("patient", patient);
        return "patient/report";
    }

    @GetMapping("/create-report")
    public String createReport(Model model) {
        if (patientService.getCurrentPatient() != null && !patientService.getCurrentPatient().isDeleted()) {
            return "redirect:/patient/report";
        }
        model.addAttribute("patient", new Patient());
        return "patient/create-report";
    }

    @GetMapping("/have-report")
    public String haveReport(@RequestParam(value = "code", required = false) String code, Model model) {
        if (code != null && !code.isEmpty()) {
            Optional<Patient> optionalPatient = patientService.findByPatientCode(code);
            if (optionalPatient.isPresent()) {
                Patient patient = optionalPatient.get();
                patientService.updatePatient(patient);
                model.addAttribute("patient", patient);
            } else {
                model.addAttribute("notFound", true);
            }
        }
        return "patient/have-report";
    }

    @GetMapping("/update-report")
    public String updateReport(Model model) {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            return "redirect:/patient/create-report";
        }
        model.addAttribute("patient", patient);
        return "patient/update-report";
    }

    @PostMapping("/save-report")
    public String saveReport(@ModelAttribute("patient") Patient patientForm,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", true);
            return "patient/update-report";
        }

        Patient existing = patientService.getCurrentPatient();
        if (existing != null) {
            // Cập nhật thông tin Patient
            existing.setDob(patientForm.getDob());
            existing.setAddress(patientForm.getAddress());
            existing.setEthnicity(patientForm.getEthnicity());
            existing.setDeleted(false);
            // Cập nhật các trường trong User
            if(existing.getUser() == null) {
                existing.setUser(patientForm.getUser());
            } else {
                existing.getUser().setPhoneNumber(patientForm.getUser().getPhoneNumber());
                existing.getUser().setGender(patientForm.getUser().getGender());
                existing.getUser().setEmail(patientForm.getUser().getEmail());
                existing.getUser().setFullName(patientForm.getUser().getFullName());
                existing.getUser().setIdentityNumber(patientForm.getUser().getIdentityNumber());
            }
            patientService.updatePatient(existing);
        } else {
            patientService.createPatient(patientForm);
        }
        return "redirect:/patient/report";
    }

    @GetMapping("/delete-report")
    public String deleteRecord(@RequestParam(value = "patientId", required = false) Long patientId) {
        if (patientId != null) {
            patientService.hideCurrentPatientById(patientId);
        }
        return "redirect:/patient/report";
    }

    @GetMapping("/appointment-list")
    public String listAppointments(Model model) {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            model.addAttribute("error", "Vui lòng tạo hồ sơ");
            return "redirect:/patient/report";
        }
        List<Appointment> appointments = appointmentRepository.findAppointmentsByPatient(patient.getPatientID());

        Map<Long, AppointmentDTO> appointmentMap = new LinkedHashMap<>();
        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.CONFIRMED || appointment.getStatus() == AppointmentStatus.PENDING) {
                AppointmentDTO appointmentDTO = new AppointmentDTO();
                appointmentDTO.setSpecialtyName(appointment.getDoctor().getSpecialty().getSpecialtyName());
                appointmentDTO.setDoctorAcademicDegree(appointment.getDoctor().getAcademicDegree().name());
                appointmentDTO.setDoctorName(appointment.getDoctor().getUser().getFullName());
                appointmentDTO.setAppointmentDate(appointment.getAppointmentDate());
                appointmentDTO.setQueueNumber(appointment.getQueueNumber());
                appointmentDTO.setStatusFromEnum(appointment.getStatus());
                appointmentMap.put(appointment.getAppointmentID(), appointmentDTO);
            }
        }

        model.addAttribute("appointmentMap", appointmentMap);
        return "patient/appointment-list";
    }


    @GetMapping("/appointment-list/detail")
    public String appointmentDetail(@RequestParam(value = "appointmentId", required = false) Long appointmentId, Model model) {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            model.addAttribute("error", "Vui lòng tạo hồ sơ");
            return "redirect:/patient/report";
        }

        if (appointmentId == null) {
            model.addAttribute("error", "Thông tin lịch hẹn không hợp lệ");
            return "redirect:/patient/appointment-list";
        }

        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy lịch hẹn");
            return "redirect:/patient/appointment-list";
        }

        Appointment appointment = optionalAppointment.get();
        // Kiểm tra xem lịch hẹn có thuộc về bệnh nhân hiện tại không
        if (!appointment.getPatient().getPatientID().equals(patient.getPatientID())) {
            model.addAttribute("error", "Bạn không có quyền xem lịch hẹn này");
            return "redirect:/patient/appointment-list";
        }

        model.addAttribute("appointment", appointment);
        return "patient/detail-appointment";
    }

    @PostMapping("/appointment/notification")
    public String appointmentNotification(@RequestParam("appointmentId") Long appointmentId,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reminderTime,
                                          @RequestParam String message,
                                          Model model) {

        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            model.addAttribute("error", "Vui lòng tạo hồ sơ");
            return "redirect:/patient/report";
        }

        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy lịch hẹn");
            return "redirect:/patient/appointment-list";
        }

        Appointment appointment = optionalAppointment.get();
        // Kiểm tra xem lịch hẹn có thuộc về bệnh nhân hiện tại không
        if (!appointment.getPatient().getPatientID().equals(patient.getPatientID())) {
            model.addAttribute("error", "Bạn không có quyền thực hiện thao tác này");
            return "redirect:/patient/appointment-list";
        }

        AppointmentReminder reminder;
        if (appointment.isReminderStatus()) {
            // Đã có nhắc hẹn => cập nhật reminder hiện có
            reminder = patientService.findAppointmentReminderByAppointmentId(appointmentId)
                    .orElse(new AppointmentReminder()); // fallback nếu dữ liệu lỗi
        } else {
            reminder = new AppointmentReminder();
            reminder.setAppointment(appointment);
            reminder.setPatient(patient);
            reminder.setReminderStatus(true);
            appointment.setReminderStatus(true);
            appointment.setReminderStatus(true);
            appointmentRepository.save(appointment);
        }
        reminder.setMessage(message);
        reminder.setReminderTime(Timestamp.valueOf(reminderTime));

        patientService.updateAppointmentReminder(reminder);

        model.addAttribute("notification", "Lịch nhắc cuộc hẹn đã được lưu.");
        return "redirect:/patient/appointment-list/detail?appointmentId=" + appointmentId;
    }

    @Scheduled(fixedRate = 6000)
    public void processAppointmentReminders() {
        Date now = new Date();
        List<AppointmentReminder> dueReminders = patientService.dueAppointmentReminders(now);

        for (AppointmentReminder reminder : dueReminders) {
            String email = reminder.getPatient().getUser().getEmail();
            String subject = "Nhắc nhở cuộc hẹn: " + reminder.getAppointment().getAppointmentDate();
            String message = "Bạn có một cuộc hẹn vào " + reminder.getAppointment().getAppointmentDate() +
                    ". Nội dung nhắc nhở: " + reminder.getMessage();

            emailService.sendEmail(email, subject, message);
            reminder.setReminderStatus(false);
            patientService.updateAppointmentReminder(reminder);

            Appointment appointment = reminder.getAppointment();
            appointment.setReminderStatus(false);
            appointmentRepository.save(appointment);
        }
    }

    @GetMapping("/booking")
    public String booking(Model model) {
        Patient patient = patientService.getCurrentPatient();
        model.addAttribute("patient", patient);
        return "patient/booking";
    }

    @GetMapping("/create-appointment")
    public String showDoctorList(
            @RequestParam(value = "fullName", defaultValue = "") String fullName,
            @RequestParam(value = "academicDegree", required = false) DoctorDegree academicDegree,
            @RequestParam(value = "specialty", required = false) Long specialtyId,
            @RequestParam(value = "sortField", defaultValue = "fullName") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        final int pageSize = 5;

        Page<Doctor> doctors = adminService.getAllDoctors(
                fullName, academicDegree, specialtyId, enabled, sortField, sortDir, PageRequest.of(page, pageSize));

        Map<Long, List<Schedule>> doctorSchedules = new HashMap<>();
        Map<Long, String> minDates = new HashMap<>();
        Map<Long, String> maxDates = new HashMap<>();

        for (Doctor doctor : doctors.getContent()) {
            List<Schedule> schedules = adminService.getSchedulesByDoctorId(doctor.getDoctorID());
            doctorSchedules.put(doctor.getDoctorID(), schedules);

            if (!schedules.isEmpty()) {
                // Sắp xếp danh sách lịch để tìm minDate và maxDate
                schedules.sort(Comparator.comparing(Schedule::getStartTime));
                minDates.put(doctor.getDoctorID(), new SimpleDateFormat("yyyy-MM-dd").format(schedules.get(0).getStartTime()));
                maxDates.put(doctor.getDoctorID(), new SimpleDateFormat("yyyy-MM-dd").format(schedules.get(schedules.size() - 1).getEndTime()));
            }
        }

        model.addAttribute("degreeList", DoctorDegree.values());
        model.addAttribute("doctor", new Doctor());
        model.addAttribute("academicDegree", academicDegree);
        model.addAttribute("fullName", fullName);
        model.addAttribute("specialtyId", specialtyId);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("page", page);
        model.addAttribute("pages", doctors.getTotalPages());
        model.addAttribute("doctors", doctors.getContent());
        model.addAttribute("specialty", specialtyRepository.findAll());
        model.addAttribute("doctorSchedules", doctorSchedules);
        model.addAttribute("minDates", minDates);
        model.addAttribute("maxDates", maxDates);

        return "patient/create-appointment";
    }


    @PostMapping("/create-appointment")
    public String createAppointment(@RequestParam("doctorID") Long doctorId,
                                    @RequestParam("appointmentDate")
                                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date appointmentDate,
                                    Model model) {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            model.addAttribute("error", "Vui lòng tạo hồ sơ");
            //Hiện thêm popup thông báo nếu có
            return "redirect:/patient/report";
        }

        List<Appointment> existingPatientAppointments = appointmentRepository
                .findAppointmentByPatientDoctorAndDate(patient.getPatientID(), doctorId, appointmentDate);
        if (!existingPatientAppointments.isEmpty()) {
            model.addAttribute("error", "Bạn đã có lịch hẹn với bác sĩ này vào thời gian đã chọn.");
            return "patient/booking";
        }

        // Tạo appointment ở trạng thái PENDING
        Appointment appointment = new Appointment();
        Doctor doctor = adminService.getDoctorById(doctorId);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStatus(AppointmentStatus.PENDING); // Tạm thời là PENDING
        appointment.setCreatedAt(new Date());

        appointmentRepository.save(appointment);

        // Chuyển sang trang xem thông tin trước khi xác nhận
        return "redirect:/patient/preview-appointment?appointmentId=" + appointment.getAppointmentID();
    }

    @GetMapping("/preview-appointment")
    public String previewAppointment(@RequestParam(value = "appointmentId", required = false) Long appointmentId,
                                     Model model) {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            return "redirect:/patient/create-report";
        }
        if (appointmentId != null) {
            Optional<Appointment> optional = appointmentRepository.findById(appointmentId);
            if (optional.isPresent()) {
                Appointment appointment = optional.get();
                if (!appointment.getPatient().getPatientID().equals(patient.getPatientID()) ||
                        appointment.getStatus() != AppointmentStatus.PENDING) {
                    model.addAttribute("error", "Lịch hẹn này không thể xác nhận.");
                    return "redirect:/patient/appointment-list";
                }
                AppointmentDTO dto = new AppointmentDTO();
                dto.setDoctorAcademicDegree(appointment.getDoctor().getAcademicDegree().name());
                dto.setDoctorName(appointment.getDoctor().getUser().getFullName());
                dto.setSpecialtyName(appointment.getDoctor().getSpecialty().getSpecialtyName());
                dto.setAppointmentDate(appointment.getAppointmentDate());
                dto.setStatusFromEnum(appointment.getStatus());
                dto.setStatusFromEnum(appointment.getStatus());

                model.addAttribute("appointment", dto);
                model.addAttribute("appointmentId", appointmentId);
            }
        }
        model.addAttribute("patient", patient);
        return "patient/preview-appointment";
    }

    @PostMapping("/preview-appointment")
    public String deletePreviewAppointment(@RequestParam("appointmentId") Long appointmentId, Model
                                           model) {appointmentRepository.deleteById(appointmentId);

        Patient patient = patientService.getCurrentPatient();
        model.addAttribute("patient", patient);

        return "patient/preview-appointment";
    }

    @PostMapping("/confirm-appointment")
    public String confirmAppointment(@RequestParam("appointmentId") Long appointmentId,
                                     Model model) {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            model.addAttribute("error", "Vui lòng tạo hồ sơ");
            return "redirect:/patient/report";
        }

        Optional<Appointment> optional = appointmentRepository.findById(appointmentId);
        if (optional.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy lịch hẹn.");
            return "patient/booking";
        }

        Appointment appointment = optional.get();

        // Kiểm tra xem appointment này có còn PENDING không
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            model.addAttribute("error", "Lịch hẹn này đã được xác nhận hoặc không còn ở trạng thái chờ.");
            return "patient/booking";
        }

        // Kiểm tra xem bác sĩ đã đủ lượt trong cùng thời điểm chưa
        List<Appointment> doctorAppointments = appointmentRepository.findAppointmentsByDoctorAndDate(
                appointment.getDoctor().getDoctorID(),
                appointment.getAppointmentDate()
        );
        List<Schedule> schedules = scheduleRepository.findSchedulesByDoctorDoctorID(appointment.getDoctor().getDoctorID());
        Schedule validSchedule = schedules.stream()
                .filter(schedule -> !appointment.getAppointmentDate().before(schedule.getStartTime())
                        && !appointment.getAppointmentDate().after(schedule.getEndTime()))
                .findFirst().orElse(null);

        if (validSchedule != null) {
            int seatCount = validSchedule.getSeatCount();
            if (doctorAppointments.size() >= seatCount) {
                model.addAttribute("error", "Lịch hẹn của bác sĩ đã đầy trong khoảng thời gian này.");
                return "patient/booking";
            }
        } else {
            model.addAttribute("error", "Không tìm thấy lịch làm việc phù hợp cho bác sĩ.");
            return "patient/booking";
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setCreatedAt(new Date());
        appointment.setQueueNumber(doctorAppointments.size());
        appointmentRepository.save(appointment);

        return "redirect:/patient/appointment-list";
    }

    @GetMapping("/record")
    public String listRecord(Model model) {
        Patient patient = patientService.getCurrentPatient();
        if (patient == null) {
            model.addAttribute("error", "Vui lòng tạo hồ sơ");
            return "redirect:/patient/report";
        }
        List<Appointment> appointments = appointmentRepository.findAppointmentsByPatient(patient.getPatientID());

        Map<Long, AppointmentDTO> appointmentMap = new LinkedHashMap<>();
        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELED) {
                AppointmentDTO appointmentDTO = new AppointmentDTO();
                appointmentDTO.setSpecialtyName(appointment.getDoctor().getSpecialty().getSpecialtyName());
                appointmentDTO.setDoctorAcademicDegree(appointment.getDoctor().getAcademicDegree().name());
                appointmentDTO.setDoctorName(appointment.getDoctor().getUser().getFullName());
                appointmentDTO.setAppointmentDate(appointment.getAppointmentDate());
                appointmentDTO.setQueueNumber(appointment.getQueueNumber());
                appointmentDTO.setStatusFromEnum(appointment.getStatus());
                appointmentMap.put(appointment.getAppointmentID(), appointmentDTO);
            }
        }

        model.addAttribute("appointmentMap", appointmentMap);
        return "patient/history";
    }


    @GetMapping("/reminder")
    public String reminder(Model model) {
        Patient patient = patientService.getCurrentPatient();
        List<MedicalRecord> records = adminService.searchMedicalRecordsByPatient(patient);
        model.addAttribute("records", records);
        return "patient/reminder";
    }

    @GetMapping("/reminder/{id}")
    public String reminder(@PathVariable long id, Model model) {
        MedicalRecord medicalRecord = adminService.getMedicalRecordById(id);

        model.addAttribute("medicalRecord", medicalRecord);
        return "patient/reminder-detail";
    }

    @PostMapping("/reminder/notification")
    public String reminderNotification(@RequestParam Long id,
                                       @RequestParam Long prescriptionId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reminderTime,
                                       Model model) {

        MedicalRecord medicalRecord = adminService.getMedicalRecordById(id);
        Patient patient = patientService.getCurrentPatient();

        Prescription targetPrescription = medicalRecord.getPrescriptions()
                .stream()
                .filter(p -> p.getPrescriptionID().equals(prescriptionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        MedicationReminder reminder;

        if (targetPrescription.isReminder()) {
            reminder = patientService.findMedicationReminderByPrescriptionId(prescriptionId)
                    .orElse(new MedicationReminder());
        } else {
            reminder = new MedicationReminder();
            reminder.setPatient(patient);
            reminder.setPrescriptionId(targetPrescription.getPrescriptionID());
            reminder.setMedicationName(targetPrescription.getMedicineName());
            reminder.setDosage(targetPrescription.getInstruction());
            reminder.setReminderStatus(true);

            targetPrescription.setReminder(true);
            patientService.updatePrescription(targetPrescription);
        }

        reminder.setReminderTime(Timestamp.valueOf(reminderTime));
        patientService.updateMedicationReminder(reminder);

        model.addAttribute("notification", "Lịch nhắc uống thuốc đã được lưu.");
        return "redirect:/patient/reminder/" + id;
    }


    @Scheduled(fixedRate = 60000)
    public void processMedicationReminders() {
        Date now = new Date();
        List<MedicationReminder> dueReminders = patientService.dueReminders(now);

        for (MedicationReminder reminder : dueReminders) {
            if (reminder.isReminderStatus()) {
                String email = reminder.getPatient().getUser().getEmail();
                String subject = "Nhắc nhở uống thuốc: " + reminder.getMedicationName();
                String message = "Bạn cần sử dụng thuốc " + reminder.getMedicationName()
                        + ". Hướng dẫn: " + reminder.getDosage();

                emailService.sendEmail(email, subject, message);
                reminder.setReminderStatus(false);
                patientService.updateMedicationReminder(reminder);

                Optional<Prescription> p = patientService.findPrescriptionById(reminder.getPrescriptionId());
                if (p.isPresent()) {
                    p.get().setReminder(false);
                    patientService.updatePrescription(p.get());
                }
            }
        }
    }

}
