package com.pickkasso.pickkasso.user.repository;

import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Photographer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotographerRepository extends JpaRepository<Photographer, Long> {
    Photographer findByAccount(Account account);
}
