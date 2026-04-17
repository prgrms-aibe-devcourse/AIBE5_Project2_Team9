package com.pickkasso.pickkasso.item.dto;

import com.pickkasso.pickkasso.global.region.RegionDto;
import com.pickkasso.pickkasso.item.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemBoxDto {
    private Long id;
    private String name;
    private String description;
    private RegionDto regionDto;
    private Integer avgScore;
    private Integer defaultPrice;
    private ItemType itemType;
    private String imgUrl;
    private String photographerName;
    private Integer reviewCount;
    private String tagName;
}
