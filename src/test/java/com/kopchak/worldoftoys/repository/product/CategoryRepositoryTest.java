package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.exception.CategoryNotFoundException;
import com.kopchak.worldoftoys.mapper.product.CategoryMapperImpl;
import com.kopchak.worldoftoys.repository.product.impl.CategoryRepositoryImpl;
import com.kopchak.worldoftoys.repository.specifications.ProductSpecifications;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("integrationtest")
@Import({ProductSpecificationsImpl.class, CategoryRepositoryImpl.class, CategoryMapperImpl.class})
class CategoryRepositoryTest {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductSpecifications productSpecifications;

    @Test
    public void findById_ReturnsProductCategory() throws CategoryNotFoundException {
        Class<BrandCategory> productCategoryType = BrandCategory.class;

        BrandCategory expectedBrandCategory = new BrandCategory();
        expectedBrandCategory.setId(5);
        expectedBrandCategory.setName("comelon");
        expectedBrandCategory.setSlug("сomelon");
        expectedBrandCategory.setProducts(new HashSet<>());

        BrandCategory actualBrandCategory = categoryRepository.findById(5, productCategoryType);

        assertThat(actualBrandCategory).isNotNull();
        assertThat(actualBrandCategory).isInstanceOf(productCategoryType);
        assertThat(actualBrandCategory).usingRecursiveComparison().isEqualTo(expectedBrandCategory);
    }

    @Test
    public void findById_InvalidCategoryId_ThrowsCategoryNotFoundException() {
        Class<OriginCategory> productCategoryType = OriginCategory.class;
        Integer id = 5;

        String categoryNotFoundExceptionMsg = String.format("%s with id: %d does not exist",
                productCategoryType.getSimpleName(), id);

        assertException(CategoryNotFoundException.class, categoryNotFoundExceptionMsg,
                () -> categoryRepository.findById(id, productCategoryType));
    }

    @Test
    public void findAllCategories_ReturnsSetOfOriginCategories() {
        Class<OriginCategory> productCategoryType = OriginCategory.class;
        int expectedOriginCategoriesSetSize = 2;

        Set<OriginCategory> actualOriginCategories = categoryRepository.findAllCategories(productCategoryType);

        assertThat(actualOriginCategories).isNotNull();
        assertThat(actualOriginCategories).isNotEmpty();
        assertThat(actualOriginCategories.size()).isEqualTo(expectedOriginCategoriesSetSize);
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}