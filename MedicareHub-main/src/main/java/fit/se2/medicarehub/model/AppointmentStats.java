package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "appointment_stats")
@Data
public class AppointmentStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statID;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date statDate;

    private Long totalAppointment;
    private Long totalPatient;

    @Lob
    private String topSpecialtiesJson;
}
