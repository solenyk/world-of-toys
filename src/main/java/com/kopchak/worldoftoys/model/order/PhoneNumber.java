package com.kopchak.worldoftoys.model.order;

import com.kopchak.worldoftoys.model.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class PhoneNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CountryCode countryCode;

    @Column(length = 2, nullable = false)
    @NotBlank(message = "Invalid operator code: code is mandatory")
    @Pattern(regexp = "\\d{2}", message = "Invalid operator code: code format is invalid")
    private String operatorCode;

    @Column(length = 7, nullable = false)
    @NotBlank(message = "Invalid number: number is mandatory")
    @Pattern(regexp = "\\d{7}", message = "Invalid number: number format is invalid")
    private String number;

    @OneToOne(mappedBy = "phoneNumber")
    private OrderRecipient orderRecipients;
}
