package com.pickkasso.pickkasso.review.dto;

import com.pickkasso.pickkasso.review.entity.Review;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class ReviewDto {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final String memberName;
    private final String memberInitial;
    private final String itemName;
    private final int rating;
    private final String content;
    private final String createdAt;

    public ReviewDto(Review r) {
        this.memberName = r.getMember().getName();
        this.memberInitial = r.getMember().getName().substring(0, 1);
        this.itemName = r.getReservation().getItem().getName();
        this.rating = r.getRating();
        this.content = r.getContent();
        this.createdAt = r.getCreatedAt().format(FORMATTER);
    }
}
