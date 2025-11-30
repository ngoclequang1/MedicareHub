package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prescriptionID;

    // Tên thuốc
    private String medicineName;

    // Chỉ định sử dụng
    private String instruction;

    // Số lượng thuốc
    private Integer quantity;

    // Đơn giá thuốc
    private double price;

    private boolean paid = false;

    // Liên kết đến Record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recordID", nullable = false)
    private MedicalRecord record;

    private boolean reminder = false;
}
