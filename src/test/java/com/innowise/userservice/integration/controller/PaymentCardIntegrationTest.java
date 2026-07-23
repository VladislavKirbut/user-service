package com.innowise.userservice.integration.controller;

import com.innowise.userservice.dto.request.CreatePaymentCardRequest;
import com.innowise.userservice.dto.request.UpdatePaymentCardRequest;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class PaymentCardIntegrationTest extends AbstractIntegrationTest {

    private User createUser() {

        return userRepository.save(
                User.builder()
                        .name("Ivan")
                        .surname("Ivanov")
                        .email("ivan@test.com")
                        .birthDate(LocalDate.of(1998, 10, 5))
                        .active(true)
                        .build()
        );
    }

    private PaymentCard createCard(User user) {

        return paymentCardRepository.save(
                PaymentCard.builder()
                        .number("1111111111111111")
                        .holder("IVAN IVANOV")
                        .expirationDate(LocalDate.of(2030, 10, 1))
                        .active(true)
                        .user(user)
                        .build()
        );
    }

    @Test
    void shouldCreatePaymentCardSuccessfully() throws Exception {

        User user = createUser();

        CreatePaymentCardRequest request = CreatePaymentCardRequest.builder()
                        .number("1111111111111111")
                        .holder("IVAN IVANOV")
                        .expirationDate(LocalDate.of(2030, 10, 1))
                        .build();

        mockMvc.perform(post("/api/v1/users/{userId}/cards", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("1111222233334444"))
                .andExpect(jsonPath("$.holder").value("IVAN IVANOV"));

        assertThat(paymentCardRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

        CreatePaymentCardRequest request = CreatePaymentCardRequest.builder()
                        .number("1111111111111111")
                        .holder("IVAN IVANOV")
                        .expirationDate(LocalDate.of(2030,10,1))
                        .build();

        mockMvc.perform(post("/api/v1/users/{userId}/cards", 1000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertThat(paymentCardRepository.count()).isZero();
    }

    @Test
    void shouldReturnPaymentCardById() throws Exception {

        User user = createUser();
        PaymentCard card = createCard(user);

        mockMvc.perform(get("/api/v1/cards/{id}", card.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(card.getId()))
                .andExpect(jsonPath("$.number").value(card.getNumber()));

    }

    @Test
    void shouldReturnCardsByUserId() throws Exception {

        User user = createUser();

        createCard(user);

        mockMvc.perform(get("/api/v1/users/{userId}/cards", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].holder").value("IVAN IVANOV"));
    }

    @Test
    void shouldReturnAllCardsWithPagination() throws Exception {

        User user = createUser();

        createCard(user);

        mockMvc.perform(get("/api/v1/cards")
                        .param("page","0")
                        .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

    }

    @Test
    void shouldUpdatePaymentCardSuccessfully() throws Exception {

        User user = createUser();

        PaymentCard card = createCard(user);

        UpdatePaymentCardRequest request = UpdatePaymentCardRequest.builder()
                        .holder("NEW HOLDER")
                        .expirationDate(LocalDate.of(2035,1,1))
                        .build();

        mockMvc.perform(put("/api/v1/cards/{id}", card.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("NEW HOLDER"));

        PaymentCard updated = paymentCardRepository.findById(card.getId()).orElseThrow();

        assertThat(updated.getHolder()).isEqualTo("NEW HOLDER");
    }

    @Test
    void shouldDeactivateCardSuccessfully() throws Exception {

        User user = createUser();

        PaymentCard card = createCard(user);

        mockMvc.perform(
                        patch("/api/v1/cards/{id}/deactivate", card.getId())
                )
                .andExpect(status().isNoContent());

        PaymentCard updated = paymentCardRepository.findById(card.getId()).orElseThrow();

        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void shouldActivateCardSuccessfully() throws Exception {

        User user = createUser();

        PaymentCard card = createCard(user);

        card.setActive(false);
        paymentCardRepository.save(card);

        mockMvc.perform(
                        patch("/api/v1/cards/{id}/activate", card.getId())
                )
                .andExpect(status().isNoContent());

        PaymentCard updated = paymentCardRepository.findById(card.getId()).orElseThrow();

        assertThat(updated.getActive()).isTrue();

    }

    @Test
    void shouldNotAllowMoreThanFiveCardsForUser() throws Exception {

        User user = createUser();

        for (int i = 0; i < 5; i++) {

            PaymentCard card = PaymentCard.builder()
                    .number("11112222333344" + i)
                    .holder("USER")
                    .expirationDate(LocalDate.of(2030,1,1))
                    .active(true)
                    .user(user)
                    .build();

            paymentCardRepository.save(card);
        }

        CreatePaymentCardRequest request = CreatePaymentCardRequest.builder()
                        .number("9999888877776666")
                        .holder("NEW")
                        .expirationDate(LocalDate.of(2030,1,1))
                        .build();

        mockMvc.perform(post("/api/v1/users/{id}/cards", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        assertThat(paymentCardRepository.count()).isEqualTo(5);
    }
}