package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class MedicalRecord  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long recordID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorID", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID", nullable = false)
    private Patient patient;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date examinationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date appoinmentDate;

    @Lob
    private String symptoms;

    @Lob
    private String diagnosis;

    private boolean reExamination;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestRecord> tests = new ArrayList<>();

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();

}
