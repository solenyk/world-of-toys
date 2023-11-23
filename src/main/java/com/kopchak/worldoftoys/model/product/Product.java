package com.kopchak.worldoftoys.model.product;

import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.order.details.OrderDetails;
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
    @NotBlank(message = "Invalid name: name is mandatory")
    @Size(min = 3, max = 60,
            message = "Invalid name: name '${validatedValue}' must be between {min} and {max} characters long")
    private String name;

    @Column(length = 80, nullable = false, unique = true)
    @NotBlank(message = "Invalid slug: slug is mandatory")
    @Size(min = 3, max = 80,
            message = "Invalid slug: slug '${validatedValue}' must be between {min} and {max} characters long")
    private String slug;

    @Column(length = 250, nullable = false)
    @NotBlank(message = "Invalid description: description is mandatory")
    @Size(max = 500,
            message = "Invalid description: description '${validatedValue}' must be up to 250 characters long")
    private String description;

    @Column(nullable = false, scale = 2)
    @NotNull(message = "Invalid price: price is mandatory")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Invalid price: price '${formatter.format('%1$.2f', validatedValue)}' must not be greater than {value}")
    private BigDecimal price;

    @Column(nullable = false)
    @NotNull(message = "Invalid quantity: product quantity is mandatory")
    @Min(value = 0, message = "Invalid quantity: product quantity '${validatedValue}' must not be less than {value}")
    private BigInteger availableQuantity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private Image mainImage;

    @OneToMany(mappedBy = "product")
    private Set<Image> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_id", referencedColumnName = "id", nullable = false)
    private OriginCategory originCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", referencedColumnName = "id", nullable = false)
    private BrandCategory brandCategory;

    @ManyToMany
    @JoinTable(
            name = "product_age_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "age_category_id"))
    private Set<AgeCategory> ageCategories;

    @OneToMany(mappedBy = "product")
    private Set<OrderDetails> orderDetails;
}
