package com.innowise.userservice.controller;

import com.innowise.userservice.dto.request.CreateUserRequest;
import com.innowise.userservice.dto.request.UpdateUserRequest;
import com.innowise.userservice.dto.response.UserDetailsResponse;
import com.innowise.userservice.dto.response.UserResponse;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {

        UserResponse response = userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDetailsResponse> findCurrentUser(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(userService.getById(userId));
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDetailsResponse> findUser(@PathVariable Long id) {

        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserResponse>> findUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable
    ) {

        return ResponseEntity.ok(userService.getAll(name, surname, pageable));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        return ResponseEntity.ok(userService.update(id, request));
    }

    @PutMapping(value = "/me", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        return ResponseEntity.ok(userService.update(userId, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {

        userService.updateActiveStatus(id, active);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {

        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
