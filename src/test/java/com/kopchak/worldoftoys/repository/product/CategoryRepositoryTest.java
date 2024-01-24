package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.exception.exception.category.CategoryAlreadyExistsException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryCreationException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryNotFoundException;
import com.kopchak.worldoftoys.mapper.product.CategoryMapperImpl;
import com.kopchak.worldoftoys.repository.product.impl.CategoryRepositoryImpl;
import com.kopchak.worldoftoys.repository.specifications.ProductSpecifications;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
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

    private Specification<Product> productSpecification;

    @BeforeEach
    public void setUp() {
        String productName = "Лялька";
        BigDecimal minPrice = BigDecimal.valueOf(600);
        BigDecimal maxPrice = BigDecimal.valueOf(850);
        List<String> originCategoriesSlugList = List.of("china");
        List<String> brandCategoriesSlugList = List.of("сoсomelon", "сurlimals", "devilon");
        List<String> ageCategoriesSlugList = List.of("do-1-roku", "vid-1-do-3-rokiv", "vid-6-do-9-rokiv");
        productSpecification = productSpecifications.filterByProductNamePriceAndCategories(
                productName, minPrice, maxPrice, originCategoriesSlugList, brandCategoriesSlugList,
                ageCategoriesSlugList, null);
    }

    @Test
    public void findById_ReturnsProductCategory() throws CategoryNotFoundException {
        Class<BrandCategory> productCategoryType = BrandCategory.class;
        Integer id = 1004;

        BrandCategory expectedBrandCategory = new BrandCategory();
        expectedBrandCategory.setId(id);
        expectedBrandCategory.setName("comelon");
        expectedBrandCategory.setSlug("сomelon");
        expectedBrandCategory.setProducts(new HashSet<>());

        BrandCategory actualBrandCategory = categoryRepository.findById(id, productCategoryType);

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

    @Test
    public void deleteCategory_CategoryWithoutProducts() throws Exception {
        Class<BrandCategory> productCategoryType = BrandCategory.class;
        Integer id = 5;

        categoryRepository.deleteCategory(productCategoryType, id);

        String categoryNotFoundExceptionMsg = String.format("%s with id: %d does not exist",
                productCategoryType.getSimpleName(), id);

        assertException(CategoryNotFoundException.class, categoryNotFoundExceptionMsg,
                () -> categoryRepository.findById(id, productCategoryType));
    }

    @Test
    public void deleteCategory_CategoryWithProducts_ThrowsCategoryContainsProductsException() {
        Class<BrandCategory> productCategoryType = BrandCategory.class;
        Integer id = 1001;

        String categoryContainsProductsExceptionMsg = String.format("It is not possible to delete a category " +
                "with id: %d because there are products in this category.", id);

        assertException(CategoryContainsProductsException.class, categoryContainsProductsExceptionMsg,
                () -> categoryRepository.deleteCategory(productCategoryType, id));
    }

    @Test
    public void updateCategory_NonExistingCategoryName() throws CategoryAlreadyExistsException, CategoryNotFoundException {
        Class<BrandCategory> productCategoryType = BrandCategory.class;
        Integer id = 1001;
        String newCategoryName = "new-name";

        categoryRepository.updateCategory(productCategoryType, id, newCategoryName);

        var updatedBrandCategory = categoryRepository.findById(id, productCategoryType);

        assertThat(updatedBrandCategory).isNotNull();
        assertThat(updatedBrandCategory).isInstanceOf(productCategoryType);
        assertThat(updatedBrandCategory.getName()).isEqualTo(newCategoryName);
    }

    @Test
    public void updateCategory_ExistingCategoryName_ThrowsCategoryAlreadyExistsException() {
        Class<BrandCategory> productCategoryType = BrandCategory.class;
        Integer id = 2;
        String existingCategoryName = "Devilon";

        String categoryAlreadyExistsExceptionMsg = String.format("Category with name: %s already exist",
                existingCategoryName);

        assertException(CategoryAlreadyExistsException.class, categoryAlreadyExistsExceptionMsg,
                () -> categoryRepository.updateCategory(productCategoryType, id, existingCategoryName));
    }

    @Test
    public void updateCategory_NonExistingCategoryId_ThrowsCategoryNotFoundException() {
        Class<BrandCategory> productCategoryType = BrandCategory.class;
        Integer id = 8;
        String newCategoryName = "new-name";

        String categoryNotFoundExceptionMsg = String.format("Category with id: %d doesn't exist", id);

        assertException(CategoryNotFoundException.class, categoryNotFoundExceptionMsg,
                () -> categoryRepository.updateCategory(productCategoryType, id, newCategoryName));
    }

    @Test
    public void createCategory_NonExistingCategoryName() throws CategoryAlreadyExistsException, CategoryNotFoundException, CategoryCreationException {
        Class<BrandCategory> productCategoryType = BrandCategory.class;
        Integer id = 1;
        String newCategoryName = "new-name";

        categoryRepository.createCategory(productCategoryType, newCategoryName);
        var createdBrandCategory = categoryRepository.findById(id, productCategoryType);

        assertThat(createdBrandCategory).isNotNull();
        assertThat(createdBrandCategory).isInstanceOf(productCategoryType);
        assertThat(createdBrandCategory.getName()).isEqualTo(newCategoryName);
    }

    @Test
    public void createCategory_ExistingCategoryName_ThrowsCategoryAlreadyExistsException() {
        Class<BrandCategory> productCategoryType = BrandCategory.class;
        String existingCategoryName = "Devilon";

        String categoryAlreadyExistsExceptionMsg = String.format("Category with name: %s already exist",
                existingCategoryName);

        assertException(CategoryAlreadyExistsException.class, categoryAlreadyExistsExceptionMsg,
                () -> categoryRepository.createCategory(productCategoryType, existingCategoryName));
    }

    @Test
    public void findUniqueBrandCategoryList_ReturnsListOfProductsCategory() {
        var brandCategoryList = categoryRepository.findUniqueBrandCategoryList(productSpecification);

        assertThat(brandCategoryList).isNotNull();
        assertThat(brandCategoryList).isNotEmpty();
        assertThat(brandCategoryList.size()).isEqualTo(1);
        assertThat(brandCategoryList.get(0).getId()).isEqualTo(1000);
    }

    @Test
    public void findUniqueOriginCategoryList_ReturnsListOfProductsCategory() {
        var originCategoryList = categoryRepository.findUniqueOriginCategoryList(productSpecification);

        assertThat(originCategoryList).isNotNull();
        assertThat(originCategoryList).isNotEmpty();
        assertThat(originCategoryList.size()).isEqualTo(1);
        assertThat(originCategoryList.get(0).getId()).isEqualTo(1000);
    }

    @Test
    public void findUniqueAgeCategoryList_ReturnsListOfProductsCategory() {
        var ageCategoryList = categoryRepository.findUniqueAgeCategoryList(productSpecification);

        assertThat(ageCategoryList).isNotNull();
        assertThat(ageCategoryList).isNotEmpty();
        assertThat(ageCategoryList.size()).isEqualTo(2);
        assertThat(ageCategoryList.get(0).getId()).isEqualTo(1001);
        assertThat(ageCategoryList.get(1).getId()).isEqualTo(1002);
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}