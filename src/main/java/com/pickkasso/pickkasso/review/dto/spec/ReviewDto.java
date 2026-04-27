package com.pickkasso.pickkasso.review.dto.spec;

import com.pickkasso.pickkasso.review.entity.Review;
import lombok.Getter;

@Getter
public class ReviewDto {
    private final OrderDto order;
    private final Integer score;
    private final String description;

    private ReviewDto(OrderDto order, Integer score, String description) {
        this.order = order;
        this.score = score;
        this.description = description;
    }

    public static ReviewDto from(Review review) {
        return new ReviewDto(
            OrderDto.from(review.getReservation()),
            review.getRating(),
            review.getContent()
        );
    }
}
