package mate.academy.dto.orderitem;

import java.math.BigDecimal;
import lombok.Data;
import mate.academy.model.Book;
import mate.academy.model.Order;

@Data
public class OrderItemDto {
    private Order order;
    private Book book;
    private int quantity;
    private BigDecimal price;
}
