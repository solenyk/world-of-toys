package com.kopchak.worldoftoys.model.product.category;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Name is mandatory")
    @Size(min = 3, max = 60, message = "Name must be up to 60 characters long")
    private String name;

    @Column(length = 80, nullable = false)
    @NotBlank(message = "Slug is mandatory")
    @Size(min = 3, max = 80, message = "Slug must be up to 80 characters long")
    private String slug;
}
