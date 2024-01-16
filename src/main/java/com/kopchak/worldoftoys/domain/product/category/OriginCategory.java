package com.kopchak.worldoftoys.domain.product.category;

import com.kopchak.worldoftoys.domain.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class OriginCategory extends ProductCategory {
    @OneToMany(mappedBy = "originCategory")
    private Set<Product> products;
}
