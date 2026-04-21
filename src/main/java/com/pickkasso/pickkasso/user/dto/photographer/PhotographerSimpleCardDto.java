package com.pickkasso.pickkasso.user.dto.photographer;

import com.pickkasso.pickkasso.user.entity.Photographer;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PhotographerSimpleCardDto {
    private String imgUrl;
    private String name;

    @QueryProjection
    public PhotographerSimpleCardDto(String imgUrl, String name) {
        this.imgUrl = imgUrl;
        this.name = name;
    }

    public static PhotographerSimpleCardDto from(Photographer photographer) {
        return PhotographerSimpleCardDto.builder()
            // .imgUrl() TODO: photographer 이미지 들어가면 사용
            .name(photographer.getName())
            .build();
    }
}
