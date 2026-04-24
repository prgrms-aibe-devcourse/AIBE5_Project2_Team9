package com.pickkasso.pickkasso.review.dto;

import lombok.Getter;

@Getter
public class StarDistributionEntry {
    private final int star;
    private final int count;
    private final int percent;

    public StarDistributionEntry(int star, int count, int percent) {
        this.star = star;
        this.count = count;
        this.percent = percent;
    }
}
