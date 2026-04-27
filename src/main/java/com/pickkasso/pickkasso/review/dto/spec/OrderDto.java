package com.pickkasso.pickkasso.review.dto.spec;

import com.pickkasso.pickkasso.global.region.RegionDto;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

@Getter
public class OrderDto {
    private final Long itemId;
    private final MemberSimpleCardDto buyer;
    private final PhotographerSimpleCardDto photographer;
    private final RegionDto region;
    private final PlanDto plan;
    private final LocalDateTime startDate;
    private final ReservationStatus orderState;
    private final Map<String, Object> planField;

    private OrderDto(
        Long itemId,
        MemberSimpleCardDto buyer,
        PhotographerSimpleCardDto photographer,
        RegionDto region,
        PlanDto plan,
        LocalDateTime startDate,
        ReservationStatus orderState,
        Map<String, Object> planField
    ) {
        this.itemId = itemId;
        this.buyer = buyer;
        this.photographer = photographer;
        this.region = region;
        this.plan = plan;
        this.startDate = startDate;
        this.orderState = orderState;
        this.planField = planField;
    }

    public static OrderDto from(Reservation reservation) {
        PlanDto selectedPlan = reservation.getItem().getPlanList().stream()
            .filter(plan -> Boolean.TRUE.equals(plan.getEnabled()))
            .min(Comparator.<Plan>comparingInt(plan -> plan.getPrice() != null ? plan.getPrice() : Integer.MAX_VALUE)
                .thenComparing(plan -> plan.getId() != null ? plan.getId() : Long.MAX_VALUE))
            .map(PlanDto::from)
            .orElseGet(() -> reservation.getItem().getPlanList().stream()
                .min(Comparator.comparing(plan -> plan.getId() != null ? plan.getId() : Long.MAX_VALUE))
                .map(PlanDto::from)
                .orElse(null));

        return new OrderDto(
            reservation.getItem().getId(),
            MemberSimpleCardDto.from(reservation.getMember()),
            PhotographerSimpleCardDto.from(reservation.getPhotographer()),
            new RegionDto(
                reservation.getAddress(),
                reservation.getItem().getDetailAddress(),
                reservation.getItem().getLat(),
                reservation.getItem().getLng()
            ),
            selectedPlan,
            reservation.getScheduledAt(),
            reservation.getStatus(),
            Map.of(
                "itemName", reservation.getItem().getName(),
                "totalPrice", reservation.getTotalPrice(),
                "durationMinutes", reservation.getDurationMinutes()
            )
        );
    }
}
