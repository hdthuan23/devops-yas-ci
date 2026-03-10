package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthenticatedUserVmTest {

    @Test
    void constructor_ReturnsUsername() {
        var vm = new AuthenticatedUserVm("john.doe");
        assertThat(vm.username()).isEqualTo("john.doe");
    }
}
