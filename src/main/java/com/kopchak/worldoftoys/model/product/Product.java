package com.kopchak.worldoftoys.model.product;

import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.category.AgeCategory;
import com.kopchak.worldoftoys.model.product.category.BrandCategory;
import com.kopchak.worldoftoys.model.product.category.OriginCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 60, nullable = false, unique = true)
    @NotBlank(message = "Name is mandatory")
    @Size(min = 3, max = 60, message = "Name must be up to 60 characters long")
    private String name;

    @Column(length = 80, nullable = false, unique = true)
    @NotBlank(message = "Slug is mandatory")
    @Size(min = 3, max = 80, message = "Slug must be up to 80 characters long")
    private String slug;

    @Column(length = 250, nullable = false)
    @NotBlank(message = "Description is mandatory")
    @Size(max = 500, message = "Description must be up to 250 characters long")
    private String description;

    @NotNull(message = "Price is mandatory")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotNull(message = "Product quantity is mandatory")
    @Min(value = 0, message = "Product quantity should not be less than 0")
    private BigInteger availableQuantity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private Image mainImage;

    @OneToMany(mappedBy = "product")
    private Set<Image> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_id", nullable = false)
    @NotNull(message = "Origin category is mandatory")
    private OriginCategory originCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    @NotNull(message = "Brand category is mandatory")
    private BrandCategory brandCategory;

    @ManyToMany
    @JoinTable(
            name = "product_age_category",
            joinColumns = @JoinColumn(name = "products_id"),
            inverseJoinColumns = @JoinColumn(name = "age_category_id"))
    @NotNull(message = "Age categories is mandatory")
    private Set<AgeCategory> ageCategories;
}
