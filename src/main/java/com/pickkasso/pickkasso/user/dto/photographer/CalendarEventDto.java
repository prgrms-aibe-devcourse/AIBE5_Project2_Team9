package com.pickkasso.pickkasso.user.dto.photographer;

public record CalendarEventDto(
        Long reservationId,
        int dayIndex,
        int hourSlot,
        int rowSpanHours,
        String title,
        String subtitle,
        String colorClass
) {}
