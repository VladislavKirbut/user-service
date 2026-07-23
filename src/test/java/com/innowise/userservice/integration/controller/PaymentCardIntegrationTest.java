package com.innowise.userservice.integration.controller;

import com.innowise.userservice.dto.request.CreatePaymentCardRequest;
import com.innowise.userservice.dto.request.UpdatePaymentCardRequest;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static com.innowise.userservice.testdata.PaymentCardTestConstants.CARD_HOLDER;
import static com.innowise.userservice.testdata.PaymentCardTestConstants.CARD_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentCardIntegrationTest extends AbstractIntegrationTest {

    private MockHttpServletRequestBuilder asAdmin(
            MockHttpServletRequestBuilder request
    ) {
        return request.header(
                "Authorization",
                "Bearer " + createAdminToken(1L)
        );
    }

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
                        .number(CARD_NUMBER)
                        .holder(CARD_HOLDER)
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
                .number(CARD_NUMBER)
                .holder(CARD_HOLDER)
                .expirationDate(LocalDate.of(2030, 10, 1))
                .build();

        mockMvc.perform(asAdmin(post("/api/v1/payment-cards")
                        .param("userId", user.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.number").value(request.number()))
                .andExpect(jsonPath("$.holder").value(request.holder()));

        assertThat(paymentCardRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        CreatePaymentCardRequest request = CreatePaymentCardRequest.builder()
                .number(CARD_NUMBER)
                .holder(CARD_HOLDER)
                .expirationDate(LocalDate.of(2030, 10, 1))
                .build();

        mockMvc.perform(asAdmin(post("/api/v1/payment-cards")
                        .param("userId", "1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isNotFound());

        assertThat(paymentCardRepository.count()).isZero();
    }

    @Test
    void shouldReturnPaymentCardById() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user);

        mockMvc.perform(asAdmin(get("/api/v1/payment-cards/{id}", card.getId())
                        .param("userId", user.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(card.getId()))
                .andExpect(jsonPath("$.number").value(card.getNumber()))
                .andExpect(jsonPath("$.holder").value(card.getHolder()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnCardsByUserId() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user);

        mockMvc.perform(asAdmin(get("/api/v1/payment-cards")
                        .param("userId", user.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value(card.getNumber()))
                .andExpect(jsonPath("$[0].holder").value(card.getHolder()));
    }

    @Test
    void shouldReturnCardsFilteredByActiveStatus() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user);

        mockMvc.perform(asAdmin(get("/api/v1/payment-cards")
                        .param("userId", user.getId().toString())
                        .param("active", "true")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(card.getId()));
    }

    @Test
    void shouldReturnAllCardsWithPagination() throws Exception {
        User user = createUser();
        createCard(user);

        mockMvc.perform(asAdmin(get("/api/v1/payment-cards/filtered")
                        .param("page", "0")
                        .param("size", "10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldUpdatePaymentCardSuccessfully() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user);

        UpdatePaymentCardRequest request = UpdatePaymentCardRequest.builder()
                .number(CARD_NUMBER)
                .holder("NEW HOLDER")
                .expirationDate(LocalDate.of(2035, 1, 1))
                .build();

        mockMvc.perform(asAdmin(put("/api/v1/payment-cards/{id}", card.getId())
                        .param("userId", user.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value(request.holder()));

        PaymentCard updated = paymentCardRepository.findById(card.getId())
                .orElseThrow();

        assertThat(updated.getHolder()).isEqualTo(request.holder());
        assertThat(updated.getExpirationDate()).isEqualTo(request.expirationDate());
    }

    @Test
    void shouldDeactivateCardSuccessfully() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user);

        mockMvc.perform(asAdmin(patch("/api/v1/payment-cards/{id}", card.getId())
                        .param("userId", user.getId().toString())
                        .param("active", "false")))
                .andExpect(status().isNoContent());

        PaymentCard updated = paymentCardRepository.findById(card.getId())
                .orElseThrow();

        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void shouldActivateCardSuccessfully() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user);

        card.setActive(false);
        paymentCardRepository.save(card);

        mockMvc.perform(asAdmin(patch("/api/v1/payment-cards/{id}", card.getId())
                        .param("userId", user.getId().toString())
                        .param("active", "true")))
                .andExpect(status().isNoContent());

        PaymentCard updated = paymentCardRepository.findById(card.getId())
                .orElseThrow();

        assertThat(updated.getActive()).isTrue();
    }

    @Test
    void shouldDeletePaymentCardSuccessfully() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user);

        mockMvc.perform(asAdmin(delete("/api/v1/payment-cards/{id}", card.getId())
                        .param("userId", user.getId().toString())))
                .andExpect(status().isNoContent());

        assertThat(paymentCardRepository.findById(card.getId())).isEmpty();
    }

    @Test
    void shouldNotAllowMoreThanFiveCardsForUser() throws Exception {
        User user = createUser();

        for (int i = 0; i < 5; i++) {
            PaymentCard card = PaymentCard.builder()
                    .number("111122223333444" + i)
                    .holder("USER")
                    .expirationDate(LocalDate.of(2030, 1, 1))
                    .active(true)
                    .user(user)
                    .build();

            paymentCardRepository.save(card);
        }

        CreatePaymentCardRequest request = CreatePaymentCardRequest.builder()
                .number(CARD_NUMBER)
                .holder("NEW")
                .expirationDate(LocalDate.of(2030, 1, 1))
                .build();

        mockMvc.perform(asAdmin(post("/api/v1/payment-cards")
                        .param("userId", user.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isConflict());

        assertThat(paymentCardRepository.count()).isEqualTo(5);
    }
}