package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CartItemVmTest {

    @Test
    void constructor_ReturnsProductIdAndQuantity() {
        var vm = new CartItemVm(100L, 3);
        assertThat(vm.productId()).isEqualTo(100L);
        assertThat(vm.quantity()).isEqualTo(3);
    }

    @Test
    void fromCartDetailVm_CreatesCorrectCartItemVm() {
        var cartDetail = new CartDetailVm(1L, 200L, 5);
        var vm = CartItemVm.fromCartDetailVm(cartDetail);
        assertThat(vm.productId()).isEqualTo(200L);
        assertThat(vm.quantity()).isEqualTo(5);
    }
}
