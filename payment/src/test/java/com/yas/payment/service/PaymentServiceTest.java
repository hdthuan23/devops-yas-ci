package com.yas.payment.service;

import com.yas.payment.model.CapturedPayment;
import com.yas.payment.model.InitiatedPayment;
import com.yas.payment.model.Payment;
import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.repository.PaymentRepository;
import com.yas.payment.service.provider.handler.PaymentHandler;
import com.yas.payment.viewmodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private OrderService orderService;
    private PaymentHandler paymentHandler;
    private List<PaymentHandler> paymentHandlers = new ArrayList<>();
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        orderService = mock(OrderService.class);
        paymentHandler = mock(PaymentHandler.class);
        paymentHandlers.add(paymentHandler);
        paymentService = new PaymentService(paymentRepository, orderService, paymentHandlers);

        when(paymentHandler.getProviderId()).thenReturn(PaymentMethod.PAYPAL.name());
        paymentService.initializeProviders();

        payment = new Payment();
        payment.setId(1L);
        payment.setCheckoutId("secretCheckoutId");
        payment.setOrderId(2L);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentFee(BigDecimal.valueOf(500));
        payment.setPaymentMethod(PaymentMethod.BANKING);
        payment.setAmount(BigDecimal.valueOf(100.0));
        payment.setFailureMessage(null);
        payment.setGatewayTransactionId("gatewayId");
    }

    // =========================================================
    // initPayment tests
    // =========================================================

    @Test
    @DisplayName("initPayment - Success with PAYPAL")
    void initPayment_Success() {
        InitPaymentRequestVm initPaymentRequestVm = InitPaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .totalPrice(BigDecimal.TEN)
            .checkoutId("123")
            .build();
        InitiatedPayment initiatedPayment = InitiatedPayment.builder()
            .paymentId("123")
            .status("success")
            .redirectUrl("http://abc.com")
            .build();

        when(paymentHandler.initPayment(initPaymentRequestVm)).thenReturn(initiatedPayment);

        InitPaymentResponseVm result = paymentService.initPayment(initPaymentRequestVm);

        assertEquals(initiatedPayment.getPaymentId(), result.paymentId());
        assertEquals(initiatedPayment.getStatus(), result.status());
        assertEquals(initiatedPayment.getRedirectUrl(), result.redirectUrl());
        verify(paymentHandler, times(1)).initPayment(initPaymentRequestVm);
    }

    @Test
    @DisplayName("initPayment - Throws exception when payment method not found")
    void initPayment_ThrowsException_WhenProviderNotFound() {
        InitPaymentRequestVm initPaymentRequestVm = InitPaymentRequestVm.builder()
            .paymentMethod("UNKNOWN_METHOD")
            .totalPrice(BigDecimal.TEN)
            .checkoutId("123")
            .build();

        assertThatThrownBy(() -> paymentService.initPayment(initPaymentRequestVm))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("initPayment - Handler returns null status and redirectUrl")
    void initPayment_WhenHandlerReturnsNullFields() {
        InitPaymentRequestVm initPaymentRequestVm = InitPaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .totalPrice(BigDecimal.ZERO)
            .checkoutId("checkout-null")
            .build();
        InitiatedPayment initiatedPayment = InitiatedPayment.builder()
            .paymentId("pid-null")
            .status(null)
            .redirectUrl(null)
            .build();

        when(paymentHandler.initPayment(initPaymentRequestVm)).thenReturn(initiatedPayment);

        InitPaymentResponseVm result = paymentService.initPayment(initPaymentRequestVm);

        assertEquals("pid-null", result.paymentId());
        assertNull(result.status());
        assertNull(result.redirectUrl());
    }

    // =========================================================
    // capturePayment tests
    // =========================================================

    @Test
    @DisplayName("capturePayment - Success with COMPLETED status")
    void capturePayment_Success() {
        CapturePaymentRequestVm capturePaymentRequestVM = CapturePaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .token("123")
            .build();
        CapturedPayment capturedPayment = prepareCapturedPayment();
        Long orderId = 999L;

        when(paymentHandler.capturePayment(capturePaymentRequestVM)).thenReturn(capturedPayment);
        when(orderService.updateCheckoutStatus(capturedPayment)).thenReturn(orderId);
        when(paymentRepository.save(any())).thenReturn(payment);

        CapturePaymentResponseVm capturePaymentResponseVm = paymentService.capturePayment(capturePaymentRequestVM);

        verifyPaymentCreation(capturePaymentResponseVm);
        verifyOrderServiceInteractions(capturedPayment);
        verifyResult(capturedPayment, capturePaymentResponseVm);
    }

    @Test
    @DisplayName("capturePayment - Payment with PENDING status does not call updateOrderStatus")
    void capturePayment_WhenPendingStatus_ShouldNotUpdateOrderStatus() {
        CapturePaymentRequestVm requestVm = CapturePaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .token("token-pending")
            .build();

        CapturedPayment capturedPayment = CapturedPayment.builder()
            .orderId(3L)
            .checkoutId("checkout-pending")
            .amount(BigDecimal.valueOf(50.0))
            .paymentFee(BigDecimal.valueOf(1.0))
            .gatewayTransactionId("txn-pending")
            .paymentMethod(PaymentMethod.PAYPAL)
            .paymentStatus(PaymentStatus.PENDING)
            .failureMessage(null)
            .build();

        Payment pendingPayment = new Payment();
        pendingPayment.setId(2L);
        pendingPayment.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentHandler.capturePayment(requestVm)).thenReturn(capturedPayment);
        when(orderService.updateCheckoutStatus(capturedPayment)).thenReturn(3L);
        when(paymentRepository.save(any())).thenReturn(pendingPayment);

        CapturePaymentResponseVm result = paymentService.capturePayment(requestVm);

        verify(orderService, times(1)).updateCheckoutStatus(capturedPayment);
        // PENDING status should NOT trigger updateOrderStatus
        verify(orderService, times(0)).updateOrderStatus(any());
        assertNotNull(result);
    }

    @Test
    @DisplayName("capturePayment - Payment with FAILED status")
    void capturePayment_WhenFailedStatus_ShouldHandleFailure() {
        CapturePaymentRequestVm requestVm = CapturePaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .token("token-fail")
            .build();

        CapturedPayment capturedPayment = CapturedPayment.builder()
            .orderId(4L)
            .checkoutId("checkout-fail")
            .amount(BigDecimal.valueOf(200.0))
            .paymentFee(BigDecimal.ZERO)
            .gatewayTransactionId("txn-fail")
            .paymentMethod(PaymentMethod.PAYPAL)
            .paymentStatus(PaymentStatus.CANCELLED)
            .failureMessage("Insufficient funds")
            .build();

        Payment failedPayment = new Payment();
        failedPayment.setId(3L);
        failedPayment.setPaymentStatus(PaymentStatus.CANCELLED);
        failedPayment.setFailureMessage("Insufficient funds");

        when(paymentHandler.capturePayment(requestVm)).thenReturn(capturedPayment);
        when(orderService.updateCheckoutStatus(capturedPayment)).thenReturn(4L);
        when(paymentRepository.save(any())).thenReturn(failedPayment);

        CapturePaymentResponseVm result = paymentService.capturePayment(requestVm);

        verify(paymentRepository, times(1)).save(any());
        verify(orderService, times(1)).updateCheckoutStatus(capturedPayment);
        assertNotNull(result);
    }

    @Test
    @DisplayName("capturePayment - Throws exception when provider not found")
    void capturePayment_ThrowsException_WhenProviderNotFound() {
        CapturePaymentRequestVm requestVm = CapturePaymentRequestVm.builder()
            .paymentMethod("NOT_EXIST")
            .token("token")
            .build();

        assertThatThrownBy(() -> paymentService.capturePayment(requestVm))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("capturePayment - Correct Payment entity is saved to repository")
    void capturePayment_ShouldSaveCorrectPaymentEntity() {
        CapturePaymentRequestVm requestVm = CapturePaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .token("token-save")
            .build();

        CapturedPayment capturedPayment = CapturedPayment.builder()
            .orderId(10L)
            .checkoutId("checkout-save")
            .amount(BigDecimal.valueOf(250.0))
            .paymentFee(BigDecimal.valueOf(5.0))
            .gatewayTransactionId("txn-save")
            .paymentMethod(PaymentMethod.PAYPAL)
            .paymentStatus(PaymentStatus.COMPLETED)
            .failureMessage(null)
            .build();

        when(paymentHandler.capturePayment(requestVm)).thenReturn(capturedPayment);
        when(orderService.updateCheckoutStatus(capturedPayment)).thenReturn(10L);
        when(paymentRepository.save(any())).thenReturn(payment);

        paymentService.capturePayment(requestVm);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();

        assertThat(saved.getCheckoutId()).isEqualTo("checkout-save");
        assertThat(saved.getOrderId()).isEqualTo(10L);
        assertThat(saved.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(250.0));
        assertThat(saved.getPaymentFee()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(saved.getGatewayTransactionId()).isEqualTo("txn-save");
        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.PAYPAL);
        assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertNull(saved.getFailureMessage());
    }

    @Test
    @DisplayName("capturePayment - updateOrderStatus called with correct PaymentOrderStatusVm")
    void capturePayment_ShouldCallUpdateOrderStatus_WithCorrectData() {
        CapturePaymentRequestVm requestVm = CapturePaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .token("token-order")
            .build();

        CapturedPayment capturedPayment = CapturedPayment.builder()
            .orderId(20L)
            .checkoutId("checkout-order")
            .amount(BigDecimal.valueOf(300.0))
            .paymentFee(BigDecimal.valueOf(10.0))
            .gatewayTransactionId("txn-order")
            .paymentMethod(PaymentMethod.PAYPAL)
            .paymentStatus(PaymentStatus.COMPLETED)
            .failureMessage(null)
            .build();

        Payment savedPayment = new Payment();
        savedPayment.setId(100L);
        savedPayment.setOrderId(20L);
        savedPayment.setPaymentStatus(PaymentStatus.COMPLETED);

        when(paymentHandler.capturePayment(requestVm)).thenReturn(capturedPayment);
        when(orderService.updateCheckoutStatus(capturedPayment)).thenReturn(20L);
        when(paymentRepository.save(any())).thenReturn(savedPayment);

        paymentService.capturePayment(requestVm);

        ArgumentCaptor<PaymentOrderStatusVm> captor = ArgumentCaptor.forClass(PaymentOrderStatusVm.class);
        verify(orderService, times(1)).updateOrderStatus(captor.capture());
        PaymentOrderStatusVm statusVm = captor.getValue();

        assertThat(statusVm.orderId()).isEqualTo(20L);
        assertThat(statusVm.paymentId()).isEqualTo(100L);
        assertThat(statusVm.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED.name());
    }

    @Test
    @DisplayName("capturePayment - COD payment method success")
    void capturePayment_WithCODMethod_Success() {
        // Setup a COD handler
        PaymentHandler codHandler = mock(PaymentHandler.class);
        when(codHandler.getProviderId()).thenReturn(PaymentMethod.COD.name());
        paymentHandlers.add(codHandler);
        paymentService.initializeProviders();

        CapturePaymentRequestVm requestVm = CapturePaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.COD.name())
            .token("cod-token")
            .build();

        CapturedPayment capturedPayment = CapturedPayment.builder()
            .orderId(5L)
            .checkoutId("checkout-cod")
            .amount(BigDecimal.valueOf(75.0))
            .paymentFee(BigDecimal.ZERO)
            .gatewayTransactionId("txn-cod")
            .paymentMethod(PaymentMethod.COD)
            .paymentStatus(PaymentStatus.COMPLETED)
            .failureMessage(null)
            .build();

        when(codHandler.capturePayment(requestVm)).thenReturn(capturedPayment);
        when(orderService.updateCheckoutStatus(capturedPayment)).thenReturn(5L);
        when(paymentRepository.save(any())).thenReturn(payment);

        CapturePaymentResponseVm result = paymentService.capturePayment(requestVm);

        verify(codHandler, times(1)).capturePayment(requestVm);
        assertNotNull(result);
    }

    // =========================================================
    // initializeProviders tests
    // =========================================================

    @Test
    @DisplayName("initializeProviders - Multiple handlers registered correctly")
    void initializeProviders_MultipleHandlers_RegisteredCorrectly() {
        PaymentHandler anotherHandler = mock(PaymentHandler.class);
        when(anotherHandler.getProviderId()).thenReturn(PaymentMethod.COD.name());
        paymentHandlers.add(anotherHandler);

        paymentService.initializeProviders();

        // Both PAYPAL and COD handlers should be usable
        InitPaymentRequestVm paypalRequest = InitPaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.PAYPAL.name())
            .totalPrice(BigDecimal.ONE)
            .checkoutId("c1")
            .build();
        InitPaymentRequestVm codRequest = InitPaymentRequestVm.builder()
            .paymentMethod(PaymentMethod.COD.name())
            .totalPrice(BigDecimal.ONE)
            .checkoutId("c2")
            .build();

        InitiatedPayment paypalInitiated = InitiatedPayment.builder().paymentId("p1").status("ok").redirectUrl("url1").build();
        InitiatedPayment codInitiated = InitiatedPayment.builder().paymentId("p2").status("ok").redirectUrl("url2").build();

        when(paymentHandler.initPayment(paypalRequest)).thenReturn(paypalInitiated);
        when(anotherHandler.initPayment(codRequest)).thenReturn(codInitiated);

        InitPaymentResponseVm paypalResult = paymentService.initPayment(paypalRequest);
        InitPaymentResponseVm codResult = paymentService.initPayment(codRequest);

        assertEquals("p1", paypalResult.paymentId());
        assertEquals("p2", codResult.paymentId());
    }

    // =========================================================
    // Helper methods
    // =========================================================

    private CapturedPayment prepareCapturedPayment() {
        return CapturedPayment.builder()
            .orderId(2L)
            .checkoutId("secretCheckoutId")
            .amount(BigDecimal.valueOf(100.0))
            .paymentFee(BigDecimal.valueOf(500))
            .gatewayTransactionId("gatewayId")
            .paymentMethod(PaymentMethod.BANKING)
            .paymentStatus(PaymentStatus.COMPLETED)
            .failureMessage(null)
            .build();
    }

    private void verifyPaymentCreation(CapturePaymentResponseVm capturedPayment) {
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        Payment capturedPaymentResult = paymentCaptor.getValue();

        assertThat(capturedPaymentResult.getCheckoutId()).isEqualTo(capturedPayment.checkoutId());
        assertThat(capturedPaymentResult.getOrderId()).isEqualTo(capturedPayment.orderId());
        assertThat(capturedPaymentResult.getPaymentStatus()).isEqualTo(capturedPayment.paymentStatus());
        assertThat(capturedPaymentResult.getPaymentFee()).isEqualByComparingTo(capturedPayment.paymentFee());
        assertThat(capturedPaymentResult.getAmount()).isEqualByComparingTo(capturedPayment.amount());
    }

    private void verifyOrderServiceInteractions(CapturedPayment capturedPayment) {
        verify(orderService, times(1)).updateCheckoutStatus(capturedPayment);
        verify(orderService, times(1)).updateOrderStatus(any(PaymentOrderStatusVm.class));
    }

    private void verifyResult(CapturedPayment capturedPayment, CapturePaymentResponseVm responseVm) {
        assertEquals(capturedPayment.getOrderId(), responseVm.orderId());
        assertEquals(capturedPayment.getCheckoutId(), responseVm.checkoutId());
        assertEquals(capturedPayment.getAmount(), responseVm.amount());
        assertEquals(capturedPayment.getPaymentFee(), responseVm.paymentFee());
        assertEquals(capturedPayment.getGatewayTransactionId(), responseVm.gatewayTransactionId());
        assertEquals(capturedPayment.getPaymentMethod(), responseVm.paymentMethod());
        assertEquals(capturedPayment.getPaymentStatus(), responseVm.paymentStatus());
        assertEquals(capturedPayment.getFailureMessage(), responseVm.failureMessage());
    }
}