package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CartDetailVmTest {

    @Test
    void constructor_ReturnsAllFields() {
        var vm = new CartDetailVm(1L, 10L, 2);
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.productId()).isEqualTo(10L);
        assertThat(vm.quantity()).isEqualTo(2);
    }
}
