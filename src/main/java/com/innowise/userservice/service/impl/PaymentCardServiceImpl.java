package com.innowise.userservice.service.impl;

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
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.specification.PaymentCardSpecification;
import com.innowise.userservice.service.util.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private static final int MAX_CARDS_PER_USER = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.USERS, key = "#userId"),
        @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-true'"),
        @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-false'"),
        @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-null'")
    })
    public PaymentCardResponse create(Long userId, CreatePaymentCardRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (paymentCardRepository.countByUserId(user.getId()) >= MAX_CARDS_PER_USER) {
            throw new CardLimitExceededException(user.getId());
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(request);
        user.addPaymentCard(paymentCard);

        PaymentCard savedCard = paymentCardRepository.save(paymentCard);

        return paymentCardMapper.toResponse(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PAYMENT_CARDS, key = "#userId + '-' + #id")
    public PaymentCardResponse getById(Long id, Long userId) {

        PaymentCard paymentCard = paymentCardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        return paymentCardMapper.toResponse(paymentCard);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USER_CARDS, key = "#userId + '-' + #active")
    public List<PaymentCardResponse> getAllByUserId(Long userId, Boolean active) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        List<PaymentCard> paymentCards;

        if (active != null) {
            paymentCards = paymentCardRepository.findAllByUserIdAndActive(userId, active);
        } else {
            paymentCards = paymentCardRepository.findAllByUserId(userId);
        }

        return paymentCards.stream()
                .map(paymentCardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentCardResponse> getAll(String name, String surname, Boolean active, Pageable pageable) {

        Specification<PaymentCard> specification = Specification
                .where(PaymentCardSpecification.hasName(name))
                .and(PaymentCardSpecification.hasSurname(surname))
                .and(PaymentCardSpecification.isActive(active));

        return paymentCardRepository.findAll(specification, pageable)
                .map(paymentCardMapper::toResponse);
    }

    @Override
    @Transactional
    @Caching(
            put = {
                @CachePut(value = CacheNames.PAYMENT_CARDS, key = "#userId + '-' + #id")
            },
            evict = {
                @CacheEvict(value = CacheNames.USERS, key = "#userId"),
                @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-true'"),
                @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-false'"),
                @CacheEvict(value = CacheNames.USER_CARDS,key = "#userId + '-null'")
            })
    public PaymentCardResponse update(Long id, Long userId, UpdatePaymentCardRequest request) {

        PaymentCard paymentCard = paymentCardRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        paymentCardMapper.updateEntity(request, paymentCard);

        return paymentCardMapper.toResponse(paymentCard);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USERS, key = "#userId"),
            @CacheEvict(value = CacheNames.PAYMENT_CARDS, key = "#userId + '-' + #id"),
            @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-true'"),
            @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-false'"),
            @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-null'")
    })
    public void updateActiveStatus(Long id, Long userId, boolean active) {

        if (paymentCardRepository.findByIdAndUserId(id, userId).isEmpty()) {
            throw new PaymentCardNotFoundException(id);
        }

        paymentCardRepository.updateActiveStatus(id, userId, active);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USERS, key = "#userId"),
            @CacheEvict(value = CacheNames.PAYMENT_CARDS, key = "#userId + '-' + #id"),
            @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-true'"),
            @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-false'"),
            @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + '-null'")
    })
    public void delete(Long id, Long userId) {

        PaymentCard paymentCard = paymentCardRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        paymentCardRepository.delete(paymentCard);
    }

}
