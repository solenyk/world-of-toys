package com.kopchak.worldoftoys.repository.product.impl;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.Product_;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory_;
import com.kopchak.worldoftoys.exception.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.InvalidCategoryTypeException;
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

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CategoryRepositoryImpl implements CategoryRepository {
    private final EntityManager entityManager;

    @Override
    public <T extends ProductCategory> T findById(Integer id, Class<T> productCategoryType)
            throws InvalidCategoryTypeException {
        T category = entityManager.find(productCategoryType, id);
        if (category == null) {
            throw new InvalidCategoryTypeException(String.format("%s with id: %d does not exist",
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
            throws InvalidCategoryTypeException, CategoryContainsProductsException {
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
            throws InvalidCategoryTypeException {
        if (isCategoryWithNameExists(categoryType, name)) {
            throw new InvalidCategoryTypeException(String.format("Category with name: %s already exist", name));
        }
        T entityToUpdate = entityManager.find(categoryType, id);
        if (entityToUpdate == null) {
            throw new InvalidCategoryTypeException(String.format("Category with id: %d doesn't exist", id));
        }
        entityToUpdate.setName(name);
        entityManager.merge(entityToUpdate);
    }

    @Override
    @Transactional
    public <T extends ProductCategory> void addCategory(Class<T> categoryType, String name) throws InvalidCategoryTypeException {
        if (isCategoryWithNameExists(categoryType, name)) {
            throw new InvalidCategoryTypeException(String.format("Category with name: %s already exist", name));
        }
        try {
            T newCategory = categoryType.getDeclaredConstructor().newInstance();
            newCategory.setName(name);
            entityManager.persist(newCategory);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new InvalidCategoryTypeException(String.format("Failed to save category with name: %s. " +
                    "Error: %s. Please try a different name or contact support.", name, e.getMessage()));
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

    private <T extends ProductCategory> boolean containsProductsInCategory(Class<T> categoryType, Integer id)
            throws InvalidCategoryTypeException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(Product.class);
        Root<Product> root = criteriaQuery.from(Product.class);
        String joinField = categoryTypeToJoinField(categoryType);
        Join<Product, T> categoryJoin = root.join(joinField, JoinType.INNER);
        criteriaQuery.select(root).where(criteriaBuilder.equal(categoryJoin.get(ProductCategory_.ID), id));
        List<Product> products = entityManager.createQuery(criteriaQuery).getResultList();
        return !products.isEmpty();
    }

    private <T extends ProductCategory> String categoryTypeToJoinField(Class<T> productCategoryType)
            throws InvalidCategoryTypeException {
        Map<Class<?>, String> categoryTypeToJoinField = new HashMap<>() {{
            put(Product_.brandCategory.getJavaType(), Product_.BRAND_CATEGORY);
            put(Product_.originCategory.getJavaType(), Product_.ORIGIN_CATEGORY);
            put(Product_.ageCategories.getJavaType(), Product_.AGE_CATEGORIES);
        }};
        if (categoryTypeToJoinField.containsKey(productCategoryType)) {
            return categoryTypeToJoinField.get(productCategoryType);
        }
        throw new InvalidCategoryTypeException(String.format("Product category type: %s is incorrect",
                productCategoryType.getSimpleName()));
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
