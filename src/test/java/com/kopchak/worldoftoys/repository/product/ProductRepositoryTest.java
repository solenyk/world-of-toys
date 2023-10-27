package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.specifications.ProductSpecifications;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("integrationtest")
@Import(ProductSpecificationsImpl.class)
class ProductRepositoryTest {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductSpecifications productSpecifications;

    @Test
    public void findBySlug_ExistentProductSlug_ReturnsOptionalOfProduct() {
        String existentProductSlug = "lyalka-darynka";

        Optional<Product> returnedProductOptional = productRepository.findBySlug(existentProductSlug);

        assertThat(returnedProductOptional).isNotNull();
        assertThat(returnedProductOptional).isPresent();
    }

    @Test
    public void findBySlug_NonExistentProductSlug_ReturnsEmptyOptional() {
        String nonExistentProductSlug = "non-existent-product-slug";

        Optional<Product> returnedProductOptional = productRepository.findBySlug(nonExistentProductSlug);

        assertThat(returnedProductOptional).isNotNull();
        assertThat(returnedProductOptional).isEmpty();
    }

    @Test
    public void findAll_AllCriteriaSpecification_ReturnsPageOfProduct() {
        String productName = "Лялька";
        BigDecimal minPrice = BigDecimal.valueOf(350);
        BigDecimal maxPrice = BigDecimal.valueOf(1000);
        List<String> originCategories = List.of("china", "ukraine");
        List<String> brandCategories = List.of("сurlimals", "devilon");
        List<String> ageCategories = List.of("do-1-roku", "vid-1-do-3-rokiv");
        String priceAscSortOrder = "asc";

        Specification<Product> productSpecification = productSpecifications.filterByAllCriteria(productName,
                minPrice, maxPrice, originCategories, brandCategories, ageCategories, priceAscSortOrder);
        Pageable pageable = PageRequest.of(0, 5);

        Page<Product> returnedProductPage = productRepository.findAll(productSpecification, pageable);

        int expectedAmountOfPages = 1;
        int expectedAmountOfElements = 2;
        int expectedContentSize = 2;
        String expectedFirstProductName = "Лялька Русалочка";
        String expectedSecondProductName = "Лялька Даринка";

        assertThat(returnedProductPage).isNotNull();
        assertThat(returnedProductPage.getTotalElements()).isEqualTo(expectedAmountOfElements);
        assertThat(returnedProductPage.getTotalPages()).isEqualTo(expectedAmountOfPages);
        assertThat(returnedProductPage.getContent().size()).isEqualTo(expectedContentSize);
        assertThat(returnedProductPage.getContent().get(0).getName()).isEqualTo(expectedFirstProductName);
        assertThat(returnedProductPage.getContent().get(1).getName()).isEqualTo(expectedSecondProductName);
    }

    @Test
    public void findAll_CriteriaSpecificationWithDescPriceSortOrder_ReturnsPageOfProduct() {
        String priceDescSortOrder = "desc";

        Specification<Product> productSpecification = productSpecifications.filterByAllCriteria(null,
                null, null, null, null, null, priceDescSortOrder);
        Pageable pageable = PageRequest.of(0, 3);

        Page<Product> returnedProductPage = productRepository.findAll(productSpecification, pageable);

        int expectedAmountOfPages = 2;
        int expectedAmountOfElements = 4;
        int expectedContentSize = 3;
        String expectedFirstProductName = "Лялька Даринка";
        String expectedLastProductName = "Лялька Русалочка";

        assertThat(returnedProductPage).isNotNull();
        assertThat(returnedProductPage.getTotalElements()).isEqualTo(expectedAmountOfElements);
        assertThat(returnedProductPage.getTotalPages()).isEqualTo(expectedAmountOfPages);
        assertThat(returnedProductPage.getContent().size()).isEqualTo(expectedContentSize);
        assertThat(returnedProductPage.getContent().get(0).getName()).isEqualTo(expectedFirstProductName);
        assertThat(returnedProductPage.getContent().get(2).getName()).isEqualTo(expectedLastProductName);
    }
}