package com.kopchak.worldoftoys.repository.product.specifications.impl;

import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.AgeCategory;
import com.kopchak.worldoftoys.model.product.category.OriginCategory;
import com.kopchak.worldoftoys.repository.product.specifications.ProductSpecifications;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductSpecificationsImpl implements ProductSpecifications {
    @Override
    public Specification<Product> hasProductName(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }
    public Specification<Product> hasPriceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public Specification<Product> hasPriceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }
    public Specification<Product> hasProductInOriginCategory(List<String> originCategories) {
        return (root, query, criteriaBuilder) -> {
            Join<Product, OriginCategory> productJoin = root.join("originCategory", JoinType.INNER);
            return productJoin.get("slug").in(originCategories);
        };
    }

    public Specification<Product> hasProductInBrandCategory(List<String> brandCategories) {
        return (root, query, criteriaBuilder) -> {
            Join<Product, OriginCategory> productJoin = root.join("brandCategory", JoinType.INNER);
            return productJoin.get("slug").in(brandCategories);
        };
    }

    public Specification<Product> hasProductInAgeCategory(List<String> ageCategories) {
            return (root, query, criteriaBuilder) -> {
            SetJoin<Product, AgeCategory> productJoin = root.joinSet("ageCategories", JoinType.INNER);
            Expression<String> ageCategorySlugExpression = productJoin.get("slug");

                Predicate[] predicates = ageCategories.stream()
                        .map(slug -> criteriaBuilder.equal(ageCategorySlugExpression, slug))
                        .toArray(Predicate[]::new);

                return criteriaBuilder.or(predicates);
        };
    }
}
