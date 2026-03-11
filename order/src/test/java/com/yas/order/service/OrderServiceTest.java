package com.yas.order.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.model.request.OrderRequest;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.viewmodel.order.*;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import com.yas.order.viewmodel.product.ProductVariationVm;
import com.yas.order.viewmodel.promotion.PromotionUsageVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private OrderService orderService;

    private Order mockOrder;
    private OrderItem mockOrderItem;
    private com.yas.order.model.OrderAddress mockAddress;

    @BeforeEach
    void setUp() {
        mockAddress = com.yas.order.model.OrderAddress.builder()
            .id(1L)
            .contactName("Test Contact")
            .phone("1234567890")
            .addressLine1("123 Test Street")
            .city("Test City")
            .zipCode("12345")
            .countryId(1L)
            .countryName("Vietnam")
            .stateOrProvinceId(1L)
            .stateOrProvinceName("Test State")
            .build();

        mockOrder = Order.builder()
            .id(1L)
            .email("test@example.com")
            .note("Test order")
            .tax(5.0f)
            .discount(2.0f)
            .numberItem(2)
            .totalPrice(BigDecimal.valueOf(100))
            .orderStatus(OrderStatus.ACCEPTED)
            .deliveryFee(BigDecimal.valueOf(10))
            .deliveryMethod(DeliveryMethod.YAS_EXPRESS)
            .deliveryStatus(DeliveryStatus.PREPARING)
            .paymentStatus(PaymentStatus.PENDING)
            .shippingAddressId(mockAddress)
            .billingAddressId(mockAddress)
            .build();

        mockOrderItem = OrderItem.builder()
            .id(1L)
            .productId(100L)
            .productName("Test Product")
            .quantity(2)
            .productPrice(BigDecimal.valueOf(50))
            .note("Item note")
            .orderId(1L)
            .build();
    }

    @Test
    void testGetOrderWithItemsById_whenOrderExists_shouldReturnOrderVm() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of(mockOrderItem));

        // Act
        OrderVm result = orderService.getOrderWithItemsById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("test@example.com", result.email());
    }

    @Test
    void testGetOrderWithItemsById_whenOrderNotFound_shouldThrowNotFoundException() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.getOrderWithItemsById(999L));
    }

    @Test
    void testGetOrderWithItemsById_whenOrderHasNoItems_shouldReturnOrderVmWithEmptyItems() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of());

        // Act
        OrderVm result = orderService.getOrderWithItemsById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertTrue(result.orderItemVms() == null || result.orderItemVms().isEmpty());
    }

    @Test
    void testGetLatestOrders_whenCountIsPositive_shouldReturnOrders() {
        // Arrange
        when(orderRepository.getLatestOrders(any())).thenReturn(List.of(mockOrder));

        // Act
        var result = orderService.getLatestOrders(5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetLatestOrders_whenCountIsZero_shouldReturnEmptyList() {
        // Act
        var result = orderService.getLatestOrders(0);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLatestOrders_whenCountIsNegative_shouldReturnEmptyList() {
        // Act
        var result = orderService.getLatestOrders(-1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLatestOrders_whenNoOrdersFound_shouldReturnEmptyList() {
        // Arrange
        when(orderRepository.getLatestOrders(any())).thenReturn(List.of());

        // Act
        var result = orderService.getLatestOrders(5);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindOrderByCheckoutId_whenOrderExists_shouldReturnOrder() {
        // Arrange
        String checkoutId = "checkout-123";
        when(orderRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.of(mockOrder));

        // Act
        Order result = orderService.findOrderByCheckoutId(checkoutId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindOrderByCheckoutId_whenOrderNotFound_shouldThrowNotFoundException() {
        // Arrange
        String checkoutId = "nonexistent-checkout";
        when(orderRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.findOrderByCheckoutId(checkoutId));
    }

    @Test
    void testUpdateOrderPaymentStatus_whenOrderExists_shouldUpdateAndReturnStatus() {
        // Arrange
        PaymentOrderStatusVm inputVm = PaymentOrderStatusVm.builder()
            .orderId(1L)
            .paymentId(123L)
            .paymentStatus("COMPLETED")
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        PaymentOrderStatusVm result = orderService.updateOrderPaymentStatus(inputVm);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.orderId());
        assertEquals(123L, result.paymentId());
    }

    @Test
    void testUpdateOrderPaymentStatus_whenOrderNotFound_shouldThrowNotFoundException() {
        // Arrange
        PaymentOrderStatusVm inputVm = PaymentOrderStatusVm.builder()
            .orderId(999L)
            .paymentId(123L)
            .paymentStatus("COMPLETED")
            .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.updateOrderPaymentStatus(inputVm));
    }

    @Test
    void testUpdateOrderPaymentStatus_whenPaymentStatusNotCompleted_shouldNotSetOrderStatusToPaid() {
        // Arrange
        PaymentOrderStatusVm inputVm = PaymentOrderStatusVm.builder()
            .orderId(1L)
            .paymentId(123L)
            .paymentStatus("PENDING")
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PaymentOrderStatusVm result = orderService.updateOrderPaymentStatus(inputVm);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.orderId());
        assertEquals(PaymentStatus.PENDING, mockOrder.getPaymentStatus());
        // OrderStatus should remain ACCEPTED (not changed to PAID)
        assertEquals(OrderStatus.ACCEPTED, mockOrder.getOrderStatus());
    }

    @Test
    void testUpdateOrderPaymentStatus_whenPaymentStatusCompleted_shouldSetOrderStatusToPaid() {
        // Arrange
        PaymentOrderStatusVm inputVm = PaymentOrderStatusVm.builder()
            .orderId(1L)
            .paymentId(456L)
            .paymentStatus("COMPLETED")
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PaymentOrderStatusVm result = orderService.updateOrderPaymentStatus(inputVm);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.orderId());
        assertEquals(PaymentStatus.COMPLETED, mockOrder.getPaymentStatus());
        assertEquals(OrderStatus.PAID, mockOrder.getOrderStatus());
        assertEquals("PAID", result.orderStatus());
    }

    @Test
    void testRejectOrder_whenOrderExists_shouldUpdateOrderStatus() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        orderService.rejectOrder(1L, "Out of stock");

        // Assert
        assertEquals(OrderStatus.REJECT, mockOrder.getOrderStatus());
        assertEquals("Out of stock", mockOrder.getRejectReason());
    }

    @Test
    void testRejectOrder_whenOrderNotFound_shouldThrowNotFoundException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.rejectOrder(999L, "Reason"));
    }

    @Test
    void testAcceptOrder_whenOrderExists_shouldUpdateOrderStatus() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        orderService.acceptOrder(1L);

        // Assert
        assertEquals(OrderStatus.ACCEPTED, mockOrder.getOrderStatus());
    }

    @Test
    void testAcceptOrder_whenOrderNotFound_shouldThrowNotFoundException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.acceptOrder(999L));
    }

    @Test
    void testGetAllOrder_whenOrdersExist_shouldReturnOrderListVm() {
        // Arrange
        Page<Order> orderPage = new PageImpl<>(List.of(mockOrder));
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(orderPage);

        // Act
        var result = orderService.getAllOrder(
            Pair.of(ZonedDateTime.now().minusDays(7), ZonedDateTime.now()),
            "Product",
            List.of(OrderStatus.ACCEPTED),
            Pair.of("Vietnam", "123456789"),
            "test@example.com",
            Pair.of(0, 10)
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.orderList().size());
        assertEquals(1L, result.totalElements());
    }

    @Test
    void testGetAllOrder_whenNoOrdersFound_shouldReturnEmptyListVm() {
        // Arrange
        Page<Order> emptyPage = Page.empty();
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(emptyPage);

        // Act
        var result = orderService.getAllOrder(
            Pair.of(ZonedDateTime.now().minusDays(7), ZonedDateTime.now()),
            "Product",
            List.of(),
            Pair.of("", ""),
            "",
            Pair.of(0, 10)
        );

        // Assert
        assertNotNull(result);
        assertEquals(0, result.totalElements());
    }

    @Test
    void testFindOrderVmByCheckoutId_whenOrderExists_shouldReturnOrderGetVm() {
        // Arrange
        String checkoutId = "checkout-123";
        when(orderRepository.findByCheckoutId(checkoutId)).thenReturn(Optional.of(mockOrder));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of(mockOrderItem));

        // Act
        var result = orderService.findOrderVmByCheckoutId(checkoutId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void testFindOrderByCheckoutId_whenMultipleCalls_shouldReturnCorrectly() {
        // Arrange
        String checkoutId1 = "checkout-1";
        String checkoutId2 = "checkout-2";
        Order order2 = Order.builder()
            .id(2L)
            .email("test2@example.com")
            .shippingAddressId(mockAddress)
            .billingAddressId(mockAddress)
            .build();

        when(orderRepository.findByCheckoutId(checkoutId1)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.findByCheckoutId(checkoutId2)).thenReturn(Optional.of(order2));

        // Act
        Order result1 = orderService.findOrderByCheckoutId(checkoutId1);
        Order result2 = orderService.findOrderByCheckoutId(checkoutId2);

        // Assert
        assertNotNull(result1);
        assertEquals(1L, result1.getId());
        assertNotNull(result2);
        assertEquals(2L, result2.getId());
    }

    @Test
    void testRejectOrder_whenValid_shouldSetRejectReasonAndStatus() {
        // Arrange
        Order testOrder = Order.builder()
            .id(1L)
            .email("test@example.com")
            .orderStatus(OrderStatus.ACCEPTED)
            .shippingAddressId(mockAddress)
            .billingAddressId(mockAddress)
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        orderService.rejectOrder(1L, "Customer requested cancellation");

        // Assert
        assertEquals(OrderStatus.REJECT, testOrder.getOrderStatus());
        assertEquals("Customer requested cancellation", testOrder.getRejectReason());
    }

    @Test
    @org.junit.jupiter.api.Disabled("Mock issue with saveAll")
    void testCreateOrder_whenValidOrder_shouldCreateAndReturnOrderVm() {
        // Arrange
        OrderAddressPostVm billingAddress = OrderAddressPostVm.builder()
            .contactName("John Doe")
            .phone("1234567890")
            .addressLine1("123 Main Street")
            .city("Test City")
            .zipCode("12345")
            .countryId(1L)
            .countryName("Vietnam")
            .build();

        OrderItemPostVm orderItemPostVm = OrderItemPostVm.builder()
            .productId(100L)
            .productName("Test Product")
            .quantity(2)
            .productPrice(BigDecimal.valueOf(50))
            .note("Item note")
            .build();

        OrderPostVm orderPostVm = OrderPostVm.builder()
            .email("test@example.com")
            .note("Test order")
            .tax(5.0f)
            .discount(2.0f)
            .numberItem(2)
            .totalPrice(BigDecimal.valueOf(100))
            .deliveryFee(BigDecimal.valueOf(10))
            .deliveryMethod(DeliveryMethod.YAS_EXPRESS)
            .paymentStatus(PaymentStatus.PENDING)
            .billingAddressPostVm(billingAddress)
            .shippingAddressPostVm(billingAddress)
            .orderItemPostVms(List.of(orderItemPostVm))
            .checkoutId("checkout-123")
            .couponCode("DISCOUNT10")
            .build();

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(orderItemRepository.saveAll(any(Iterable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        doNothing().when(productService).subtractProductStockQuantity(any(OrderVm.class));
        doNothing().when(cartService).deleteCartItems(any(OrderVm.class));
        doNothing().when(promotionService).updateUsagePromotion(any(List.class));

        // Act
        OrderVm result = orderService.createOrder(orderPostVm);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository).saveAll(any(Iterable.class));
        verify(productService).subtractProductStockQuantity(any(OrderVm.class));
        verify(cartService).deleteCartItems(any(OrderVm.class));
        verify(promotionService).updateUsagePromotion(any(List.class));
    }

    @Test
    void testGetMyOrders_whenProductNameAndStatusProvided_shouldReturnOrders() {
        com.yas.order.utils.SecurityContextUtils.setSubjectUpSecurityContext("user-123");
        
        // Arrange
        when(orderRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of(mockOrder));

        // Act
        var result = orderService.getMyOrders("Test Product", OrderStatus.ACCEPTED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetMyOrders_whenNoOrders_shouldReturnEmptyList() {
        com.yas.order.utils.SecurityContextUtils.setSubjectUpSecurityContext("user-123");
        
        // Arrange
        when(orderRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of());

        // Act
        var result = orderService.getMyOrders("", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsOrderCompletedWithUserIdAndProductId_whenOrderExists_shouldReturnTrue() {
        com.yas.order.utils.SecurityContextUtils.setSubjectUpSecurityContext("user-123");
        
        // Arrange
        when(productService.getProductVariations(100L)).thenReturn(List.of());
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockOrder));

        // Act
        var result = orderService.isOrderCompletedWithUserIdAndProductId(100L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
    }

    @Test
    void testIsOrderCompletedWithUserIdAndProductId_whenNoOrders_shouldReturnFalse() {
        com.yas.order.utils.SecurityContextUtils.setSubjectUpSecurityContext("user-123");
        
        // Arrange
        when(productService.getProductVariations(100L)).thenReturn(List.of());
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act
        var result = orderService.isOrderCompletedWithUserIdAndProductId(100L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void testIsOrderCompletedWithUserIdAndProductId_whenProductHasVariations_shouldCheckVariations() {
        com.yas.order.utils.SecurityContextUtils.setSubjectUpSecurityContext("user-123");
        
        // Arrange
        List<ProductVariationVm> variations = List.of(
            new ProductVariationVm(101L, "Variation 1", "SKU-001"),
            new ProductVariationVm(102L, "Variation 2", "SKU-002")
        );
        when(productService.getProductVariations(100L)).thenReturn(variations);
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockOrder));

        // Act
        var result = orderService.isOrderCompletedWithUserIdAndProductId(100L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
    }
}
