package mvc.uga.service;

import mvc.uga.entity.*;
import mvc.uga.repository.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UgaServiceImpl implements UgaService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final RequestRepo requestRepo;
    private final TypeRepo typeRepo;
    private final EquipmentRepo equipmentRepo;
    private final BCryptPasswordEncoder passwordEncoder;


    public UgaServiceImpl(UserRepo userRepo, RoleRepo roleRepo, RequestRepo requestRepo, TypeRepo typeRepo,
                          EquipmentRepo equipmentRepo, BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.requestRepo = requestRepo;
        this.typeRepo = typeRepo;
        this.equipmentRepo = equipmentRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public List<User> searchUser(String searchTerm) {
        return userRepo.findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(searchTerm, searchTerm, searchTerm);
    }

    @Override
    public List<Role> findAllRoles() {
        return roleRepo.findAll();
    }

    @Override
    public List<Equipment> findAllEquipment() {
        return equipmentRepo.findAll();
    }

    @Override
    public List<Equipment> findAllEquipmentByUsername(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("No result found for username: " + username);
        }
        return equipmentRepo.findAllEquipmentByUserUsername(username);
    }

    @Override
    public List<Equipment> searchEquipment(String searchTerm) {
        return equipmentRepo.findByNameContainingIgnoreCaseOrInventoryNumberContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrUserUsernameContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm, searchTerm);
    }

    @Override
    public List<Type> findAllTypes(int type) {
        return typeRepo.findAllByType(type);
    }

    @Override
    public List<Request> findAllRequestsByUsername(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("No result found for username: " + username);
        }
        return requestRepo.findAllRequestByUserUsername(username);
    }

    @Override
    public User findUserById(int id) {
        Optional<User> result = userRepo.findById(id);

        User user;

        if (result.isPresent()) {
            user = result.get();
        } else {
            throw new RuntimeException("No result found for id: " + id);
        }
        return user;
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public Request findRequestById(int id) {
        Optional<Request> result = requestRepo.findById(id);

        Request request;

        if (result.isPresent()) {
            request  = result.get();
        } else {
            throw new RuntimeException("No result found for id: " + id);
        }
        return request;
    }

    @Override
    public void saveUser(User user, String originalPassword) {
        if (user.getPassword() != null && !user.getPassword().equals(originalPassword)) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setPhone(user.getPhone().replaceAll("\\D", ""));

        userRepo.save(user);
    }

    @Override
    public void saveEquipment(Equipment equipment) {
        equipmentRepo.save(equipment);
    }

    @Override
    public void saveRequest(Request request) {
        requestRepo.save(request);
    }

    @Override
    public void deleteUser(User user) {
        userRepo.delete(user);
    }

    @Override
    public void deleteEquipment(Equipment equipment) {
        equipmentRepo.delete(equipment);
    }

}
