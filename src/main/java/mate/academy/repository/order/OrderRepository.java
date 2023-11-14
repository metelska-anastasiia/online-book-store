package mate.academy.repository.order;

import java.util.Optional;
import mate.academy.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "orderItems")
    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findByUserIdAndId(Long userId, Long orderId);

    @EntityGraph(attributePaths = {"orderItems", "user.roles"})
    Optional<Order> findById(Long orderId);
}
