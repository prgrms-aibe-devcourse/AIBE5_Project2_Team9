package com.pickkasso.pickkasso.review.dto.spec;

import com.pickkasso.pickkasso.user.entity.Member;
import lombok.Getter;

@Getter
public class MemberSimpleCardDto {
    private final Long id;
    private final String imgUrl;
    private final String name;

    private MemberSimpleCardDto(Long id, String imgUrl, String name) {
        this.id = id;
        this.imgUrl = imgUrl;
        this.name = name;
    }

    public static MemberSimpleCardDto from(Member member) {
        return new MemberSimpleCardDto(member.getId(), null, member.getName());
    }
}
