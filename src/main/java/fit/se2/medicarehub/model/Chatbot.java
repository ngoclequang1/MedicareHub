package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "chatbot_interaction")
@Data
public class Chatbot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionID;

    @ManyToOne
    @JoinColumn(name = "userID")
    private User user;

    @Lob
    private String message;

    @Lob
    private String response;

    private String intent;

    private String toolName;

    private String safetyLevel;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
