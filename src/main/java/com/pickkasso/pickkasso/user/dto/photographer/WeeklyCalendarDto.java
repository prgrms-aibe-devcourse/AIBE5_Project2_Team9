package com.pickkasso.pickkasso.user.dto.photographer;

import com.pickkasso.pickkasso.user.entity.Reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

public record WeeklyCalendarDto(
        LocalDate weekStart,
        String weekStartIso,
        String rangeLabel,
        List<DayHeaderDto> dayHeaders,
        List<Integer> hourSlots,
        Map<String, CalendarEventDto> eventByCellKey,
        LocalDate today,
        String prevWeekStartIso,
        String nextWeekStartIso,
        String todayWeekStartIso,
        boolean currentWeek
) {
    private static final String[] DAY_NAMES_KOR = {"일", "월", "화", "수", "목", "금", "토"};
    private static final String[] COLOR_CLASSES = {"blue", "green", "orange", "sky", "purple"};
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter RANGE_FMT = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    public static WeeklyCalendarDto of(LocalDate weekStart, List<Reservation> reservations) {
        LocalDate today = LocalDate.now();
        LocalDate todayWeekStart = today.minusDays(today.getDayOfWeek().getValue() % 7);

        List<DayHeaderDto> headers = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            headers.add(new DayHeaderDto(i, DAY_NAMES_KOR[i], d.getDayOfMonth(),
                    d.equals(today), i == 0, i == 6));
        }

        List<Integer> hourSlots = IntStream.rangeClosed(9, 18)
                .boxed()
                .toList();

        Map<String, CalendarEventDto> eventMap = new LinkedHashMap<>();
        for (Reservation r : reservations) {
            LocalDate rDate = r.getScheduledAt().toLocalDate();
            int dayIdx = (int) ChronoUnit.DAYS.between(weekStart, rDate);
            if (dayIdx < 0 || dayIdx > 6) continue;
            int hour = r.getScheduledAt().getHour();
            if (hour < 9 || hour > 18) continue;

            int rowSpan = Math.max(1, (int) Math.ceil(r.getDurationMinutes() / 60.0));
            String colorClass = COLOR_CLASSES[(int) (r.getItem().getId() % 5)];
            String startTime = r.getScheduledAt().format(TIME_FMT);
            String subtitle = r.getMember().getName() + " · " + startTime;

            String key = dayIdx + "-" + hour;
            eventMap.putIfAbsent(key, new CalendarEventDto(
                    r.getId(), dayIdx, hour, rowSpan,
                    r.getItem().getName(), subtitle, colorClass
            ));
        }

        LocalDate weekEnd = weekStart.plusDays(6);
        String rangeLabel = weekStart.format(RANGE_FMT) + " – " + weekEnd.getDayOfMonth() + "일";

        return new WeeklyCalendarDto(
                weekStart,
                weekStart.toString(),
                rangeLabel,
                headers,
                hourSlots,
                eventMap,
                today,
                weekStart.minusWeeks(1).toString(),
                weekStart.plusWeeks(1).toString(),
                todayWeekStart.toString(),
                weekStart.equals(todayWeekStart)
        );
    }
}
