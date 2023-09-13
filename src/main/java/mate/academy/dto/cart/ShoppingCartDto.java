package mate.academy.dto.cart;

import java.util.Set;
import lombok.Data;
import mate.academy.dto.cartitem.CartItemResponseDto;

@Data
public class ShoppingCartDto {
    private Long id;
    private Long userId;
    private Set<CartItemResponseDto> cartItems;
}
