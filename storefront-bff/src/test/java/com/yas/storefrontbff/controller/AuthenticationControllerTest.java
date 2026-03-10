package com.yas.storefrontbff.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class AuthenticationControllerTest {

    private final AuthenticationController controller = new AuthenticationController();

    @Test
    void user_WhenPrincipalIsNull_ReturnsNotAuthenticated() {
        ResponseEntity<?> response = controller.user(null);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        var body = (com.yas.storefrontbff.viewmodel.AuthenticationInfoVm) response.getBody();
        assertThat(body.isAuthenticated()).isFalse();
        assertThat(body.authenticatedUser()).isNull();
    }

    @Test
    void user_WhenPrincipalHasUser_ReturnsAuthenticated() {
        Map<String, Object> attributes = Map.of("preferred_username", "testuser");
        OAuth2User principal = new DefaultOAuth2User(
                Collections.emptyList(),
                attributes,
                "preferred_username");

        ResponseEntity<?> response = controller.user(principal);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        var body = (com.yas.storefrontbff.viewmodel.AuthenticationInfoVm) response.getBody();
        assertThat(body.isAuthenticated()).isTrue();
        assertThat(body.authenticatedUser()).isNotNull();
        assertThat(body.authenticatedUser().username()).isEqualTo("testuser");
    }
}
