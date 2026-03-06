package com.yas.recommendation.vector.product.service;

import static org.mockito.Mockito.*;

import com.yas.commonlibrary.kafka.cdc.message.Product;
import com.yas.recommendation.vector.product.store.ProductVectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductVectorSyncServiceTest {

    @Mock
    private ProductVectorRepository productVectorRepository;

    private ProductVectorSyncService productVectorSyncService;

    @BeforeEach
    void setUp() {
        productVectorSyncService = new ProductVectorSyncService(productVectorRepository);
    }

    @Test
    void testCreateProductVector_whenProductIsPublished_shouldAddToRepository() {
        // Given
        Product product = new Product();
        product.setId(1L);
        product.setPublished(true);

        // When
        productVectorSyncService.createProductVector(product);

        // Then
        verify(productVectorRepository).add(1L);
        verifyNoMoreInteractions(productVectorRepository);
    }

    @Test
    void testCreateProductVector_whenProductIsNotPublished_shouldNotAddToRepository() {
        // Given
        Product product = new Product();
        product.setId(1L);
        product.setPublished(false);

        // When
        productVectorSyncService.createProductVector(product);

        // Then
        verifyNoInteractions(productVectorRepository);
    }

    @Test
    void testUpdateProductVector_whenProductIsPublished_shouldUpdateInRepository() {
        // Given
        Product product = new Product();
        product.setId(2L);
        product.setPublished(true);

        // When
        productVectorSyncService.updateProductVector(product);

        // Then
        verify(productVectorRepository).update(2L);
        verifyNoMoreInteractions(productVectorRepository);
    }

    @Test
    void testUpdateProductVector_whenProductIsNotPublished_shouldDeleteFromRepository() {
        // Given
        Product product = new Product();
        product.setId(3L);
        product.setPublished(false);

        // When
        productVectorSyncService.updateProductVector(product);

        // Then
        verify(productVectorRepository).delete(3L);
        verifyNoMoreInteractions(productVectorRepository);
    }

    @Test
    void testDeleteProductVector_shouldDeleteFromRepository() {
        // Given
        Long productId = 5L;

        // When
        productVectorSyncService.deleteProductVector(productId);

        // Then
        verify(productVectorRepository).delete(productId);
        verifyNoMoreInteractions(productVectorRepository);
    }

    @Test
    void testDeleteProductVector_withDifferentProductId_shouldDeleteCorrectProduct() {
        // Given
        Long productId = 99L;

        // When
        productVectorSyncService.deleteProductVector(productId);

        // Then
        verify(productVectorRepository).delete(99L);
    }
}
