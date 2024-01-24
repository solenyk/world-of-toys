package com.kopchak.worldoftoys.repository.product.impl;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.Product_;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory_;
import com.kopchak.worldoftoys.exception.exception.category.DublicateCategoryNameException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryCreationException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryNotFoundException;
import com.kopchak.worldoftoys.repository.product.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CategoryRepositoryImpl implements CategoryRepository {
    private final EntityManager entityManager;

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

    @Override
    @Transactional
    public <T extends ProductCategory> void deleteCategory(Class<T> productCategoryType, Integer id)
            throws CategoryContainsProductsException {
        if (containsProductsInCategory(productCategoryType, id)) {
            throw new CategoryContainsProductsException(String.format("It is not possible to delete a category " +
                    "with id: %d because there are products in this category.", id));
        }
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> criteriaQuery = criteriaBuilder.createCriteriaDelete(productCategoryType);
        Root<T> root = criteriaQuery.from(productCategoryType);
        criteriaQuery.where(criteriaBuilder.equal(root.get(ProductCategory_.ID), id));
        entityManager.createQuery(criteriaQuery).executeUpdate();
    }

    @Override
    @Transactional
    public <T extends ProductCategory> void updateCategory(Class<T> categoryType, Integer id, String name)
            throws CategoryNotFoundException, DublicateCategoryNameException {
        if (isCategoryWithNameExists(categoryType, name)) {
            throw new DublicateCategoryNameException(String.format("Category with name: %s already exist", name));
        }
        T entityToUpdate = entityManager.find(categoryType, id);
        if (entityToUpdate == null) {
            throw new CategoryNotFoundException(String.format("Category with id: %d doesn't exist", id));
        }
        entityToUpdate.setName(name);
        entityManager.merge(entityToUpdate);
    }

    @Override
    @Transactional
    public <T extends ProductCategory> void createCategory(Class<T> categoryType, String name)
            throws DublicateCategoryNameException, CategoryCreationException {
        if (isCategoryWithNameExists(categoryType, name)) {
            throw new DublicateCategoryNameException(String.format("Category with name: %s already exist", name));
        }
        try {
            T newCategory = categoryType.getDeclaredConstructor().newInstance();
            newCategory.setName(name);
            entityManager.persist(newCategory);
        } catch (ReflectiveOperationException e) {
            throw new CategoryCreationException(e.getMessage());
        }
    }

    @Override
    public List<ProductCategory> findUniqueBrandCategoryList(Specification<Product> spec) {
        return findUniqueProductCategoryList(
                spec,
                root -> root.get(Product_.BRAND_CATEGORY)
        );
    }

    @Override
    public List<ProductCategory> findUniqueOriginCategoryList(Specification<Product> spec) {
        return findUniqueProductCategoryList(
                spec,
                root -> root.get(Product_.ORIGIN_CATEGORY)
        );
    }

    @Override
    public List<ProductCategory> findUniqueAgeCategoryList(Specification<Product> spec) {
        return findUniqueProductCategoryList(
                spec,
                root -> root.join(Product_.AGE_CATEGORIES, JoinType.INNER)
        );
    }

    private List<ProductCategory> findUniqueProductCategoryList(Specification<Product> spec,
                                                                Function<Root<Product>, Path<ProductCategory>>
                                                                        categoryAttributeExtractor) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductCategory> criteriaQuery = criteriaBuilder.createQuery(ProductCategory.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        Path<ProductCategory> categoryAttribute = categoryAttributeExtractor.apply(root);

        criteriaQuery.select(categoryAttribute);
        criteriaQuery.distinct(true);
        criteriaQuery.orderBy(criteriaBuilder.asc(categoryAttribute));

        if (spec != null) {
            criteriaQuery.where(spec.toPredicate(root, criteriaQuery, criteriaBuilder));
        }

        TypedQuery<ProductCategory> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

    private <T extends ProductCategory> boolean containsProductsInCategory(Class<T> categoryType, Integer id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(Product.class);
        Root<Product> root = criteriaQuery.from(Product.class);
        String joinField = categoryTypeToJoinField(categoryType);
        Join<Product, T> categoryJoin = root.join(joinField, JoinType.INNER);
        criteriaQuery.select(root).where(criteriaBuilder.equal(categoryJoin.get(ProductCategory_.ID), id));
        List<Product> products = entityManager.createQuery(criteriaQuery).getResultList();
        return !products.isEmpty();
    }

    private <T extends ProductCategory> String categoryTypeToJoinField(Class<T> productCategoryType) {
        Map<Class<?>, String> categoryTypeToJoinField = new HashMap<>() {{
            put(Product_.brandCategory.getJavaType(), Product_.BRAND_CATEGORY);
            put(Product_.originCategory.getJavaType(), Product_.ORIGIN_CATEGORY);
            put(Product_.ageCategories.getJavaType(), Product_.AGE_CATEGORIES);
        }};
        return categoryTypeToJoinField.get(productCategoryType);
    }

    private <T extends ProductCategory> boolean isCategoryWithNameExists(Class<T> productCategoryType, String name) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(productCategoryType);
        Root<T> root = criteriaQuery.from(productCategoryType);
        criteriaQuery.where(criteriaBuilder.equal(root.get(ProductCategory_.NAME), name));
        try {
            entityManager.createQuery(criteriaQuery).getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }
}
