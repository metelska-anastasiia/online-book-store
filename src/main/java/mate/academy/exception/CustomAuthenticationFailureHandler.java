package mate.academy.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler,
        AuthenticationEntryPoint {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        Map<String, Object> data = new HashMap<>();
        data.put(
                "timestamp",
                Calendar.getInstance().getTime());
        data.put(
                "exception",
                exception.getMessage());

        response.getOutputStream()
                .println(objectMapper.writeValueAsString(data));
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {
        // Встановлюємо код помилки 401 (UNAUTHORIZED)
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        // Встановлюємо тип відповіді на JSON
        response.setContentType("application/json;charset=UTF-8");

        // Отримуємо повідомлення про помилку
        String errorMessage = "Authentication failed: " + authException.getMessage();

        // Створюємо JSON-об'єкт з повідомленням про помилку
        String jsonResponse = "{\"error\": \"" + errorMessage + "\"}";

        // Відправляємо JSON-відповідь
        response.getWriter().write(jsonResponse);
    }
}
