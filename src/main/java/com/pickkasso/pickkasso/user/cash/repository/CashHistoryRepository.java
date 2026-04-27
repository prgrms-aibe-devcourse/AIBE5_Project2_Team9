package com.pickkasso.pickkasso.user.cash.repository;

import com.pickkasso.pickkasso.user.cash.entity.CashHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashHistoryRepository extends JpaRepository<CashHistory, Long> {
}
