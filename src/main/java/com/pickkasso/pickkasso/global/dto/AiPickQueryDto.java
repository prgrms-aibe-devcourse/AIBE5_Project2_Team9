package com.pickkasso.pickkasso.global.dto;

import java.util.List;

/**
 * @param sort 추천 후보 DB 정렬. {@code "score"}(기본) 평균 점수·리뷰 수 기준, {@code "random"} 조건에 맞는 후보를 무작위 풀에서 가져옴.
 */
public record AiPickQueryDto(
    String rawQuery,
    String requestedDate,
    String location,
    Integer maxPrice,
    String category,
    List<String> styleTags,
    String sort
) {
}
