package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.AppointmentStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AppointmentStatsRepository extends JpaRepository<AppointmentStats, Long> {
    @Query("SELECT s FROM AppointmentStats s " +
            "WHERE s.statDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.statDate")
    List<AppointmentStats> findByDateRange(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate);

    AppointmentStats findByStatDate(Date statDate);
}
