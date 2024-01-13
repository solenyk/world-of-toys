package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.dto.product.category.CategoryDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.mapper.product.CategoryMapper;
import com.kopchak.worldoftoys.mapper.product.CategoryMapperImpl;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.impl.CategoryRepositoryImpl;
import com.kopchak.worldoftoys.repository.specifications.ProductSpecifications;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@Import({ProductSpecificationsImpl.class, CategoryRepositoryImpl.class, CategoryMapperImpl.class})
class CategoryRepositoryTest {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductSpecifications productSpecifications;

    @Autowired
    CategoryMapper categoryMapper;

    @Test
    public void findUniqueFilteringProductCategories() {
        String productName = "Лялька";
        BigDecimal minPrice = BigDecimal.valueOf(600);
        BigDecimal maxPrice = BigDecimal.valueOf(850);
        List<String> originCategoriesSlugList = List.of("china");
        List<String> brandCategoriesSlugList = List.of("сoсomelon", "сurlimals", "devilon");
        List<String> ageCategoriesSlugList = List.of("do-1-roku", "vid-1-do-3-rokiv", "vid-6-do-9-rokiv");

        Specification<Product> productSpecification = productSpecifications.filterByProductNamePriceAndCategories(
                productName, minPrice, maxPrice, originCategoriesSlugList, brandCategoriesSlugList, ageCategoriesSlugList);

        FilteringCategoriesDto filteringCategoriesDto = categoryRepository
                .findUniqueFilteringProductCategories(productSpecification);

        List<CategoryDto> expectedOriginCategories = List.of(new CategoryDto("Китай", "china"));
        List<CategoryDto> expectedBrandCategories = List.of(
                new CategoryDto("CoComelon", "сoсomelon"));
        List<CategoryDto> expectedAgeCategories = List.of(
                new CategoryDto("від 1 до 3 років", "vid-1-do-3-rokiv"),
                new CategoryDto("від 6 до 9 років", "vid-6-do-9-rokiv"));

        List<CategoryDto> actualOriginCategories = filteringCategoriesDto.originCategories();
        List<CategoryDto> actualBrandCategories = filteringCategoriesDto.brandCategories();
        List<CategoryDto> actualAgeCategories = filteringCategoriesDto.ageCategories();

        assertThat(actualOriginCategories).isEqualTo(expectedOriginCategories);
        assertThat(actualBrandCategories).isEqualTo(expectedBrandCategories);
        assertThat(actualAgeCategories).isEqualTo(expectedAgeCategories);
    }
}