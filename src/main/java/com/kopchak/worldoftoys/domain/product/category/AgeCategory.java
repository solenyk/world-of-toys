package com.kopchak.worldoftoys.domain.product.category;

import com.kopchak.worldoftoys.domain.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class AgeCategory extends ProductCategory {
    @ManyToMany(mappedBy = "ageCategories")
    private Set<Product> products;
}
