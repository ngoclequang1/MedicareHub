package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "specialties")
@Data
@ToString(exclude = "doctors")
public class Specialty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long specialtyID;

    private String specialtyName;

    private String specialtyDescription;

    private int numberOfDoctors;

    private boolean specialtyStatus;

    @OneToMany(mappedBy = "specialty", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Doctor> doctors;

    public int getNumberOfDoctors(){
        return doctors != null ? doctors.size() : 0;
    }
}
