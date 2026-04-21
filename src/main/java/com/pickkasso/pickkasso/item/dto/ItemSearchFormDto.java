package com.pickkasso.pickkasso.item.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class ItemSearchFormDto {
    private String address;
    private Double lat;
    private Double lng;
    private String tag;
    private LocalDate date;
    private String orderBy;
    private Integer page;
    private String itemType;
}
