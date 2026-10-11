package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.request.CreatePaymentCardRequest;
import com.innowise.userservice.dto.request.UpdatePaymentCardRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import com.innowise.userservice.entity.PaymentCard;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.*;

@Mapper(componentModel = ComponentModel.SPRING)
public interface PaymentCardMapper {

    PaymentCard toEntity(CreatePaymentCardRequest request);

    PaymentCardResponse toResponse(PaymentCard paymentCard);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdatePaymentCardRequest request,
                      @MappingTarget PaymentCard paymentCard);

}
