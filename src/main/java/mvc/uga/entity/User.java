package mvc.uga.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mvc.uga.constant.AppConstants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private int id;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Column(name = "username")
    private String username;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Column(name = "password")
    private String password;

    @Column(name = "enabled")
    private boolean enabled;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Column(name = "FirstName")
    private String firstName;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Column(name = "LastName")
    private String lastName;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Pattern(regexp = AppConstants.EMAIL_REGEXP, message = AppConstants.INVALID_VALUE)
    @Column(name = "Email")
    private String email;

    @NotEmpty(message = AppConstants.REQUIRED_FIELD)
    @Pattern(regexp = AppConstants.PHONE_REGEXP, message = AppConstants.INVALID_VALUE)
    @Column(name = "Phone")
    private String phone;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Equipment> equipmentList;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE,CascadeType.MERGE})
    private List<Request> requests;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "User_Role",
            joinColumns = @JoinColumn(name = "UserID"),
            inverseJoinColumns = @JoinColumn(name = "RoleID"))
    private Set<Role> roles = new HashSet<>();

}
