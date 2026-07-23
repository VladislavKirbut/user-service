package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    List<PaymentCard> findAllByUserId(Long userId);

    @Query(
            value = """
                    SELECT *
                    FROM payment_cards
                    WHERE active = true
                    """,
            nativeQuery = true
    )
    List<PaymentCard> findActiveCards();

    long countByUserIdAndActiveTrue(Long userId);

}
