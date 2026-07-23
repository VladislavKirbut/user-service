package com.innowise.userservice.service;

import com.innowise.userservice.dto.request.CreatePaymentCardRequest;
import com.innowise.userservice.dto.request.UpdatePaymentCardRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardService {

    PaymentCardResponse create(Long userId, CreatePaymentCardRequest request);

    PaymentCardResponse getById(Long id);

    List<PaymentCardResponse> getByUserId(Long userId);

    Page<PaymentCardResponse> getAll(Pageable pageable);

    PaymentCardResponse update(Long id, UpdatePaymentCardRequest request);

    void activate(Long id);

    void deactivate(Long id);

}
