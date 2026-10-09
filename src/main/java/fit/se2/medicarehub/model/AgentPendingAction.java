package fit.se2.medicarehub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class AgentPendingAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionID;

    @jakarta.persistence.Column(unique = true, nullable = false)
    private String token;
    private String actionType;
    private Long doctorId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date appointmentDate;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    private boolean executed;
}
