package mvc.uga.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import mvc.uga.constant.AppConstants;

@Entity
@Getter
@Setter
@Table(name = "Request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestID")
    private int id;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Column(name = "Description")
    private String description;

    @Column(name = "Attachment")
    private byte[] attachment;

    @ManyToOne
    @JoinColumn(name = "EquipmentID")
    private Equipment equipment;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @NotNull(message = AppConstants.REQUIRED_FIELD)
    @ManyToOne
    @JoinColumn(name = "Category", referencedColumnName = "TypeID")
    private Type category;

    @ManyToOne
    @JoinColumn(name = "Option", referencedColumnName = "TypeID")
    private Type option;

    @NotNull(message = AppConstants.REQUIRED_FIELD)
    @ManyToOne
    @JoinColumn(name = "Scope", referencedColumnName = "TypeID")
    private Type scope;

    @NotNull(message = AppConstants.REQUIRED_FIELD)
    @ManyToOne
    @JoinColumn(name = "Priority", referencedColumnName = "TypeID")
    private Type priority;

}
