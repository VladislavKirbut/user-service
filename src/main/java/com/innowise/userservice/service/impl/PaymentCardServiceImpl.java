package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.request.CreatePaymentCardRequest;
import com.innowise.userservice.dto.request.UpdatePaymentCardRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.PaymentCardAlreadyExistsException;
import com.innowise.userservice.exception.PaymentCardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.util.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private static final int MAX_CARDS_PER_USER = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    private final CacheManager cacheManager;

    @Override
    @Transactional
    public PaymentCardResponse create(Long userId, CreatePaymentCardRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (paymentCardRepository.countByUserId(user.getId()) >= MAX_CARDS_PER_USER) {
            throw new CardLimitExceededException(user.getId());
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(request);

        user.addCard(paymentCard);

        try {
            PaymentCard savedCard = paymentCardRepository.save(paymentCard);

            evictUserCache(userId);

            return paymentCardMapper.toResponse(savedCard);
        } catch (DataIntegrityViolationException ex) {
            throw new PaymentCardAlreadyExistsException(request.number());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentCardResponse getById(Long id) {

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        return paymentCardMapper.toResponse(paymentCard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentCardResponse> getByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return paymentCardRepository.findAllByUserId(userId)
                .stream()
                .map(paymentCardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentCardResponse> getAll(Pageable pageable) {

        return paymentCardRepository.findAll(pageable)
                .map(paymentCardMapper::toResponse);
    }

    @Override
    @Transactional
    public PaymentCardResponse update(Long id, UpdatePaymentCardRequest request) {

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        paymentCardMapper.updateEntity(request, paymentCard);

        evictUserCache(paymentCard.getUser().getId());

        return paymentCardMapper.toResponse(paymentCard);
    }

    @Override
    @Transactional
    public void activate(Long id) {

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        paymentCard.setActive(true);

        evictUserCache(paymentCard.getUser().getId());
    }

    @Override
    @Transactional
    public void deactivate(Long id) {

        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));

        paymentCard.setActive(false);

        evictUserCache(paymentCard.getUser().getId());
    }

    private void evictUserCache(Long userId) {

        Objects.requireNonNull(cacheManager.getCache(CacheNames.USERS))
                .evict(userId);
    }

}
