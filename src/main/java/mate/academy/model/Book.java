package mate.academy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Entity
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Book title cannot be null")
    private String title;
    @NotNull(message = "Author cannot be null")
    private String author;

    @NotNull(message = "isbn cannot be null")
    @Column(unique = true)
    private String isbn;
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;
    private String description;
    private String coverImage;
}
