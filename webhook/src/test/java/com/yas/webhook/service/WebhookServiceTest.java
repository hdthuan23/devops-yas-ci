package com.yas.webhook.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.webhook.integration.api.WebhookApi;
import com.yas.webhook.model.Event;
import com.yas.webhook.model.Webhook;
import com.yas.webhook.model.WebhookEvent;
import com.yas.webhook.model.WebhookEventNotification;
import com.yas.webhook.model.dto.WebhookEventNotificationDto;
import com.yas.webhook.model.mapper.WebhookMapper;
import com.yas.webhook.model.viewmodel.webhook.EventVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookDetailVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookListGetVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookPostVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookVm;
import com.yas.webhook.repository.EventRepository;
import com.yas.webhook.repository.WebhookEventNotificationRepository;
import com.yas.webhook.repository.WebhookEventRepository;
import com.yas.webhook.repository.WebhookRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    WebhookRepository webhookRepository;
    @Mock
    EventRepository eventRepository;
    @Mock
    WebhookEventRepository webhookEventRepository;
    @Mock
    WebhookEventNotificationRepository webhookEventNotificationRepository;
    @Mock
    WebhookMapper webhookMapper;
    @Mock
    WebhookApi webHookApi;

    @InjectMocks
    WebhookService webhookService;

    @Test
    void test_notifyToWebhook_ShouldNotException() {
        WebhookEventNotificationDto notificationDto = WebhookEventNotificationDto
            .builder()
            .notificationId(1L)
            .url("")
            .secret("")
            .build();

        WebhookEventNotification notification = new WebhookEventNotification();
        when(webhookEventNotificationRepository.findById(notificationDto.getNotificationId()))
            .thenReturn(Optional.of(notification));

        webhookService.notifyToWebhook(notificationDto);

        verify(webhookEventNotificationRepository).save(notification);
        verify(webHookApi).notify(notificationDto.getUrl(), notificationDto.getSecret(), notificationDto.getPayload());
    }

    @Test
    void getPageableWebhooks_ShouldReturnList() {
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        Page<Webhook> page = new PageImpl<>(List.of(webhook));
        WebhookListGetVm expectedVm = WebhookListGetVm.builder().build();

        when(webhookRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(webhookMapper.toWebhookListGetVm(page, 0, 10)).thenReturn(expectedVm);

        WebhookListGetVm result = webhookService.getPageableWebhooks(0, 10);
        assertNotNull(result);
    }

    @Test
    void findAllWebhooks_ShouldReturnList() {
        Webhook webhook = new Webhook();
        WebhookVm webhookVm = new WebhookVm();

        when(webhookRepository.findAll(any(Sort.class))).thenReturn(List.of(webhook));
        when(webhookMapper.toWebhookVm(webhook)).thenReturn(webhookVm);

        List<WebhookVm> result = webhookService.findAllWebhooks();
        assertEquals(1, result.size());
    }

    @Test
    void findById_Success() {
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        WebhookDetailVm detailVm = new WebhookDetailVm();

        when(webhookRepository.findById(1L)).thenReturn(Optional.of(webhook));
        when(webhookMapper.toWebhookDetailVm(webhook)).thenReturn(detailVm);

        WebhookDetailVm result = webhookService.findById(1L);
        assertNotNull(result);
    }

    @Test
    void findById_WhenNotFound_ThrowsNotFoundException() {
        when(webhookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> webhookService.findById(99L));
    }

    @Test
    void create_WithoutEvents_ShouldReturnDetail() {
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://example.com");
        postVm.setSecret("secret");
        postVm.setIsActive(true);
        postVm.setEvents(null);

        Webhook createdWebhook = new Webhook();
        createdWebhook.setId(1L);
        WebhookDetailVm detailVm = new WebhookDetailVm();

        when(webhookMapper.toCreatedWebhook(postVm)).thenReturn(createdWebhook);
        when(webhookRepository.save(createdWebhook)).thenReturn(createdWebhook);
        when(webhookMapper.toWebhookDetailVm(createdWebhook)).thenReturn(detailVm);

        WebhookDetailVm result = webhookService.create(postVm);
        assertNotNull(result);
    }

    @Test
    void create_WithEvents_ShouldReturnDetailWithEvents() {
        EventVm eventVm = EventVm.builder().id(1L).build();
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://example.com");
        postVm.setSecret("secret");
        postVm.setIsActive(true);
        postVm.setEvents(List.of(eventVm));

        Webhook createdWebhook = new Webhook();
        createdWebhook.setId(1L);
        Event event = new Event();
        WebhookEvent webhookEvent = new WebhookEvent();
        WebhookDetailVm detailVm = new WebhookDetailVm();

        when(webhookMapper.toCreatedWebhook(postVm)).thenReturn(createdWebhook);
        when(webhookRepository.save(createdWebhook)).thenReturn(createdWebhook);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(webhookEventRepository.saveAll(anyList())).thenReturn(List.of(webhookEvent));
        when(webhookMapper.toWebhookDetailVm(createdWebhook)).thenReturn(detailVm);

        WebhookDetailVm result = webhookService.create(postVm);
        assertNotNull(result);
    }

    @Test
    void update_Success() {
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://updated.com");
        postVm.setEvents(null);

        Webhook existingWebhook = new Webhook();
        existingWebhook.setId(1L);
        existingWebhook.setWebhookEvents(new ArrayList<>());

        when(webhookRepository.findById(1L)).thenReturn(Optional.of(existingWebhook));
        when(webhookMapper.toUpdatedWebhook(existingWebhook, postVm)).thenReturn(existingWebhook);

        webhookService.update(postVm, 1L);

        verify(webhookRepository).save(existingWebhook);
    }

    @Test
    void update_WhenNotFound_ThrowsNotFoundException() {
        WebhookPostVm postVm = new WebhookPostVm();
        when(webhookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> webhookService.update(postVm, 99L));
    }

    @Test
    void delete_Success() {
        when(webhookRepository.existsById(1L)).thenReturn(true);

        webhookService.delete(1L);

        verify(webhookEventRepository).deleteByWebhookId(1L);
        verify(webhookRepository).deleteById(1L);
    }

    @Test
    void delete_WhenNotFound_ThrowsNotFoundException() {
        when(webhookRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> webhookService.delete(99L));
    }
}
