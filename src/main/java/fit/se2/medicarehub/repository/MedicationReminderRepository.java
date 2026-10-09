package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {
    List<MedicationReminder> findByReminderTimeLessThanEqualAndReminderStatusTrueAndAttemptCountLessThan(Date now, int maxAttempts);

    @Modifying
    @Transactional
    @Query("UPDATE MedicationReminder r SET r.reminderStatus = false, " +
            "r.attemptCount = r.attemptCount + 1, r.lastAttemptAt = :now " +
            "WHERE r.reminderID = :id AND r.reminderStatus = true AND r.attemptCount < :maxAttempts")
    int claimForSending(@Param("id") Long id, @Param("now") Date now,
                        @Param("maxAttempts") int maxAttempts);

    @Modifying
    @Transactional
    @Query("UPDATE MedicationReminder r SET r.reminderStatus = :retry, r.lastError = :error " +
            "WHERE r.reminderID = :id")
    void recordFailure(@Param("id") Long id, @Param("error") String error,
                       @Param("retry") boolean retry);
    @Query("SELECT m FROM MedicationReminder m WHERE m.prescriptionId = :prescriptionId")
    Optional<MedicationReminder> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

}
