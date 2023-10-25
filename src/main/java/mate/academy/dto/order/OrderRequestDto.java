package mate.academy.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import mate.academy.dto.orderitem.OrderItemDto;
import mate.academy.model.Order;
import mate.academy.model.User;

@Data
public class OrderRequestDto {
    private User user;
    private Order.Status status;
    private BigDecimal total;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private Set<OrderItemDto> orderItems;
}
