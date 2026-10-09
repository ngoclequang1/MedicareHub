package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Schedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findSchedulesByDoctorDoctorID(Long doctorID);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Schedule s WHERE s.doctor.doctorID = :doctorId " +
            "AND :appointmentDate BETWEEN s.startTime AND s.endTime")
    List<Schedule> findMatchingSchedulesForUpdate(@Param("doctorId") Long doctorId,
                                                   @Param("appointmentDate") Date appointmentDate);
}
