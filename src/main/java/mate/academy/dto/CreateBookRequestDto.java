package mate.academy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateBookRequestDto {
    private String title;
    private String author;
    private BigDecimal price;
    private String description;
    private String coverImage;
}
