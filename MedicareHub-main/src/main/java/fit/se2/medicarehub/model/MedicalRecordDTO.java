package fit.se2.medicarehub.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MedicalRecordDTO {
    private Long doctorId;
    private Long patientId;
    private Date examinationDate;
    private String symptoms;
    private String diagnosis;
    private boolean reExamination;
    private List<TestRecordDTO> tests;
    private List<PrescriptionDTO> prescriptions;
}
