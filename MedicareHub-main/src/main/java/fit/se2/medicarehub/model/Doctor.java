package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data

public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorID;

    @OneToOne
    @JoinColumn(name = "userID", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "specialtyID", nullable = false)
    private Specialty specialty;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    private String licenseNumber;
    private String clinicAddress;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @Enumerated(EnumType.STRING)
    private DoctorDegree academicDegree;

    private boolean isHead;

}
