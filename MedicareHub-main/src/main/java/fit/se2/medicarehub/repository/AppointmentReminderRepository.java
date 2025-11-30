package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.AppointmentReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AppointmentReminderRepository extends JpaRepository<AppointmentReminder, Long> {
    List<AppointmentReminder> findByReminderTimeLessThanEqualAndReminderStatusTrue(Date now);
    @Query("SELECT r FROM AppointmentReminder r WHERE r.appointment.appointmentID = :appointmentId")
    Optional<AppointmentReminder> findByAppointmentId(@Param("appointmentId") Long appointmentId);

}
