package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.request.CreateUserRequest;
import com.innowise.userservice.dto.request.UpdateUserRequest;
import com.innowise.userservice.dto.response.UserDetailsResponse;
import com.innowise.userservice.dto.response.UserResponse;
import com.innowise.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.*;

@Mapper(
        componentModel = ComponentModel.SPRING,
        uses = PaymentCardMapper.class
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "paymentCards", ignore = true)
    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User user);

    UserDetailsResponse toDetailsResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "paymentCards", ignore = true)
    void updateUser(UpdateUserRequest updateUserRequest,
                    @MappingTarget User user);

}
