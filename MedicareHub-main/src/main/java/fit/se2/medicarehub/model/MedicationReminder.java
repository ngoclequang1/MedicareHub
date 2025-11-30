package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "medication_reminder")
@Data
public class MedicationReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminderID;

    @Column(name = "prescription_id")
    private Long prescriptionId;

    @ManyToOne
    @JoinColumn(name = "patientID", nullable = false)
    private Patient patient;

    private String medicationName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reminderTime;

    private String dosage;

    private boolean reminderStatus = false;
}
