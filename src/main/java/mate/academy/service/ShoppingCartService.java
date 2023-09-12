package mate.academy.service;

import mate.academy.dto.cart.ShoppingCartDto;
import mate.academy.dto.item.CartItemQuantityRequestDto;
import mate.academy.dto.item.CartItemRequestDto;
import org.springframework.security.core.Authentication;

public interface ShoppingCartService {
    void addItemToCart(Authentication authentication, CartItemRequestDto cartItemRequestDto);

    ShoppingCartDto getAllCartItems(Authentication authentication);

    void updateBookQuantity(Authentication authentication, Long cartItemId,
                            CartItemQuantityRequestDto qtyToSubtract);
}
