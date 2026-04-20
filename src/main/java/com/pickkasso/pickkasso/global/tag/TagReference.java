package com.pickkasso.pickkasso.global.tag;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagReference {
    private Long id;
    private String name;

    @QueryProjection
    public TagReference(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static TagReference from(Tag tag) {
        return TagReference.builder()
            .id(tag.getId())
            .name(tag.getName())
            .build();
    }
}
