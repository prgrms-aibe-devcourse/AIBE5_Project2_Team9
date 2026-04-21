package com.pickkasso.pickkasso.user.repository;


import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNameAndEmail(String name, String email);
    Optional<Member> findByEmail(String email);
    Member findByAccount(Account account);
}