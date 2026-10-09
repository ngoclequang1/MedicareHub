package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.AgentPendingAction;
import fit.se2.medicarehub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentPendingActionRepository extends JpaRepository<AgentPendingAction, Long> {
    Optional<AgentPendingAction> findByTokenAndUser(String token, User user);
}
