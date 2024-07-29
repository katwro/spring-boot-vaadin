package mvc.uga.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import mvc.uga.constant.AppConstants;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EquipmentID")
    private int id;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Column(name = "Name")
    private String name;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Column(name = "SerialNumber")
    private String serialNumber;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Column(name = "InventoryNumber")
    private String inventoryNumber;

    @OneToMany(mappedBy = "equipment", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE,CascadeType.MERGE})
    private List<Request> requests;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

}
