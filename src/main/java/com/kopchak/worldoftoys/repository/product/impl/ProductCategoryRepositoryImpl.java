package com.kopchak.worldoftoys.repository.product.impl;

import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.exception.exception.CategoryNotFoundException;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
            throws CategoryNotFoundException {
        T category = entityManager.find(productCategoryType, id);
        if (category == null) {
            throw new CategoryNotFoundException(String.format("%s with id: %d does not exist",
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
}
