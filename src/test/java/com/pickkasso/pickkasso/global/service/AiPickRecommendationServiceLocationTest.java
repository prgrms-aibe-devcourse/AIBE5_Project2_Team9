package com.pickkasso.pickkasso.global.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiPickRecommendationServiceLocationTest {

    private final AiPickRecommendationService service = new AiPickRecommendationService(null, null);

    @Test
    @DisplayName("생활권/행정동 입력을 대표 지역으로 정규화한다")
    void parseLocation_normalizesAliasAndDong() throws Exception {
        assertEquals("홍대", invokeParseLocation("홍대에서 데이트 스냅 찍고 싶어요"));
        assertEquals("홍대", invokeParseLocation("연남동 야외 커플촬영 추천해줘"));
        assertEquals("강남", invokeParseLocation("역삼동 프로필 사진 가능해?"));
        assertEquals("성수", invokeParseLocation("서울숲 근처 감성 스냅"));
        assertEquals("서울", invokeParseLocation("서울 어디든 괜찮아"));
    }

    @Test
    @DisplayName("취업/증명 키워드는 프로필 카테고리로 정규화한다")
    void parseCategory_normalizesProfileSynonyms() throws Exception {
        assertEquals("프로필", invokeParseCategory("다음주 토요일 강남에서 취업 사진 찍고싶어"));
        assertEquals("프로필", invokeParseCategory("증명사진 느낌으로 찍고 싶어요"));
    }

    @Test
    @DisplayName("주요 촬영 카테고리 키워드를 안정적으로 인식한다")
    void parseCategory_supportsMajorCategories() throws Exception {
        assertEquals("웨딩", invokeParseCategory("야외 웨딩 스냅 촬영 원해요"));
        assertEquals("가족", invokeParseCategory("주말에 가족사진 찍고 싶어요"));
        assertEquals("제품", invokeParseCategory("브랜드 제품 촬영 문의합니다"));
        assertEquals("데이트", invokeParseCategory("홍대에서 커플 사진 찍고 싶어요"));
        assertEquals("졸업", invokeParseCategory("졸업사진 감성으로 찍고 싶어요"));
    }

    @Test
    @DisplayName("요일 축약 표현(월~일, 다음주 토 등)을 날짜로 인식한다")
    void resolveRelativeDate_supportsShortWeekdayText() throws Exception {
        assertEquals(DayOfWeek.SATURDAY, LocalDate.parse(invokeResolveRelativeDate("다음주 토")).getDayOfWeek());
        assertEquals(DayOfWeek.TUESDAY, LocalDate.parse(invokeResolveRelativeDate("다음주 화 촬영")).getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, LocalDate.parse(invokeResolveRelativeDate("일")).getDayOfWeek());
    }

    @Test
    @DisplayName("대표 지역에 맞는 인접 검색 키워드를 반환한다")
    @SuppressWarnings("unchecked")
    void nearbyRegionKeywords_expandsByRegion() throws Exception {
        List<String> hongdae = (List<String>) invokeNearbyKeywords("홍대");
        assertTrue(hongdae.contains("연남동"));
        assertTrue(hongdae.contains("서교동"));
        assertTrue(hongdae.contains("합정동"));

        List<String> gangnam = (List<String>) invokeNearbyKeywords("강남");
        assertTrue(gangnam.contains("역삼동"));
        assertTrue(gangnam.contains("신사동"));
        assertTrue(gangnam.contains("압구정"));

        List<String> seoul = (List<String>) invokeNearbyKeywords("서울");
        assertTrue(seoul.contains("강남"));
        assertTrue(seoul.contains("마포"));
        assertTrue(seoul.contains("용산"));
    }

    private String invokeParseLocation(String query) throws Exception {
        Method method = AiPickRecommendationService.class.getDeclaredMethod("parseLocation", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, query);
    }

    private Object invokeNearbyKeywords(String location) throws Exception {
        Method method = AiPickRecommendationService.class.getDeclaredMethod("nearbyRegionKeywords", String.class);
        method.setAccessible(true);
        return method.invoke(service, location);
    }

    private String invokeParseCategory(String query) throws Exception {
        Method method = AiPickRecommendationService.class.getDeclaredMethod("parseCategory", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, query);
    }

    private String invokeResolveRelativeDate(String query) throws Exception {
        Method method = AiPickRecommendationService.class.getDeclaredMethod("resolveRelativeDate", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, query);
    }
}
