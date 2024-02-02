package com.kopchak.worldoftoys.domain.order.recipient.address;

import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 16, nullable = false)
    @NotBlank(message = "Invalid region: region is mandatory")
    @Size(min = 4, max = 16,
            message = "Invalid region: region '${validatedValue}' must be between {min} and {max} characters long")
    private String region;

    @Column(length = 60, nullable = false)
    @NotBlank(message = "Invalid settlement: settlement is mandatory")
    @Size(min = 2, max = 60,
            message = "Invalid settlement: settlement '${validatedValue}' must be between {min} and {max} characters long")
    private String settlement;

    @Column(length = 80, nullable = false)
    @NotBlank(message = "Invalid street: street is mandatory")
    @Size(min = 2, max = 80,
            message = "Invalid street: street '${validatedValue}' must be between {min} and {max} characters long")
    private String street;

    @Column(nullable = false)
    @NotNull(message = "Invalid house: house is mandatory")
    @Min(value = 1, message = "Invalid house number: house number '${validatedValue}' should not be less than {status}")
    private Integer house;

    @Min(value = 1,
            message = "Invalid apartment number: apartment number '${validatedValue}' should not be less than {status}")
    private Integer apartment;

    @OneToMany(mappedBy = "address")
    private Set<OrderRecipient> orderRecipients = new LinkedHashSet<>();
}
