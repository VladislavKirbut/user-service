package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>,
        JpaSpecificationExecutor<PaymentCard> {

    @Query(
            value = """
            SELECT *
            FROM payment_cards
            WHERE user_id = :userId
            """,
            nativeQuery = true
    )
    List<PaymentCard> findAllByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);

    Optional<PaymentCard> findByIdAndUserId(Long id, Long userId);
    List<PaymentCard> findAllByUserIdAndActive(Long userId, Boolean active);

    @Modifying
    @Query("""
           UPDATE PaymentCard pc
           SET pc.active = :active
           WHERE pc.id = :id AND pc.user.id = :userId
           """)
    void updateActiveStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("active") boolean active);

}
