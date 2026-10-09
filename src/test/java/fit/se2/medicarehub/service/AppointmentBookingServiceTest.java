package fit.se2.medicarehub.service;

import fit.se2.medicarehub.model.Appointment;
import fit.se2.medicarehub.model.AppointmentStatus;
import fit.se2.medicarehub.model.Doctor;
import fit.se2.medicarehub.model.Patient;
import fit.se2.medicarehub.model.Schedule;
import fit.se2.medicarehub.repository.AppointmentRepository;
import fit.se2.medicarehub.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    private AppointmentBookingService service;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        service = new AppointmentBookingService(appointmentRepository, scheduleRepository);

        Patient patient = new Patient();
        patient.setPatientID(10L);
        Doctor doctor = new Doctor();
        doctor.setDoctorID(20L);

        appointment = new Appointment();
        appointment.setAppointmentID(30L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(new Date());
        appointment.setStatus(AppointmentStatus.PENDING);
    }

    @Test
    void confirmsAndAssignsOneBasedQueueNumberWhenCapacityIsAvailable() {
        Schedule schedule = new Schedule();
        schedule.setSeatCount(3);
        when(appointmentRepository.findById(30L)).thenReturn(Optional.of(appointment));
        when(scheduleRepository.findMatchingSchedulesForUpdate(20L, appointment.getAppointmentDate()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.countByDoctorDoctorIDAndAppointmentDateAndStatusIn(
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.eq(appointment.getAppointmentDate()), anyList()))
                .thenReturn(1L);

        AppointmentBookingService.ConfirmationResult result = service.confirm(30L, 10L);

        assertEquals(AppointmentBookingService.ConfirmationResult.CONFIRMED, result);
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals(2, appointment.getQueueNumber());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void rejectsAppointmentOwnedByAnotherPatientBeforeLockingSchedule() {
        when(appointmentRepository.findById(30L)).thenReturn(Optional.of(appointment));

        AppointmentBookingService.ConfirmationResult result = service.confirm(30L, 99L);

        assertEquals(AppointmentBookingService.ConfirmationResult.FORBIDDEN, result);
        verify(scheduleRepository, never()).findMatchingSchedulesForUpdate(20L, appointment.getAppointmentDate());
        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    void rejectsConfirmationWhenScheduleIsFull() {
        Schedule schedule = new Schedule();
        schedule.setSeatCount(1);
        when(appointmentRepository.findById(30L)).thenReturn(Optional.of(appointment));
        when(scheduleRepository.findMatchingSchedulesForUpdate(20L, appointment.getAppointmentDate()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.countByDoctorDoctorIDAndAppointmentDateAndStatusIn(
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.eq(appointment.getAppointmentDate()), anyList()))
                .thenReturn(1L);

        AppointmentBookingService.ConfirmationResult result = service.confirm(30L, 10L);

        assertEquals(AppointmentBookingService.ConfirmationResult.FULL, result);
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        verify(appointmentRepository, never()).save(appointment);
    }
}
