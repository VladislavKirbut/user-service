package com.innowise.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String surname;

    private LocalDate birthDate;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PaymentCard> paymentCards = new ArrayList<>();

    public void addCard(PaymentCard card) {

        if (!paymentCards.contains(card)) {
            paymentCards.add(card);
            card.setUser(this);
        }
    }

    public void removeCard(PaymentCard card) {
        paymentCards.remove(card);
        card.setUser(null);
    }

}
