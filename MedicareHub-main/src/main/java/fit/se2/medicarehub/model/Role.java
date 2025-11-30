package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleID;

    private String roleName;

    @OneToMany(mappedBy = "roleID", cascade = CascadeType.ALL)
    private List<User> users;


}
