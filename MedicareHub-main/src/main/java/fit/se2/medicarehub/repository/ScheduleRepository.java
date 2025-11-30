package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findSchedulesByDoctorDoctorID(Long doctorID);
}
