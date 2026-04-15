package com.pickkasso.pickkasso.common.tag;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id)tag")
    private Long id;

    @Column(name = "name")
    private String name;

    private Tag(String name) {
        this.name = name;
    }

    //== 생성 method ==//
    public static Tag createTag(String name) {
        return new Tag(name);
    }
}
