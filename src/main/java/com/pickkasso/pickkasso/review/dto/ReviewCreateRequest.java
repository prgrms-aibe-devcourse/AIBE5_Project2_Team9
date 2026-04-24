package com.pickkasso.pickkasso.review.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequest {
    private int rating;
    private String content;
}
