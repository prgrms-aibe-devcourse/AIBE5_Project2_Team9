package com.pickkasso.pickkasso.item.repository;

import java.util.List;

/**
 * AI PICK: 파싱된 JSON 조건을 DB WHERE / ORDER BY에 반영하기 위한 스펙.
 */
public record AiItemQuerySpec(
    CategoryMode categoryMode,
    List<String> categoryKeywords,
    LocationMode locationMode,
    String singleLocation,
    List<String> locationOrKeywords,
    PriceMode priceMode,
    Integer maxPrice,
    AiItemSort sort,
    int limit
) {
    public enum CategoryMode {
        /** 카테고리 조건 없음 */
        ANY,
        /** tag.name 키워드 포함(엄격) */
        TAG_STRICT,
        /** tag / 상품명 / 설명에 키워드 포함(완화) */
        TAG_OR_TEXT
    }

    public enum LocationMode {
        ANY,
        /** address 에 단일 문자열 포함 */
        CONTAINS,
        /** address 가 키워드들 중 하나라도 포함 */
        OR_KEYWORDS
    }

    public enum PriceMode {
        ANY,
        LTE,
        LTE_RELAX_130
    }

    public enum AiItemSort {
        /** 평균 점수 내림차순(기본) */
        AVG_SCORE_DESC,
        /** RAND() (후보 다양성) */
        RANDOM
    }
}
