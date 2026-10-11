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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment-cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping(value = "/me", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentCardResponse> createCurrentUserPaymentCard(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreatePaymentCardRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentCardService.create(userId, request));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentCardResponse> createPaymentCard(
            @RequestParam Long userId,
            @Valid @RequestBody CreatePaymentCardRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentCardService.create(userId, request));
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentCardResponse> findPaymentCard(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {

        return ResponseEntity.ok(paymentCardService.getById(id, userId));
    }

    @GetMapping(value = "/{id}/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentCardResponse> findCurrentUserPaymentCard(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {

        return ResponseEntity.ok(paymentCardService.getById(id, userId));
    }

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentCardResponse>> findCurrentUserPaymentCards(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Boolean active
    ) {

        return ResponseEntity.ok(paymentCardService.getAllByUserId(userId, active));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentCardResponse>> findPaymentCardsByUserId(
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean active
    ) {

        return ResponseEntity.ok(paymentCardService.getAllByUserId(userId, active));
    }

    @GetMapping(value = "/filtered", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<PaymentCardResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {

        return ResponseEntity.ok(paymentCardService.getAll(name, surname, active, pageable));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentCardResponse> update(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody UpdatePaymentCardRequest request
    ) {

        return ResponseEntity.ok(paymentCardService.update(id, userId, request));
    }

    @PutMapping(value = "/{id}/me", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentCardResponse> updateCurrentUserPaymentCard(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdatePaymentCardRequest request
    ) {

        return ResponseEntity.ok(paymentCardService.update(id, userId, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam boolean active
    ) {

        paymentCardService.updateActiveStatus(id, userId, active);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/me")
    public ResponseEntity<Void> updateCurrentUserActiveStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam boolean active
    ) {

        paymentCardService.updateActiveStatus(id, userId, active);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentCard(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {

        paymentCardService.delete(id, userId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/me")
    public ResponseEntity<Void> deleteCurrentUserPaymentCard(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {

        paymentCardService.delete(id, userId);

        return ResponseEntity.noContent().build();
    }

}
