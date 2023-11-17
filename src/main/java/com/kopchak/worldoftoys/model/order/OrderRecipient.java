package com.kopchak.worldoftoys.model.order;

import jakarta.persistence.*;
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
public class OrderRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 60, nullable = false)
    @NotBlank(message = "Invalid firstname: firstname is blank")
    @Size(min = 3, max = 60,
            message = "Invalid firstname: firstname '${validatedValue}' must be from {min} to {max} characters long")
    private String firstname;

    @Column(length = 60, nullable = false)
    @NotBlank(message = "Invalid lastname: lastname is blank")
    @Size(min = 3, max = 60,
            message = "Invalid lastname: lastname '${validatedValue}' must be from {min} to {max} characters long")
    private String lastname;

    @Column(length = 60, nullable = false)
    @NotBlank(message = "Invalid patronymic: patronymic is blank")
    @Size(min = 3, max = 60,
            message = "Invalid patronymic: patronymic '${validatedValue}' must be from {min} to {max} characters long")
    private String patronymic;

    @OneToOne
    @JoinColumn(name = "phone_id", referencedColumnName = "id", nullable = false)
    private PhoneNumber phoneNumber;

    @ManyToOne
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "orderRecipient")
    private Set<OrderDetails> orders;
}
