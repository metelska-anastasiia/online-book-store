package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.cart.ShoppingCartDto;
import mate.academy.dto.cartitem.CartItemQuantityRequestDto;
import mate.academy.dto.cartitem.CartItemRequestDto;
import mate.academy.repository.cartitem.CartItemRepository;
import mate.academy.service.ShoppingCartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping cart management", description = "Shopping cart for managing categories")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;
    private final CartItemRepository cartItemRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add new book to cart",
            description = "Before adding new book we check if this book is already in cart "
                    + "and than or change qty or add new cartItem")
    @PreAuthorize("hasAuthority('USER')")
    public void addBook(Authentication authentication,
                        @RequestBody @Valid CartItemRequestDto cartItemRequestDto) {
        shoppingCartService.addItemToCart(authentication, cartItemRequestDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Get all books from cart", description = "Get all books from cart")
    public ShoppingCartDto getAllCartItems(Authentication authentication) {
        return shoppingCartService.getAllCartItems(authentication);
    }

    @PutMapping("/books/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update book qty in cart", description = "Update book qty in cart")
    public void updateCartItemByBookId(Authentication authentication, @PathVariable Long id,
                                       @RequestBody @Valid
                                       CartItemQuantityRequestDto qtyToSubtract) {
        shoppingCartService.updateBookQuantity(authentication, id, qtyToSubtract);
    }

    @DeleteMapping("/cart-items/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete cart item", description = "Delete cart item")
    public void removeCartItemByBookId(Authentication authentication, @PathVariable Long id) {
        cartItemRepository.deleteById(id);
    }
}
