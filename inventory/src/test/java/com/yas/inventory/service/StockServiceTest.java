package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.StockExistingException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockPostVm;
import com.yas.inventory.viewmodel.stock.StockQuantityUpdateVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stock.StockVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ProductService productService;
    @Mock
    private WarehouseService warehouseService;
    @Mock
    private StockHistoryService stockHistoryService;

    @InjectMocks
    private StockService stockService;

    private Warehouse warehouse;
    private ProductInfoVm productInfoVm;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");

        productInfoVm = new ProductInfoVm(1L, "Product 1", "SKU-001", true);
    }

    @Test
    void addProductIntoWarehouse_Success() {
        StockPostVm postVm = new StockPostVm(1L, 1L);

        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(productInfoVm);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        stockService.addProductIntoWarehouse(List.of(postVm));

        verify(stockRepository).saveAll(anyList());
    }

    @Test
    void addProductIntoWarehouse_WhenStockAlreadyExists_ThrowsStockExistingException() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(true);

        assertThrows(StockExistingException.class,
            () -> stockService.addProductIntoWarehouse(List.of(postVm)));
    }

    @Test
    void addProductIntoWarehouse_WhenProductNotFound_ThrowsNotFoundException() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(null);

        assertThrows(NotFoundException.class,
            () -> stockService.addProductIntoWarehouse(List.of(postVm)));
    }

    @Test
    void addProductIntoWarehouse_WhenWarehouseNotFound_ThrowsNotFoundException() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(productInfoVm);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> stockService.addProductIntoWarehouse(List.of(postVm)));
    }

    @Test
    void getStocksByWarehouseIdAndProductNameAndSku_Success() {
        Stock stock = Stock.builder()
            .id(1L)
            .productId(1L)
            .quantity(100L)
            .reservedQuantity(10L)
            .warehouse(warehouse)
            .build();

        when(warehouseService.getProductWarehouse(eq(1L), eq("Product"), eq("SKU"),
            eq(FilterExistInWhSelection.YES)))
            .thenReturn(List.of(productInfoVm));
        when(stockRepository.findByWarehouseIdAndProductIdIn(eq(1L), anyList()))
            .thenReturn(List.of(stock));

        List<StockVm> result = stockService.getStocksByWarehouseIdAndProductNameAndSku(
            1L, "Product", "SKU");

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().productId());
    }

    @Test
    void updateProductQuantityInStock_Success() {
        Stock stock = Stock.builder()
            .id(1L)
            .productId(1L)
            .quantity(100L)
            .reservedQuantity(10L)
            .warehouse(warehouse)
            .build();

        StockQuantityVm quantityVm = new StockQuantityVm(1L, 50L, "Adding 50 units");
        StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));

        when(stockRepository.findAllById(anyList())).thenReturn(List.of(stock));

        stockService.updateProductQuantityInStock(updateVm);

        assertEquals(150L, stock.getQuantity());
        verify(stockRepository).saveAll(anyList());
        verify(stockHistoryService).createStockHistories(anyList(), anyList());
        verify(productService).updateProductQuantity(anyList());
    }

    @Test
    void updateProductQuantityInStock_WhenNoMatchingStock_SkipsUpdate() {
        Stock stock = Stock.builder()
            .id(1L)
            .productId(1L)
            .quantity(100L)
            .reservedQuantity(10L)
            .warehouse(warehouse)
            .build();

        StockQuantityVm quantityVm = new StockQuantityVm(99L, 50L, "No match");
        StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));

        when(stockRepository.findAllById(anyList())).thenReturn(List.of(stock));

        stockService.updateProductQuantityInStock(updateVm);

        assertEquals(100L, stock.getQuantity());
    }
}
