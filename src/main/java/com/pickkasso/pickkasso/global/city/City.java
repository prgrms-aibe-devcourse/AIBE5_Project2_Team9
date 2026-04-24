package com.pickkasso.pickkasso.global.city;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public enum City {
    // 시
    BUSAN("부산"),
    DAEGU("대구"),
    SEOUL("서울"),
    INCHEON("인천"),
    GWANGJU("광주"),
    DAEJEON("대전"),
    ULSAN("울산"),
    SEJONG("세종"),
    // 도
    GYEONGGI("경기"),
    CHUNGCHEONG_N("충북", "충청북도"),
    CHUNGCHEONG_W("충남", "충청남도"),
    JEOLLA_N("전북", "전라북도"),
    JEOLLA_W("전남", "전라남도"),
    GYEONGSANG_N("경북", "경상북도"),
    GYEONGSANG_W("경남", "경상남도"),
    GANGWON("강원"),
    JEJU("제주");

    private final String displayName;
    private final Set<String> aliases;

    City(String displayName, String... aliases) {
        this.displayName = displayName;
        this.aliases = new HashSet<>(Arrays.asList(aliases));
        this.aliases.add(displayName);
    }

    public String getDisplayName() { return displayName; }

    public String toString() {
        return this.displayName;
    }

    public static String toString(City city) {
        return city.displayName;
    }

    public static City fromString(String value) {
        if (value == null) throw new IllegalArgumentException("City value must not be null");
        return Arrays.stream(values())
            .filter(c -> c.aliases.stream().anyMatch(value::startsWith))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown city: " + value));
    }
}
