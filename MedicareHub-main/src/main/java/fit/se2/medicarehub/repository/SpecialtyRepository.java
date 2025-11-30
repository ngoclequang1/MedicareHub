package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findBySpecialtyName(String specialtyName);

    @Query("SELECT d.user.fullName FROM Doctor d WHERE d.isHead = true AND d.specialty.specialtyID = :specialtyId")
    Optional<String> findHeadDoctorBySpecialtyId(@Param("specialtyId") Long specialtyId);

    @Query("SELECT d.doctorID FROM Doctor d WHERE d.isHead = true AND d.specialty.specialtyID = :specialtyId")
    Optional<Long> findHeadDoctorIdBySpecialtyId(@Param("specialtyId") Long specialtyId);
}
