package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthenticationInfoVmTest {

    @Test
    void constructor_WhenNotAuthenticated_ReturnsCorrectValues() {
        var vm = new AuthenticationInfoVm(false, null);
        assertThat(vm.isAuthenticated()).isFalse();
        assertThat(vm.authenticatedUser()).isNull();
    }

    @Test
    void constructor_WhenAuthenticated_ReturnsCorrectValues() {
        var user = new AuthenticatedUserVm("testuser");
        var vm = new AuthenticationInfoVm(true, user);
        assertThat(vm.isAuthenticated()).isTrue();
        assertThat(vm.authenticatedUser()).isEqualTo(user);
        assertThat(vm.authenticatedUser().username()).isEqualTo("testuser");
    }
}
