package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.MedicalRecord;
import fit.se2.medicarehub.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findTopByPatientOrderByExaminationDateDesc(Patient patient);
    List<MedicalRecord> findAllByPatientOrderByExaminationDateDesc(Patient patient);
}
