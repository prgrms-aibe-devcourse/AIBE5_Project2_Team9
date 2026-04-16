package com.pickkasso.pickkasso.user.repository;

import com.pickkasso.pickkasso.user.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerRepository extends JpaRepository<Career, Long> {

    // 작가 별 경력 목록 조회
    List<Career> findByPhotographerId(Long photographerId);

    //수정 시 전체 교체를 위해 기존 데이터 삭제
    void deleteByPhotographerId(Long photographerId);
}
