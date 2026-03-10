package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GuestUserVmTest {

    @Test
    void constructor_ReturnsAllFields() {
        var vm = new GuestUserVm("user-123", "guest@test.com", "secret");
        assertThat(vm.userId()).isEqualTo("user-123");
        assertThat(vm.email()).isEqualTo("guest@test.com");
        assertThat(vm.password()).isEqualTo("secret");
    }
}
