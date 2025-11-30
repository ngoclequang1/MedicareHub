package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "admin")
@Data
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminID;

    @OneToOne
    @JoinColumn(name = "userID", nullable = false, unique = true)
    private User user;

    private String address;
}
