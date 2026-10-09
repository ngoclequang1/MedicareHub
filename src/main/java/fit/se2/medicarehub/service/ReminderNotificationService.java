package fit.se2.medicarehub.service;

import fit.se2.medicarehub.model.Appointment;
import fit.se2.medicarehub.model.AppointmentReminder;
import fit.se2.medicarehub.model.MedicationReminder;
import fit.se2.medicarehub.model.Prescription;
import fit.se2.medicarehub.repository.AppointmentReminderRepository;
import fit.se2.medicarehub.repository.AppointmentRepository;
import fit.se2.medicarehub.repository.MedicationReminderRepository;
import fit.se2.medicarehub.repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ReminderNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderNotificationService.class);
    private static final int MAX_ATTEMPTS = 5;

    private final AppointmentReminderRepository appointmentReminderRepository;
    private final MedicationReminderRepository medicationReminderRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final EmailService emailService;

    public ReminderNotificationService(AppointmentReminderRepository appointmentReminderRepository,
                                       MedicationReminderRepository medicationReminderRepository,
                                       AppointmentRepository appointmentRepository,
                                       PrescriptionRepository prescriptionRepository,
                                       EmailService emailService) {
        this.appointmentReminderRepository = appointmentReminderRepository;
        this.medicationReminderRepository = medicationReminderRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedDelayString = "${app.reminders.poll-interval-ms:60000}")
    public void sendDueAppointmentReminders() {
        Date now = new Date();
        for (AppointmentReminder reminder : appointmentReminderRepository
                .findByReminderTimeLessThanEqualAndReminderStatusTrueAndAttemptCountLessThan(now, MAX_ATTEMPTS)) {
            if (appointmentReminderRepository.claimForSending(reminder.getReminderID(), now, MAX_ATTEMPTS) != 1) {
                continue;
            }
            try {
                String subject = "Nhắc nhở cuộc hẹn: " + reminder.getAppointment().getAppointmentDate();
                String message = "Bạn có một cuộc hẹn vào " + reminder.getAppointment().getAppointmentDate()
                        + ". Nội dung nhắc nhở: " + reminder.getMessage();
                emailService.sendEmail(reminder.getPatient().getUser().getEmail(), subject, message);

                Appointment appointment = reminder.getAppointment();
                appointment.setReminderStatus(false);
                appointmentRepository.save(appointment);
            } catch (RuntimeException exception) {
                appointmentReminderRepository.recordFailure(
                        reminder.getReminderID(), safeError(exception), reminder.getAttemptCount() + 1 < MAX_ATTEMPTS);
                LOGGER.warn("Không thể gửi email nhắc lịch hẹn {}", reminder.getReminderID(), exception);
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.reminders.poll-interval-ms:60000}")
    public void sendDueMedicationReminders() {
        Date now = new Date();
        for (MedicationReminder reminder : medicationReminderRepository
                .findByReminderTimeLessThanEqualAndReminderStatusTrueAndAttemptCountLessThan(now, MAX_ATTEMPTS)) {
            if (medicationReminderRepository.claimForSending(reminder.getReminderID(), now, MAX_ATTEMPTS) != 1) {
                continue;
            }
            try {
                String subject = "Nhắc nhở uống thuốc: " + reminder.getMedicationName();
                String message = "Bạn cần sử dụng thuốc " + reminder.getMedicationName()
                        + ". Hướng dẫn: " + reminder.getDosage();
                emailService.sendEmail(reminder.getPatient().getUser().getEmail(), subject, message);

                prescriptionRepository.findById(reminder.getPrescriptionId()).ifPresent(prescription -> {
                    prescription.setReminder(false);
                    prescriptionRepository.save(prescription);
                });
            } catch (RuntimeException exception) {
                medicationReminderRepository.recordFailure(
                        reminder.getReminderID(), safeError(exception), reminder.getAttemptCount() + 1 < MAX_ATTEMPTS);
                LOGGER.warn("Không thể gửi email nhắc uống thuốc {}", reminder.getReminderID(), exception);
            }
        }
    }

    private String safeError(RuntimeException exception) {
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        return message.substring(0, Math.min(message.length(), 500));
    }
}
