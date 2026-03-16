package com.yas.delivery;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.delivery.controller.DeliveryController;
import com.yas.delivery.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

class DeliveryLayerAnnotationTest {

    @Test
    void deliveryController_ShouldHaveRestControllerAnnotation() {
        assertTrue(DeliveryController.class.isAnnotationPresent(RestController.class));
    }

    @Test
    void deliveryService_ShouldHaveServiceAnnotation() {
        assertTrue(DeliveryService.class.isAnnotationPresent(Service.class));
    }

    @Test
    void classes_ShouldBeInstantiable() {
        assertNotNull(new DeliveryController());
        assertNotNull(new DeliveryService());
    }
}