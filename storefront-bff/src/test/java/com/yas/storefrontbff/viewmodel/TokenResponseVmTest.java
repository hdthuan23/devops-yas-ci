package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenResponseVmTest {

    @Test
    void constructor_ReturnsAccessAndRefreshToken() {
        var vm = new TokenResponseVm("access-token-123", "refresh-token-456");
        assertThat(vm.accessToken()).isEqualTo("access-token-123");
        assertThat(vm.refreshToken()).isEqualTo("refresh-token-456");
    }
}
