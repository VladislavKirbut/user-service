package com.innowise.userservice.service;

import com.innowise.userservice.dto.request.CreatePaymentCardRequest;
import com.innowise.userservice.dto.request.UpdatePaymentCardRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardService {

    PaymentCardResponse create(Long userId, CreatePaymentCardRequest request);

    PaymentCardResponse getById(Long id, Long userId);

    List<PaymentCardResponse> getAllByUserId(Long userId, Boolean active);

    Page<PaymentCardResponse> getAll(String name, String surname, Boolean active, Pageable pageable);

    PaymentCardResponse update(Long id, Long userId, UpdatePaymentCardRequest request);

    void updateActiveStatus(Long id, Long userId, boolean active);

    void delete(Long id, Long userId);

}
