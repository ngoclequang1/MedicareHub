package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUser_Email(String email);
}
