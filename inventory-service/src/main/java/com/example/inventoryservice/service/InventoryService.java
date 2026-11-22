package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.request.*;
import com.example.inventoryservice.dto.response.InventoryItemResponse;
import com.example.inventoryservice.dto.response.SerialNumberResponse;
import com.example.inventoryservice.entity.InventoryItem;
import com.example.inventoryservice.entity.SerialNumber;
import com.example.inventoryservice.entity.SerialStatus;
import com.example.inventoryservice.exception.AppException;
import com.example.inventoryservice.exception.ResourceNotFoundException;
import com.example.inventoryservice.repository.InventoryItemRepository;
import com.example.inventoryservice.repository.SerialNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final SerialNumberRepository serialNumberRepository;

    // 1. Giữ hàng tạm thời khi khách đặt hàng (tránh Race Condition)
    @Transactional
    public InventoryItemResponse reserveStock(StockReserveRequest req) {
        InventoryItem item = inventoryItemRepository.findByProductSku(req.getProductSku())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tồn kho cho SKU: " + req.getProductSku()));

        if (item.getAvailableQuantity() < req.getQuantity()) {
            throw new AppException(400, "Sản phẩm " + req.getProductSku() + " chỉ còn lại " + item.getAvailableQuantity() + " máy, không đủ số lượng yêu cầu!");
        }

        item.setQuantityReserved(item.getQuantityReserved() + req.getQuantity());
        InventoryItem saved = inventoryItemRepository.save(item);
        return mapToDTO(saved);
    }

    // 2. Hoàn lại tồn kho khi khách hủy đơn hoặc thanh toán thất bại
    @Transactional
    public InventoryItemResponse releaseStock(StockReleaseRequest req) {
        InventoryItem item = inventoryItemRepository.findByProductSku(req.getProductSku())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tồn kho cho SKU: " + req.getProductSku()));

        int newReserved = Math.max(0, item.getQuantityReserved() - req.getQuantity());
        item.setQuantityReserved(newReserved);

        InventoryItem saved = inventoryItemRepository.save(item);
        return mapToDTO(saved);
    }

    // 3. Trừ kho thực tế khi đơn hàng thanh toán thành công
    @Transactional
    public InventoryItemResponse deductStock(StockDeductRequest req) {
        InventoryItem item = inventoryItemRepository.findByProductSku(req.getProductSku())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tồn kho cho SKU: " + req.getProductSku()));

        if (item.getQuantityInStock() < req.getQuantity()) {
            throw new AppException(400, "Lỗi tồn kho: Số lượng thực tế không đủ để xuất kho!");
        }

        item.setQuantityInStock(item.getQuantityInStock() - req.getQuantity());
        item.setQuantityReserved(Math.max(0, item.getQuantityReserved() - req.getQuantity()));

        InventoryItem saved = inventoryItemRepository.save(item);
        return mapToDTO(saved);
    }

    // 4. Cập nhật hoặc tạo mới số lượng kho (Dành cho Quản lý kho nhập hàng)
    @Transactional
    public InventoryItemResponse updateStock(StockUpdateRequest req) {
        InventoryItem item = inventoryItemRepository.findByProductSku(req.getProductSku())
                .orElse(InventoryItem.builder()
                        .productSku(req.getProductSku())
                        .quantityInStock(0)
                        .quantityReserved(0)
                        .build());

        item.setQuantityInStock(req.getQuantityInStock());
        if (req.getWarehouseCode() != null) {
            item.setWarehouseCode(req.getWarehouseCode());
        }

        InventoryItem saved = inventoryItemRepository.save(item);
        return mapToDTO(saved);
    }

    // 5. Lấy thông tin tồn kho theo SKU
    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryBySku(String productSku) {
        InventoryItem item = inventoryItemRepository.findByProductSku(productSku)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy SKU: " + productSku));
        return mapToDTO(item);
    }

    // 6. Kích hoạt bảo hành cho chiếc máy (gắn Serial với đơn hàng)
    @Transactional
    public SerialNumberResponse activateWarranty(WarrantyActivationRequest req) {
        SerialNumber serial = serialNumberRepository.findBySerialCode(req.getSerialCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy số Serial: " + req.getSerialCode()));

        LocalDateTime now = LocalDateTime.now();
        serial.setOrderId(req.getOrderId());
        serial.setStatus(SerialStatus.SOLD);
        serial.setWarrantyStartDate(now);
        serial.setWarrantyEndDate(now.plusMonths(req.getWarrantyMonths()));

        SerialNumber saved = serialNumberRepository.save(serial);
        return mapToSerialDTO(saved);
    }

    // 7. Lấy danh sách Serial theo mã đơn hàng
    @Transactional(readOnly = true)
    public List<SerialNumberResponse> getSerialsByOrderId(Long orderId) {
        return serialNumberRepository.findByOrderId(orderId).stream()
                .map(this::mapToSerialDTO)
                .collect(Collectors.toList());
    }

    private InventoryItemResponse mapToDTO(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .productSku(item.getProductSku())
                .quantityInStock(item.getQuantityInStock())
                .quantityReserved(item.getQuantityReserved())
                .availableQuantity(item.getAvailableQuantity())
                .warehouseCode(item.getWarehouseCode())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private SerialNumberResponse mapToSerialDTO(SerialNumber s) {
        return SerialNumberResponse.builder()
                .id(s.getId())
                .serialCode(s.getSerialCode())
                .productSku(s.getProductSku())
                .orderId(s.getOrderId())
                .status(s.getStatus())
                .warrantyStartDate(s.getWarrantyStartDate())
                .warrantyEndDate(s.getWarrantyEndDate())
                .build();
    }
}
