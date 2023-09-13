package mate.academy.dto.order;

import lombok.Data;
import mate.academy.model.Order;

@Data
public class OrderStatusDto {
    private Order.Status status;
}
