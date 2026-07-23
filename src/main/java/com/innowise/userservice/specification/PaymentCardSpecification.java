package com.innowise.userservice.specification;

import com.innowise.userservice.entity.PaymentCard;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentCardSpecification {

    public static Specification<PaymentCard> hasName(String name) {

        return (root, query, cb) -> {

            if (name == null || name.isBlank()) return cb.conjunction();

            return cb.like(
                    cb.lower(root.get("user").get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PaymentCard> hasSurname(String surname) {

        return (root, query, cb) -> {

            if (surname == null || surname.isBlank()) return cb.conjunction();

            return cb.like(
                    cb.lower(root.get("user").get("surname")),
                    "%" + surname.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PaymentCard> isActive(Boolean active) {

        return (root, query, cb) -> {

            if (active == null) return cb.conjunction();

            return cb.equal(root.get("active"), active);
        };
    }

}
