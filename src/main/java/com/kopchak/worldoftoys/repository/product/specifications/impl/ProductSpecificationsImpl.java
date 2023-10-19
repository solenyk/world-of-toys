package com.kopchak.worldoftoys.repository.product.specifications.impl;

import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.Product_;
import com.kopchak.worldoftoys.model.product.category.*;
import com.kopchak.worldoftoys.repository.product.specifications.ProductSpecifications;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductSpecificationsImpl implements ProductSpecifications {
    @Override
    public Specification<Product> hasProductName(String productName) {
        return (root, query, criteriaBuilder) -> {
            if (productName == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get(Product_.name), "%" + productName + "%");
        };
    }

    public Specification<Product> hasPriceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null || maxPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(Product_.price), maxPrice);
        };
    }

    public Specification<Product> hasPriceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null || minPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get(Product_.price), minPrice);
        };
    }

    public Specification<Product> hasProductInOriginCategory(List<String> originCategories) {
        return hasProductInProductCategory(OriginCategory.class,
                Product_.originCategory, originCategories);
    }

    public Specification<Product> hasProductInBrandCategory(List<String> brandCategories) {
        return hasProductInProductCategory(BrandCategory.class,
                Product_.brandCategory, brandCategories);
    }

    public Specification<Product> hasProductInAgeCategory(List<String> ageCategories) {
        return (root, query, criteriaBuilder) -> {
            if (ageCategories == null || ageCategories.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            SetJoin<Product, AgeCategory> productAgeCategorySetJoin = root.joinSet(Product_.AGE_CATEGORIES, JoinType.INNER);
            Expression<String> ageCategorySlugExpression = productAgeCategorySetJoin.get(AgeCategory_.slug);

            Predicate[] predicates = ageCategories.stream()
                    .map(slug -> criteriaBuilder.equal(ageCategorySlugExpression, slug))
                    .toArray(Predicate[]::new);

            return criteriaBuilder.or(predicates);
        };
    }

    public Specification<Product> sortByPrice(String sortOrder) {
        return (root, query, criteriaBuilder) -> {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                query.orderBy(criteriaBuilder.asc(root.get(Product_.price)));
            } else if ("desc".equalsIgnoreCase(sortOrder)) {
                query.orderBy(criteriaBuilder.desc(root.get(Product_.price)));
            }
            return query.getRestriction();
        };
    }

    private Specification<Product> hasProductInProductCategory(Class<? extends ProductCategory> productCategoryType,
                                                               SingularAttribute<Product, ? extends ProductCategory>
                                                                       productCategoryAttribute,
                                                               List<String> productCategories) {
        return (root, query, criteriaBuilder) -> {
            if (productCategories == null || productCategories.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, ?> productCategoryJoin = root.join(productCategoryAttribute, JoinType.INNER);
            Predicate typePredicate = criteriaBuilder.equal(productCategoryJoin.type(), productCategoryType);
            Predicate categoryPredicate = productCategoryJoin.get(ProductCategory_.SLUG).in(productCategories);
            return criteriaBuilder.and(typePredicate, categoryPredicate);
        };
    }
}
