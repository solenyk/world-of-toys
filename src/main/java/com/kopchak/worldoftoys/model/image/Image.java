package com.kopchak.worldoftoys.model.image;

import com.kopchak.worldoftoys.model.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "Invalid name: name is blank")
    private String name;

    @Column(length = 10, nullable = false)
    @NotBlank(message = "Invalid type: type is blank")
    private String type;

    @Lob
    @Column(nullable = false, length = 1_000_000, columnDefinition = "BLOB")
    @NotEmpty(message = "Invalid image: image is empty")
    private byte[] image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
