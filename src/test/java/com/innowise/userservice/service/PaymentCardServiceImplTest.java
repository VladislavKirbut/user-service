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

import java.util.List;
import java.util.Optional;

import static com.innowise.userservice.testdata.PaymentCardTestConstants.*;
import static com.innowise.userservice.testdata.UserTestConstants.*;
import static com.innowise.userservice.testdata.UserTestConstants.ACTIVE;
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
                .email(OLD_EMAIL)
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
            when(paymentCardRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(MIN_COUNT_CARD);
            when(paymentCardMapper.toEntity(createRequest)).thenReturn(paymentCard);
            when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
            when(paymentCardMapper.toResponse(paymentCard)).thenReturn(paymentCardResponse);

            PaymentCardResponse result = paymentCardService.create(USER_ID, createRequest);

            assertThat(result).isEqualTo(paymentCardResponse);

            verify(userRepository).findById(USER_ID);
            verify(paymentCardRepository).countByUserIdAndActiveTrue(USER_ID);
            verify(paymentCardMapper).toEntity(createRequest);
            verify(paymentCardRepository).save(paymentCard);
            verify(paymentCardMapper).toResponse(paymentCard);

            assertThat(paymentCard.getUser()).isEqualTo(user);
            assertThat(user.getPaymentCards()).contains(paymentCard);

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
            when(paymentCardRepository.countByUserIdAndActiveTrue(USER_ID)).thenReturn(MAX_COUNT_CARD);

            assertThatThrownBy(() -> paymentCardService.create(USER_ID, createRequest))
                    .isInstanceOf(CardLimitExceededException.class)
                    .hasMessage("User with id: " + USER_ID + " already has the maximum number of payment cards");

            verify(userRepository).findById(USER_ID);
            verify(paymentCardRepository).countByUserIdAndActiveTrue(USER_ID);
            verifyNoInteractions(paymentCardMapper);
            verifyNoMoreInteractions(userRepository, paymentCardRepository);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void shouldReturnPaymentCardById() {

            when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));
            when(paymentCardMapper.toResponse(paymentCard)).thenReturn(paymentCardResponse);

            PaymentCardResponse result = paymentCardService.getById(CARD_ID);

            assertThat(result).isEqualTo(paymentCardResponse);

            verify(paymentCardRepository).findById(CARD_ID);
            verify(paymentCardMapper).toResponse(paymentCard);
            verifyNoMoreInteractions(paymentCardRepository, paymentCardMapper);
            verifyNoInteractions(userRepository);
        }

        @Test
        void shouldThrowExceptionWhenPaymentCardNotFound() {

            when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.getById(CARD_ID))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card with id " + CARD_ID + " not found");

            verify(paymentCardRepository).findById(CARD_ID);
            verifyNoInteractions(paymentCardMapper, userRepository);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }

    @Nested
    class GetByUserIdTests {

        @Test
        void shouldReturnPaymentCardsByUserId() {

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(paymentCardRepository.findAllByUserId(USER_ID)).thenReturn(List.of(paymentCard));
            when(paymentCardMapper.toResponse(paymentCard)).thenReturn(paymentCardResponse);

            List<PaymentCardResponse> result = paymentCardService.getByUserId(USER_ID);

            assertThat(result)
                    .hasSize(1)
                    .containsExactly(paymentCardResponse);

            verify(userRepository).existsById(USER_ID);
            verify(paymentCardRepository).findAllByUserId(USER_ID);
            verify(paymentCardMapper).toResponse(paymentCard);
            verifyNoMoreInteractions(userRepository, paymentCardRepository, paymentCardMapper);
        }

        @Test
        void shouldReturnEmptyListWhenUserHasNoCards() {

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(paymentCardRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

            List<PaymentCardResponse> result = paymentCardService.getByUserId(USER_ID);

            assertThat(result).isEmpty();

            verify(userRepository).existsById(USER_ID);
            verify(paymentCardRepository).findAllByUserId(USER_ID);
            verifyNoInteractions(paymentCardMapper);
            verifyNoMoreInteractions(userRepository, paymentCardRepository);
        }

        @Test
        void shouldThrowUserNotFoundException() {

            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> paymentCardService.getByUserId(USER_ID))
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

            when(paymentCardRepository.findAll(pageable)).thenReturn(page);
            when(paymentCardMapper.toResponse(paymentCard)).thenReturn(paymentCardResponse);

            Page<PaymentCardResponse> result = paymentCardService.getAll(pageable);

            assertThat(result).hasSize(1);
            assertThat(result.getContent()).containsExactly(paymentCardResponse);

            verify(paymentCardRepository).findAll(pageable);
            verify(paymentCardMapper).toResponse(paymentCard);
            verifyNoInteractions(userRepository);
            verifyNoMoreInteractions(paymentCardRepository, paymentCardMapper);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdatePaymentCardSuccessfully() {

            when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));
            when(paymentCardMapper.toResponse(paymentCard)).thenReturn(paymentCardResponse);

            PaymentCardResponse result = paymentCardService.update(CARD_ID, updateRequest);

            assertThat(result).isEqualTo(paymentCardResponse);

            verify(paymentCardRepository).findById(CARD_ID);
            verify(paymentCardMapper).updateEntity(updateRequest, paymentCard);
            verify(paymentCardMapper).toResponse(paymentCard);
            verifyNoInteractions(userRepository);
            verifyNoMoreInteractions(paymentCardRepository, paymentCardMapper);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonExistingCard() {

            when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.update(CARD_ID, updateRequest))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card with id " + CARD_ID + " not found");

            verify(paymentCardRepository).findById(CARD_ID);
            verifyNoInteractions(userRepository, paymentCardMapper);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }

    @Nested
    class ActivateTests {

        @Test
        void shouldActivatePaymentCard() {

            paymentCard.setActive(CARD_INACTIVE);

            when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));

            paymentCardService.activate(CARD_ID);

            assertThat(paymentCard.getActive()).isTrue();

            verify(paymentCardRepository).findById(CARD_ID);
            verifyNoInteractions(userRepository, paymentCardMapper);
            verifyNoMoreInteractions(paymentCardRepository);
        }

        @Test
        void shouldThrowExceptionWhenActivatingNonExistingCard() {

            when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.activate(CARD_ID))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card with id " + CARD_ID + " not found");

            verify(paymentCardRepository).findById(CARD_ID);
            verifyNoInteractions(paymentCardMapper, userRepository);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }

    @Nested
    class DeactivateTests {

        @Test
        void shouldDeactivatePaymentCard() {

            when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.of(paymentCard));

            paymentCardService.deactivate(CARD_ID);

            assertThat(paymentCard.getActive()).isFalse();

            verify(paymentCardRepository).findById(CARD_ID);
            verifyNoInteractions(paymentCardMapper, userRepository);
            verifyNoMoreInteractions(paymentCardRepository);
        }

        @Test
        void shouldThrowExceptionWhenDeactivatingNonExistingCard() {

            when(paymentCardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.deactivate(CARD_ID))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card with id " + CARD_ID + " not found");

            verify(paymentCardRepository).findById(CARD_ID);
            verifyNoInteractions(userRepository, paymentCardMapper);
            verifyNoMoreInteractions(paymentCardRepository);
        }
    }
}
