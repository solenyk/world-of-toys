package com.kopchak.worldoftoys.model.product.category;

import com.github.slugify.Slugify;
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

    @Column(length = 60, nullable = false, unique = true)
    @NotBlank(message = "Invalid name: name is mandatory")
    @Size(min = 3, max = 60, message = "Invalid name: name must be up to 60 characters long")
    private String name;

    @Column(length = 80, nullable = false, unique = true)
    @NotBlank(message = "Invalid slug: slug is mandatory")
    @Size(min = 3, max = 80, message = "Invalid slug: slug must be up to 80 characters long")
    private String slug;

    @PrePersist
    @PreUpdate
    private void setSlug() {
        System.out.println("call");
        final Slugify slg = Slugify.builder().transliterator(true).build();
        this.slug = slg.slugify(this.name);
    }
}
