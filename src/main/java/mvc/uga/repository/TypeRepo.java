package mvc.uga.repository;

import mvc.uga.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TypeRepo extends JpaRepository<Type, Integer> {

    List<Type> findAllByType(int type);

}
