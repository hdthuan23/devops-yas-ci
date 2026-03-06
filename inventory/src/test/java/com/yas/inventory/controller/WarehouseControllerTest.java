package com.yas.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.service.WarehouseService;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseDetailVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseListGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehousePostVm;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

@WebMvcTest(controllers = WarehouseController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class WarehouseControllerTest {

    @MockitoBean
    private WarehouseService warehouseService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getProductByWarehouse_ShouldReturnOk() throws Exception {
        ProductInfoVm product = new ProductInfoVm(1L, "Product 1", "SKU-001", true);
        when(warehouseService.getProductWarehouse(anyLong(), anyString(), anyString(),
            any(FilterExistInWhSelection.class)))
            .thenReturn(List.of(product));

        mockMvc.perform(get("/backoffice/warehouses/1/products")
                .param("productName", "Product")
                .param("productSku", "SKU")
                .param("existStatus", "YES")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getPageableWarehouses_ShouldReturnOk() throws Exception {
        WarehouseListGetVm listGetVm = new WarehouseListGetVm(
            List.of(new WarehouseGetVm(1L, "Warehouse")), 0, 10, 1, 1, true);
        when(warehouseService.getPageableWarehouses(anyInt(), anyInt())).thenReturn(listGetVm);

        mockMvc.perform(get("/backoffice/warehouses/paging")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void listWarehouses_ShouldReturnOk() throws Exception {
        when(warehouseService.findAllWarehouses())
            .thenReturn(List.of(new WarehouseGetVm(1L, "Warehouse")));

        mockMvc.perform(get("/backoffice/warehouses")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getWarehouse_ShouldReturnOk() throws Exception {
        WarehouseDetailVm detailVm = new WarehouseDetailVm(
            1L, "Warehouse", "John", "1234", "Addr1", "Addr2",
            "HCMC", "70000", 1L, 1L, 1L);
        when(warehouseService.findById(1L)).thenReturn(detailVm);

        mockMvc.perform(get("/backoffice/warehouses/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void createWarehouse_ShouldReturnCreated() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("New Warehouse");

        when(warehouseService.create(any(WarehousePostVm.class))).thenReturn(warehouse);

        WarehousePostVm postVm = WarehousePostVm.builder()
            .name("New Warehouse")
            .contactName("John")
            .phone("123456")
            .addressLine1("123 Main")
            .city("HCMC")
            .zipCode("70000")
            .districtId(1L)
            .stateOrProvinceId(1L)
            .countryId(1L)
            .build();

        ObjectWriter writer = new ObjectMapper().writer();
        String json = writer.writeValueAsString(postVm);

        mockMvc.perform(post("/backoffice/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated());
    }

    @Test
    void updateWarehouse_ShouldReturnNoContent() throws Exception {
        doNothing().when(warehouseService).update(any(WarehousePostVm.class), eq(1L));

        WarehousePostVm postVm = WarehousePostVm.builder()
            .name("Updated Warehouse")
            .contactName("John")
            .phone("123456")
            .addressLine1("123 Main")
            .city("HCMC")
            .zipCode("70000")
            .districtId(1L)
            .stateOrProvinceId(1L)
            .countryId(1L)
            .build();

        ObjectWriter writer = new ObjectMapper().writer();
        String json = writer.writeValueAsString(postVm);

        mockMvc.perform(put("/backoffice/warehouses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteWarehouse_ShouldReturnNoContent() throws Exception {
        doNothing().when(warehouseService).delete(1L);

        mockMvc.perform(delete("/backoffice/warehouses/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }
}