package com.innowise.userservice.repository;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "paymentCards")
    Optional<User> findWithPaymentCardsById(Long id);

    @Modifying
    @Query("UPDATE User u SET u.active = :active WHERE u.id = :id")
    void updateActive(@Param("id") Long id, @Param("active") boolean active);

}
