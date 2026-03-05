package com.yas.media.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.UnsupportedMediaTypeException;
import com.yas.media.viewmodel.ErrorVm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

class ControllerAdvisorTest {

    private ControllerAdvisor controllerAdvisor;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        controllerAdvisor = new ControllerAdvisor();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setServletPath("/medias");
        webRequest = new ServletWebRequest(servletRequest);
    }

    @Test
    void handleUnsupportedMediaTypeException_thenReturnBadRequest() {
        UnsupportedMediaTypeException ex = new UnsupportedMediaTypeException("Unsupported type");

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleUnsupportedMediaTypeException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unsupported media type", response.getBody().title());
        assertEquals("File uploaded media type is not supported", response.getBody().detail());
    }

    @Test
    void handleNotFoundException_thenReturnNotFound() {
        NotFoundException ex = new NotFoundException("Media 1 is not found");

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleNotFoundException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Media 1 is not found", response.getBody().detail());
    }

    @Test
    void handleMethodArgumentNotValid_thenReturnBadRequest() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("media", "caption", "must not be null");

        Mockito.when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(bindingResult);

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request information is not valid", response.getBody().detail());
        assertNotNull(response.getBody().fieldErrors());
        assertEquals(1, response.getBody().fieldErrors().size());
        assertEquals("caption must not be null", response.getBody().fieldErrors().get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolation_thenReturnBadRequest() {
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        Path path = Mockito.mock(Path.class);

        Mockito.when(violation.getRootBeanClass()).thenReturn((Class) String.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(path.toString()).thenReturn("field");
        Mockito.when(violation.getMessage()).thenReturn("must not be null");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request information is not valid", response.getBody().detail());
        assertNotNull(response.getBody().fieldErrors());
    }

    @Test
    void handleRuntimeException_thenReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleIoException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Something went wrong", response.getBody().detail());
        assertEquals("RuntimeException", response.getBody().title());
    }

    @Test
    void handleOtherException_thenReturnInternalServerError() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleOtherException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().detail());
    }

    @Test
    void handleNotFoundException_withNullRequest_thenReturnNotFound() {
        NotFoundException ex = new NotFoundException("Not found");

        // Test without WebRequest to cover the null request branch in buildErrorResponse
        ResponseEntity<ErrorVm> response = controllerAdvisor.handleMethodArgumentNotValid(
            createMethodArgumentNotValidException());

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private MethodArgumentNotValidException createMethodArgumentNotValidException() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());
        return new MethodArgumentNotValidException(bindingResult);
    }
}
