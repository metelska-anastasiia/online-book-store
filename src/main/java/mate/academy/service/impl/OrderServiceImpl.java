package mate.academy.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.order.OrderResponseDto;
import mate.academy.dto.order.OrderStatusDto;
import mate.academy.dto.order.ShippingAddressRequestDto;
import mate.academy.dto.orderitem.OrderItemResponseDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.OrderItemMapper;
import mate.academy.mapper.OrderMapper;
import mate.academy.model.Order;
import mate.academy.model.OrderItem;
import mate.academy.model.ShoppingCart;
import mate.academy.model.User;
import mate.academy.repository.cart.ShoppingCartRepository;
import mate.academy.repository.order.OrderRepository;
import mate.academy.repository.orderitem.OrderItemRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public void placeOrder(Authentication authentication,
                           ShippingAddressRequestDto shippingAddress) {
        User user = getUser(authentication);
        Order order = createOrder(authentication, shippingAddress);
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find shopping cart "
                        + "by user id " + user.getId()));
        Set<OrderItem> orderItems = shoppingCart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setBook(cartItem.getBook());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getBook().getPrice());
                    orderItem.setOrder(order);
                    return orderItemRepository.save(orderItem);
                })
                .collect(Collectors.toSet());
        shoppingCartRepository.delete(shoppingCart);
    }

    @Override
    public List<OrderResponseDto> getAllOrders(Authentication authentication,
                                               Pageable pageable) {
        User user = getUser(authentication);
        Page<Order> allOrders = orderRepository.findAllByUserId(user.getId(), pageable);
        return allOrders.stream()
                .map(orderMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<OrderItemResponseDto> getAllOrderItems(Authentication authentication,
                                                       Long orderId,
                                                       Pageable pageable) {
        User user = getUser(authentication);
        Order order = orderRepository.findByUserIdAndId(user.getId(), orderId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find order by order id "
                        + orderId + " and user id " + user.getId()));
        return order.getOrderItems().stream()
                .map(orderItemMapper::toResponseDto)
                .toList();
    }

    @Override
    public OrderItemResponseDto getSpecificOrderItem(Authentication authentication,
                                                     Long orderId,
                                                     Long itemId) {
        List<OrderItemResponseDto> allOrderItems = getAllOrderItems(authentication,
                orderId, Pageable.unpaged());
        return allOrderItems.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Can't find order item by id "
                        + itemId));
    }

    @Override
    public OrderResponseDto updateOrderStatus(Authentication authentication, Long orderId,
                                              OrderStatusDto statusDto) {
        User user = getUser(authentication);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find order by id "
                        + orderId));
        order.setStatus(statusDto.getStatus());
        return orderMapper.toDto(orderRepository.save(order));
    }

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can not find user by email" + email));
    }

    private Order createOrder(Authentication authentication,
                            ShippingAddressRequestDto shippingAddress) {
        User user = getUser(authentication);
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find shopping cart "
                        + "by user id " + user.getId()));
        double total = shoppingCart.getCartItems().stream()
                .mapToDouble(cartItem -> (double) cartItem.getQuantity()
                        * cartItem.getBook().getPrice().doubleValue())
                .sum();
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress.getShippingAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.Status.NEW);
        order.setTotal(BigDecimal.valueOf(total));
        order.setOrderItems(new HashSet<>());
        return orderRepository.save(order);
    }
}
