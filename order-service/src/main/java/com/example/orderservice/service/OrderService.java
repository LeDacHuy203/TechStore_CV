package com.example.orderservice.service;

import com.example.orderservice.dto.request.CheckoutItemRequest;
import com.example.orderservice.dto.request.CheckoutRequest;
import com.example.orderservice.dto.request.OrderStatusUpdateRequest;
import com.example.orderservice.dto.response.OrderItemResponse;
import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.exception.AppException;
import com.example.orderservice.exception.ResourceNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // 1. Xử lý đặt hàng (Checkout) với Idempotency-Key chống trùng lặp
    @Transactional
    public OrderResponse processCheckout(String idempotencyKey, CheckoutRequest req) {
        // Kiểm tra Idempotency-Key nếu có: Nếu đã tạo rồi thì trả về đơn cũ ngay lập tức
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOrder.isPresent()) {
                return mapToDTO(existingOrder.get());
            }
        }

        // Tính toán tổng tiền đơn hàng
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // Khởi tạo mã đơn hàng duy nhất
        String orderCode = generateOrderCode();

        Order order = Order.builder()
                .orderCode(orderCode)
                .idempotencyKey(idempotencyKey)
                .userId(req.getUserId())
                .customerName(req.getCustomerName())
                .customerPhone(req.getCustomerPhone())
                .customerEmail(req.getCustomerEmail())
                .shippingAddress(req.getShippingAddress())
                .paymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : "COD")
                .paymentStatus("PENDING")
                .status(OrderStatus.PENDING)
                .discountAmount(BigDecimal.ZERO)
                .build();

        for (CheckoutItemRequest itemReq : req.getItems()) {
            BigDecimal itemTotal = itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productSku(itemReq.getProductSku())
                    .productName(itemReq.getProductName())
                    .quantity(itemReq.getQuantity())
                    .price(itemReq.getPrice())
                    .totalPrice(itemTotal)
                    .build();

            orderItems.add(item);
        }

        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount); // Sau khi trừ voucher/discount nếu có
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);
        return mapToDTO(saved);
    }

    // 2. Lấy thông tin đơn hàng theo mã OrderCode
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));
        return mapToDTO(order);
    }

    // 3. Lấy danh sách đơn hàng của một người dùng
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 4. Cập nhật trạng thái đơn hàng (CONFIRMED, SHIPPING, COMPLETED, CANCELLED)
    @Transactional
    public OrderResponse updateOrderStatus(String orderCode, OrderStatusUpdateRequest req) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        if (req.getStatus() != null) {
            order.setStatus(req.getStatus());
        }
        if (req.getPaymentStatus() != null) {
            order.setPaymentStatus(req.getPaymentStatus());
        }

        Order saved = orderRepository.save(order);
        return mapToDTO(saved);
    }

    private OrderResponse mapToDTO(Order order) {
        List<OrderItemResponse> itemDTOs = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .productSku(i.getProductSku())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .idempotencyKey(order.getIdempotencyKey())
                .userId(order.getUserId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String generateOrderCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = 1000 + new Random().nextInt(9000);
        return "ORD-" + dateStr + "-" + rand;
    }
}
