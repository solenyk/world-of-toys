package com.kopchak.worldoftoys.repository.product.impl;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.Product_;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory_;
import com.kopchak.worldoftoys.repository.product.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CategoryRepositoryImpl implements CategoryRepository {
    private final EntityManager entityManager;

    public <T extends ProductCategory> Optional<T> findByIdAndType(Integer id, Class<T> categoryType) {
        T category = entityManager.find(categoryType, id);
        return Optional.ofNullable(category);
    }

    @Override
    public <T extends ProductCategory> Set<T> findAll(Class<T> categoryType) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(categoryType);
        Root<T> root = criteriaQuery.from(categoryType);
        criteriaQuery.select(root);
        TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
        return new LinkedHashSet<>(query.getResultList());
    }

    @Override
    @Transactional
    public <T extends ProductCategory> void deleteByIdAndType(Integer id, Class<T> categoryType) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> criteriaQuery = criteriaBuilder.createCriteriaDelete(categoryType);
        Root<T> root = criteriaQuery.from(categoryType);
        criteriaQuery.where(criteriaBuilder.equal(root.get(ProductCategory_.ID), id));
        entityManager.createQuery(criteriaQuery).executeUpdate();
    }

    @Override
    @Transactional
    public <T extends ProductCategory> void updateNameByIdAndType(Integer id, String name, Class<T> categoryType) {
        Optional<T> entityToUpdateOptional = findByIdAndType(id, categoryType);
        if (entityToUpdateOptional.isPresent()) {
            T entityToUpdate = entityToUpdateOptional.get();
            entityToUpdate.setName(name);
            entityManager.merge(entityToUpdate);
        }
    }

    @Override
    @Transactional
    public <T extends ProductCategory> void create(String name, Class<T> categoryType) throws ReflectiveOperationException {
        T newCategory = categoryType.getDeclaredConstructor().newInstance();
        newCategory.setName(name);
        entityManager.persist(newCategory);
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

    @Override
    public <T extends ProductCategory> boolean containsProductsInCategory(Integer id, Class<T> categoryType) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(Product.class);
        Root<Product> root = criteriaQuery.from(Product.class);
        String joinField = categoryTypeToJoinField(categoryType);
        if (joinField == null) {
            return false;
        }
        Join<Product, T> categoryJoin = root.join(joinField, JoinType.INNER);
        criteriaQuery.select(root).where(criteriaBuilder.equal(categoryJoin.get(ProductCategory_.ID), id));
        List<Product> products = entityManager.createQuery(criteriaQuery).getResultList();
        return !products.isEmpty();
    }

    @Override
    public <T extends ProductCategory> boolean isCategoryWithNameExists(String name, Class<T> categoryType) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(categoryType);
        Root<T> root = criteriaQuery.from(categoryType);
        criteriaQuery.where(criteriaBuilder.equal(root.get(ProductCategory_.NAME), name));
        try {
            entityManager.createQuery(criteriaQuery).getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
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

    private <T extends ProductCategory> String categoryTypeToJoinField(Class<T> productCategoryType) {
        Field[] fields = Product_.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(SingularAttribute.class) || field.getType().equals(SetAttribute.class)) {
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Class<?> actualType = (Class<?>) genericType.getActualTypeArguments()[1];
                if (actualType.equals(productCategoryType)) {
                    return field.getName();
                }
            }
        }
        return null;
    }
}
