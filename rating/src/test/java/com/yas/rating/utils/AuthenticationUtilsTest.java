package com.yas.rating.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.yas.commonlibrary.exception.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticationUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extractUserId_ShouldReturnSubject() {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("rating-user")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        String userId = AuthenticationUtils.extractUserId();

        assertEquals("rating-user", userId);
    }

    @Test
    void extractUserId_WhenAnonymous_ShouldThrowAccessDeniedException() {
        AnonymousAuthenticationToken anonymousAuthenticationToken = new AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymousAuthenticationToken);

        assertThrows(AccessDeniedException.class, AuthenticationUtils::extractUserId);
    }
}