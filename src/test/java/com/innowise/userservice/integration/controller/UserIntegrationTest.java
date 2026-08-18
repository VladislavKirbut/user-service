package com.innowise.userservice.integration.controller;

import com.innowise.userservice.dto.request.CreateUserRequest;
import com.innowise.userservice.dto.request.UpdateUserRequest;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateUserSuccessfully() throws Exception {

        CreateUserRequest request = CreateUserRequest.builder()
                .name("John")
                .surname("Smith")
                .email("johnsmith@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(userRepository.count()).isEqualTo(1);

        User user = userRepository.findAll().getFirst();

        assertThat(user)
                .extracting(
                        User::getName,
                        User::getSurname,
                        User::getEmail,
                        User::getActive)
                .containsExactly(
                        request.name(),
                        request.surname(),
                        request.email(),
                        true
                );
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExist() throws Exception {

        User user = User.builder()
                .name("Ivan")
                .surname("Ivanov")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(true)
                .build();

        userRepository.save(user);

        CreateUserRequest request = CreateUserRequest.builder()
                .name("John")
                .surname("Smith")
                .email("jsmith@gmail.com")
                .birthDate(LocalDate.of(2001, 1,5))
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        assertThat(userRepository.count()).isOne();
    }

    @Test
    void shouldReturnUserById() throws Exception {

        User user = User.builder()
                .name("Ivan")
                .surname("Ivanov")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value(savedUser.getName()))
                .andExpect(jsonPath("$.surname").value(savedUser.getSurname()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()));
    }

    @Test
    void shouldReturnUsersWithPagination() throws Exception {

        User user = User.builder()
                .name("Ivan")
                .surname("Ivanov")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(true)
                .build();

        userRepository.save(user);


        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldReturnUsersFilteredByName() throws Exception {

        User user = User.builder()
                .name("Ivan")
                .surname("Ivanov")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(true)
                .build();

        userRepository.save(user);

        User user1 = User.builder()
                .name("John")
                .surname("Smith")
                .email("john@test.com")
                .birthDate(LocalDate.of(1990,4,5))
                .active(true)
                .build();

        userRepository.save(user1);


        mockMvc.perform(get("/api/v1/users").param("name", "Ivan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value(user.getName()));
    }

    @Test
    void shouldReturnUsersFilteredBySurname() throws Exception {

        User user = User.builder()
                .name("Ivan")
                .surname("Ivanov")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(true)
                .build();

        userRepository.save(user);

        User user1 = User.builder()
                .name("John")
                .surname("Smith")
                .email("john@test.com")
                .birthDate(LocalDate.of(1990,4,5))
                .active(true)
                .build();

        userRepository.save(user1);


        mockMvc.perform(get("/api/v1/users").param("surname", "Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].surname").value(user1.getName()));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {

        User user = User.builder()
                .name("Ivan")
                .surname("Ivanov")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);


        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("John")
                .surname("Smith")
                .birthDate(LocalDate.of(1997,3,15))
                .email("john@gmail.com")
                .build();


        mockMvc.perform(put("/api/v1/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.surname").value(request.surname()))
                .andExpect(jsonPath("$.email").value(request.email()));


        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThat(updatedUser.getName()).isEqualTo(request.name());
    }

    @Test
    void shouldActivateUserSuccessfully() throws Exception {

        User user = User.builder()
                .name("Ivan")
                .surname("Ivanov")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        mockMvc.perform(patch("/api/v1/users/{id}/activate", savedUser.getId()))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(updatedUser.getActive()).isTrue();
    }

    @Test
    void shouldDeactivateUserSuccessfully() throws Exception {

        User user = User.builder()
                .name("Ivan")
                .surname("Ivanov")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", savedUser.getId()))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(updatedUser.getActive()).isFalse();
    }

}
