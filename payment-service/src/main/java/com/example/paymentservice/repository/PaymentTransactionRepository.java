package com.example.paymentservice.repository;

import com.example.paymentservice.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByOrderCode(String orderCode);
    Optional<PaymentTransaction> findByTransactionNo(String transactionNo);
}
