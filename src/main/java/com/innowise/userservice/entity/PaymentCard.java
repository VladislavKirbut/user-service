package com.innowise.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Table(name = "payment_cards")
public class PaymentCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 19)
    private String number;

    @Column(nullable = false, length = 100)
    private String holder;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

}
