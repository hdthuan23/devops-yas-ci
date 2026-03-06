package com.yas.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.inventory.service.StockService;
import com.yas.inventory.viewmodel.stock.StockPostVm;
import com.yas.inventory.viewmodel.stock.StockQuantityUpdateVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stock.StockVm;
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

@WebMvcTest(controllers = StockController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class StockControllerTest {

    @MockitoBean
    private StockService stockService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addProductIntoWarehouse_ShouldReturnNoContent() throws Exception {
        doNothing().when(stockService).addProductIntoWarehouse(anyList());

        StockPostVm postVm = new StockPostVm(1L, 1L);
        ObjectWriter writer = new ObjectMapper().writer();
        String json = writer.writeValueAsString(List.of(postVm));

        mockMvc.perform(post("/backoffice/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNoContent());
    }

    @Test
    void getStocksByWarehouseIdAndProductNameAndSku_ShouldReturnOk() throws Exception {
        StockVm stockVm = new StockVm(1L, 1L, "Product 1", "SKU", 100L, 10L, 1L);
        when(stockService.getStocksByWarehouseIdAndProductNameAndSku(anyLong(), anyString(), anyString()))
            .thenReturn(List.of(stockVm));

        mockMvc.perform(get("/backoffice/stocks")
                .param("warehouseId", "1")
                .param("productName", "Product")
                .param("productSku", "SKU")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void updateProductQuantityInStock_ShouldReturnOk() throws Exception {
        doNothing().when(stockService).updateProductQuantityInStock(any(StockQuantityUpdateVm.class));

        StockQuantityVm quantityVm = new StockQuantityVm(1L, 50L, "note");
        StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));
        ObjectWriter writer = new ObjectMapper().writer();
        String json = writer.writeValueAsString(updateVm);

        mockMvc.perform(put("/backoffice/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }
}