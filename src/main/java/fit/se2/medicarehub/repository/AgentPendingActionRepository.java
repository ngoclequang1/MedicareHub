package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.AgentPendingAction;
import fit.se2.medicarehub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AgentPendingActionRepository extends JpaRepository<AgentPendingAction, Long> {
    Optional<AgentPendingAction> findByTokenAndUser(String token, User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AgentPendingAction a WHERE a.token = :token AND a.user = :user")
    Optional<AgentPendingAction> findByTokenAndUserForUpdate(@Param("token") String token,
                                                              @Param("user") User user);
}
