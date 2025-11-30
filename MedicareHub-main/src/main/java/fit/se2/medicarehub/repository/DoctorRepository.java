package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Doctor;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByUser_Email(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Doctor d SET d.isHead = CASE WHEN d.doctorID = :doctorId THEN true ELSE false END WHERE d.specialty.specialtyID = :specialtyId")
    void updateHeadDoctor(@Param("specialtyId") Long specialtyId, @Param("doctorId") Long doctorId);
}
