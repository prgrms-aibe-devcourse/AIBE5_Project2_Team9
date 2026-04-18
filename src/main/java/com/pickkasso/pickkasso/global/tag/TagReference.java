package com.pickkasso.pickkasso.global.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TagReference {
    private Long id;
    private String name;

    public static TagReference from(Tag tag) {
        return TagReference.builder()
            .id(tag.getId())
            .name(tag.getName())
            .build();
    }
}
