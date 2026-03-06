package com.yas.recommendation.kafka.consumer;

import static com.yas.commonlibrary.kafka.cdc.message.Operation.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.kafka.cdc.message.Product;
import com.yas.commonlibrary.kafka.cdc.message.ProductCdcMessage;
import com.yas.commonlibrary.kafka.cdc.message.ProductMsgKey;
import com.yas.recommendation.vector.product.service.ProductVectorSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductSyncServiceTest {

    @Mock
    private ProductVectorSyncService productVectorSyncService;

    private ProductSyncService productSyncService;

    @BeforeEach
    void setUp() {
        productSyncService = new ProductSyncService(productVectorSyncService);
    }

    @Test
    void testSync_whenCdcMessageIsNull_shouldDeleteProduct() {
        // Given
        ProductMsgKey key = new ProductMsgKey();
        key.setId(1L);

        // When
        productSyncService.sync(key, null);

        // Then
        verify(productVectorSyncService).deleteProductVector(1L);
        verifyNoMoreInteractions(productVectorSyncService);
    }

    @Test
    void testSync_whenOperationIsDelete_shouldDeleteProduct() {
        // Given
        ProductMsgKey key = new ProductMsgKey();
        key.setId(2L);
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(DELETE);

        // When
        productSyncService.sync(key, message);

        // Then
        verify(productVectorSyncService).deleteProductVector(2L);
        verifyNoMoreInteractions(productVectorSyncService);
    }

    @Test
    void testSync_whenOperationIsCreate_shouldCreateProductVector() {
        // Given
        ProductMsgKey key = new ProductMsgKey();
        key.setId(3L);
        Product product = new Product();
        product.setId(3L);
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(CREATE);
        message.setAfter(product);

        // When
        productSyncService.sync(key, message);

        // Then
        verify(productVectorSyncService).createProductVector(product);
        verifyNoMoreInteractions(productVectorSyncService);
    }

    @Test
    void testSync_whenOperationIsRead_shouldCreateProductVector() {
        // Given
        ProductMsgKey key = new ProductMsgKey();
        key.setId(4L);
        Product product = new Product();
        product.setId(4L);
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(READ);
        message.setAfter(product);

        // When
        productSyncService.sync(key, message);

        // Then
        verify(productVectorSyncService).createProductVector(product);
        verifyNoMoreInteractions(productVectorSyncService);
    }

    @Test
    void testSync_whenOperationIsUpdate_shouldUpdateProductVector() {
        // Given
        ProductMsgKey key = new ProductMsgKey();
        key.setId(5L);
        Product product = new Product();
        product.setId(5L);
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(UPDATE);
        message.setAfter(product);

        // When
        productSyncService.sync(key, message);

        // Then
        verify(productVectorSyncService).updateProductVector(product);
        verifyNoMoreInteractions(productVectorSyncService);
    }

    @Test
    void testSync_whenAfterIsNull_shouldNotCallAnyService() {
        // Given
        ProductMsgKey key = new ProductMsgKey();
        key.setId(6L);
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(CREATE);
        message.setAfter(null);

        // When
        productSyncService.sync(key, message);

        // Then
        verifyNoInteractions(productVectorSyncService);
    }
}
