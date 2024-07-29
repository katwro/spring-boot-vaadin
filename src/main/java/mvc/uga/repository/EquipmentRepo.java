package mvc.uga.repository;

import mvc.uga.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepo extends JpaRepository<Equipment, Integer> {

    List<Equipment> findAllEquipmentByUserUsername(String username);

    List<Equipment> findByNameContainingIgnoreCaseOrInventoryNumberContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrUserUsernameContainingIgnoreCase(
            String searchTerm1, String searchTerm2, String searchTerm3, String searchTerm4);

}
