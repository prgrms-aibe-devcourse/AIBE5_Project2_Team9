package com.pickkasso.pickkasso.user.repository;

import com.pickkasso.pickkasso.user.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String username);
}