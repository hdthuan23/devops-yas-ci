package com.yas.webhook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.webhook.model.viewmodel.webhook.WebhookDetailVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookListGetVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookPostVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookVm;
import com.yas.webhook.service.WebhookService;
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

@WebMvcTest(controllers = WebhookController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {

    @MockitoBean
    private WebhookService webhookService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPageableWebhooks_ShouldReturnOk() throws Exception {
        WebhookListGetVm listGetVm = WebhookListGetVm.builder()
            .webhooks(List.of())
            .pageNo(0).pageSize(10).totalElements(0).totalPages(0).isLast(true)
            .build();
        when(webhookService.getPageableWebhooks(anyInt(), anyInt())).thenReturn(listGetVm);

        mockMvc.perform(get("/backoffice/webhooks/paging")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void listWebhooks_ShouldReturnOk() throws Exception {
        when(webhookService.findAllWebhooks()).thenReturn(List.of(new WebhookVm()));

        mockMvc.perform(get("/backoffice/webhooks")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getWebhook_ShouldReturnOk() throws Exception {
        WebhookDetailVm detailVm = new WebhookDetailVm();
        when(webhookService.findById(1L)).thenReturn(detailVm);

        mockMvc.perform(get("/backoffice/webhooks/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void createWebhook_ShouldReturnCreated() throws Exception {
        WebhookDetailVm detailVm = new WebhookDetailVm();
        when(webhookService.create(any(WebhookPostVm.class))).thenReturn(detailVm);

        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://example.com");
        postVm.setSecret("secret");
        postVm.setIsActive(true);

        ObjectWriter writer = new ObjectMapper().writer();
        String json = writer.writeValueAsString(postVm);

        mockMvc.perform(post("/backoffice/webhooks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated());
    }

    @Test
    void updateWebhook_ShouldReturnNoContent() throws Exception {
        doNothing().when(webhookService).update(any(WebhookPostVm.class), eq(1L));

        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://updated.com");
        postVm.setSecret("secret2");
        postVm.setIsActive(true);

        ObjectWriter writer = new ObjectMapper().writer();
        String json = writer.writeValueAsString(postVm);

        mockMvc.perform(put("/backoffice/webhooks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteWebhook_ShouldReturnNoContent() throws Exception {
        doNothing().when(webhookService).delete(1L);

        mockMvc.perform(delete("/backoffice/webhooks/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }
}
