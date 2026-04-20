package com.pickkasso.pickkasso.item.dto;

import com.pickkasso.pickkasso.global.tag.Tag;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ItemSearchCondition {
    private Double lat;
    private Double lng;
    private Integer distance;
    private Tag tag;
    private LocalDate date;
    private String orderBy;
    private Integer page;
}
