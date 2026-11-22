package com.example.inventoryservice.repository;

import com.example.inventoryservice.entity.SerialNumber;
import com.example.inventoryservice.entity.SerialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {
    Optional<SerialNumber> findBySerialCode(String serialCode);
    boolean existsBySerialCode(String serialCode);
    List<SerialNumber> findByProductSkuAndStatus(String productSku, SerialStatus status);
    List<SerialNumber> findByOrderId(Long orderId);
}
