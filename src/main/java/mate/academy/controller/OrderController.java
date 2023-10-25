package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.order.OrderResponseDto;
import mate.academy.dto.order.OrderStatusDto;
import mate.academy.dto.order.ShippingAddressRequestDto;
import mate.academy.dto.orderitem.OrderItemResponseDto;
import mate.academy.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order management", description = "Endpoints for managing orders")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Create a new order", description = "Create a new order")
    @PreAuthorize("hasAuthority('USER')")
    void createOrder(Authentication authentication,
                     @RequestBody @Valid ShippingAddressRequestDto shippingAddress) {
        orderService.placeOrder(authentication, shippingAddress);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Get all orders")
    @PreAuthorize("hasAuthority('USER')")
    List<OrderResponseDto> getAllOrders(Authentication authentication,
                                        Pageable pageable) {
        return orderService.getAllOrders(authentication, pageable);
    }

    @GetMapping("/{orderId}/items")
    @Operation(summary = "Get order items in order by order id",
            description = "Retrieve all OrderItems for a specific order")
    @PreAuthorize("hasAuthority('USER')")
    List<OrderItemResponseDto> getAllItemsByOrderId(Authentication authentication,
                                                    @PathVariable Long orderId,
                                                    Pageable pageable) {
        return orderService.getAllOrderItems(authentication,orderId, pageable);
    }

    @GetMapping("/{orderId}/items/{id}")
    @Operation(summary = "Get a specific item in order",
            description = "View a specific item in order")
    @PreAuthorize("hasAuthority('USER')")
    OrderItemResponseDto getOrderItemByOrderIdAndItemId(Authentication authentication,
                                                        @PathVariable Long orderId,
                                                        @PathVariable Long id) {
        return orderService.getSpecificOrderItem(authentication, orderId, id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update order status",
            description = "Update order status by Admin")
    OrderResponseDto updateOrderStatus(Authentication authentication, @PathVariable Long id,
                                       @RequestBody OrderStatusDto statusDto) {
        return orderService.updateOrderStatus(authentication, id,statusDto);
    }
}
