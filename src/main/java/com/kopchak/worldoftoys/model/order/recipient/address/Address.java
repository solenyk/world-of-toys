package com.kopchak.worldoftoys.model.order.recipient.address;

import com.kopchak.worldoftoys.model.order.recipient.OrderRecipient;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
    @Min(value = 1, message = "Invalid house number: house number '${validatedValue}' should not be less than {value}")
    private int house;

    @Column(nullable = false)
    @Min(value = 1,
            message = "Invalid apartment number: apartment number '${validatedValue}' should not be less than {value}")
    private int apartment;

    @OneToMany(mappedBy = "address")
    private Set<OrderRecipient> orderRecipients;
}
