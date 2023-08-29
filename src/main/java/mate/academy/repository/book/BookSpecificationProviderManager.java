package mate.academy.repository.book;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.exception.EntityNotFoundException;
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
                .filter(provider -> provider.getKey().equals(key)).findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find correct specification for key "
                                + key));
    }
}
