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

    UserResponse getByEmail(String email);

    UserResponse update(Long id, UpdateUserRequest request);

    Page<UserResponse> getAll(String name, String surname, Pageable pageable);

    void activate(Long id);

    void deactivate(Long id);

}
