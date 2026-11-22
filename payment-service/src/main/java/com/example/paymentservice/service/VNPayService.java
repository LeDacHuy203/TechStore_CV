package com.example.paymentservice.service;

import com.example.paymentservice.dto.request.PaymentCreateRequest;
import com.example.paymentservice.dto.response.PaymentCallbackResponse;
import com.example.paymentservice.dto.response.PaymentCreateResponse;
import com.example.paymentservice.entity.PaymentTransaction;
import com.example.paymentservice.exception.AppException;
import com.example.paymentservice.repository.PaymentTransactionRepository;
import com.example.paymentservice.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {

    private final PaymentTransactionRepository paymentRepository;

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    // 1. Tạo Link thanh toán VNPay Sandbox
    @Transactional
    public PaymentCreateResponse createPaymentUrl(PaymentCreateRequest req) {
        // Lưu lịch sử giao dịch trạng thái PENDING
        PaymentTransaction transaction = paymentRepository.findByOrderCode(req.getOrderCode())
                .orElse(PaymentTransaction.builder()
                        .orderCode(req.getOrderCode())
                        .amount(req.getAmount())
                        .paymentMethod("VNPAY")
                        .status("PENDING")
                        .build());

        transaction.setAmount(req.getAmount());
        transaction.setStatus("PENDING");
        paymentRepository.save(transaction);

        // Chuẩn bị tham số VNPay 2.1.0
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        // VNPay quy định số tiền nhân 100 (bỏ phần thập phân)
        long vnpAmount = req.getAmount().multiply(new BigDecimal(100)).longValue();
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", req.getOrderCode());
        vnpParams.put("vnp_OrderInfo", req.getOrderInfo() != null ? req.getOrderInfo() : "Thanh toan don hang " + req.getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", req.getIpAddress() != null ? req.getIpAddress() : "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Sinh chuỗi hash data và checksum HMAC-SHA512
        String hashData = VNPayUtil.buildHashData(vnpParams);
        String secureHash = VNPayUtil.hmacSHA512(vnpHashSecret, hashData);

        String paymentUrl = vnpPayUrl + "?" + hashData + "&vnp_SecureHash=" + secureHash;

        return PaymentCreateResponse.builder()
                .orderCode(req.getOrderCode())
                .paymentUrl(paymentUrl)
                .build();
    }

    // 2. Xử lý Callback IPN / Return từ VNPay
    @Transactional
    public PaymentCallbackResponse processReturn(Map<String, String> queryParams) {
        String vnpSecureHash = queryParams.get("vnp_SecureHash");

        // Loại bỏ tham số hash để tính lại checksum đối soát
        Map<String, String> fields = new HashMap<>(queryParams);
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = VNPayUtil.hmacSHA512(vnpHashSecret, VNPayUtil.buildHashData(fields));

        if (!signValue.equalsIgnoreCase(vnpSecureHash)) {
            throw new AppException(400, "Chữ ký số VNPay không hợp lệ (Dữ liệu bị can thiệp)!");
        }

        String orderCode = queryParams.get("vnp_TxnRef");
        String responseCode = queryParams.get("vnp_ResponseCode");
        String transactionNo = queryParams.get("vnp_TransactionNo");
        String bankCode = queryParams.get("vnp_BankCode");
        long amountLong = Long.parseLong(queryParams.get("vnp_Amount")) / 100;

        PaymentTransaction transaction = paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy giao dịch với mã: " + orderCode));

        transaction.setTransactionNo(transactionNo);
        transaction.setBankCode(bankCode);
        transaction.setResponseCode(responseCode);

        boolean isSuccess = "00".equals(responseCode);
        transaction.setStatus(isSuccess ? "SUCCESS" : "FAILED");
        paymentRepository.save(transaction);

        return PaymentCallbackResponse.builder()
                .orderCode(orderCode)
                .amount(BigDecimal.valueOf(amountLong))
                .status(isSuccess ? "SUCCESS" : "FAILED")
                .transactionNo(transactionNo)
                .bankCode(bankCode)
                .responseCode(responseCode)
                .message(isSuccess ? "Giao dịch thanh toán thành công!" : "Giao dịch thanh toán thất bại!")
                .build();
    }

    // 3. Tra cứu thông tin giao dịch theo orderCode
    @Transactional(readOnly = true)
    public PaymentTransaction getTransactionByOrderCode(String orderCode) {
        return paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy giao dịch cho đơn hàng: " + orderCode));
    }
}
