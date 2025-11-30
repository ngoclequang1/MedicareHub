package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "patients")
@Data
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientID;

    @OneToOne
    @JoinColumn(name = "userID", nullable = false, unique = true)
    private User user;

    private String patientCode;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    private String address;

    private String ethnicity;

    @Column(nullable = false)
    private boolean deleted = false;
}
