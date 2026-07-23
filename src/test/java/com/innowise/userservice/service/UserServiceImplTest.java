package com.innowise.userservice.service;

import com.innowise.userservice.dto.request.CreateUserRequest;
import com.innowise.userservice.dto.request.UpdateUserRequest;
import com.innowise.userservice.dto.response.UserDetailsResponse;
import com.innowise.userservice.dto.response.UserResponse;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.EmailAlreadyExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.UserServiceImpl;
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

import static com.innowise.userservice.testdata.UserTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    private CreateUserRequest createRequest;

    private UpdateUserRequest updateRequest;

    private UserResponse userResponse;

    private UserDetailsResponse userDetailsResponse;

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

        createRequest = CreateUserRequest.builder()
                .name(USER_NAME)
                .surname(USER_SURNAME)
                .email(OLD_EMAIL)
                .birthDate(USER_BIRTH_DATE)
                .build();

        updateRequest = UpdateUserRequest.builder()
                .name(USER_NAME)
                .surname(USER_SURNAME)
                .birthDate(USER_BIRTH_DATE)
                .email(NEW_EMAIL)
                .build();

        userResponse = UserResponse.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .surname(USER_SURNAME)
                .email(OLD_EMAIL)
                .active(ACTIVE)
                .build();

        userDetailsResponse = UserDetailsResponse.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .surname(USER_SURNAME)
                .email(OLD_EMAIL)
                .active(ACTIVE)
                .birthDate(USER_BIRTH_DATE)
                .build();
    }


    @Nested
    class CreateTests {

        @Test
        void shouldCreateUserSuccessfully() {

            when(userRepository.existsByEmail(createRequest.email())).thenReturn(false);
            when(userMapper.toEntity(createRequest)).thenReturn(user);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.create(createRequest);

            assertThat(result)
                    .isNotNull()
                    .isEqualTo(userResponse);

            verify(userRepository).existsByEmail(createRequest.email());
            verify(userMapper).toEntity(createRequest);
            verify(userRepository).save(user);
            verify(userMapper).toResponse(user);
            verifyNoMoreInteractions(userRepository, userMapper);
        }

        @Test
        void shouldThrowExceptionWhenEmailAlreadyExists() {

            when(userRepository.existsByEmail(createRequest.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.create(createRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("User with email: " + createRequest.email() + " already exists");

            verify(userRepository).existsByEmail(createRequest.email());
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void shouldReturnUserById() {

            when(userRepository.findWithPaymentCardsById(USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toDetailsResponse(user)).thenReturn(userDetailsResponse);

            UserDetailsResponse result = userService.getById(USER_ID);

            assertThat(result).isEqualTo(userDetailsResponse);

            verify(userRepository).findWithPaymentCardsById(USER_ID);
            verify(userMapper).toDetailsResponse(user);
            verifyNoMoreInteractions(userRepository, userMapper);
        }

        @Test
        void shouldThrowExceptionWhenUserNotFound() {

            when(userRepository.findWithPaymentCardsById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getById(USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with id: " + USER_ID + " not found");

            verify(userRepository).findWithPaymentCardsById(USER_ID);
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateUserSuccessfully() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail(updateRequest.email())).thenReturn(false);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.update(USER_ID, updateRequest);

            assertThat(result).isEqualTo(userResponse);

            verify(userRepository).findById(USER_ID);
            verify(userRepository).existsByEmail(updateRequest.email());
            verify(userMapper).updateUser(updateRequest, user);
            verify(userMapper).toResponse(user);
            verifyNoMoreInteractions(userRepository, userMapper);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingToExistingEmail() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail(updateRequest.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.update(USER_ID, updateRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("User with email: " + updateRequest.email() + " already exists");

            verify(userRepository).findById(USER_ID);
            verify(userRepository).existsByEmail(updateRequest.email());
            verifyNoMoreInteractions(userRepository, userMapper);
        }

        @Test
        void shouldUpdateUserWhenEmailNotChanged() {

            updateRequest = updateRequest.toBuilder()
                    .email(OLD_EMAIL)
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse actualResponse = userService.update(USER_ID, updateRequest);

            assertThat(actualResponse).isEqualTo(userResponse);

            verify(userRepository).findById(USER_ID);
            verify(userRepository, never()).existsByEmail(any());
            verify(userMapper).updateUser(updateRequest, user);
            verify(userMapper).toResponse(user);
            verifyNoMoreInteractions(userRepository, userMapper);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonExistingUser() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(USER_ID, updateRequest))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with id: " + USER_ID + " not found");

            verify(userRepository).findById(USER_ID);
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    class ActivateTests {

        @Test
        void shouldActivateUser() {

            user.setActive(INACTIVE);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            userService.activate(USER_ID);

            assertThat(user.getActive()).isTrue();

            verify(userRepository).findById(USER_ID);
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        void shouldThrowExceptionWhenActivatingNonExistingUser() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.activate(USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with id: " + USER_ID + " not found");

            verify(userRepository).findById(USER_ID);
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    class DeactivateTests {

        @Test
        void shouldDeactivateUser() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            userService.deactivate(USER_ID);

            assertThat(user.getActive()).isFalse();

            verify(userRepository).findById(USER_ID);
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        void shouldThrowExceptionWhenDeactivatingNonExistingUser() {

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deactivate(USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with id: " + USER_ID +  " not found");

            verify(userRepository).findById(USER_ID);
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    class GetAllTests {

        @Test
        void shouldReturnUsersPage() {

            Pageable pageable = PageRequest.of(0, 10);

            Page<User> users = new PageImpl<>(List.of(user));

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(users);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            Page<UserResponse> result = userService.getAll(USER_NAME, USER_SURNAME, pageable);

            assertThat(result).hasSize(1);
            assertThat(result.getContent()).containsExactly(userResponse);

            verify(userRepository).findAll(any(Specification.class), eq(pageable));
            verify(userMapper).toResponse(user);
            verifyNoMoreInteractions(userRepository, userMapper);
        }
    }

}
