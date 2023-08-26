package mate.academy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Data
@SQLDelete(sql = "UPDATE books SET is_Deleted=true WHERE id=?")
@Where(clause = "is_deleted=false")
@Table(name = "books")
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
    @Column(nullable = false)
    private boolean isDeleted = false;
}
