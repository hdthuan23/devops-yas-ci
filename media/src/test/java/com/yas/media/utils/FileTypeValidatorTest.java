package com.yas.media.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintValidatorContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileTypeValidatorTest {

    private FileTypeValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new FileTypeValidator();

        // Create a mock ValidFileType annotation
        ValidFileType annotation = Mockito.mock(ValidFileType.class);
        Mockito.when(annotation.allowedTypes()).thenReturn(new String[]{"image/jpeg", "image/png", "image/gif"});
        Mockito.when(annotation.message()).thenReturn("File type not allowed");

        validator.initialize(annotation);

        // Setup context for violation building
        Mockito.when(context.buildConstraintViolationWithTemplate(Mockito.anyString()))
            .thenReturn(violationBuilder);
    }

    @Test
    void isValid_whenFileIsNull_thenReturnFalse() {
        boolean result = validator.isValid(null, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenContentTypeIsNull_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.png", null, "content".getBytes());
        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenContentTypeNotAllowed_thenReturnFalse() {
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }

    @Test
    void isValid_whenValidImageJpeg_thenReturnTrue() throws IOException {
        byte[] imageBytes = createValidImage("jpg");
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", imageBytes);

        boolean result = validator.isValid(file, context);
        assertTrue(result);
    }

    @Test
    void isValid_whenValidImagePng_thenReturnTrue() throws IOException {
        byte[] imageBytes = createValidImage("png");
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", imageBytes);

        boolean result = validator.isValid(file, context);
        assertTrue(result);
    }

    @Test
    void isValid_whenValidImageGif_thenReturnTrue() throws IOException {
        byte[] imageBytes = createValidImage("gif");
        MultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", imageBytes);

        boolean result = validator.isValid(file, context);
        assertTrue(result);
    }

    @Test
    void isValid_whenContentTypeMatchesButNotRealImage_thenReturnFalse() {
        // Content type says image/png but content is not a real image
        MultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", "not-an-image".getBytes());

        boolean result = validator.isValid(file, context);
        assertFalse(result);
    }

    private byte[] createValidImage(String format) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}
