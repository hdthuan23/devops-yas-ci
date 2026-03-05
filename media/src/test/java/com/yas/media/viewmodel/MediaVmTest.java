package com.yas.media.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MediaVmTest {

    @Test
    void mediaVm_constructorAndGetters() {
        MediaVm mediaVm = new MediaVm(1L, "caption", "file.png", "image/png", "http://example.com/file.png");

        assertEquals(1L, mediaVm.getId());
        assertEquals("caption", mediaVm.getCaption());
        assertEquals("file.png", mediaVm.getFileName());
        assertEquals("image/png", mediaVm.getMediaType());
        assertEquals("http://example.com/file.png", mediaVm.getUrl());
    }

    @Test
    void mediaVm_setters() {
        MediaVm mediaVm = new MediaVm(1L, "caption", "file.png", "image/png", null);

        assertNull(mediaVm.getUrl());

        mediaVm.setUrl("http://example.com/updated.png");
        assertEquals("http://example.com/updated.png", mediaVm.getUrl());

        mediaVm.setCaption("new caption");
        assertEquals("new caption", mediaVm.getCaption());

        mediaVm.setFileName("new-file.png");
        assertEquals("new-file.png", mediaVm.getFileName());

        mediaVm.setMediaType("image/gif");
        assertEquals("image/gif", mediaVm.getMediaType());

        mediaVm.setId(2L);
        assertEquals(2L, mediaVm.getId());
    }
}
