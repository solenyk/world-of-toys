package com.kopchak.worldoftoys.repository.product.specifications.impl;

import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.AgeCategory;
import com.kopchak.worldoftoys.model.product.category.BrandCategory;
import com.kopchak.worldoftoys.model.product.category.OriginCategory;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
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
        return hasProductInProductCategory(OriginCategory.class,
                "originCategory", originCategories);
    }

    public Specification<Product> hasProductInBrandCategory(List<String> brandCategories) {
        return hasProductInProductCategory(BrandCategory.class,
                "brandCategory", brandCategories);
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

    private Specification<Product> hasProductInProductCategory(Class<? extends ProductCategory> productCategoryType,
                                                              String joinFieldName,
                                                              List<String> productCategories) {
        return (root, query, criteriaBuilder) -> {
            Join<Product, ?> productJoin = root.join(joinFieldName, JoinType.INNER);
            Predicate typePredicate = criteriaBuilder.equal(productJoin.type(), productCategoryType);
            Predicate brandPredicate = productJoin.get("slug").in(productCategories);
            return criteriaBuilder.and(typePredicate, brandPredicate);
        };
    }
}
