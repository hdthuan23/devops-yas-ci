package com.yas.media.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_whenValidInput_thenReturnOk() {
        MultipartFile multipartFile = new MockMultipartFile(
            "file", "test.png", "image/png", "content".getBytes());
        MediaPostVm mediaPostVm = new MediaPostVm("caption", multipartFile, "override.png");

        Media media = new Media();
        media.setId(1L);
        media.setCaption("caption");
        media.setFileName("override.png");
        media.setMediaType("image/png");

        when(mediaService.saveMedia(any(MediaPostVm.class))).thenReturn(media);

        ResponseEntity<Object> response = mediaController.create(mediaPostVm);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        NoFileMediaVm body = (NoFileMediaVm) response.getBody();
        assertEquals(1L, body.id());
        assertEquals("caption", body.caption());
        assertEquals("override.png", body.fileName());
        assertEquals("image/png", body.mediaType());
        verify(mediaService).saveMedia(any(MediaPostVm.class));
    }

    @Test
    void delete_whenValidId_thenReturnNoContent() {
        doNothing().when(mediaService).removeMedia(1L);

        ResponseEntity<Void> response = mediaController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(mediaService).removeMedia(1L);
    }

    @Test
    void get_whenMediaExists_thenReturnOk() {
        MediaVm mediaVm = new MediaVm(1L, "caption", "file.png", "image/png", "http://example.com/file.png");
        when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

        ResponseEntity<MediaVm> response = mediaController.get(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("caption", response.getBody().getCaption());
        assertEquals("file.png", response.getBody().getFileName());
    }

    @Test
    void get_whenMediaNotFound_thenReturnNotFound() {
        when(mediaService.getMediaById(1L)).thenReturn(null);

        ResponseEntity<MediaVm> response = mediaController.get(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getByIds_whenMediasExist_thenReturnOk() {
        List<Long> ids = List.of(1L, 2L);
        List<MediaVm> mediaVms = List.of(
            new MediaVm(1L, "cap1", "file1.png", "image/png", "http://example.com/1"),
            new MediaVm(2L, "cap2", "file2.png", "image/png", "http://example.com/2")
        );
        when(mediaService.getMediaByIds(ids)).thenReturn(mediaVms);

        ResponseEntity<List<MediaVm>> response = mediaController.getByIds(ids);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getByIds_whenNoMediasFound_thenReturnNotFound() {
        List<Long> ids = List.of(99L, 100L);
        when(mediaService.getMediaByIds(ids)).thenReturn(Collections.emptyList());

        ResponseEntity<List<MediaVm>> response = mediaController.getByIds(ids);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getFile_whenFileExists_thenReturnFileWithHeaders() {
        byte[] fileContent = "file-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        MediaDto mediaDto = MediaDto.builder()
            .content(inputStream)
            .mediaType(MediaType.IMAGE_PNG)
            .build();

        when(mediaService.getFile(1L, "test.png")).thenReturn(mediaDto);

        ResponseEntity<InputStreamResource> response = mediaController.getFile(1L, "test.png");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition);
        assertEquals("attachment; filename=\"test.png\"", contentDisposition);
    }
}
