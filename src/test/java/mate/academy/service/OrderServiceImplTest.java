package mate.academy.service;

import static mate.academy.config.DatabaseHelper.prepareBook;
import static mate.academy.config.DatabaseHelper.prepareCartItem;
import static mate.academy.config.DatabaseHelper.prepareOrder;
import static mate.academy.config.DatabaseHelper.prepareOrderItem;
import static mate.academy.config.DatabaseHelper.prepareOrderItemResponseDto;
import static mate.academy.config.DatabaseHelper.prepareShoppingCart;
import static mate.academy.config.DatabaseHelper.prepareUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.order.OrderResponseDto;
import mate.academy.dto.order.OrderStatusDto;
import mate.academy.dto.order.ShippingAddressRequestDto;
import mate.academy.dto.orderitem.OrderItemResponseDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.OrderItemMapper;
import mate.academy.mapper.OrderMapper;
import mate.academy.model.Book;
import mate.academy.model.CartItem;
import mate.academy.model.Order;
import mate.academy.model.OrderItem;
import mate.academy.model.ShoppingCart;
import mate.academy.model.User;
import mate.academy.repository.cart.ShoppingCartRepository;
import mate.academy.repository.order.OrderRepository;
import mate.academy.repository.orderitem.OrderItemRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final int CART_ITEM_QTY = 1;
    private static final Long CART_ITEM_ID = 1L;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Place order for valid user")
    void placeOrder_validUser_Success() {
        User user = prepareUser();
        Book book = prepareBook();
        CartItem cartItem = prepareCartItem(book, CART_ITEM_ID, CART_ITEM_QTY);
        ShoppingCart shoppingCart = prepareShoppingCart(user, Set.of(cartItem));
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findShoppingCartByUserId(user.getId()))
                .thenReturn(Optional.of(shoppingCart));
        Order order = prepareOrder(user, Order.Status.NEW, ORDER_ID);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ShippingAddressRequestDto shippingAddressRequest = new ShippingAddressRequestDto()
                .setShippingAddress("Kyiv, Maydan Nezalezhnosty 1");
        orderService.placeOrder(authentication, shippingAddressRequest);
        verify(shoppingCartRepository, times(1)).delete(shoppingCart);
    }

    @Test
    @DisplayName("Place order for invalid user should return EntityNotFoundException")
    void placeOrder_invalidUser_shouldReturnEntityNotFoundException() {
        ShippingAddressRequestDto shippingAddressRequest = new ShippingAddressRequestDto()
                .setShippingAddress("Kyiv, Maydan Nezalezhnosty 1");
        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.placeOrder(authentication, shippingAddressRequest)
        );
        assertEquals(EntityNotFoundException.class, entityNotFoundException.getClass());
    }

    @Test
    @DisplayName("Get all orders for user")
    void getAllOrders_validAuthenticationWithPagination_Success() {
        OrderResponseDto orderResponseDto = new OrderResponseDto()
                .setOrderDate(LocalDateTime.of(2023, 11, 4, 11,30,1))
                .setOrderStatus("NEW")
                .setTotal(BigDecimal.valueOf(100))
                .setUserId(USER_ID)
                .setOrderItems(Set.of(prepareOrderItemResponseDto()));
        List<OrderResponseDto> expected = new ArrayList<>();
        expected.add(orderResponseDto);
        User user = prepareUser();
        Order order = prepareOrder(user, Order.Status.NEW, ORDER_ID);
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> ordersPage = new PageImpl<>(orders, pageable, orders.size());
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findAllByUserId(user.getId(), pageable)).thenReturn(ordersPage);
        when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

        List<OrderResponseDto> actual = orderService.getAllOrders(authentication, pageable);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get all order items for user with order id")
    void getAllOrderItems_validAuthenticationAndOrderId_Success() {
        OrderItemResponseDto orderItemResponseDto = prepareOrderItemResponseDto();
        List<OrderItemResponseDto> expected = new ArrayList<>();
        expected.add(orderItemResponseDto);
        User user = prepareUser();
        Order order = prepareOrder(user, Order.Status.NEW, ORDER_ID)
                .setOrderItems(Set.of(prepareOrderItem()));
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndId(USER_ID, ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemMapper.toResponseDto(any(OrderItem.class))).thenReturn(orderItemResponseDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<OrderItemResponseDto> actual = orderService
                .getAllOrderItems(authentication, USER_ID, pageable);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get all order items for user with invalid order id should"
            + "return EntityNotFoundException")
    void getAllOrderItems_invalidUser_shouldThrowEntityNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);
        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.getAllOrderItems(authentication, 2L, pageable)
        );
        assertEquals(EntityNotFoundException.class, entityNotFoundException.getClass());
    }

    @Test
    @DisplayName("Get order item from order")
    void getSpecificOrderItem_validUserAndIds_success() {
        OrderItemResponseDto orderItemResponseDto = prepareOrderItemResponseDto();
        List<OrderItemResponseDto> allOrderItems = new ArrayList<>();
        allOrderItems.add(orderItemResponseDto);
        User user = prepareUser();
        Order order = prepareOrder(user, Order.Status.NEW, ORDER_ID)
                        .setOrderItems(Set.of(prepareOrderItem()));
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndId(USER_ID, ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemMapper.toResponseDto(any(OrderItem.class))).thenReturn(orderItemResponseDto);

        OrderItemResponseDto actual = orderService
                .getSpecificOrderItem(authentication, ORDER_ID, 1L);
        assertEquals(orderItemResponseDto, actual);
    }

    @Test
    @DisplayName("Update order status")
    void updateOrderStatus_validData_Success() {
        User user = prepareUser();
        Order updatedOrder = prepareOrder(user, Order.Status.PROCEED, ORDER_ID);

        OrderStatusDto statusDto = new OrderStatusDto()
                .setStatus(Order.Status.PROCEED);

        OrderResponseDto expected = new OrderResponseDto()
                .setOrderStatus("PROCEED")
                .setOrderDate(LocalDateTime.of(2023, 11, 4, 11,30,1))
                .setTotal(BigDecimal.valueOf(100))
                .setUserId(USER_ID)
                .setOrderItems(Set.of(prepareOrderItemResponseDto()));
        Order order = prepareOrder(user, Order.Status.NEW, ORDER_ID);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(expected);
        OrderResponseDto actual = orderService
                .updateOrderStatus(authentication, ORDER_ID, statusDto);
        assertEquals(expected, actual);
    }
}
