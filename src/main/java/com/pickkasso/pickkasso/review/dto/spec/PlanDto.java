package com.pickkasso.pickkasso.review.dto.spec;

import com.pickkasso.pickkasso.item.entity.Plan;
import lombok.Getter;

@Getter
public class PlanDto {
    private final Long id;
    private final String name;
    private final Integer price;

    public PlanDto(Long id, String name, Integer price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static PlanDto from(Plan plan) {
        if (plan == null) {
            return null;
        }
        return new PlanDto(plan.getId(), plan.getName(), plan.getPrice());
    }
}
