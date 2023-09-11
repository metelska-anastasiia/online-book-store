package mate.academy.mapper;

import mate.academy.config.MapperConfig;
import mate.academy.dto.category.CategoryDto;
import mate.academy.dto.category.CategoryResponseDto;
import mate.academy.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {

    CategoryResponseDto toDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toModel(CategoryDto categoryDto);
}
