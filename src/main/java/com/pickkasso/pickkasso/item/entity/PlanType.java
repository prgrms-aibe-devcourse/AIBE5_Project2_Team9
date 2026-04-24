package com.pickkasso.pickkasso.item.entity;

public enum PlanType {
    STANDARD(1), DELUXE(2), PREMIUM(3);

    private final int order;
    PlanType(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
