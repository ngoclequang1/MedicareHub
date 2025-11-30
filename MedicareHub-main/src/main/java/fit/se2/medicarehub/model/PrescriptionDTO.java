package fit.se2.medicarehub.model;

import lombok.Data;

@Data
public class PrescriptionDTO {
    private String medicineName;
    private String instruction;
    private Integer quantity;
    private double price;
}
