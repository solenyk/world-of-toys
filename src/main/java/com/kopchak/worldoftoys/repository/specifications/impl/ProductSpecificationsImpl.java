package com.kopchak.worldoftoys.repository.specifications.impl;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.Product_;
import com.kopchak.worldoftoys.domain.product.category.*;
import com.kopchak.worldoftoys.repository.specifications.ProductSpecifications;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Component
public class ProductSpecificationsImpl implements ProductSpecifications {
    @Override
    public Specification<Product> filterByProductNamePriceAndCategories(String productName, BigDecimal minPrice,
                                                                        BigDecimal maxPrice,
                                                                        List<String> originCategories,
                                                                        List<String> brandCategories,
                                                                        List<String> ageCategories,
                                                                        String availability) {
        return Specification
                .where(hasProductName(productName))
                .and(isAvailable(availability))
                .and(hasPriceGreaterThanOrEqualTo(minPrice))
                .and(hasPriceLessThanOrEqualTo(maxPrice))
                .and(hasProductInOriginCategory(originCategories))
                .and(hasProductInBrandCategory(brandCategories))
                .and(hasProductInAgeCategory(ageCategories));
    }

    @Override
    public Specification<Product> filterByAllCriteria(String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                      List<String> originCategories, List<String> brandCategories,
                                                      List<String> ageCategories, String priceSortOrder,
                                                      String availability) {
        return filterByProductNamePriceAndCategories(productName, minPrice, maxPrice, originCategories, brandCategories,
                ageCategories, availability)
                .and(sortByPrice(priceSortOrder));
    }

    private Specification<Product> hasProductName(String productName) {
        return (root, query, criteriaBuilder) -> {
            if (productName == null) {
                return criteriaBuilder.conjunction();
            }
            String pattern = String.format("%%%s%%", productName);
            return criteriaBuilder.like(criteriaBuilder.lower(root.get(Product_.name)), pattern.toLowerCase());
        };
    }

    private Specification<Product> hasPriceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null || maxPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(Product_.price), maxPrice);
        };
    }

    private Specification<Product> hasPriceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null || minPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get(Product_.price), minPrice);
        };
    }

    private Specification<Product> isAvailable(String availability) {
        return (root, query, criteriaBuilder) -> {
            if ("available".equalsIgnoreCase(availability)) {
                Predicate isAvailablePredicate = criteriaBuilder.isTrue(root.get(Product_.isAvailable));
                Predicate quantityGreaterThanZeroPredicate =
                        criteriaBuilder.greaterThan(root.get(Product_.availableQuantity), BigInteger.ZERO);
                return criteriaBuilder.and(isAvailablePredicate, quantityGreaterThanZeroPredicate);
            } else if ("unavailable".equalsIgnoreCase(availability)) {
                Predicate isUnavailablePredicate = criteriaBuilder.isFalse(root.get(Product_.isAvailable));
                Predicate quantityEqualZeroPredicate =
                        criteriaBuilder.equal(root.get(Product_.availableQuantity), BigInteger.ZERO);
                return criteriaBuilder.or(isUnavailablePredicate, quantityEqualZeroPredicate);
            }
            return query.getRestriction();
        };
    }

    private Specification<Product> hasProductInOriginCategory(List<String> originCategories) {
        return hasProductInProductCategory(OriginCategory.class,
                Product_.originCategory, originCategories);
    }

    private Specification<Product> hasProductInBrandCategory(List<String> brandCategories) {
        return hasProductInProductCategory(BrandCategory.class,
                Product_.brandCategory, brandCategories);
    }

    private Specification<Product> hasProductInAgeCategory(List<String> ageCategories) {
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

    private Specification<Product> sortByPrice(String sortOrder) {
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
