package com.innowise.userservice.service;

import com.innowise.userservice.dto.request.CreateUserRequest;
import com.innowise.userservice.dto.request.UpdateUserRequest;
import com.innowise.userservice.dto.response.UserDetailsResponse;
import com.innowise.userservice.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserDetailsResponse getById(Long id);

    Page<UserResponse> getAll(String name, String surname, Pageable pageable);

    UserResponse update(Long id, UpdateUserRequest request);

    void updateActiveStatus(Long id, boolean active);

    void delete(Long id);

}
