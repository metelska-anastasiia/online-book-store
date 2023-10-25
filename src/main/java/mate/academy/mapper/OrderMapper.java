package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.order.OrderRequestDto;
import mate.academy.dto.order.OrderResponseDto;
import mate.academy.model.Order;
import mate.academy.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = OrderItemMapper.class)
public interface OrderMapper {

    OrderRequestDto toDto(ShoppingCart shoppingCart);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderStatus", source = "status")
    OrderResponseDto toDto(Order order);

    Order toModel(OrderRequestDto orderRequestDto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "total", source = "total")
    @Mapping(target = "orderStatus", source = "status")
    OrderResponseDto toResponseDto(Order order);
}

