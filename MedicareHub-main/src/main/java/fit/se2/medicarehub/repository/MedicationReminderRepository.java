package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {
    List<MedicationReminder> findByReminderTimeBefore(Date now);
    @Query("SELECT m FROM MedicationReminder m WHERE m.prescriptionId = :prescriptionId")
    Optional<MedicationReminder> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

}
