package mate.academy.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import mate.academy.dto.orderitem.OrderItemResponseDto;

@Data
@Accessors(chain = true)
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private Set<OrderItemResponseDto> orderItems;
    private LocalDateTime orderDate;
    private BigDecimal total;
    private String orderStatus;

}
