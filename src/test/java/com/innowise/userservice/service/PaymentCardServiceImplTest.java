package com.innowise.userservice.service;

import com.innowise.userservice.dto.request.CreatePaymentCardRequest;
import com.innowise.userservice.dto.request.UpdatePaymentCardRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.PaymentCardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static com.innowise.userservice.testdata.PaymentCardTestConstants.*;
import static com.innowise.userservice.testdata.UserTestConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    private User user;

    private CreatePaymentCardRequest createRequest;

    private UpdatePaymentCardRequest updateRequest;

    private PaymentCard paymentCard;

    private PaymentCardResponse paymentCardResponse;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .surname(USER_SURNAME)
                .email(EMAIL)
                .birthDate(USER_BIRTH_DATE)
                .active(ACTIVE)
                .build();

        paymentCard = PaymentCard.builder()
                .id(CARD_ID)
                .number(CARD_NUMBER)
                .holder(CARD_HOLDER)
                .expirationDate(CARD_EXPIRATION_DATE)
                .active(CARD_ACTIVE)
                .user(user)
                .build();

        createRequest = CreatePaymentCardRequest.builder()
                .number(CARD_NUMBER)
                .holder(CARD_HOLDER)
                .expirationDate(CARD_EXPIRATION_DATE)
                .build();

        updateRequest = UpdatePaymentCardRequest.builder()
                .number(CARD_NUMBER)
                .holder(CARD_HOLDER)
                .expirationDate(CARD_EXPIRATION_DATE)
                .build();

        paymentCardResponse = PaymentCardResponse.builder()
                .id(CARD_ID)
                .number(CARD_NUMBER)
                .holder(CARD_HOLDER)
                .expirationDate(CARD_EXPIRATION_DATE)
                .active(CARD_ACTIVE)
                .build();
    }

    @Nested
    class CreateTests {

        @Test
        void shouldCreateCardSuccessfully() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(paymentCardRepository.countByUserId(USER_ID)).thenReturn(MIN_COUNT_CARD);
            when(paymentCardMapper.toEntity(createRequest)).thenReturn(paymentCard);
            when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
            when(paymentCardMapper.toResponse(paymentCard)).thenReturn(paymentCardResponse);

            PaymentCardResponse result = paymentCardService.create(USER_ID, createRequest);

            assertThat(result).isEqualTo(paymentCardResponse);

            assertThat(paymentCard.getUser()).isEqualTo(user);
            assertThat(user.getPaymentCards()).contains(paymentCard);

            verify(userRepository).findById(USER_ID);
            verify(paymentCardRepository).countByUserId(USER_ID);
            verify(paymentCardMapper).toEntity(createRequest);
            verify(paymentCardRepository).save(paymentCard);
            verify(paymentCardMapper).toResponse(paymentCard);
            verifyNoMoreInteractions(userRepository, paymentCardRepository, paymentCardMapper);
        }

        @Test
        void shouldThrowExceptionWhenUserNotFound() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.create(USER_ID, createRequest))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with id: " + USER_ID + " not found");

            verify(userRepository).findById(USER_ID);
            verifyNoInteractions(paymentCardRepository, paymentCardMapper);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        void shouldThrowExceptionWhenCardLimitExceeded() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(paymentCardRepository.countByUserId(USER_ID)).thenReturn(MAX_COUNT_CARD);

            assertThatThrownBy(() -> paymentCardService.create(USER_ID, createRequest))
                    .isInstanceOf(CardLimitExceededException.class)
                    .hasMessage("User with id: " + USER_ID
                            + " already has the maximum number of payment cards");

            verify(userRepository).findById(USER_ID);
            verify(paymentCardRepository).countByUserId(USER_ID);
            verifyNoInteractions(paymentCardMapper);
            verifyNoMoreInteractions(userRepository, paymentCardRepository);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void shouldReturnPaymentCardById() {

            when(paymentCardRepository.findByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.of(paymentCard));
            when(paymentCardMapper.toResponse(paymentCard))
                    .thenReturn(paymentCardResponse);

            PaymentCardResponse result = paymentCardService.getById(CARD_ID, USER_ID);

            assertThat(result).isEqualTo(paymentCardResponse);

            verify(paymentCardRepository).findByIdAndUserId(CARD_ID, USER_ID);
            verify(paymentCardMapper).toResponse(paymentCard);
            verifyNoMoreInteractions(paymentCardRepository, paymentCardMapper);
            verifyNoInteractions(userRepository);
        }

        @Test
        void shouldThrowExceptionWhenPaymentCardNotFound() {

            when(paymentCardRepository.findByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.getById(CARD_ID, USER_ID))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card with id: " + CARD_ID + " not found");

            verify(paymentCardRepository).findByIdAndUserId(CARD_ID, USER_ID);
            verifyNoInteractions(paymentCardMapper, userRepository);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }

    @Nested
    class GetAllByUserIdTests {

        @Test
        void shouldReturnAllPaymentCardsByUserId() {

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(paymentCardRepository.findAllByUserId(USER_ID))
                    .thenReturn(List.of(paymentCard));
            when(paymentCardMapper.toResponse(paymentCard))
                    .thenReturn(paymentCardResponse);

            List<PaymentCardResponse> result =
                    paymentCardService.getAllByUserId(USER_ID, null);

            assertThat(result)
                    .hasSize(1)
                    .containsExactly(paymentCardResponse);

            verify(userRepository).existsById(USER_ID);
            verify(paymentCardRepository).findAllByUserId(USER_ID);
            verify(paymentCardMapper).toResponse(paymentCard);
            verify(paymentCardRepository, never())
                    .findAllByUserIdAndActive(anyLong(), anyBoolean());
            verifyNoMoreInteractions(userRepository, paymentCardRepository, paymentCardMapper);
        }

        @Test
        void shouldReturnPaymentCardsFilteredByActiveStatus() {

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(paymentCardRepository.findAllByUserIdAndActive(USER_ID, true))
                    .thenReturn(List.of(paymentCard));
            when(paymentCardMapper.toResponse(paymentCard))
                    .thenReturn(paymentCardResponse);

            List<PaymentCardResponse> result =
                    paymentCardService.getAllByUserId(USER_ID, true);

            assertThat(result)
                    .hasSize(1)
                    .containsExactly(paymentCardResponse);

            verify(userRepository).existsById(USER_ID);
            verify(paymentCardRepository).findAllByUserIdAndActive(USER_ID, true);
            verify(paymentCardMapper).toResponse(paymentCard);
            verify(paymentCardRepository, never()).findAllByUserId(anyLong());
            verifyNoMoreInteractions(userRepository, paymentCardRepository, paymentCardMapper);
        }

        @Test
        void shouldReturnEmptyListWhenUserHasNoCards() {

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(paymentCardRepository.findAllByUserId(USER_ID))
                    .thenReturn(List.of());

            List<PaymentCardResponse> result =
                    paymentCardService.getAllByUserId(USER_ID, null);

            assertThat(result).isEmpty();

            verify(userRepository).existsById(USER_ID);
            verify(paymentCardRepository).findAllByUserId(USER_ID);
            verifyNoInteractions(paymentCardMapper);
            verifyNoMoreInteractions(userRepository, paymentCardRepository);
        }

        @Test
        void shouldThrowUserNotFoundException() {

            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> paymentCardService.getAllByUserId(USER_ID, null))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with id: " + USER_ID + " not found");

            verify(userRepository).existsById(USER_ID);
            verifyNoInteractions(paymentCardRepository, paymentCardMapper);
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    class GetAllTests {

        @Test
        void shouldReturnPageOfPaymentCards() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<PaymentCard> page = new PageImpl<>(List.of(paymentCard));

            when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(paymentCardMapper.toResponse(paymentCard))
                    .thenReturn(paymentCardResponse);

            Page<PaymentCardResponse> result =
                    paymentCardService.getAll(USER_NAME, USER_SURNAME, CARD_ACTIVE, pageable);

            assertThat(result)
                    .hasSize(1)
                    .containsExactly(paymentCardResponse);

            verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
            verify(paymentCardMapper).toResponse(paymentCard);
            verifyNoInteractions(userRepository);
            verifyNoMoreInteractions(paymentCardRepository, paymentCardMapper);
        }

        @Test
        void shouldReturnEmptyPageWhenNoPaymentCardsFound() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<PaymentCard> emptyPage = Page.empty(pageable);

            when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            Page<PaymentCardResponse> result =
                    paymentCardService.getAll(null, null, null, pageable);

            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
            verifyNoInteractions(paymentCardMapper, userRepository);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdatePaymentCardSuccessfully() {

            when(paymentCardRepository.findByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.of(paymentCard));
            when(paymentCardMapper.toResponse(paymentCard))
                    .thenReturn(paymentCardResponse);

            PaymentCardResponse result =
                    paymentCardService.update(CARD_ID, USER_ID, updateRequest);

            assertThat(result).isEqualTo(paymentCardResponse);

            verify(paymentCardRepository).findByIdAndUserId(CARD_ID, USER_ID);
            verify(paymentCardMapper).updateEntity(updateRequest, paymentCard);
            verify(paymentCardMapper).toResponse(paymentCard);
            verifyNoInteractions(userRepository);
            verifyNoMoreInteractions(paymentCardRepository, paymentCardMapper);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonExistingCard() {

            when(paymentCardRepository.findByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    paymentCardService.update(CARD_ID, USER_ID, updateRequest))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card with id: " + CARD_ID + " not found");

            verify(paymentCardRepository).findByIdAndUserId(CARD_ID, USER_ID);
            verifyNoInteractions(userRepository, paymentCardMapper);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }

    @Nested
    class UpdateActiveStatusTests {

        @Test
        void shouldUpdatePaymentCardActiveStatusSuccessfully() {

            when(paymentCardRepository.findByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.of(paymentCard));

            paymentCardService.updateActiveStatus(CARD_ID, USER_ID, false);

            verify(paymentCardRepository).findByIdAndUserId(CARD_ID, USER_ID);
            verify(paymentCardRepository).updateActiveStatus(CARD_ID, USER_ID, false);
            verifyNoInteractions(userRepository, paymentCardMapper);
            verifyNoMoreInteractions(paymentCardRepository);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingStatusOfNonExistingCard() {

            when(paymentCardRepository.findByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    paymentCardService.updateActiveStatus(CARD_ID, USER_ID, false))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card with id: " + CARD_ID + " not found");

            verify(paymentCardRepository).findByIdAndUserId(CARD_ID, USER_ID);
            verify(paymentCardRepository, never())
                    .updateActiveStatus(anyLong(), anyLong(), anyBoolean());
            verifyNoInteractions(userRepository, paymentCardMapper);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void shouldDeletePaymentCardSuccessfully() {

            when(paymentCardRepository.findByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.of(paymentCard));

            paymentCardService.delete(CARD_ID, USER_ID);

            verify(paymentCardRepository).findByIdAndUserId(CARD_ID, USER_ID);
            verify(paymentCardRepository).delete(paymentCard);
            verifyNoInteractions(userRepository, paymentCardMapper);
            verifyNoMoreInteractions(paymentCardRepository);
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonExistingCard() {

            when(paymentCardRepository.findByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.delete(CARD_ID, USER_ID))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card with id: " + CARD_ID + " not found");

            verify(paymentCardRepository).findByIdAndUserId(CARD_ID, USER_ID);
            verify(paymentCardRepository, never()).delete(any(PaymentCard.class));
            verifyNoInteractions(userRepository, paymentCardMapper);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }
}
