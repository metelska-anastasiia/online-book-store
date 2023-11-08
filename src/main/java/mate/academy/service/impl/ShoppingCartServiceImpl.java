package mate.academy.service.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.cart.ShoppingCartDto;
import mate.academy.dto.cartitem.CartItemQuantityRequestDto;
import mate.academy.dto.cartitem.CartItemRequestDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.CartItemMapper;
import mate.academy.mapper.ShoppingCartMapper;
import mate.academy.model.Book;
import mate.academy.model.CartItem;
import mate.academy.model.ShoppingCart;
import mate.academy.model.User;
import mate.academy.repository.book.BookRepository;
import mate.academy.repository.cart.ShoppingCartRepository;
import mate.academy.repository.cartitem.CartItemRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.ShoppingCartService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final UserRepository userRepository;

    @Override
    public void addItemToCart(Authentication authentication,
                              CartItemRequestDto cartItemRequestDto) {
        User user = getUser(authentication);
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartByUserId(user.getId())
                .orElseGet(() -> registerNewShoppingCart(user));

        Optional<CartItem> existingCartItem = shoppingCart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(cartItemRequestDto.getBookId()))
                .findFirst();
        CartItem cartItemToUpdate;
        if (existingCartItem.isPresent()) {
            cartItemToUpdate = existingCartItem.get();
            cartItemToUpdate.setQuantity(cartItemRequestDto.getQuantity()
                    + cartItemToUpdate.getQuantity());
        } else {
            cartItemToUpdate = createNewCartItem(cartItemRequestDto, shoppingCart);
        }
        cartItemRepository.save(cartItemToUpdate);
    }

    @Override
    public ShoppingCartDto getAllCartItems(Authentication authentication) {
        User user = getUser(authentication);
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartByUserId(user.getId())
                .orElseGet(() -> registerNewShoppingCart(user));
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public void updateBookQuantity(Authentication authentication,
                                   Long cartItemId,
                                   CartItemQuantityRequestDto qtyRequestDto) {
        User user = getUser(authentication);
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find shopping cart by "
                        + "user id " + user.getId()));

        CartItem cartItem = cartItemRepository.findByIdAndShoppingCartId(cartItemId,
                        shoppingCart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find cart item "
                        + "by cart item id " + cartItemId));
        cartItem.setQuantity(qtyRequestDto.getQuantity());
        cartItemRepository.save(cartItem);
    }

    private ShoppingCart registerNewShoppingCart(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        return shoppingCartRepository.save(shoppingCart);
    }

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can not find user by email" + email));
    }

    private CartItem createNewCartItem(CartItemRequestDto cartItemRequestDto,
                                       ShoppingCart shoppingCart) {
        Book bookFromDb = bookRepository
                .findById(cartItemRequestDto.getBookId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can not find book with id: "
                                + cartItemRequestDto.getBookId()));
        CartItem cartItem = cartItemMapper.toEntity(cartItemRequestDto);
        cartItem.setBook(bookFromDb);
        cartItem.setShoppingCart(shoppingCart);
        return cartItem;
    }
}
