package com.yas.recommendation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yas.recommendation.configuration.RecommendationConfig;
import com.yas.recommendation.viewmodel.ProductDetailVm;
import java.net.URI;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RecommendationConfig config;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(restClient, config);
    }

    @Test
    void testGetProductDetail_whenValidProductId_shouldReturnProductDetail() {
        // Given
        Long productId = 1L;
        String apiUrl = "http://api.example.com";
        ProductDetailVm expectedProduct = new ProductDetailVm(
            1L, "Product", "Short desc", "Description", "Spec", "SKU123", "GTIN", "slug",
            true, true, false, true, false, 99.99, 10L, Collections.emptyList(),
            "Meta", "Keywords", "Meta desc", 1L, "Brand", Collections.emptyList(),
            Collections.emptyList(), null, Collections.emptyList()
        );

        when(config.getApiUrl()).thenReturn(apiUrl);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(expectedProduct));

        // When
        ProductDetailVm result = productService.getProductDetail(productId);

        // Then
        assertNotNull(result);
        assertEquals(expectedProduct, result);
        assertEquals(1L, result.id());
        assertEquals("Product", result.name());
        verify(config).getApiUrl();
        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(any(URI.class));
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).toEntity(any(ParameterizedTypeReference.class));
    }

    @Test
    void testGetProductDetail_whenApiReturnsNull_shouldReturnNull() {
        // Given
        Long productId = 1L;
        String apiUrl = "http://api.example.com";

        when(config.getApiUrl()).thenReturn(apiUrl);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        // When
        ProductDetailVm result = productService.getProductDetail(productId);

        // Then
        assertNull(result);
    }

    @Test
    void testGetProductDetail_whenDifferentProductId_shouldUseCorrectId() {
        // Given
        Long productId = 42L;
        String apiUrl = "http://api.example.com";
        ProductDetailVm expectedProduct = new ProductDetailVm(
            42L, "Product 42", "Short", "Desc", "Spec", "SKU", "GTIN", "slug",
            true, true, false, true, false, 49.99, 5L, Collections.emptyList(),
            "Meta", "Keys", "Desc", 2L, "Brand", Collections.emptyList(),
            Collections.emptyList(), null, Collections.emptyList()
        );

        when(config.getApiUrl()).thenReturn(apiUrl);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(expectedProduct));

        // When
        ProductDetailVm result = productService.getProductDetail(productId);

        // Then
        assertNotNull(result);
        assertEquals(42L, result.id());
        assertEquals("Product 42", result.name());
    }
}
