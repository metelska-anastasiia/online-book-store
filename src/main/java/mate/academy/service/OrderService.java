package mate.academy.service;

import java.util.List;
import mate.academy.dto.order.OrderResponseDto;
import mate.academy.dto.order.OrderStatusDto;
import mate.academy.dto.order.ShippingAddressRequestDto;
import mate.academy.dto.orderitem.OrderItemResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface OrderService {

    void placeOrder(Authentication authentication,
                    ShippingAddressRequestDto shippingAddress);

    List<OrderResponseDto> getAllOrders(Authentication authentication, Pageable pageable);

    List<OrderItemResponseDto> getAllOrderItems(Authentication authentication,
                                                Long orderId, Pageable pageable);

    OrderItemResponseDto getSpecificOrderItem(Authentication authentication, Long orderId,
                                              Long itemId);

    OrderResponseDto updateOrderStatus(Authentication authentication, Long orderId,
                                       OrderStatusDto statusDto);
}
