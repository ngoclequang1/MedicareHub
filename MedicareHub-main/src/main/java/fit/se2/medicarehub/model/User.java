package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "users")
@Data
@ToString(exclude = "roleID")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userID;

    @ManyToOne
    @JoinColumn(name = "roleID", nullable = false)
    private Role roleID;

    @Column(unique = true, nullable = false)
    private String username;
    private String fullName;
    @Column(unique = true, nullable = false)
    private String email;
    private String phoneNumber;
    private String password;
    private String gender;
    private String identityNumber;
    private String image;
    private boolean enabled;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    private String UUID;

}
