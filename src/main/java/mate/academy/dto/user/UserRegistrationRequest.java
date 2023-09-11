package mate.academy.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import mate.academy.lib.FieldMatch;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldMatch(first = "password", second = "repeatPassword", message = "Passwords must match")
public class UserRegistrationRequest {
    @NotBlank
    @Size(min = 4, max = 50)
    private String email;
    @NotBlank
    @Size(min = 4, max = 100)
    private String password;
    @NotBlank
    @Size(min = 4, max = 100)
    private String repeatPassword;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String shippingAddress;
}
