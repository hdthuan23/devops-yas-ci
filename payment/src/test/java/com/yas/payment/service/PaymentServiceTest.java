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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@DisplayName("Payment Service Unit Tests")
class PaymentServiceTest {
    private PaymentRepository paymentRepository;
    private OrderService orderService;
    private PaymentHandler paymentHandler;
    private PaymentHandler bankingPaymentHandler;
    private List<PaymentHandler> paymentHandlers = new ArrayList<>();
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        orderService = mock(OrderService.class);
        paymentHandler = mock(PaymentHandler.class);
        bankingPaymentHandler = mock(PaymentHandler.class);
        paymentHandlers.add(paymentHandler);
        paymentHandlers.add(bankingPaymentHandler);
        paymentService = new PaymentService(paymentRepository, orderService, paymentHandlers);

        when(paymentHandler.getProviderId()).thenReturn(PaymentMethod.PAYPAL.name());
        when(bankingPaymentHandler.getProviderId()).thenReturn(PaymentMethod.BANKING.name());
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

    @Nested
    @DisplayName("Initiate Payment Tests")
    class InitPaymentTests {
        @Test
        @DisplayName("Should successfully initiate payment with PayPal")
        void initPayment_withPaypal_success() {
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
            
            assertNotNull(result);
            assertEquals(initiatedPayment.getPaymentId(), result.paymentId());
            assertEquals(initiatedPayment.getStatus(), result.status());
            assertEquals(initiatedPayment.getRedirectUrl(), result.redirectUrl());
            verify(paymentHandler, times(1)).initPayment(initPaymentRequestVm);
        }

        @Test
        @DisplayName("Should successfully initiate payment with banking")
        void initPayment_withBanking_success() {
            InitPaymentRequestVm initPaymentRequestVm = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .totalPrice(BigDecimal.valueOf(100))
                    .checkoutId("456")
                    .build();
            InitiatedPayment initiatedPayment = InitiatedPayment.builder()
                    .paymentId("456")
                    .status("pending")
                    .redirectUrl("http://banking.com")
                    .build();
            when(bankingPaymentHandler.initPayment(initPaymentRequestVm)).thenReturn(initiatedPayment);
            
            InitPaymentResponseVm result = paymentService.initPayment(initPaymentRequestVm);
            
            assertNotNull(result);
            assertEquals("456", result.paymentId());
            verify(bankingPaymentHandler, times(1)).initPayment(initPaymentRequestVm);
        }

        @Test
        @DisplayName("Should throw exception for invalid payment provider")
        void initPayment_withInvalidProvider_throwsException() {
            InitPaymentRequestVm initPaymentRequestVm = InitPaymentRequestVm.builder()
                    .paymentMethod("INVALID_PROVIDER")
                    .totalPrice(BigDecimal.TEN)
                    .checkoutId("123")
                    .build();
            
            assertThatThrownBy(() -> paymentService.initPayment(initPaymentRequestVm))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No payment handler found for provider");
        }
    }

    @Nested
    @DisplayName("Capture Payment Tests")
    class CapturePaymentTests {
        @Test
        @DisplayName("Should successfully capture payment with PayPal")
        void capturePayment_withPaypal_success() {
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
            
            assertNotNull(capturePaymentResponseVm);
            verifyPaymentCreation(capturePaymentResponseVm);
            verifyOrderServiceInteractions(capturedPayment);
            verifyResult(capturedPayment, capturePaymentResponseVm);
        }

        @Test
        @DisplayName("Should successfully capture payment with different amounts")
        void capturePayment_withDifferentAmount_success() {
            CapturePaymentRequestVm capturePaymentRequestVM = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .token("456")
                    .build();
            
            CapturedPayment capturedPayment = CapturedPayment.builder()
                    .orderId(3L)
                    .checkoutId("456")
                    .amount(BigDecimal.valueOf(500))
                    .paymentFee(BigDecimal.valueOf(10))
                    .gatewayTransactionId("txn-789")
                    .paymentMethod(PaymentMethod.BANKING)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();
            
            when(bankingPaymentHandler.capturePayment(capturePaymentRequestVM)).thenReturn(capturedPayment);
            when(orderService.updateCheckoutStatus(capturedPayment)).thenReturn(3L);
            when(paymentRepository.save(any())).thenReturn(payment);
            
            CapturePaymentResponseVm result = paymentService.capturePayment(capturePaymentRequestVM);
            
            assertNotNull(result);
            assertEquals(3L, result.orderId());
            assertEquals(BigDecimal.valueOf(500), result.amount());
            verify(bankingPaymentHandler, times(1)).capturePayment(capturePaymentRequestVM);
        }

        @Test
        @DisplayName("Should throw exception for invalid payment provider on capture")
        void capturePayment_withInvalidProvider_throwsException() {
            CapturePaymentRequestVm capturePaymentRequestVM = CapturePaymentRequestVm.builder()
                    .paymentMethod("UNKNOWN")
                    .token("123")
                    .build();
            
            assertThatThrownBy(() -> paymentService.capturePayment(capturePaymentRequestVM))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No payment handler found for provider");
        }

        @Test
        @DisplayName("Should handle cancelled payment capture")
        void capturePayment_withCancelledStatus_success() {
            CapturePaymentRequestVm capturePaymentRequestVM = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .token("cancelled-123")
                    .build();
            
            CapturedPayment capturedPayment = CapturedPayment.builder()
                    .orderId(4L)
                    .checkoutId("cancelled-checkout")
                    .amount(BigDecimal.valueOf(100))
                    .paymentFee(BigDecimal.ZERO)
                    .gatewayTransactionId("cancelled-txn")
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .paymentStatus(PaymentStatus.CANCELLED)
                    .failureMessage("Payment cancelled by user")
                    .build();
            
            when(paymentHandler.capturePayment(capturePaymentRequestVM)).thenReturn(capturedPayment);
            when(orderService.updateCheckoutStatus(capturedPayment)).thenReturn(4L);
            when(paymentRepository.save(any())).thenReturn(payment);
            
            CapturePaymentResponseVm result = paymentService.capturePayment(capturePaymentRequestVM);
            
            assertNotNull(result);
            assertEquals(PaymentStatus.CANCELLED, result.paymentStatus());
            assertEquals("Payment cancelled by user", result.failureMessage());
            verify(orderService, times(1)).updateCheckoutStatus(capturedPayment);
            verify(orderService, times(1)).updateOrderStatus(any(PaymentOrderStatusVm.class));
        }
    }

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
        verify(orderService, times(1)).updateCheckoutStatus((capturedPayment));
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

    @Nested
    @DisplayName("Provider Initialization Tests")
    class ProviderInitializationTests {
        @Test
        @DisplayName("Should initialize payment providers correctly")
        void initializeProviders_shouldRegisterAllHandlers() {
            InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .totalPrice(BigDecimal.TEN)
                    .checkoutId("test")
                    .build();
            
            InitiatedPayment initiatedPayment = InitiatedPayment.builder()
                    .paymentId("123")
                    .status("success")
                    .redirectUrl("http://test.com")
                    .build();
            
            when(paymentHandler.initPayment(request)).thenReturn(initiatedPayment);
            
            InitPaymentResponseVm result = paymentService.initPayment(request);
            
            assertNotNull(result);
            assertEquals("123", result.paymentId());
        }

        @Test
        @DisplayName("Should support multiple payment handlers")
        void initializeProviders_withMultipleHandlers_shouldSupportAll() {
            InitPaymentRequestVm paypalRequest = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .totalPrice(BigDecimal.TEN)
                    .checkoutId("123")
                    .build();
            
            InitPaymentRequestVm bankingRequest = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .totalPrice(BigDecimal.TEN)
                    .checkoutId("456")
                    .build();
            
            InitiatedPayment paypalPayment = InitiatedPayment.builder()
                    .paymentId("pp-123")
                    .status("success")
                    .redirectUrl("http://paypal.com")
                    .build();
            
            InitiatedPayment bankingPayment = InitiatedPayment.builder()
                    .paymentId("bk-456")
                    .status("pending")
                    .redirectUrl("http://banking.com")
                    .build();
            
            when(paymentHandler.initPayment(paypalRequest)).thenReturn(paypalPayment);
            when(bankingPaymentHandler.initPayment(bankingRequest)).thenReturn(bankingPayment);
            
            InitPaymentResponseVm paypalResult = paymentService.initPayment(paypalRequest);
            InitPaymentResponseVm bankingResult = paymentService.initPayment(bankingRequest);
            
            assertNotNull(paypalResult);
            assertNotNull(bankingResult);
            assertEquals("pp-123", paypalResult.paymentId());
            assertEquals("bk-456", bankingResult.paymentId());
        }
    }

    @Nested
    @DisplayName("Payment Response Construction Tests")
    class PaymentResponseConstructionTests {
        @Test
        @DisplayName("Should construct initPayment response correctly")
        void initPaymentResponse_shouldHaveAllRequiredFields() {
            InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .totalPrice(BigDecimal.valueOf(99.99))
                    .checkoutId("checkout-001")
                    .build();
            
            InitiatedPayment initiatedPayment = InitiatedPayment.builder()
                    .paymentId("payment-001")
                    .status("initialized")
                    .redirectUrl("https://secure.paypal.com/checkout")
                    .build();
            
            when(paymentHandler.initPayment(request)).thenReturn(initiatedPayment);
            
            InitPaymentResponseVm response = paymentService.initPayment(request);
            
            assertNotNull(response);
            assertNotNull(response.paymentId());
            assertNotNull(response.status());
            assertNotNull(response.redirectUrl());
            assertEquals("payment-001", response.paymentId());
            assertEquals("initialized", response.status());
            assertEquals("https://secure.paypal.com/checkout", response.redirectUrl());
        }

        @Test
        @DisplayName("Should construct capturePayment response with complete payment details")
        void capturePaymentResponse_shouldContainAllPaymentInformation() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .token("token-xyz")
                    .build();
            
            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(100L)
                    .checkoutId("checkout-100")
                    .amount(BigDecimal.valueOf(1500))
                    .paymentFee(BigDecimal.valueOf(50))
                    .gatewayTransactionId("gateway-001")
                    .paymentMethod(PaymentMethod.BANKING)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();
            
            when(bankingPaymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(100L);
            when(paymentRepository.save(any())).thenReturn(payment);
            
            CapturePaymentResponseVm response = paymentService.capturePayment(request);
            
            assertNotNull(response);
            assertEquals(100L, response.orderId());
            assertEquals("checkout-100", response.checkoutId());
            assertEquals(BigDecimal.valueOf(1500), response.amount());
            assertEquals(BigDecimal.valueOf(50), response.paymentFee());
            assertEquals("gateway-001", response.gatewayTransactionId());
            assertEquals(PaymentMethod.BANKING, response.paymentMethod());
            assertEquals(PaymentStatus.COMPLETED, response.paymentStatus());
            assertNull(response.failureMessage());
        }

        @Test
        @DisplayName("Should handle response with failure message")
        void capturePaymentResponse_withFailureMessage_shouldIncludeMessage() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .token("failed-token")
                    .build();
            
            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(200L)
                    .checkoutId("checkout-200")
                    .amount(BigDecimal.valueOf(2000))
                    .paymentFee(BigDecimal.ZERO)
                    .gatewayTransactionId("gateway-002")
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .paymentStatus(PaymentStatus.CANCELLED)
                    .failureMessage("Insufficient funds")
                    .build();
            
            when(paymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(200L);
            when(paymentRepository.save(any())).thenReturn(payment);
            
            CapturePaymentResponseVm response = paymentService.capturePayment(request);
            
            assertNotNull(response);
            assertEquals("Insufficient funds", response.failureMessage());
            assertEquals(PaymentStatus.CANCELLED, response.paymentStatus());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle pending payment status")
        void capturePayment_withPendingStatus_success() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .token("pending-token")
                    .build();
            
            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(5L)
                    .checkoutId("pending-checkout")
                    .amount(BigDecimal.valueOf(500))
                    .paymentFee(BigDecimal.valueOf(5))
                    .gatewayTransactionId("pending-txn")
                    .paymentMethod(PaymentMethod.BANKING)
                    .paymentStatus(PaymentStatus.PENDING)
                    .failureMessage(null)
                    .build();
            
            when(bankingPaymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(5L);
            when(paymentRepository.save(any())).thenReturn(payment);
            
            CapturePaymentResponseVm response = paymentService.capturePayment(request);
            
            assertNotNull(response);
            assertEquals(PaymentStatus.PENDING, response.paymentStatus());
            verify(orderService, times(1)).updateOrderStatus(any(PaymentOrderStatusVm.class));
        }

        @Test
        @DisplayName("Should verify payment repository save is called with correct payment")
        void capturePayment_shouldPersistPaymentWithCorrectData() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .token("save-test")
                    .build();
            
            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(6L)
                    .checkoutId("save-checkout")
                    .amount(BigDecimal.valueOf(300))
                    .paymentFee(BigDecimal.valueOf(10))
                    .gatewayTransactionId("save-txn")
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();
            
            when(paymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(6L);
            when(paymentRepository.save(any())).thenReturn(payment);
            
            paymentService.capturePayment(request);
            
            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());
            
            Payment savedPayment = captor.getValue();
            assertEquals("save-checkout", savedPayment.getCheckoutId());
            assertEquals(6L, savedPayment.getOrderId());
            assertEquals(PaymentMethod.PAYPAL, savedPayment.getPaymentMethod());
            assertEquals(PaymentStatus.COMPLETED, savedPayment.getPaymentStatus());
        }

        @Test
        @DisplayName("Should handle zero payment fee")
        void capturePayment_withZeroFee_success() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .token("zero-fee-token")
                    .build();
            
            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(7L)
                    .checkoutId("zero-fee-checkout")
                    .amount(BigDecimal.valueOf(1000))
                    .paymentFee(BigDecimal.ZERO)
                    .gatewayTransactionId("zero-fee-txn")
                    .paymentMethod(PaymentMethod.BANKING)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();
            
            when(bankingPaymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(7L);
            when(paymentRepository.save(any())).thenReturn(payment);
            
            CapturePaymentResponseVm response = paymentService.capturePayment(request);
            
            assertNotNull(response);
            assertEquals(BigDecimal.ZERO, response.paymentFee());
        }
    }

    @Nested
    @DisplayName("Payment Handler Exception Tests")
    class PaymentHandlerExceptionTests {
        @Test
        @DisplayName("Should throw exception when handler not found")
        void initPayment_handlerNotFound_throwsException() {
            InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                    .paymentMethod("UNKNOWN_PROVIDER")
                    .totalPrice(BigDecimal.valueOf(100))
                    .checkoutId("unknown-checkout")
                    .build();

            assertThatThrownBy(() -> paymentService.initPayment(request))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should throw exception when capture handler not found")
        void capturePayment_handlerNotFound_throwsException() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod("NONEXISTENT")
                    .token("invalid-token")
                    .build();

            assertThatThrownBy(() -> paymentService.capturePayment(request))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Multiple Payment Providers Tests")
    class MultiplePaymentProvidersTests {
        @Test
        @DisplayName("Should handle multiple payment provider initialization")
        void initializeProviders_multipleHandlers() {
            verify(paymentHandler, times(1)).getProviderId();
            verify(bankingPaymentHandler, times(1)).getProviderId();
            
            assertEquals(2, paymentHandlers.size());
        }

        @Test
        @DisplayName("Should verify multiple payment handlers registered")
        void paymentHandlers_multipleProvidersRegistered() {
            assertEquals(2, paymentHandlers.size());
            verify(paymentHandler, times(1)).getProviderId();
            verify(bankingPaymentHandler, times(1)).getProviderId();
        }
    }

    @Nested
    @DisplayName("Large Amount Payment Tests")
    class LargeAmountPaymentTests {
        @Test
        @DisplayName("Should handle large payment amounts")
        void initPayment_largeAmount() {
            InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .totalPrice(BigDecimal.valueOf(999999.99))
                    .checkoutId("large-amount-checkout")
                    .build();

            InitiatedPayment initiatedPayment = InitiatedPayment.builder()
                    .paymentId("large-payment-123")
                    .status("success")
                    .redirectUrl("http://paypal.com/large")
                    .build();

            when(paymentHandler.initPayment(request)).thenReturn(initiatedPayment);

            InitPaymentResponseVm result = paymentService.initPayment(request);

            assertNotNull(result);
            assertEquals("large-payment-123", result.paymentId());
        }

        @Test
        @DisplayName("Should capture large amount payment")
        void capturePayment_largeAmount() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .token("large-amount-token")
                    .build();

            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(100L)
                    .checkoutId("large-capture-checkout")
                    .amount(BigDecimal.valueOf(500000.00))
                    .paymentFee(BigDecimal.valueOf(5000))
                    .gatewayTransactionId("large-txn")
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();

            when(paymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(100L);
            when(paymentRepository.save(any())).thenReturn(payment);

            CapturePaymentResponseVm response = paymentService.capturePayment(request);

            assertNotNull(response);
            assertEquals(BigDecimal.valueOf(500000.00), response.amount());
        }
    }

    @Nested
    @DisplayName("Payment Status Response Tests")
    class PaymentStatusResponseTests {
        @Test
        @DisplayName("Should return correct payment response structure")
        void capturePayment_verifyResponseStructure() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .token("response-test")
                    .build();

            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(50L)
                    .checkoutId("response-checkout")
                    .amount(BigDecimal.valueOf(250.00))
                    .paymentFee(BigDecimal.valueOf(5.00))
                    .gatewayTransactionId("response-txn")
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();

            when(paymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(50L);
            when(paymentRepository.save(any())).thenReturn(payment);

            CapturePaymentResponseVm response = paymentService.capturePayment(request);

            assertNotNull(response.checkoutId());
            assertNotNull(response.paymentStatus());
            assertNotNull(response.amount());
            assertNotNull(response.paymentFee());
        }

        @Test
        @DisplayName("Should handle failure message in response")
        void capturePayment_withFailureMessage() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .token("failure-token")
                    .build();

            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(51L)
                    .checkoutId("failure-checkout")
                    .amount(BigDecimal.valueOf(0))
                    .paymentFee(BigDecimal.ZERO)
                    .gatewayTransactionId("failure-txn")
                    .paymentMethod(PaymentMethod.BANKING)
                    .paymentStatus(PaymentStatus.CANCELLED)
                    .failureMessage("Card declined")
                    .build();

            when(bankingPaymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(51L);
            when(paymentRepository.save(any())).thenReturn(payment);

            CapturePaymentResponseVm response = paymentService.capturePayment(request);

            assertNotNull(response);
            assertEquals(PaymentStatus.CANCELLED, response.paymentStatus());
            assertEquals("Card declined", response.failureMessage());
        }
    }

    @Nested
    @DisplayName("Capture Payment with Banking Provider Tests")
    class CapturePaymentBankingTests {
        @Test
        @DisplayName("Should capture banking payment successfully")
        void capturePayment_banking_success() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .token("banking-token-123")
                    .build();

            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(200L)
                    .checkoutId("banking-checkout")
                    .amount(BigDecimal.valueOf(1500.00))
                    .paymentFee(BigDecimal.valueOf(15.00))
                    .gatewayTransactionId("banking-txn-123")
                    .paymentMethod(PaymentMethod.BANKING)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();

            when(bankingPaymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(200L);
            when(paymentRepository.save(any())).thenReturn(payment);

            CapturePaymentResponseVm response = paymentService.capturePayment(request);

            assertNotNull(response);
            assertEquals(PaymentStatus.COMPLETED, response.paymentStatus());
            assertEquals(BigDecimal.valueOf(1500.00), response.amount());
            verify(bankingPaymentHandler).capturePayment(request);
        }

        @Test
        @DisplayName("Should update order status for banking payment")
        void capturePayment_banking_updatesOrderStatus() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .token("banking-update")
                    .build();

            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(201L)
                    .checkoutId("update-checkout")
                    .amount(BigDecimal.valueOf(800.00))
                    .paymentFee(BigDecimal.valueOf(8.00))
                    .gatewayTransactionId("update-txn")
                    .paymentMethod(PaymentMethod.BANKING)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();

            when(bankingPaymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(201L);
            when(paymentRepository.save(any())).thenReturn(payment);

            paymentService.capturePayment(request);

            verify(orderService).updateCheckoutStatus(captured);
            verify(orderService).updateOrderStatus(any(PaymentOrderStatusVm.class));
        }
    }

    @Nested
    @DisplayName("Init Payment with Different Amounts Tests")
    class InitPaymentAmountTests {
        @Test
        @DisplayName("Should handle minimum amount payment")
        void initPayment_minimumAmount() {
            InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .totalPrice(BigDecimal.valueOf(0.01))
                    .checkoutId("min-amount")
                    .build();

            InitiatedPayment response = InitiatedPayment.builder()
                    .paymentId("min-payment")
                    .status("success")
                    .redirectUrl("http://min.paypal.com")
                    .build();

            when(paymentHandler.initPayment(request)).thenReturn(response);

            InitPaymentResponseVm result = paymentService.initPayment(request);

            assertNotNull(result);
            assertEquals("min-payment", result.paymentId());
        }

        @Test
        @DisplayName("Should handle payment with specific amount precision")
        void initPayment_precisionAmount() {
            InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .totalPrice(BigDecimal.valueOf(123.45))
                    .checkoutId("precision-checkout")
                    .build();

            InitiatedPayment response = InitiatedPayment.builder()
                    .paymentId("precision-payment")
                    .status("success")
                    .redirectUrl("http://paypal.com")
                    .build();

            when(paymentHandler.initPayment(request)).thenReturn(response);

            InitPaymentResponseVm result = paymentService.initPayment(request);

            assertNotNull(result);
            assertEquals("precision-payment", result.paymentId());
        }
    }

    @Nested
    @DisplayName("Payment Persistence Tests")
    class PaymentPersistenceTests {
        @Test
        @DisplayName("Should persist payment with all required fields")
        void capturePayment_persistsAllFields() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .token("persist-token")
                    .build();

            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(300L)
                    .checkoutId("persist-checkout")
                    .amount(BigDecimal.valueOf(250.00))
                    .paymentFee(BigDecimal.valueOf(5.00))
                    .gatewayTransactionId("persist-txn")
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();

            when(paymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(300L);
            when(paymentRepository.save(any())).thenReturn(payment);

            paymentService.capturePayment(request);

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());
            
            Payment savedPayment = captor.getValue();
            assertEquals(PaymentMethod.PAYPAL, savedPayment.getPaymentMethod());
            assertEquals(PaymentStatus.COMPLETED, savedPayment.getPaymentStatus());
            assertEquals(BigDecimal.valueOf(250.00), savedPayment.getAmount());
        }

        @Test
        @DisplayName("Should save payment with gateway transaction id")
        void capturePayment_savesGatewayTransactionId() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .token("gateway-token")
                    .build();

            CapturedPayment captured = CapturedPayment.builder()
                    .orderId(301L)
                    .checkoutId("gateway-checkout")
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentFee(BigDecimal.valueOf(2.00))
                    .gatewayTransactionId("gateway-txn-unique-id")
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();

            when(paymentHandler.capturePayment(request)).thenReturn(captured);
            when(orderService.updateCheckoutStatus(captured)).thenReturn(301L);
            when(paymentRepository.save(any())).thenReturn(payment);

            paymentService.capturePayment(request);

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());
            
            Payment savedPayment = captor.getValue();
            assertEquals("gateway-txn-unique-id", savedPayment.getGatewayTransactionId());
        }
    }

    @Nested
    @DisplayName("Payment Handler Integration Tests")
    class PaymentHandlerIntegrationTests {
        @Test
        @DisplayName("Should call correct handler for paypal provider")
        void initPayment_callsPaypalHandler() {
            InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.PAYPAL.name())
                    .totalPrice(BigDecimal.valueOf(100))
                    .checkoutId("handler-test")
                    .build();

            InitiatedPayment response = InitiatedPayment.builder()
                    .paymentId("handler-payment")
                    .status("success")
                    .redirectUrl("http://handler.test")
                    .build();

            when(paymentHandler.initPayment(request)).thenReturn(response);

            paymentService.initPayment(request);

            verify(paymentHandler).initPayment(request);
            verify(bankingPaymentHandler, never()).initPayment(any());
        }

        @Test
        @DisplayName("Should call correct handler for banking provider")
        void capturePayment_callsBankingHandler() {
            CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                    .paymentMethod(PaymentMethod.BANKING.name())
                    .token("banking-handler")
                    .build();

            CapturedPayment response = CapturedPayment.builder()
                    .orderId(302L)
                    .checkoutId("handler-banking")
                    .amount(BigDecimal.valueOf(500))
                    .paymentFee(BigDecimal.valueOf(5))
                    .gatewayTransactionId("banking-handler-txn")
                    .paymentMethod(PaymentMethod.BANKING)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .failureMessage(null)
                    .build();

            when(bankingPaymentHandler.capturePayment(request)).thenReturn(response);
            when(orderService.updateCheckoutStatus(response)).thenReturn(302L);
            when(paymentRepository.save(any())).thenReturn(payment);

            paymentService.capturePayment(request);

            verify(bankingPaymentHandler).capturePayment(request);
        }
    }

}


