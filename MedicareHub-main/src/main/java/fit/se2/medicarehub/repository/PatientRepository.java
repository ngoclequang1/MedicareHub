package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByUser_UserID(Long userId);
    Patient findByUser_Email(String email);
    Optional<Patient> findByPatientCode(String patientCode);
}
