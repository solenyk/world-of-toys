package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.mapper.product.ProductCategoryMapper;
import com.kopchak.worldoftoys.mapper.product.ProductCategoryMapperImpl;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.impl.ProductCategoryRepositoryImpl;
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
@Import({ProductSpecificationsImpl.class, ProductCategoryRepositoryImpl.class, ProductCategoryMapperImpl.class})
class ProductCategoryRepositoryTest {
    @Autowired
    ProductCategoryRepository productCategoryRepository;

    @Autowired
    ProductSpecifications productSpecifications;

    @Autowired
    ProductCategoryMapper productCategoryMapper;

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

        FilteringProductCategoriesDto filteringProductCategoriesDto = productCategoryRepository
                .findUniqueFilteringProductCategories(productSpecification);

        List<ProductCategoryDto> expectedOriginCategories = List.of(new ProductCategoryDto("Китай", "china"));
        List<ProductCategoryDto> expectedBrandCategories = List.of(
                new ProductCategoryDto("CoComelon", "сoсomelon"));
        List<ProductCategoryDto> expectedAgeCategories = List.of(
                new ProductCategoryDto("від 1 до 3 років", "vid-1-do-3-rokiv"),
                new ProductCategoryDto("від 6 до 9 років", "vid-6-do-9-rokiv"));

        List<ProductCategoryDto> actualOriginCategories = filteringProductCategoriesDto.originCategories();
        List<ProductCategoryDto> actualBrandCategories = filteringProductCategoriesDto.brandCategories();
        List<ProductCategoryDto> actualAgeCategories = filteringProductCategoriesDto.ageCategories();

        assertThat(actualOriginCategories).isEqualTo(expectedOriginCategories);
        assertThat(actualBrandCategories).isEqualTo(expectedBrandCategories);
        assertThat(actualAgeCategories).isEqualTo(expectedAgeCategories);
    }
}