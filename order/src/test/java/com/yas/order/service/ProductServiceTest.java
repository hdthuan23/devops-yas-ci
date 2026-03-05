package com.yas.order.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.config.ServiceUrlConfig;
import com.yas.order.viewmodel.order.OrderItemVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.product.ProductCheckoutListVm;
import com.yas.order.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.order.viewmodel.product.ProductVariationVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private RestClient restClient;
    private ServiceUrlConfig serviceUrlConfig;
    private ProductService productService;
    private RestClient.ResponseSpec responseSpec;

    private static final String PRODUCT_URL = "http://api.yas.local/product";

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        serviceUrlConfig = mock(ServiceUrlConfig.class);
        productService = new ProductService(restClient, serviceUrlConfig);
        responseSpec = Mockito.mock(RestClient.ResponseSpec.class);
        com.yas.order.utils.SecurityContextUtils.setUpSecurityContext("test");
        when(serviceUrlConfig.product()).thenReturn(PRODUCT_URL);
    }

    @Test
    void testGetProductVariations_whenNormalCase_shouldReturnList() {
        // Arrange
        Long productId = 1L;
        List<ProductVariationVm> expectedList = List.of(
            new ProductVariationVm(100L, "Variation 1", "SKU-001")
        );

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        ResponseEntity<List<ProductVariationVm>> responseEntity = ResponseEntity.ok(expectedList);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        List<ProductVariationVm> result = productService.getProductVariations(productId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /* TODO: Fix mocking for body() method
    @Test
    void testSubtractProductStockQuantity_whenNormalCase_shouldNoException() {
        // Arrange
        OrderItemVm orderItemVm = OrderItemVm.builder()
            .id(1L)
            .productId(1L)
            .productName("Product")
            .quantity(2)
            .productPrice(BigDecimal.TEN)
            .note("Note")
            .discountAmount(BigDecimal.ONE)
            .taxAmount(BigDecimal.valueOf(0.1))
            .taxPercent(BigDecimal.valueOf(10))
            .orderId(100L)
            .build();
        
        OrderVm orderVm = OrderVm.builder()
            .orderItemVms(Set.of(orderItemVm))
            .build();

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);

        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        // Act & Assert
        assertDoesNotThrow(() -> productService.subtractProductStockQuantity(orderVm));
    }
    */


    @Test
    void testGetProductInformation_whenNormalCase_shouldReturnMap() {
        // Arrange
        Set<Long> ids = Set.of(1L, 2L);
        ProductCheckoutListVm productVm = ProductCheckoutListVm.builder()
            .id(1L)
            .name("Product 1")
            .build();
        
        ProductGetCheckoutListVm responseVm = new ProductGetCheckoutListVm(
            List.of(productVm),
            0,
            10,
            1,
            1,
            true
        );

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        ResponseEntity<ProductGetCheckoutListVm> responseEntity = ResponseEntity.ok(responseVm);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        Map<Long, ProductCheckoutListVm> result = productService.getProductInfomation(ids, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
    }

    @Test
    void testGetProductInformation_whenResponseIsNull_shouldThrowNotFoundException() {
        // Arrange
        Set<Long> ids = Set.of(1L);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        ResponseEntity<ProductGetCheckoutListVm> responseEntity = ResponseEntity.ok(null);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act & Assert
        assertThrows(NotFoundException.class, 
            () -> productService.getProductInfomation(ids, 0, 10));
    }

    @Test
    void testGetProductInformation_whenProductListIsNull_shouldThrowNotFoundException() {
        // Arrange
        Set<Long> ids = Set.of(1L);
        ProductGetCheckoutListVm responseVm = new ProductGetCheckoutListVm(null, 0, 10, 0, 0, true);

        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        ResponseEntity<ProductGetCheckoutListVm> responseEntity = ResponseEntity.ok(responseVm);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act & Assert
        assertThrows(NotFoundException.class, 
            () -> productService.getProductInfomation(ids, 0, 10));
    }
}
