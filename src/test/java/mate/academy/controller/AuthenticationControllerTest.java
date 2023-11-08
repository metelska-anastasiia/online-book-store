package mate.academy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.dto.user.UserLoginRequestDto;
import mate.academy.dto.user.UserLoginResponseDto;
import mate.academy.dto.user.UserRegistrationRequest;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.security.AuthenticationService;
import mate.academy.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private JwtUtil jwtUtil;

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
                    new ClassPathResource("database/controller"
                            + "/authentication/remove-from-users_roles.sql")
            );

            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller"
                            + "/authentication/remove-from-roles.sql")
            );

            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller"
                            + "/authentication/remove-from-users.sql")
            );
        }
    }

    @SneakyThrows
    static void setupDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller"
                            + "/authentication/add-user-to-users-table.sql")
            );
        }
    }

    @Test
    @DisplayName("Register new user")
    void register_validRequest_Success() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest()
                .setEmail("newuser@test.com")
                .setPassword("Newuser12345")
                .setRepeatPassword("Newuser12345")
                .setFirstName("User")
                .setLastName("New");

        UserResponseDto expected = new UserResponseDto()
                .setId(3L)
                .setEmail(request.getEmail())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName());

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/auth/register")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        UserResponseDto actual = objectMapper.readValue(jsonResponse, UserResponseDto.class);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Login user with valid UserLoginRequestDto")
    void login_validUserLoginRequestDto_Success() throws Exception {
        UserLoginRequestDto loginRequestDto = new UserLoginRequestDto("john@test.com", "12345678");
        String expectedToken = authenticationService.authenticate(loginRequestDto).token();
        String expectedEmail = jwtUtil.getUsername(expectedToken);

        String jsonRequest = objectMapper.writeValueAsString(loginRequestDto);

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();

        String actualToken = objectMapper
                .readValue(jsonResponse, UserLoginResponseDto.class).token();
        String actualEmail = jwtUtil.getUsername(actualToken);

        assertEquals(expectedEmail, actualEmail);
    }
}








