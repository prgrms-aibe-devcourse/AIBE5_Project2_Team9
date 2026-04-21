package com.pickkasso.pickkasso.item.dto;

import com.pickkasso.pickkasso.item.entity.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ItemRegisterRequest {
    private String tagName;
    private ItemType itemType;
    private String name;
    private String description;
    private String includes;
    private String excludes;
    private String address;
    private Double lat;
    private Double lng;
    private Integer minBookingLeadTime;
    private String cancellationPolicy;
    private List<PlanRegisterRequest> plans = new ArrayList<>();
}
