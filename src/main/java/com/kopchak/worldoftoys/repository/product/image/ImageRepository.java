package com.kopchak.worldoftoys.repository.product.image;

import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    Optional<Image> findByNameAndProduct_Id(String name, Integer productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.product.id = :productId AND i.name NOT IN :imageNames")
    void deleteImagesByProductIdNotInNames(@Param("productId") Integer productId, @Param("imageNames") Set<String> imageNames);
}
