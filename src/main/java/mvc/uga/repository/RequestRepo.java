package mvc.uga.repository;

import mvc.uga.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepo extends JpaRepository<Request, Integer> {

    List<Request> findAllRequestByUserUsername(String username);

}
