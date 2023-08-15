package mate.academy;

import java.math.BigDecimal;
import mate.academy.model.Book;
import mate.academy.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnlineBookStoreApplication {
    private final BookService bookService;

    @Autowired
    public OnlineBookStoreApplication(BookService bookService) {
        this.bookService = bookService;
    }

    public static void main(String[] args) {
        SpringApplication.run(OnlineBookStoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Book kobzar = new Book();
            kobzar.setTitle("Kobzar");
            kobzar.setAuthor("Taras Shevchenko");
            kobzar.setDescription("Unique Ukrainian poem");
            kobzar.setPrice(BigDecimal.valueOf(599L));
            kobzar.setIsbn("1234567890");
            kobzar.setCoverImage("something that should be here");

            bookService.save(kobzar);

            System.out.println(bookService.findAll());
        };
    }

}
