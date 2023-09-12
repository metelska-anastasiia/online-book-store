package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.item.CartItemRequestDto;
import mate.academy.dto.item.CartItemResponseDto;
import mate.academy.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(config = MapperConfig.class)
@Component
public interface CartItemMapper {

    @Mapping(target = "book.id", source = "bookId")
    CartItem toEntity(CartItemRequestDto cartItemRequestDto);

    @Mapping(target = "bookId", source = "book.id")
    CartItemRequestDto toRequestDto(CartItem cartItem);

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    CartItemResponseDto toResponseDto(CartItem cartItem);
}
