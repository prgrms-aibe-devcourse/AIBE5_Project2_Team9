package com.pickkasso.pickkasso.user.dto.photographer;

import com.pickkasso.pickkasso.user.entity.Photographer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PhotographerSimpleCardDto {
    private String imgUrl;
    private String name;

    public static PhotographerSimpleCardDto from(Photographer photographer) {
        return PhotographerSimpleCardDto.builder()
            // .imgUrl() TODO: photographer 이미지 들어가면 사용
            .name(photographer.getName())
            .build();
    }
}
