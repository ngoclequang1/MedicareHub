package fit.se2.medicarehub.service.agent;

import fit.se2.medicarehub.model.AgentPendingAction;
import fit.se2.medicarehub.model.Appointment;
import fit.se2.medicarehub.model.AppointmentStatus;
import fit.se2.medicarehub.model.Doctor;
import fit.se2.medicarehub.model.Patient;
import fit.se2.medicarehub.model.User;
import fit.se2.medicarehub.repository.AgentPendingActionRepository;
import fit.se2.medicarehub.repository.AppointmentRepository;
import fit.se2.medicarehub.repository.DoctorRepository;
import fit.se2.medicarehub.repository.PatientRepository;
import fit.se2.medicarehub.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class AgentActionService {
    private static final Duration ACTION_TTL = Duration.ofMinutes(10);

    private final AgentPendingActionRepository actionRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;

    public AgentActionService(AgentPendingActionRepository actionRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              AppointmentRepository appointmentRepository,
                              ScheduleRepository scheduleRepository) {
        this.actionRepository = actionRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public AgentPendingAction prepareAppointment(User user, Long doctorId, Date appointmentDate) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ đã chọn."));
        LocalDate requestedDate = appointmentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (requestedDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày khám phải ở hiện tại hoặc tương lai.");
        }
        boolean inSchedule = scheduleRepository.findSchedulesByDoctorDoctorID(doctorId).stream()
                .anyMatch(schedule -> !appointmentDate.before(schedule.getStartTime())
                        && !appointmentDate.after(schedule.getEndTime()));
        if (!inSchedule) {
            throw new IllegalArgumentException("Bác sĩ không có lịch làm việc phù hợp trong ngày đã chọn.");
        }
        AgentPendingAction action = new AgentPendingAction();
        action.setToken(UUID.randomUUID().toString());
        action.setActionType("CREATE_APPOINTMENT");
        action.setDoctorId(doctorId);
        action.setAppointmentDate(appointmentDate);
        action.setUser(user);
        action.setExpiresAt(Date.from(new Date().toInstant().plus(ACTION_TTL)));
        return actionRepository.save(action);
    }

    @Transactional
    public Appointment confirmAppointment(User user, String token) {
        AgentPendingAction action = actionRepository.findByTokenAndUser(token, user)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu xác nhận không hợp lệ."));
        if (action.isExecuted() || action.getExpiresAt().before(new Date())) {
            throw new IllegalArgumentException("Yêu cầu xác nhận đã hết hạn hoặc đã được sử dụng.");
        }
        Patient patient = patientRepository.findByUser_UserID(user.getUserID());
        if (patient == null) {
            throw new IllegalStateException("Bạn cần tạo hồ sơ bệnh nhân trước khi đặt lịch.");
        }
        Doctor doctor = doctorRepository.findById(action.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ đã chọn."));
        if (!appointmentRepository.findAppointmentByPatientDoctorAndDate(
                patient.getPatientID(), doctor.getDoctorID(), action.getAppointmentDate()).isEmpty()) {
            throw new IllegalArgumentException("Bạn đã có lịch với bác sĩ này trong ngày đã chọn.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(action.getAppointmentDate());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(new Date());
        appointmentRepository.save(appointment);
        action.setExecuted(true);
        actionRepository.save(action);
        return appointment;
    }
}
