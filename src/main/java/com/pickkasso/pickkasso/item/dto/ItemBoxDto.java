package com.pickkasso.pickkasso.item.dto;

import com.pickkasso.pickkasso.global.region.RegionDto;
import com.pickkasso.pickkasso.global.tag.TagReference;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.ItemType;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerSimpleCardDto;
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
    private String imgUrl;
    private TagReference tag;
    private PhotographerSimpleCardDto photographer;
    private RegionDto region;
    private Integer defaultPrice;
    private ItemType itemType;
    private Long reviewScore;
    private Integer reviewCount;
    private Integer purchaseCount;
    private Double distance;

    public static ItemBoxDto from(Item item) {
        return ItemBoxDto.builder()
            .id(item.getId())
            .name(item.getName())
            .imgUrl(item.getThumbnailImgUrl())
            .tag(TagReference.from(item.getTag()))
            .photographer(PhotographerSimpleCardDto.from(item.getPhotographer()))
            .region(RegionDto.from(item))
            .defaultPrice(item.getDefaultPrice())
            .itemType(item.getItemType())
            .reviewScore(item.getReviewScore())
            .reviewCount(item.getReviewCount())
            .purchaseCount(item.getPurchaseCount())
            .build();
    }
}
