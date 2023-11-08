package mate.academy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.dto.order.OrderResponseDto;
import mate.academy.dto.order.OrderStatusDto;
import mate.academy.dto.order.ShippingAddressRequestDto;
import mate.academy.dto.orderitem.OrderItemResponseDto;
import mate.academy.model.Order;
import mate.academy.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {
    private static final Long VALID_ID = 1L;
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderService orderService;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) {
        setupDatabase(dataSource);
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller/order/remove-from-tables.sql")
            );
        }
    }

    @SneakyThrows
    static void setupDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller/order/add-users-orders.sql")
            );
        }
    }

    @Test
    @DisplayName("Create order for user")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void createOrder_validAuthenticationAndShippingAddressRequestDto_Success() throws Exception {
        ShippingAddressRequestDto requestDto = new ShippingAddressRequestDto()
                .setShippingAddress("Long address line");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/orders")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("Get all orders for user")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void getAllOrders_validUser_Success() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<OrderResponseDto> expected = orderService
                .getAllOrders(authentication, Pageable.unpaged());
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        List<OrderResponseDto> actual = objectMapper.readValue(
                jsonResponse,
                new TypeReference<>() {}
        );
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get all items by order id")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void getAllItemsByOrderId_validId_Success() throws Exception {
        OrderItemResponseDto itemResponseDto = new OrderItemResponseDto()
                .setBookId(VALID_ID)
                .setQuantity(1)
                .setId(VALID_ID);
        List<OrderItemResponseDto> expected = new ArrayList<>();
        expected.add(itemResponseDto);

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/orders/1/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page", String.valueOf(
                                        pageable.getPageNumber()))
                                .param("size", String.valueOf(pageable.getPageSize()))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        List<OrderItemResponseDto> actual = objectMapper.readValue(
                jsonResponse,
                new TypeReference<>() {}
        );
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get specific order item by order id and item id")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void getOrderItemByOrderIdAndItemId_validIds_Success() throws Exception {
        OrderItemResponseDto expected = new OrderItemResponseDto()
                .setId(VALID_ID)
                .setBookId(VALID_ID)
                .setQuantity(1);

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/orders/1/items/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        OrderItemResponseDto actual = objectMapper
                .readValue(jsonResponse, OrderItemResponseDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin@test.com", password = "test", authorities = {"ADMIN"})
    void updateOrderStatus_withValidParameters() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OrderStatusDto orderStatusDto = new OrderStatusDto().setStatus(Order.Status.SHIPPED);
        OrderResponseDto expected = orderService
                .updateOrderStatus(authentication, VALID_ID, orderStatusDto);

        String jsonRequest = objectMapper.writeValueAsString(orderStatusDto);

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/orders/1")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        OrderResponseDto actual = objectMapper.readValue(jsonResponse, OrderResponseDto.class);
        assertEquals(expected, actual);
    }

}
