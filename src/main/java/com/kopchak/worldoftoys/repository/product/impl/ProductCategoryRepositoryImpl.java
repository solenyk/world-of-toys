package com.kopchak.worldoftoys.repository.product.impl;

import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.exception.exception.CategoryException;
import com.kopchak.worldoftoys.mapper.product.ProductCategoryMapper;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.Product_;
import com.kopchak.worldoftoys.model.product.category.AgeCategory;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import com.kopchak.worldoftoys.model.product.category.ProductCategory_;
import com.kopchak.worldoftoys.repository.product.ProductCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {
    private final EntityManager entityManager;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public FilteringProductCategoriesDto findUniqueFilteringProductCategories(Specification<Product> spec) {
        return FilteringProductCategoriesDto
                .builder()
                .originCategories(findUniqueProductCategoryDtoList(spec, Product_.ORIGIN_CATEGORY))
                .brandCategories(findUniqueProductCategoryDtoList(spec, Product_.BRAND_CATEGORY))
                .ageCategories(findUniqueProductCategoryDtoList(spec, Product_.AGE_CATEGORIES))
                .build();
    }

    @Override
    public <T extends ProductCategory> T findById(Integer id, Class<T> productCategoryType)
            throws CategoryException {
        T category = entityManager.find(productCategoryType, id);
        if (category == null) {
            throw new CategoryException(String.format("%s with id: %d does not exist",
                    productCategoryType.getSimpleName(), id));
        }
        return category;
    }

    @Override
    public <T extends ProductCategory> Set<T> findAllCategories(Class<T> productCategoryType) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(productCategoryType);
        Root<T> root = criteriaQuery.from(productCategoryType);
        criteriaQuery.select(root);
        TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
        return new LinkedHashSet<>(query.getResultList());
    }

    @Override
    public <T extends ProductCategory> void deleteCategory(Class<T> productCategoryType, Integer id)
            throws CategoryException {
        if (containsProductsInCategory(productCategoryType, id)) {
            throw new CategoryException(String.format("It is not possible to delete a category with id: %d " +
                    "because it contains products.", id));
        }
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> criteriaQuery = criteriaBuilder.createCriteriaDelete(productCategoryType);
        Root<T> root = criteriaQuery.from(productCategoryType);
        criteriaQuery.where(criteriaBuilder.equal(root.get(ProductCategory_.ID), id));
        entityManager.createQuery(criteriaQuery).executeUpdate();
    }

    private List<ProductCategoryDto> findUniqueProductCategoryDtoList(Specification<Product> spec,
                                                                      String productCategoryAttribute) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductCategory> criteriaQuery = criteriaBuilder.createQuery(ProductCategory.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        if (productCategoryAttribute.equals(Product_.ORIGIN_CATEGORY) ||
                productCategoryAttribute.equals(Product_.BRAND_CATEGORY)) {
            criteriaQuery.select(root.get(productCategoryAttribute));
        } else if (productCategoryAttribute.equals(Product_.AGE_CATEGORIES)) {
            Join<Product, AgeCategory> join = root.join(productCategoryAttribute, JoinType.INNER);
            criteriaQuery.select(join);
        }

        criteriaQuery.distinct(true);
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(productCategoryAttribute).get(ProductCategory_.SLUG)));

        if (spec != null) {
            criteriaQuery.where(spec.toPredicate(root, criteriaQuery, criteriaBuilder));
        }

        TypedQuery<ProductCategory> query = entityManager.createQuery(criteriaQuery);
        List<ProductCategoryDto> productCategoryDtoList = productCategoryMapper.toProductCategoryDtoList(query.getResultList());
        log.info("Found {} unique product categories for category: {}", productCategoryDtoList.size(), productCategoryAttribute);
        return productCategoryDtoList;
    }

    private <T extends ProductCategory> boolean containsProductsInCategory(Class<T> productCategoryType, Integer id)
            throws CategoryException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(Product.class);
        Root<Product> root = criteriaQuery.from(Product.class);
        String joinField = categoryTypeToJoinField(productCategoryType);
        Join<Product, T> categoryJoin = root.join(joinField, JoinType.INNER);
        criteriaQuery.select(root).where(criteriaBuilder.equal(categoryJoin.get(ProductCategory_.ID), id));
        List<Product> products = entityManager.createQuery(criteriaQuery).getResultList();
        return !products.isEmpty();
    }

    private <T extends ProductCategory> String categoryTypeToJoinField(Class<T> productCategoryType)
            throws CategoryException {
        Map<Class<?>, String> categoryTypeToJoinField = new HashMap<>() {{
            put(Product_.brandCategory.getJavaType(), Product_.BRAND_CATEGORY);
            put(Product_.originCategory.getJavaType(), Product_.ORIGIN_CATEGORY);
            put(Product_.ageCategories.getJavaType(), Product_.AGE_CATEGORIES);
        }};
        if (categoryTypeToJoinField.containsKey(productCategoryType)) {
            return categoryTypeToJoinField.get(productCategoryType);
        }
        throw new CategoryException(String.format("Product category type: %s is incorrect",
                productCategoryType.getSimpleName()));
    }
}
