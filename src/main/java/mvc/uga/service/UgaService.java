package mvc.uga.service;

import mvc.uga.entity.*;

import java.util.List;

public interface UgaService {

    List<User> findAllUsers();

    List<User> searchUser(String searchTerm);

    List<Role> findAllRoles();

    List<Equipment> findAllEquipment();

    List<Equipment> findAllEquipmentByUsername(String username);

    List<Equipment> searchEquipment(String searchTerm);

    List<Type> findAllTypes(int type);

    List<Request> findAllRequestsByUsername(String username);

    User findUserById(int id);

    User findUserByUsername(String username);

    Request findRequestById(int id);

    void saveUser(User user,String originalPassword);

    void saveEquipment(Equipment equipment);

    void saveRequest(Request request);

    void deleteUser(User user);

    void deleteEquipment(Equipment equipment);

}
