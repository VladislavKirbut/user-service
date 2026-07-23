package com.innowise.userservice.security.service;

import java.util.List;
import java.util.UUID;

public interface JwtService {

    Long extractUserId(String token);

    List<String> extractRoles(String token);

    String extractEmail(String token);

    void validateToken(String token);

}
