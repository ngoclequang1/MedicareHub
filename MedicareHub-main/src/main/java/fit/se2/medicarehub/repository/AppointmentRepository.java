package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Appointment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    //Parent appointment
    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.doctor d " +
            "JOIN FETCH d.user " +
            "WHERE a.patient.patientID = :patientID " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findAppointmentsByPatient(@Param("patientID") Long patientID);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.appointmentDate = :appointmentDate " +
            "AND a.doctor.doctorID = :doctorID " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findAppointmentsByDoctorAndDate(@Param("doctorID") Long doctorID, @Param("appointmentDate") Date appointmentDate);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.appointmentDate = :appointmentDate " +
            "AND a.patient.patientID = :patientID " +
            "AND a.doctor.doctorID = :doctorID")
    List<Appointment> findAppointmentByPatientDoctorAndDate(@Param("patientID") Long patientID,
                                                            @Param("doctorID") Long doctorID,
                                                            @Param("appointmentDate") Date appointmentDate);

    //Apointmentstats
    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE FUNCTION('DATE', a.appointmentDate) = FUNCTION('DATE', :date)")
    Long countByDate(@Param("date") Date date);

    @Query("SELECT COUNT(DISTINCT a.patient.patientID) FROM Appointment a " +
            "WHERE FUNCTION('DATE', a.appointmentDate) = FUNCTION('DATE', :date)")
    Long countDistinctPatientByDate(@Param("date") Date date);

    // Trả về List<Object[]> = [specialtyID, specialtyName, count]
    @Query("SELECT d.specialty.specialtyID, d.specialty.specialtyName, COUNT(a) as cnt " +
            "FROM Appointment a " +
            "JOIN a.doctor d " +
            "WHERE FUNCTION('DATE', a.appointmentDate) = FUNCTION('DATE', :date) " +
            "GROUP BY d.specialty.specialtyID, d.specialty.specialtyName " +
            "ORDER BY cnt DESC")
    List<Object[]> findTopSpecialtiesByDate(@Param("date") Date date, Pageable pageable);

    // Trả về 1 dòng [doctorName, count]
    @Query("SELECT d.user.fullName, COUNT(a) as cnt " +
            "FROM Appointment a " +
            "JOIN a.doctor d " +
            "WHERE FUNCTION('DATE', a.appointmentDate) = FUNCTION('DATE', :date) " +
            "  AND d.specialty.specialtyID = :specialtyID " +
            "GROUP BY d.user.fullName " +
            "ORDER BY cnt DESC")
    List<Object[]> findTopDoctorBySpecialtyAndDate(@Param("date") Date date,
                                                   @Param("specialtyID") Long specialtyID,
                                                   Pageable pageable);


}

