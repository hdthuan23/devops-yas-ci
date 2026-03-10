package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CartGetDetailVmTest {

    @Test
    void constructor_WithEmptyCartDetails_ReturnsCorrectStructure() {
        var vm = new CartGetDetailVm(1L, "customer-123", Collections.emptyList());
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.customerId()).isEqualTo("customer-123");
        assertThat(vm.cartDetails()).isEmpty();
    }

    @Test
    void constructor_WithCartDetails_ReturnsCorrectStructure() {
        var detail = new CartDetailVm(1L, 10L, 2);
        var vm = new CartGetDetailVm(100L, "cust-1", List.of(detail));
        assertThat(vm.cartDetails()).hasSize(1);
        assertThat(vm.cartDetails().get(0).productId()).isEqualTo(10L);
    }
}
