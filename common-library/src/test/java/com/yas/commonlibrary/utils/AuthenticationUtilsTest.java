package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticationUtilsTest {

    @Test
    void testGetAuthentication_returnsAuthenticationFromContext() {
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(authentication);

            Authentication result = AuthenticationUtils.getAuthentication();

            assertEquals(authentication, result);
        }
    }

    @Test
    void testExtractUserId_withAnonymousUser_throwsAccessDeniedException() {
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            AnonymousAuthenticationToken anonymous = mock(AnonymousAuthenticationToken.class);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(anonymous);

            assertThrows(AccessDeniedException.class, AuthenticationUtils::extractUserId);
        }
    }

    @Test
    void testExtractUserId_withJwtUser_returnsSubject() {
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("user-123");
            when(jwtToken.getToken()).thenReturn(jwt);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(jwtToken);

            String userId = AuthenticationUtils.extractUserId();

            assertEquals("user-123", userId);
        }
    }

    @Test
    void testExtractJwt_withValidJwtPrincipal_returnsTokenValue() {
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
            Jwt jwt = mock(Jwt.class);
            when(jwt.getTokenValue()).thenReturn("eyJhbGciOiJSUzI1NiJ9.test");
            when(jwtToken.getPrincipal()).thenReturn(jwt);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(jwtToken);

            String tokenValue = AuthenticationUtils.extractJwt();

            assertEquals("eyJhbGciOiJSUzI1NiJ9.test", tokenValue);
        }
    }
}
