package fit.se2.medicarehub.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class TestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testID;

    // Tên dịch vụ xét nghiệm
    private String serviceName;

    // Chỉ định thực hiện
    private String instruction;

    // Ghi chú (nếu cần)
    private String note;

    // Liên kết đến Record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recordID", nullable = false)
    private MedicalRecord record;
}

