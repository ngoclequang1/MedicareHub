package fit.se2.medicarehub.service;

import fit.se2.medicarehub.model.Appointment;
import fit.se2.medicarehub.model.AppointmentStatus;
import fit.se2.medicarehub.model.Schedule;
import fit.se2.medicarehub.repository.AppointmentRepository;
import fit.se2.medicarehub.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentBookingService {

    public enum ConfirmationResult {
        CONFIRMED,
        NOT_FOUND,
        FORBIDDEN,
        NOT_PENDING,
        OUTSIDE_SCHEDULE,
        FULL
    }

    private static final List<AppointmentStatus> OCCUPYING_STATUSES = List.of(
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.ONGOING,
            AppointmentStatus.COMPLETED
    );

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;

    public AppointmentBookingService(AppointmentRepository appointmentRepository,
                                     ScheduleRepository scheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional
    public ConfirmationResult confirm(Long appointmentId, Long patientId) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(appointmentId);
        if (appointmentOptional.isEmpty()) {
            return ConfirmationResult.NOT_FOUND;
        }

        Appointment appointment = appointmentOptional.get();
        if (!appointment.getPatient().getPatientID().equals(patientId)) {
            return ConfirmationResult.FORBIDDEN;
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return ConfirmationResult.NOT_PENDING;
        }

        List<Schedule> schedules = scheduleRepository.findMatchingSchedulesForUpdate(
                appointment.getDoctor().getDoctorID(), appointment.getAppointmentDate());
        if (schedules.isEmpty()) {
            return ConfirmationResult.OUTSIDE_SCHEDULE;
        }

        long occupiedSeats = appointmentRepository.countByDoctorDoctorIDAndAppointmentDateAndStatusIn(
                appointment.getDoctor().getDoctorID(), appointment.getAppointmentDate(), OCCUPYING_STATUSES);
        int capacity = schedules.stream().mapToInt(Schedule::getSeatCount).max().orElse(0);
        if (occupiedSeats >= capacity) {
            return ConfirmationResult.FULL;
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setQueueNumber(Math.toIntExact(occupiedSeats + 1));
        appointmentRepository.save(appointment);
        return ConfirmationResult.CONFIRMED;
    }
}
