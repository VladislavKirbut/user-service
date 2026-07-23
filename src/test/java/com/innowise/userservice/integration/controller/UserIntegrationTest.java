package com.innowise.userservice.integration.controller;

import com.innowise.userservice.dto.request.CreateUserRequest;
import com.innowise.userservice.dto.request.UpdateUserRequest;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
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
                        true);
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        User user = createUser("Ivan", "Ivanov", "ivan@test.com", true);
        userRepository.save(user);

        CreateUserRequest request = CreateUserRequest.builder()
                .name("John")
                .surname("Smith")
                .email("ivan@test.com")
                .birthDate(LocalDate.of(2001, 1, 5))
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnUserById() throws Exception {
        User savedUser = userRepository.save(
                createUser("Ivan", "Ivanov", "ivan@test.com", true));

        String token = createAdminToken(savedUser.getId());

        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value(savedUser.getName()))
                .andExpect(jsonPath("$.surname").value(savedUser.getSurname()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()));
    }

    @Test
    void shouldReturnUsersWithPagination() throws Exception {
        User user = userRepository.save(
                createUser("Ivan", "Ivanov", "ivan@test.com", true));

        String token = createAdminToken(user.getId());

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldReturnUsersFilteredByName() throws Exception {
        User user = userRepository.save(
                createUser("Ivan", "Ivanov", "ivan@test.com", true));

        userRepository.save(
                createUser("John", "Smith", "john@test.com", true));

        String token = createAdminToken(user.getId());

        mockMvc.perform(get("/api/v1/users")
                        .param("name", "Ivan")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ivan"));
    }

    @Test
    void shouldReturnUsersFilteredBySurname() throws Exception {
        User user = userRepository.save(
                createUser("Ivan", "Ivanov", "ivan@test.com", true));

        User user1 = userRepository.save(
                createUser("John", "Smith", "john@test.com", true));

        String token = createAdminToken(user.getId());

        mockMvc.perform(get("/api/v1/users")
                        .param("surname", "Smith")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].surname").value(user1.getSurname()));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        User savedUser = userRepository.save(
                createUser("Ivan", "Ivanov", "ivan@test.com", true));

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("John")
                .surname("Smith")
                .birthDate(LocalDate.of(1997, 3, 15))
                .email("john@gmail.com")
                .build();

        String token = createAdminToken(savedUser.getId());

        mockMvc.perform(put("/api/v1/users/{id}", savedUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.surname").value(request.surname()))
                .andExpect(jsonPath("$.email").value(request.email()));

        User updatedUser = userRepository.findById(savedUser.getId())
                .orElseThrow();

        assertThat(updatedUser.getName()).isEqualTo(request.name());
        assertThat(updatedUser.getSurname()).isEqualTo(request.surname());
        assertThat(updatedUser.getEmail()).isEqualTo(request.email());
    }

    @Test
    void shouldActivateUserSuccessfully() throws Exception {
        User savedUser = userRepository.save(
                createUser("Ivan", "Ivanov", "ivan@test.com", false));

        String token = createAdminToken(savedUser.getId());

        mockMvc.perform(patch("/api/v1/users/{id}", savedUser.getId())
                        .param("active", "true")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(savedUser.getId())
                .orElseThrow();

        assertThat(updatedUser.getActive()).isTrue();
    }

    @Test
    void shouldDeactivateUserSuccessfully() throws Exception {
        User savedUser = userRepository.save(
                createUser("Ivan", "Ivanov", "ivan@test.com", true));

        String token = createAdminToken(savedUser.getId());

        mockMvc.perform(patch("/api/v1/users/{id}", savedUser.getId())
                        .param("active", "false")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(savedUser.getId())
                .orElseThrow();

        assertThat(updatedUser.getActive()).isFalse();
    }

    private User createUser(
            String name,
            String surname,
            String email,
            boolean active) {

        return User.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .birthDate(LocalDate.of(1998, 10, 5))
                .active(active)
                .build();
    }
}