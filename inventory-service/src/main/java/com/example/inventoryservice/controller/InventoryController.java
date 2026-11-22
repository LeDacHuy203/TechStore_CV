package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.request.*;
import com.example.inventoryservice.dto.response.ApiResponse;
import com.example.inventoryservice.dto.response.InventoryItemResponse;
import com.example.inventoryservice.dto.response.SerialNumberResponse;
import com.example.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // 1. Kiểm tra tồn kho theo SKU
    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> getInventoryBySku(@PathVariable String sku) {
        InventoryItemResponse res = inventoryService.getInventoryBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(res, "Lấy thông tin tồn kho thành công!"));
    }

    // 2. Nhập kho / Cập nhật số lượng kho
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateStock(@Valid @RequestBody StockUpdateRequest req) {
        InventoryItemResponse res = inventoryService.updateStock(req);
        return ResponseEntity.ok(ApiResponse.success(res, "Cập nhật tồn kho thành công!"));
    }

    // 3. Khóa giữ tồn kho tạm thời (gọi từ Order Service)
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> reserveStock(@Valid @RequestBody StockReserveRequest req) {
        InventoryItemResponse res = inventoryService.reserveStock(req);
        return ResponseEntity.ok(ApiResponse.success(res, "Giữ tồn kho thành công!"));
    }

    // 4. Giải phóng tồn kho khi hủy đơn (gọi từ Order Service)
    @PostMapping("/release")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> releaseStock(@Valid @RequestBody StockReleaseRequest req) {
        InventoryItemResponse res = inventoryService.releaseStock(req);
        return ResponseEntity.ok(ApiResponse.success(res, "Hoàn trả tồn kho thành công!"));
    }

    // 5. Trừ kho thực tế khi hoàn tất thanh toán
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> deductStock(@Valid @RequestBody StockDeductRequest req) {
        InventoryItemResponse res = inventoryService.deductStock(req);
        return ResponseEntity.ok(ApiResponse.success(res, "Trừ tồn kho thành công!"));
    }

    // 6. Kích hoạt bảo hành cho số Serial
    @PostMapping("/warranty/activate")
    public ResponseEntity<ApiResponse<SerialNumberResponse>> activateWarranty(@Valid @RequestBody WarrantyActivationRequest req) {
        SerialNumberResponse res = inventoryService.activateWarranty(req);
        return ResponseEntity.ok(ApiResponse.success(res, "Kích hoạt bảo hành thành công!"));
    }

    // 7. Lấy danh sách Serial theo đơn hàng
    @GetMapping("/warranty/order/{orderId}")
    public ResponseEntity<ApiResponse<List<SerialNumberResponse>>> getSerialsByOrderId(@PathVariable Long orderId) {
        List<SerialNumberResponse> res = inventoryService.getSerialsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(res, "Lấy danh sách số serial thành công!"));
    }
}
