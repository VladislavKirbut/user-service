package com.innowise.userservice.security.service.impl;

import com.innowise.userservice.security.exception.InvalidTokenException;
import com.innowise.userservice.security.exception.TokenExpiredException;
import com.innowise.userservice.security.jwt.JwtProperties;
import com.innowise.userservice.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

import static com.innowise.userservice.security.jwt.JwtClaims.*;

@Component
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private final SecretKey secretKey;

    @Override
    public Long extractUserId(String token) {

        return Long.valueOf(extractClaims(token).getSubject());
    }

    @Override
    public List<String> extractRoles(String token) {

        Object roles = extractClaims(token).get(ROLES);

        if(!(roles instanceof Collection<?> collection)) {
            throw new InvalidTokenException("Roles claim is invalid");
        }

        return collection.stream()
                .map(String.class::cast)
                .toList();
    }

    @Override
    public String extractEmail(String token) {

        String email = extractClaims(token)
                .get(EMAIL, String.class);

        if (email == null || email.isBlank()) {
            throw new InvalidTokenException("Email claim is missing");
        }

        return email;
    }

    @Override
    public void validateToken(String accessToken) {

        try {
            extractClaims(accessToken);
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("Token has expired", ex);
        } catch (JwtException ex) {
            throw new InvalidTokenException("Invalid token", ex);
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}

