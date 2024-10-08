package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.mapper.product.CategoryMapperImpl;
import com.kopchak.worldoftoys.repository.product.impl.CategoryRepositoryImpl;
import com.kopchak.worldoftoys.repository.specifications.ProductSpecifications;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@Import({ProductSpecificationsImpl.class, CategoryRepositoryImpl.class, CategoryMapperImpl.class})
class CategoryRepositoryTest {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductSpecifications productSpecifications;

    private final static Class<BrandCategory> BRAND_CATEGORY_CLASS = BrandCategory.class;

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
    public void findByIdAndType_BrandCategoryType_ReturnsOptionalOfBrandCategory() {
        Integer id = 1004;

        BrandCategory expectedBrandCategory = new BrandCategory();
        expectedBrandCategory.setId(id);
        expectedBrandCategory.setName("comelon");
        expectedBrandCategory.setSlug("сomelon");
        expectedBrandCategory.setProducts(new HashSet<>());

        Optional<BrandCategory> actualBrandCategory = categoryRepository.findByIdAndType(id, BRAND_CATEGORY_CLASS);

        assertThat(actualBrandCategory).isNotNull();
        assertThat(actualBrandCategory).isNotEmpty();
        assertThat(actualBrandCategory.get()).isInstanceOf(BRAND_CATEGORY_CLASS);
        assertThat(actualBrandCategory.get()).usingRecursiveComparison().isEqualTo(expectedBrandCategory);
    }

    @Test
    public void findAll_ReturnsSetOfOriginCategories() {
        Class<OriginCategory> categoryClass = OriginCategory.class;
        int expectedOriginCategoriesSetSize = 2;

        Set<OriginCategory> actualOriginCategories = categoryRepository.findAll(categoryClass);

        assertThat(actualOriginCategories).isNotNull();
        assertThat(actualOriginCategories).isNotEmpty();
        assertThat(actualOriginCategories.size()).isEqualTo(expectedOriginCategoriesSetSize);
    }

    @Test
    public void deleteByIdAndType() {
        Integer id = 1004;

        categoryRepository.deleteByIdAndType(id, BRAND_CATEGORY_CLASS);

        Optional<BrandCategory> brandCategory = categoryRepository.findByIdAndType(id, BRAND_CATEGORY_CLASS);

        assertThat(brandCategory).isNotNull();
        assertThat(brandCategory).isEmpty();
    }

    @Test
    public void updateNameByIdAndType() {
        Integer id = 1001;
        String newCategoryName = "new-name";

        categoryRepository.updateNameByIdAndType(id, newCategoryName, BRAND_CATEGORY_CLASS);

        Optional<BrandCategory> updatedBrandCategory = categoryRepository.findByIdAndType(id, BRAND_CATEGORY_CLASS);

        assertThat(updatedBrandCategory).isNotNull();
        assertThat(updatedBrandCategory).isNotEmpty();
        assertThat(updatedBrandCategory.get()).isInstanceOf(BRAND_CATEGORY_CLASS);
        assertThat(updatedBrandCategory.get().getName()).isEqualTo(newCategoryName);
    }

    @Test
    public void create() throws ReflectiveOperationException {
        Integer id = 1;
        String newCategoryName = "new-name";

        categoryRepository.create(newCategoryName, BRAND_CATEGORY_CLASS);
        Optional<BrandCategory> createdBrandCategory = categoryRepository.findByIdAndType(id, BRAND_CATEGORY_CLASS);

        assertThat(createdBrandCategory).isNotNull();
        assertThat(createdBrandCategory).isNotEmpty();
        assertThat(createdBrandCategory.get()).isInstanceOf(BRAND_CATEGORY_CLASS);
        assertThat(createdBrandCategory.get().getName()).isEqualTo(newCategoryName);
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

    @Test
    public void containsProductsInCategory_CategoryWithProducts_ReturnsTrue() {
        Integer id = 1000;

        boolean containsProductsInCategory = categoryRepository.containsProductsInCategory(id, BRAND_CATEGORY_CLASS);

        assertThat(containsProductsInCategory).isTrue();
    }

    @Test
    public void containsProductsInCategory_CategoryWithoutProducts_ReturnsFalse() {
        Class<TestProductCategory> categoryClass = TestProductCategory.class;
        Integer id = 1000;

        boolean containsProductsInCategory = categoryRepository.containsProductsInCategory(id, categoryClass);

        assertThat(containsProductsInCategory).isFalse();
    }

    @Test
    public void isCategoryWithNameExists_ExistentCategoryName_ReturnsTrue() {
        String name = "Disney";

        boolean isCategoryWithNameExists = categoryRepository.isCategoryWithNameExists(name, BRAND_CATEGORY_CLASS);

        assertThat(isCategoryWithNameExists).isTrue();
    }

    @Test
    public void isCategoryWithNameExists_NonExistentCategoryName_ReturnsFalse() {
        String name = "non-existent-brand-name";

        boolean isCategoryWithNameExists = categoryRepository.isCategoryWithNameExists(name, BRAND_CATEGORY_CLASS);

        assertThat(isCategoryWithNameExists).isFalse();
    }

    private static class TestProductCategory extends ProductCategory {
    }
}