package fit.se2.medicarehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.se2.medicarehub.model.*;
import fit.se2.medicarehub.repository.*;
import fit.se2.medicarehub.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.text.Normalizer;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdminService adminService;
    @Autowired
    private SpecialtyRepository specialtyRepository;
    @Autowired
    private AppointmentStatsRepository appointmentStatsRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/admin-detail")
    public String adminDetail(Model model) {
        Admin admin = adminService.getCurrentUser();
        model.addAttribute("admin", admin);
        return "admin/admin-detail";
    }

    @PostMapping("/admin-detail")
    public String adminDetail(@ModelAttribute Admin admin) {
        Admin adminCurrent = adminService.getCurrentUser();

        adminCurrent.getUser().setFullName(admin.getUser().getFullName());
        adminCurrent.getUser().setPhoneNumber(admin.getUser().getPhoneNumber());
        adminCurrent.getUser().setIdentityNumber(admin.getUser().getIdentityNumber());
        adminCurrent.setAddress(admin.getAddress());
        adminService.updateCurrentUser(adminCurrent);

        return "redirect:/admin/admin-detail";
    }

    //Stats
    @GetMapping("/dashboard")
    public String adminStatistics(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            Model model) {

        Date today = new Date();
        if (startDate == null) {
            startDate = today;
        }
        if (endDate == null) {
            endDate = today;
        }

        adminService.appointmentStats(startDate, endDate);
        List<AppointmentStats> stats = appointmentStatsRepository.findByDateRange(startDate, endDate);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("stats", stats);

        ObjectMapper mapper = new ObjectMapper();
        String statsJson = "[]";
        try {
            statsJson = mapper.writeValueAsString(stats);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(statsJson);
        model.addAttribute("statsJson", statsJson);

        return "admin/dashboard";
    }

    @PostMapping("/dashboard")
    public String adminStatistics(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        adminService.appointmentStats(startDate, endDate);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = (startDate != null) ? sdf.format(startDate) : "";
        String eDate = (endDate != null) ? sdf.format(endDate) : "";

        return "redirect:/admin/dashboard?startDate=" + sDate + "&endDate=" + eDate;
    }

    //Doctor
    @GetMapping("/doctors")
    public String listDoctors(
            @RequestParam(value = "fullName", defaultValue = "") String fullName,
            @RequestParam(value = "filterDegree", required = false) DoctorDegree filterDegree,
            @RequestParam(value = "specialtyId", required = false) Long specialtyId,
            @RequestParam(value = "sortField", defaultValue = "fullName") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        final int pageSize = 5;
        Page<Doctor> doctors = adminService.getAllDoctors(
                fullName, filterDegree, specialtyId, enabled, sortField, sortDir, PageRequest.of(page, pageSize));
        model.addAttribute("fullName", fullName);
        model.addAttribute("filterDegree", filterDegree);
        model.addAttribute("specialtyId", specialtyId);
        model.addAttribute("enabled", enabled);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("page", page);
        model.addAttribute("pages", doctors.getTotalPages());
        model.addAttribute("doctors", doctors.getContent());


        model.addAttribute("doctor", new Doctor());
        if (!model.containsAttribute("doctorDTO")) {
            model.addAttribute("doctorDTO", new DoctorDTO());
        }
        model.addAttribute("specialty", specialtyRepository.findAll());
        model.addAttribute("degreeList", DoctorDegree.values());

        return "admin/doctor-list";
    }

    @GetMapping("/doctors/detail/{id}")
    public String getDoctorById(@PathVariable("id") Long id, Model model) {
        Doctor doctor = adminService.getDoctorById(id);
        if (doctor != null) {
            model.addAttribute("doctor", doctor);
            model.addAttribute("specialty", specialtyRepository.findAll());
            model.addAttribute("degreeList", DoctorDegree.values());
            return "admin/doctor-detail";
        }
        return "admin/doctor-list";
    }

    @PostMapping("/doctors/save")
    public String saveDoctor(@Valid @ModelAttribute("doctorDTO") DoctorDTO doctorDTO,
                             BindingResult bindingResult,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("doctorDTO", doctorDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.doctorDTO", bindingResult);
            return "redirect:/admin/doctors?add=false";
        }

        Doctor doctor = doctorDTO.getDoctor();
        if (doctor.getDoctorID() == null) {
            User user = doctor.getUser();
            if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                // Chuẩn hóa và loại bỏ dấu
                String normalizedFullName = Normalizer.normalize(user.getFullName(), Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                // Loại bỏ khoảng trắng và ký tự không phải là chữ
                String baseEmail = normalizedFullName.trim().replaceAll("\\s+", "").toLowerCase() + "@example.com";

                String uniqueEmail = baseEmail;
                int counter = 1;
                if (userRepository.findByUsername(baseEmail).isPresent()) {
                    uniqueEmail = baseEmail.replace("@example.com", counter + "@example.com");
                    counter++;
                }
                user.setEmail(uniqueEmail);
                user.setUsername(uniqueEmail);
            }
            adminService.createDoctor(doctorDTO, doctor, imageFile);
        }
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/update")
    public String updateDoctor(@Valid @ModelAttribute("doctorDTO") DoctorDTO doctorDTO,
                               BindingResult bindingResult,
                               @ModelAttribute("doctor") Doctor doctor,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getFieldErrors());
            return "admin/doctor-detail";
        }

        doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        doctor.setClinicAddress(doctorDTO.getClinicAddress());
        doctor.setDob(doctorDTO.getDob());
        doctor.setAcademicDegree(DoctorDegree.valueOf(doctorDTO.getAcademicDegree()));

        adminService.updateDoctor(doctor, imageFile);

        return "redirect:/admin/doctors";
    }




    @GetMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable("id") Long id) {
        adminService.deleteDoctor(id);
        return "redirect:/admin/doctors";
    }


    //Patient
    @GetMapping("/patients")
    public String listPatients(
            @RequestParam(value = "fullName", defaultValue = "") String fullName,
            @RequestParam(value = "sortField", defaultValue = "fullName") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        final int pageSize = 5;
        Page<Patient> patients = adminService.getAllPatients(
                fullName, enabled, sortField, sortDir, PageRequest.of(page, pageSize));
        model.addAttribute("fullName", fullName);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("enabled", enabled);
        model.addAttribute("page", page);
        model.addAttribute("pages", patients.getTotalPages());
        model.addAttribute("patients", patients.getContent());
        return "admin/patient-list";
    }

    @GetMapping("/patients/detail/{id}")
    public String getPatientById(@PathVariable("id") Long id, Model model) {
        Patient patient = adminService.getPatientById(id);
        if (patient != null) {
            model.addAttribute("patient", patient);
            return "admin/patient-detail";
        }
        return "admin/patient-list";
    }

    @PostMapping("/patients/update")
    public String updatePatient(@ModelAttribute("patient") Patient patient,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        adminService.updatePatient(patient, imageFile);

        return "redirect:/admin/patients";
    }



    @GetMapping("/specialty")
    public String listSpecialty(@RequestParam(value = "specialtyName", defaultValue = "") String specialtyName,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                Model model) {
        final int pageSize = 5;

        Page<Specialty> specialtiesPage = adminService.getAllSpecialty(specialtyName, PageRequest.of(page, pageSize));
        List<Specialty> specialties = specialtiesPage.getContent();

        Map<Long, String> headDoctorMap = specialties.stream()
                .collect(Collectors.toMap(Specialty::getSpecialtyID,
                        s->specialtyRepository.findHeadDoctorBySpecialtyId(s.getSpecialtyID())
                                .orElse("Chưa có trưởng khoa")));

        model.addAttribute("page", page);
        model.addAttribute("specialtyName", specialtyName);
        model.addAttribute("pages", specialtiesPage.getTotalPages());
        model.addAttribute("specialties", specialties);
        model.addAttribute("headDoctorMap", headDoctorMap);

        if (!model.containsAttribute("specialtyDTO")) {
            model.addAttribute("specialtyDTO", new SpecialtyDTO());
        }
        model.addAttribute("specialty", new Specialty());


        return "admin/specialty-list";
    }

    @PostMapping("/specialty/save")
    public String saveSpecialty(@Valid @ModelAttribute("specialtyDTO") SpecialtyDTO specialtyDTO,
                                BindingResult bindingResult,
                                @ModelAttribute("specialty") Specialty specialty,
                                RedirectAttributes redirectAttributes) {
        if (specialtyRepository.findBySpecialtyName(specialtyDTO.getSpecialtyName()).isPresent()) {
            bindingResult.rejectValue("specialtyName", "error.specialtyDTO", "Khoa đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            // Chuyển flash attributes để giữ lại đối tượng và lỗi binding
            redirectAttributes.addFlashAttribute("specialtyDTO", specialtyDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.specialtyDTO", bindingResult);

            return "redirect:/admin/specialty?add=false";
        }

        adminService.createSpecialty(specialtyDTO, specialty);

        return "redirect:/admin/specialty";
    }

    @GetMapping("/specialty/detail/{id}")
    public String getSpecialtyById(@PathVariable("id") Long id,
                                   Model model) {
        Specialty specialty = adminService.getSpecialtyById(id);
        if (specialty != null) {
            model.addAttribute("specialty", specialty);

            Optional<Long> headDoctorIdOpt = specialtyRepository.findHeadDoctorIdBySpecialtyId(id);
            Long headDoctorId = headDoctorIdOpt.orElse(null);
            model.addAttribute("doctorHeadId", headDoctorId);

            // Lấy tên trưởng khoa
            Optional<String> headDoctorNameOpt = specialtyRepository.findHeadDoctorBySpecialtyId(id);
            String headDoctorName = headDoctorNameOpt.orElse("Chưa có trưởng khoa");
            model.addAttribute("headDoctor", headDoctorName);

            if (!model.containsAttribute("specialtyDTO")) {
                SpecialtyDTO specialtyDTO = new SpecialtyDTO();
                specialtyDTO.setSpecialtyName(specialty.getSpecialtyName());
                specialtyDTO.setSpecialtyDescription(specialty.getSpecialtyDescription());
                specialtyDTO.setDoctorHeadId(headDoctorId);
                model.addAttribute("specialtyDTO", specialtyDTO);
            }

            if (!model.containsAttribute("doctorDTO")) {
                model.addAttribute("doctorDTO", new DoctorDTO());
            }

            model.addAttribute("degreeList", DoctorDegree.values());

            return "admin/specialty-detail";
        }
        return "admin/specialty-list";
    }

    @PostMapping("/specialty/update")
    public String updateSpecialty(@Valid @ModelAttribute("specialtyDTO") SpecialtyDTO specialtyDTO,
                                  BindingResult bindingResult,
                                  @RequestParam("id") Long specialtyID,
                                  RedirectAttributes redirectAttributes) {

        Optional<Specialty> existingSpecialty = specialtyRepository.findById(specialtyID);
        if (existingSpecialty.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khoa cần cập nhật");
            return "redirect:/admin/specialty";
        }

        // Kiểm tra nếu tên khoa đã tồn tại nhưng không phải bản ghi đang cập nhật
        Optional<Specialty> duplicateSpecialty = specialtyRepository.findBySpecialtyName(specialtyDTO.getSpecialtyName());
        if (duplicateSpecialty.isPresent() && !duplicateSpecialty.get().getSpecialtyID().equals(specialtyID)) {
            bindingResult.rejectValue("specialtyName", "error.specialtyDTO", "Khoa đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("specialtyDTO", specialtyDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.specialtyDTO", bindingResult);

            return "redirect:/admin/specialty/detail/" + specialtyID + "?edit=false";
        }

        Specialty specialty = existingSpecialty.get();
        specialty.setSpecialtyName(specialtyDTO.getSpecialtyName());
        specialty.setSpecialtyDescription(specialtyDTO.getSpecialtyDescription());

        if (specialtyDTO.getDoctorHeadId() != null) {
            doctorRepository.updateHeadDoctor(specialtyID, specialtyDTO.getDoctorHeadId());
        }
        specialty.setNumberOfDoctors(specialty.getDoctors() != null ? specialty.getDoctors().size() : 0);
        adminService.updateSpecialty(specialty);

        return "redirect:/admin/specialty/detail/" + specialtyID ;
    }


    @PostMapping("/specialty/update-status")
    public String updateSpecialtyStatus(@RequestParam("specialtyID") Long specialtyId,
                                        @RequestParam("status") boolean status) {
        adminService.updateSpecialtyEnabledStatus(specialtyId, status);
        return "redirect:/admin/specialty";
    }

    //Lịch trực
    @GetMapping("/schedules")
    public String listSchedules(
            @RequestParam(value = "filterDegree", required = false) DoctorDegree filterDegree,
            @RequestParam(value = "specialtyId", required = false) Long specialtyId,
            @RequestParam(value = "sortField", defaultValue = "fullName") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(value = "fullName", defaultValue = "") String fullName,
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        final int pageSize = 6;
        Page<Doctor> doctorPage = adminService.getAllDoctors(fullName, filterDegree, specialtyId, enabled ,sortField, sortDir, PageRequest.of(page, pageSize));

        Map<Long, List<Schedule>> doctorSchedules = new HashMap<>();
        Map<Long, String> minDates = new HashMap<>();
        Map<Long, String> maxDates = new HashMap<>();
        for (Doctor doctor : doctorPage.getContent()) {
            List<Schedule> schedules = adminService.getSchedulesByDoctorId(doctor.getDoctorID());
            doctorSchedules.put(doctor.getDoctorID(), schedules);

            if (!schedules.isEmpty()) {
                // Sắp xếp danh sách lịch để tìm minDate và maxDate
                schedules.sort(Comparator.comparing(Schedule::getStartTime));
                minDates.put(doctor.getDoctorID(), new SimpleDateFormat("yyyy-MM-dd").format(schedules.get(0).getStartTime()));
                maxDates.put(doctor.getDoctorID(), new SimpleDateFormat("yyyy-MM-dd").format(schedules.get(schedules.size() - 1).getEndTime()));
            }
        }

        model.addAttribute("fullName", fullName);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("page", page);
        model.addAttribute("pages", doctorPage.getTotalPages());
        model.addAttribute("doctors", doctorPage.getContent());
        model.addAttribute("doctorSchedules", doctorSchedules);
        model.addAttribute("minDates", minDates);
        model.addAttribute("maxDates", maxDates);

        model.addAttribute("specialty", specialtyRepository.findAll());
        if (!model.containsAttribute("scheduleDTO")) {
            model.addAttribute("scheduleDTO", new ScheduleDTO());
        }

        return "admin/schedule";
    }



    // Xem lịch làm việc của một bác sĩ cụ thể
    @PostMapping("/schedules/create")
    public String createSchedule(@Valid @ModelAttribute("scheduleDTO") ScheduleDTO scheduleDTO,
                                 BindingResult bindingResult,
                                 @RequestParam("startDate") String startDate,
                                 @RequestParam("endDate") String endDate,
                                 @RequestParam("doctorID") Long doctorID,
                                 @RequestParam(value = "morning", required = false) String morning,
                                 @RequestParam(value = "afternoon", required = false) String afternoon,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("scheduleDTO", scheduleDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.scheduleDTO", bindingResult);
            return "redirect:/admin/schedules?add=false";
        }

        Doctor doctor = adminService.getDoctorById(doctorID);
        if (doctor != null) {

            // Chuyển đổi scheduleDate và scheduleEndDate thành Date
            Date start = java.sql.Date.valueOf(startDate);
            Date end = java.sql.Date.valueOf(endDate);

            int startMorning = 8, endMorning = 12;
            int startAfternoon = 13, endAfternoon = 17;

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(start);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(end);

            if ((morning == null && afternoon == null) || (morning != null && afternoon != null)) {
                // Nếu không chọn hoặc chọn cả hai -> Ca cả ngày: từ 8h đến 17h
                startCal.set(Calendar.HOUR_OF_DAY, startMorning);
                startCal.set(Calendar.MINUTE, 0);
                endCal.set(Calendar.HOUR_OF_DAY, endAfternoon);
                endCal.set(Calendar.MINUTE, 0);
            } else if (morning != null) {
                // Chọn buổi sáng: từ 8h đến 12h
                startCal.set(Calendar.HOUR_OF_DAY, startMorning);
                startCal.set(Calendar.MINUTE, 0);
                endCal.set(Calendar.HOUR_OF_DAY, endMorning);
                endCal.set(Calendar.MINUTE, 0);
            } else if (afternoon != null) {
                // Chọn buổi chiều: từ 13h đến 17h
                startCal.set(Calendar.HOUR_OF_DAY, startAfternoon);
                startCal.set(Calendar.MINUTE, 0);
                endCal.set(Calendar.HOUR_OF_DAY, endAfternoon);
                endCal.set(Calendar.MINUTE, 0);
            }

            // Kiểm tra xem đã có lịch cùng ngày chưa
            List<Schedule> doctorSchedules = scheduleRepository.findSchedulesByDoctorDoctorID(doctorID);
            Schedule existingSchedule = null;
            for (Schedule s : doctorSchedules) {
                if (isSameDay(s.getStartTime(), start)) {
                    existingSchedule = s;
                    break;
                }
            }

            if (existingSchedule != null) {
                existingSchedule.setService(scheduleDTO.getService());
                existingSchedule.setRoom("Phòng "+scheduleDTO.getRoom());
                existingSchedule.setSeatCount(scheduleDTO.getSeatCount());
                existingSchedule.setStartTime(startCal.getTime());
                existingSchedule.setEndTime(endCal.getTime());
                scheduleRepository.save(existingSchedule);
                return "redirect:/admin/schedules?update=true";
            } else {
                // Tạo lịch mới
                Schedule schedule = new Schedule();
                schedule.setDoctor(doctor);
                schedule.setCreatedAt(new Date());
                schedule.setService(scheduleDTO.getService());
                schedule.setRoom("Phòng "+scheduleDTO.getRoom());
                schedule.setSeatCount(scheduleDTO.getSeatCount());
                schedule.setStartTime(startCal.getTime());
                schedule.setEndTime(endCal.getTime());
                scheduleRepository.save(schedule);
            }
        }
        return "redirect:/admin/schedules";
    }


    private boolean isSameDay(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(d1);
        cal2.setTime(d2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }


    @GetMapping("/history")
    public String listHistory(@RequestParam(name = "fullName", required = false) String fullName,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              Model model) {
        final int pageSize = 5;
        Page<MedicalRecord> recordsPage = adminService.findMedicalRecords(PageRequest.of(page, pageSize, Sort.by("examinationDate").descending()), fullName);
        model.addAttribute("recordsPage", recordsPage);
        model.addAttribute("fullName", fullName);
        return "admin/history";
    }

    @GetMapping("/history/detail/{recordId}")
    public String showHistory(@PathVariable Long recordId, Model model) {
        MedicalRecord record = adminService.getMedicalRecordById(recordId);
        Doctor doctor = record.getDoctor();
        Patient patient = record.getPatient();

        List<Appointment> appointments = adminService.findAppointmentByPatientDoctorAndDate(patient.getPatientID(), doctor.getDoctorID(), record.getAppoinmentDate());
        Appointment matchedAppointment = appointments.isEmpty() ? null : appointments.get(0);
        Schedule matchedSchedule = null;

        if (matchedAppointment != null) {
            Date appointmentTime = matchedAppointment.getAppointmentDate();
            matchedSchedule = doctor.getSchedules().stream()
                    .filter(s -> !appointmentTime.before(s.getStartTime()) && !appointmentTime.after(s.getEndTime()))
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("record", record);
        model.addAttribute("matchedSchedule", matchedSchedule);
        return "admin/history-detail";
    }

    @PostMapping("/history/updateStatus")
    public String updateStatus(@RequestParam("recordId") Long recordId,
                               @RequestParam("paid") Boolean paid) {
        MedicalRecord record = adminService.getMedicalRecordById(recordId);

        record.getPrescriptions().forEach(prescription -> prescription.setPaid(paid));

        adminService.updateMedicalRecord(record);
        return "redirect:/admin/history/detail/" + recordId;
    }
}
