package com.pickkasso.pickkasso.user.repository;

import com.pickkasso.pickkasso.user.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {

    //작가 별 학력 목록 조회
    List<Education> findByPhotographerId(Long photographerId);

    //수정 시 전체 교체를 위해 기존 데이터 삭제
    void  deleteByPhotographerId(Long photographerId);

}

