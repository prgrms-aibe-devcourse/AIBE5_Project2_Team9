package com.pickkasso.pickkasso.user.dto.photographer;

import java.time.LocalDate;

public record EducationDto(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}
