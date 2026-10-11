package com.innowise.userservice.specification;

import com.innowise.userservice.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserSpecification {

    public static Specification<User> hasName(String name) {

        return (root, query, cb) -> {

            if (name == null || name.isBlank()) return cb.conjunction();

            return cb.like(
                cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, cb) -> {

            if (surname == null || surname.isBlank()) return cb.conjunction();

            return cb.like(
                    cb.lower(root.get("surname")),
                    "%" + surname.toLowerCase() + "%"
            );
        };
    }

}
