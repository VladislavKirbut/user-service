package com.innowise.userservice.controller;

import com.innowise.userservice.dto.request.CreatePaymentCardRequest;
import com.innowise.userservice.dto.request.UpdatePaymentCardRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<PaymentCardResponse> create(
            @PathVariable Long userId,
            @Valid @RequestBody CreatePaymentCardRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentCardService.create(userId, request));
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<PaymentCardResponse> getById(@PathVariable Long id) {

        return ResponseEntity.ok(paymentCardService.getById(id));
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<PaymentCardResponse>> getAll(Pageable pageable) {

        return ResponseEntity.ok(paymentCardService.getAll(pageable));
    }

    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<PaymentCardResponse>> getByUserId(@PathVariable Long userId) {

        return ResponseEntity.ok(paymentCardService.getByUserId(userId));
    }

    @PutMapping("/cards/{id}")
    public ResponseEntity<PaymentCardResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentCardRequest request
    ) {

        return ResponseEntity.ok(paymentCardService.update(id, request));
    }

    @PatchMapping("/cards/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {

        paymentCardService.activate(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cards/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {

        paymentCardService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

}
