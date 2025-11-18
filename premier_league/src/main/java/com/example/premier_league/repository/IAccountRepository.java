package com.example.premier_league.repository;

import com.example.premier_league.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface IAccountRepository extends JpaRepository<Account, Long> {
    // Phương thức tìm theo username
    Optional<Account> findByUsername(String username);
}
