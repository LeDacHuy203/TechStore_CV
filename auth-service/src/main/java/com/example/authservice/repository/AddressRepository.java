package com.example.authservice.repository;

import com.example.authservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long>
{
    List<Address> findByUser_Id(Long userId);
    // Tìm địa chỉ được chọn làm mặc định của người dùng đó:
    Optional<Address> findByUserAndIsDefaultTrue(Long userId);
}
