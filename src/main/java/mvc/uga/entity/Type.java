package mvc.uga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Type")
public class Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TypeID")
    private int id;

    @Column(name = "Type")
    private int type;

    @Column(name = "Number")
    private int number;

    @Column(name = "Name")
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Request> categories;

    @OneToMany(mappedBy = "option")
    private List<Request> options;

    @OneToMany(mappedBy = "scope")
    private List<Request> scopes;

    @OneToMany(mappedBy = "priority")
    private List<Request> priorities;

}
