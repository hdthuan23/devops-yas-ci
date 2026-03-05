package com.yas.media.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NoFileMediaVmTest {

    @Test
    void noFileMediaVm_recordAccessors() {
        NoFileMediaVm vm = new NoFileMediaVm(1L, "caption", "file.png", "image/png");

        assertEquals(1L, vm.id());
        assertEquals("caption", vm.caption());
        assertEquals("file.png", vm.fileName());
        assertEquals("image/png", vm.mediaType());
    }

    @Test
    void noFileMediaVm_withNullValues() {
        NoFileMediaVm vm = new NoFileMediaVm(null, null, null, null);

        assertEquals(null, vm.id());
        assertEquals(null, vm.caption());
        assertEquals(null, vm.fileName());
        assertEquals(null, vm.mediaType());
    }
}
