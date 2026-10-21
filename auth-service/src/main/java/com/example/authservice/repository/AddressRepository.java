package com.example.authservice.repository;

import com.example.authservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Tìm danh sách địa chỉ theo ID người dùng:
    List<Address> findByUser_Id(Long userId);

    // Tìm địa chỉ được chọn làm mặc định của người dùng đó:
    Optional<Address> findByUser_IdAndIsDefaultTrue(Long userId);
}
