package com.pickkasso.pickkasso.user.dto.photographer;

public record ProfileCompletionDto(
        boolean hasProfileImage,
        boolean hasIntro,
        boolean hasServices,
        boolean hasEquipment,
        int score
) {}
