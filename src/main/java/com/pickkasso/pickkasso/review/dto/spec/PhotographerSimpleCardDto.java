package com.pickkasso.pickkasso.review.dto.spec;

import com.pickkasso.pickkasso.user.entity.Photographer;
import lombok.Getter;

@Getter
public class PhotographerSimpleCardDto {
    private final Long id;
    private final String imgUrl;
    private final String name;

    private PhotographerSimpleCardDto(Long id, String imgUrl, String name) {
        this.id = id;
        this.imgUrl = imgUrl;
        this.name = name;
    }

    public static PhotographerSimpleCardDto from(Photographer photographer) {
        return new PhotographerSimpleCardDto(photographer.getId(), null, photographer.getName());
    }
}
