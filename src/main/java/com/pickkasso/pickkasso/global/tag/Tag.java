package com.pickkasso.pickkasso.global.tag;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "emoji")
    private String emoji;

    private Tag(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    //== 생성 method ==//
    public static Tag createTag(String name, String emoji) {
        return new Tag(name, emoji);
    }
}
