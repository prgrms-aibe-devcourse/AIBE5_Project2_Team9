package com.pickkasso.pickkasso.item.dto;

import com.pickkasso.pickkasso.global.region.RegionDto;
import com.pickkasso.pickkasso.item.entity.ItemType;

public class ItemBoxDto {
    private Long id;
    private String name;
    private String description;
    // private PhotographerDto photographerDto;
    private RegionDto regionDto;
    private Integer avgScore;
    private Integer defaultPrice;
    ItemType itemType;
}
