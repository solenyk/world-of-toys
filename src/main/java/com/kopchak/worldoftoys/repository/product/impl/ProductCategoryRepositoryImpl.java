package com.kopchak.worldoftoys.repository.product.impl;

import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.mapper.ProductCategoryMapper;
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

import java.util.List;

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
