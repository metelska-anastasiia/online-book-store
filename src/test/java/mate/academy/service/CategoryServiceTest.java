package mate.academy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import mate.academy.dto.category.CategoryDto;
import mate.academy.dto.category.CategoryResponseDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.CategoryMapper;
import mate.academy.model.Category;
import mate.academy.repository.category.CategoryRepository;
import mate.academy.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    private static final Long CATEGORY_ID = 1L;
    private static final Long INVALID_CATEGORY_ID = -1L;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Verify findAll() returns all not-deleted categories in DB")
    void findAll_validPageable_ReturnsAllCategories() {
        //Given
        Pageable pageable = PageRequest.of(0, 10);

        CategoryResponseDto categoryDto = prepareCategoryResponseDto();
        List<CategoryResponseDto> expected = Arrays.asList(categoryDto);

        Category category = prepareCategory();

        List<Category> categories = Arrays.asList(category);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        //When
        List<CategoryResponseDto> actual = categoryService.findAll(pageable);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getById() with valid id returns need category from DB")
    void getById_ValidId_ShouldReturnCategory() {
        CategoryResponseDto expected = prepareCategoryResponseDto();
        Category category = prepareCategory();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(expected);

        CategoryResponseDto actual = categoryService.getById(CATEGORY_ID);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getById() with invalid id returns exception")
    void getById_InvalidId_ShouldThrowEntityNotFoundException() {
        when(categoryRepository.findById(INVALID_CATEGORY_ID))
                .thenThrow(new EntityNotFoundException("Can't find category by id "
                        + INVALID_CATEGORY_ID));

        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.getById(INVALID_CATEGORY_ID)
        );

        assertEquals("Can't find category by id " + INVALID_CATEGORY_ID,
                entityNotFoundException.getMessage());
        assertEquals(EntityNotFoundException.class, entityNotFoundException.getClass());
    }

    @Test
    @DisplayName("Verify save() returns correct category after saving")
    void save_ValidCategoryDto_ShouldSaveCategory() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Category 1");
        categoryDto.setDescription("Test");

        CategoryResponseDto expected = prepareCategoryResponseDto();
        Category category = prepareCategory();

        when(categoryMapper.toModel(categoryDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expected);

        CategoryResponseDto actual = categoryService.save(categoryDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify update() updated category with valid ID and input parameters")
    void update_ValidIdAndRequestParams_ShouldUpdateCategory() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setDescription("New Category");
        categoryDto.setName("Category new");

        Category updatedCategory = new Category();
        updatedCategory.setDescription(categoryDto.getDescription());
        updatedCategory.setName(categoryDto.getName());
        updatedCategory.setId(CATEGORY_ID);

        CategoryResponseDto expected = categoryMapper.toDto(updatedCategory);
        Category category = prepareCategory();

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponseDto actual = categoryService.update(CATEGORY_ID, categoryDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify update() throws exception for invalid ID")
    void update_InvalidId_ShouldThrowEntityNotFoundException() {
        //Given
        when(categoryRepository.findById(INVALID_CATEGORY_ID))
                .thenThrow(new EntityNotFoundException("Can't find category by id "
                        + INVALID_CATEGORY_ID));
        CategoryDto categoryDto = new CategoryDto();

        //When
        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.update(INVALID_CATEGORY_ID, categoryDto)
        );

        //Then
        assertEquals("Can't find category by id " + INVALID_CATEGORY_ID,
                entityNotFoundException.getMessage());
        assertEquals(EntityNotFoundException.class, entityNotFoundException.getClass());
    }

    @Test
    @DisplayName("Verify delete() removes category by id")
    void deleteById_ValidId_ShouldDeleteCategory() {
        doNothing().when(categoryRepository).deleteById(CATEGORY_ID);
        categoryService.deleteById(CATEGORY_ID);
        verify(categoryRepository, times(1)).deleteById(CATEGORY_ID);
    }

    private Category prepareCategory() {
        return new Category()
                .setId(CATEGORY_ID)
                .setName("Category 1")
                .setDescription("Test");
    }

    private CategoryResponseDto prepareCategoryResponseDto() {
        return new CategoryResponseDto()
                .setId(CATEGORY_ID)
                .setName("Category 1")
                .setDescription("Test category");
    }
}
