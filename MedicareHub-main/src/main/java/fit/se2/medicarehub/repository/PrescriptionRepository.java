package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Prescription;
import org.springframework.data.repository.CrudRepository;

public interface PrescriptionRepository extends CrudRepository<Prescription, Long> {
}
