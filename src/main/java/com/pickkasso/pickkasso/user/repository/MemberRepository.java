package com.pickkasso.pickkasso.user.repository;


import com.pickkasso.pickkasso.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}