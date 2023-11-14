package mate.academy.service;

import static mate.academy.config.DatabaseHelper.prepareBook;
import static mate.academy.config.DatabaseHelper.prepareCartItem;
import static mate.academy.config.DatabaseHelper.prepareShoppingCart;
import static mate.academy.config.DatabaseHelper.prepareUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.cart.ShoppingCartDto;
import mate.academy.dto.cartitem.CartItemQuantityRequestDto;
import mate.academy.dto.cartitem.CartItemRequestDto;
import mate.academy.dto.cartitem.CartItemResponseDto;
import mate.academy.mapper.ShoppingCartMapper;
import mate.academy.model.Book;
import mate.academy.model.CartItem;
import mate.academy.model.ShoppingCart;
import mate.academy.model.User;
import mate.academy.repository.cart.ShoppingCartRepository;
import mate.academy.repository.cartitem.CartItemRepository;
import mate.academy.repository.user.UserRepository;
import mate.academy.service.impl.ShoppingCartServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    private static final Long BOOK_ID = 1L;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Test
    @DisplayName("Add Item to Shopping Cart")
    void addItemToCart_ValidCartItemRequestDto_Success() {
        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto()
                .setBookId(BOOK_ID)
                .setQuantity(1);
        CartItem cartItem = prepareCartItem(prepareBook(), 1L, cartItemRequestDto.getQuantity());
        User user = prepareUser();
        ShoppingCart shoppingCart = prepareShoppingCart(user, Set.of(cartItem));
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findShoppingCartByUserId(user.getId()))
                .thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        shoppingCartService.addItemToCart(authentication, cartItemRequestDto);
        verify(shoppingCartRepository, times(1)).findShoppingCartByUserId(user.getId());
        assertEquals(1, shoppingCart.getCartItems().size());
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    @DisplayName("Get all cart items in Shopping Cart")
    void getAllCartItems_ValidAuthentication_Success() {
        Book book2 = new Book()
                .setId(2L)
                .setAuthor("Author 2")
                .setTitle("Test Book 2")
                .setPrice(BigDecimal.valueOf(300))
                .setIsbn("54321");

        CartItem cartItem1 = prepareCartItem(prepareBook(), 1L, 1);
        CartItem cartItem2 = prepareCartItem(book2, 2L, 1);

        Set<CartItem> cartItems = new HashSet<>();
        cartItems.add(cartItem1);
        cartItems.add(cartItem2);

        CartItemResponseDto cartItemResponseDto1 = new CartItemResponseDto()
                .setId(1L)
                .setBookId(cartItem1.getBook().getId())
                .setQuantity(cartItem1.getQuantity())
                .setBookTitle(cartItem1.getBook().getTitle());
        CartItemResponseDto cartItemResponseDto2 = new CartItemResponseDto()
                .setId(2L)
                .setBookId(cartItem2.getBook().getId())
                .setQuantity(cartItem2.getQuantity())
                .setBookTitle(cartItem2.getBook().getTitle());
        Set<CartItemResponseDto> cartItemResponseDtoSet =
                Set.of(cartItemResponseDto1, cartItemResponseDto2);
        User user = prepareUser();

        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        shoppingCartDto.setUserId(user.getId());
        shoppingCartDto.setCartItems(cartItemResponseDtoSet);

        ShoppingCart shoppingCart = prepareShoppingCart(user, cartItems);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findShoppingCartByUserId(user.getId()))
                .thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(shoppingCartDto);
        when(shoppingCartService.getAllCartItems(authentication)).thenReturn(shoppingCartDto);
        ShoppingCartDto actual = shoppingCartService.getAllCartItems(authentication);

        assertEquals(2, actual.getCartItems().size());
        assertEquals(shoppingCartDto.getCartItems(), actual.getCartItems());
    }

    @Test
    @DisplayName("Update quantity of existing cart item")
    void updateBookQuantity_AllValidData_Success() {
        User user = prepareUser();
        CartItem existingCartItem = prepareCartItem(prepareBook(), 1L, 1);
        ShoppingCart shoppingCart = prepareShoppingCart(user, Set.of(existingCartItem));

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findShoppingCartByUserId(user.getId()))
                .thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.findByIdAndShoppingCartId(existingCartItem.getId(),
                shoppingCart.getId())).thenReturn(Optional.of(existingCartItem));

        CartItemQuantityRequestDto newQuantity = new CartItemQuantityRequestDto()
                .setQuantity(3);
        shoppingCartService.updateBookQuantity(authentication,
                existingCartItem.getId(), newQuantity);

        verify(shoppingCartRepository, times(1)).findShoppingCartByUserId(user.getId());
        verify(cartItemRepository, times(1))
                .findByIdAndShoppingCartId(existingCartItem.getId(), shoppingCart.getId());
        verify(cartItemRepository, times(1)).save(existingCartItem);
        assertEquals(3, newQuantity.getQuantity());
    }
}
