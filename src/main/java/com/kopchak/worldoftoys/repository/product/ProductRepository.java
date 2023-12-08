package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.model.product.Product;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    @NotNull
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = "mainImage")
    Page<Product> findAll(Specification<Product> spec, @NotNull Pageable pageable);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"images", "originCategory",
            "brandCategory", "ageCategories"})
    Optional<Product> findBySlug(String slug);

    @NotNull
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"mainImage", "images", "originCategory",
            "brandCategory", "ageCategories"})
    Optional<Product> findById(@NotNull Integer id);
}
