package mate.academy.repository.book;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.model.Book;
import mate.academy.repository.SpecificationProvider;
import mate.academy.repository.SpecificationProviderManager;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationProviderManager implements SpecificationProviderManager<Book> {
    private final List<SpecificationProvider<Book>> bookSpecificationProviders;

    @Override
    public SpecificationProvider<Book> getSpecificationProvider(String key) {
        return bookSpecificationProviders.stream()
                .filter(b -> b.getKey().equals(key)).findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find orrect specification for key "
                        + key));
    }
}
