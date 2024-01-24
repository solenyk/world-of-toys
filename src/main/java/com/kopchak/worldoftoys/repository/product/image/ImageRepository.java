package com.kopchak.worldoftoys.repository.product.image;

import com.kopchak.worldoftoys.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    Optional<Image> findByNameAndProduct_Id(String name, Integer productId);
}
