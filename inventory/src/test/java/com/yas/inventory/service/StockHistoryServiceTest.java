package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.inventory.model.Stock;
import com.yas.inventory.model.StockHistory;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockHistoryRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryListVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockHistoryServiceTest {

    @Mock
    private StockHistoryRepository stockHistoryRepository;
    @Mock
    private ProductService productService;

    @InjectMocks
    private StockHistoryService stockHistoryService;

    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");
    }

    @Test
    void createStockHistories_Success() {
        Stock stock = Stock.builder()
            .id(1L)
            .productId(1L)
            .quantity(100L)
            .warehouse(warehouse)
            .build();

        StockQuantityVm quantityVm = new StockQuantityVm(1L, 50L, "Adding units");

        stockHistoryService.createStockHistories(List.of(stock), List.of(quantityVm));

        verify(stockHistoryRepository).saveAll(anyList());
    }

    @Test
    void createStockHistories_WhenNoMatchingQuantityVm_SkipsStock() {
        Stock stock = Stock.builder()
            .id(1L)
            .productId(1L)
            .quantity(100L)
            .warehouse(warehouse)
            .build();

        StockQuantityVm quantityVm = new StockQuantityVm(99L, 50L, "No match");

        stockHistoryService.createStockHistories(List.of(stock), List.of(quantityVm));

        verify(stockHistoryRepository).saveAll(anyList());
    }

    @Test
    void getStockHistories_Success() {
        StockHistory stockHistory = StockHistory.builder()
            .id(1L)
            .productId(1L)
            .adjustedQuantity(50L)
            .note("Test note")
            .warehouse(warehouse)
            .build();

        ProductInfoVm productInfoVm = new ProductInfoVm(1L, "Product 1", "SKU-001", true);

        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(1L, 1L))
            .thenReturn(List.of(stockHistory));
        when(productService.getProduct(1L)).thenReturn(productInfoVm);

        StockHistoryListVm result = stockHistoryService.getStockHistories(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.stockHistories().size());
    }
}
