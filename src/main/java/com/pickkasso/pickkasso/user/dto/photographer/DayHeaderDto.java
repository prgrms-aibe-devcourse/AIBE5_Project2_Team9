package com.pickkasso.pickkasso.user.dto.photographer;

public record DayHeaderDto(
        int dayIndex,
        String labelKor,
        int dayOfMonth,
        boolean isToday,
        boolean isSunday,
        boolean isSaturday
) {}
