package com.kopchak.worldoftoys.domain.order.recipient.number;

import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PhoneNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CountryCode countryCode;

    @Column(length = 2, nullable = false)
    @NotBlank(message = "Invalid operator code: code is mandatory")
    @Pattern(regexp = "\\d{2}", message = "Invalid operator code: code '${validatedValue}' format is invalid")
    private String operatorCode;

    @Column(length = 7, nullable = false)
    @NotBlank(message = "Invalid number: number is mandatory")
    @Pattern(regexp = "\\d{7}", message = "Invalid number: number '${validatedValue}' format is invalid")
    private String number;

    @OneToOne(mappedBy = "phoneNumber", fetch = FetchType.LAZY)
    private OrderRecipient orderRecipients;
}
