package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.request.CreateUserRequest;
import com.innowise.userservice.dto.request.UpdateUserRequest;
import com.innowise.userservice.dto.response.UserDetailsResponse;
import com.innowise.userservice.dto.response.UserResponse;
import com.innowise.userservice.entity.User;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.*;

@Mapper(
        componentModel = ComponentModel.SPRING,
        uses = PaymentCardMapper.class
)
public interface UserMapper {

    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User user);

    UserDetailsResponse toDetailsResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(UpdateUserRequest updateUserRequest,
                    @MappingTarget User user);

}
