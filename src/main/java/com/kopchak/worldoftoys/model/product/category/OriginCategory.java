package com.kopchak.worldoftoys.model.product.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kopchak.worldoftoys.model.product.Product;
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
public class OriginCategory extends ProductCategory{
    @OneToMany(mappedBy = "originCategory")
    private Set<Product> products;
}
