package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.service.UserReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationAvailabilityController {

    private final UserReservationService userReservationService;

    @GetMapping("/availability")
    public Map<String, List<Integer>> availability(
            @RequestParam Long photographerId,
            @RequestParam String date,
            @RequestParam int duration) {
        LocalDate localDate = LocalDate.parse(date);
        return Map.of("blockedHours", userReservationService.getBlockedHours(photographerId, localDate, duration));
    }
}
