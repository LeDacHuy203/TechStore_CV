package com.example.authservice.repository;

import com.example.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
// 1.    Tìm User theo username đẻ xác thực khi đăng nhập
    Optional<User> findByUsername(String username);
//2.    Tìm User theo email để xác thực khi đăng ký
    Optional<User> findByEmail(String email);
// 3. Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);
//    4. Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
}
