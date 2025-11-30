package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "appointment_reminder")
@Data
public class AppointmentReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminderID;

    @OneToOne
    @JoinColumn(name = "appointmentID", nullable = false)
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "patientID", nullable = false)
    private Patient patient;

    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reminderTime;

    private boolean reminderStatus = false;
}
