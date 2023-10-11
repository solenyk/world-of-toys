package com.kopchak.worldoftoys.model.image;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    @NotBlank(message = "Invalid name: name is blank")
    private String name;

    @Column(length = 10, nullable = false)
    @NotBlank(message = "Invalid type: type is blank")
    private String type;

    @Lob
    @Column(nullable = false, length = 1000)
    @NotEmpty(message = "Invalid image: image is empty")
    private byte[] image;
}
