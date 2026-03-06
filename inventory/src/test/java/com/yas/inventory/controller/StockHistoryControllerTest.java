package com.yas.inventory.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.inventory.service.StockHistoryService;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryListVm;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StockHistoryController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class StockHistoryControllerTest {

    @MockitoBean
    private StockHistoryService stockHistoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getStockHistories_ShouldReturnOk() throws Exception {
        StockHistoryListVm listVm = new StockHistoryListVm(List.of());
        when(stockHistoryService.getStockHistories(anyLong(), anyLong())).thenReturn(listVm);

        mockMvc.perform(get("/backoffice/stocks/histories")
                .param("productId", "1")
                .param("warehouseId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}