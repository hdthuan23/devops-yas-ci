package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.address.AddressDetailVm;
import com.yas.inventory.viewmodel.address.AddressVm;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseDetailVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseListGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehousePostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ProductService productService;
    @Mock
    private LocationService locationService;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse warehouse;
    private WarehousePostVm warehousePostVm;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");
        warehouse.setAddressId(100L);

        warehousePostVm = WarehousePostVm.builder()
            .name("New Warehouse")
            .contactName("John Doe")
            .phone("1234567890")
            .addressLine1("123 Main St")
            .addressLine2("Apt 1")
            .city("HCMC")
            .zipCode("70000")
            .districtId(1L)
            .stateOrProvinceId(1L)
            .countryId(1L)
            .build();
    }

    @Test
    void findAllWarehouses_Success() {
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        List<WarehouseGetVm> result = warehouseService.findAllWarehouses();

        assertEquals(1, result.size());
        assertEquals("Main Warehouse", result.getFirst().name());
    }

    @Test
    void getProductWarehouse_WithProductIds_ReturnsMappedProducts() {
        ProductInfoVm product = new ProductInfoVm(1L, "Product 1", "SKU-001", false);
        when(stockRepository.getProductIdsInWarehouse(1L)).thenReturn(List.of(1L));
        when(productService.filterProducts(anyString(), anyString(), anyList(),
            eq(FilterExistInWhSelection.YES)))
            .thenReturn(List.of(product));

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(
            1L, "Product", "SKU", FilterExistInWhSelection.YES);

        assertEquals(1, result.size());
        assertEquals(true, result.getFirst().isInWarehouse());
    }

    @Test
    void getProductWarehouse_WithEmptyProductIds_ReturnsFilteredProducts() {
        ProductInfoVm product = new ProductInfoVm(1L, "Product 1", "SKU-001", false);
        when(stockRepository.getProductIdsInWarehouse(1L)).thenReturn(List.of());
        when(productService.filterProducts(anyString(), anyString(), anyList(),
            eq(FilterExistInWhSelection.NO)))
            .thenReturn(List.of(product));

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(
            1L, "Product", "SKU", FilterExistInWhSelection.NO);

        assertEquals(1, result.size());
    }

    @Test
    void findById_Success() {
        AddressDetailVm addressDetail = AddressDetailVm.builder()
            .id(100L)
            .contactName("John")
            .phone("1234567890")
            .addressLine1("123 Main St")
            .addressLine2("Apt 1")
            .city("HCMC")
            .zipCode("70000")
            .districtId(1L)
            .districtName("District 1")
            .stateOrProvinceId(1L)
            .stateOrProvinceName("Province 1")
            .countryId(1L)
            .countryName("Vietnam")
            .build();

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(locationService.getAddressById(100L)).thenReturn(addressDetail);

        WarehouseDetailVm result = warehouseService.findById(1L);

        assertNotNull(result);
        assertEquals("Main Warehouse", result.name());
    }

    @Test
    void findById_WhenNotFound_ThrowsNotFoundException() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> warehouseService.findById(99L));
    }

    @Test
    void create_Success() {
        AddressVm addressVm = AddressVm.builder().id(100L).build();
        when(warehouseRepository.existsByName("New Warehouse")).thenReturn(false);
        when(locationService.createAddress(any())).thenReturn(addressVm);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        Warehouse result = warehouseService.create(warehousePostVm);

        assertNotNull(result);
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void create_WhenDuplicateName_ThrowsDuplicatedException() {
        when(warehouseRepository.existsByName("New Warehouse")).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> warehouseService.create(warehousePostVm));
    }

    @Test
    void update_Success() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByNameWithDifferentId("New Warehouse", 1L)).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        warehouseService.update(warehousePostVm, 1L);

        verify(warehouseRepository).save(any(Warehouse.class));
        verify(locationService).updateAddress(eq(100L), any());
    }

    @Test
    void update_WhenNotFound_ThrowsNotFoundException() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> warehouseService.update(warehousePostVm, 99L));
    }

    @Test
    void update_WhenDuplicateName_ThrowsDuplicatedException() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByNameWithDifferentId("New Warehouse", 1L)).thenReturn(true);

        assertThrows(DuplicatedException.class,
            () -> warehouseService.update(warehousePostVm, 1L));
    }

    @Test
    void delete_Success() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        warehouseService.delete(1L);

        verify(warehouseRepository).deleteById(1L);
        verify(locationService).deleteAddress(100L);
    }

    @Test
    void delete_WhenNotFound_ThrowsNotFoundException() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> warehouseService.delete(99L));
    }

    @Test
    void getPageableWarehouses_Success() {
        Page<Warehouse> warehousePage = new PageImpl<>(
            List.of(warehouse), PageRequest.of(0, 10), 1);
        when(warehouseRepository.findAll(any(PageRequest.class))).thenReturn(warehousePage);

        WarehouseListGetVm result = warehouseService.getPageableWarehouses(0, 10);

        assertNotNull(result);
        assertEquals(1, result.warehouseContent().size());
        assertEquals(0, result.pageNo());
        assertEquals(10, result.pageSize());
    }
}
