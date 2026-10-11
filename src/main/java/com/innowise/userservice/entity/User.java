package com.innowise.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PaymentCard> paymentCards = new ArrayList<>();

    public void addPaymentCard(PaymentCard paymentCard) {
        if (!paymentCards.contains(paymentCard)) {
            paymentCards.add(paymentCard);
            paymentCard.setUser(this);
        }
    }

    public void removePaymentCard(PaymentCard paymentCard) {
        paymentCards.remove(paymentCard);
        paymentCard.setUser(null);
    }

}
