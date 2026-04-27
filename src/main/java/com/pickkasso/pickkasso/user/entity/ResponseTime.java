package com.pickkasso.pickkasso.user.entity;

public enum ResponseTime {
    HALF_HOUR("30분 이내"),
    ONE_HOUR("1시간 이내"),
    THREE_HOUR("3시간 이내"),
    IN_DAY("당일 이내");

    private final String displayName;

    ResponseTime(String displayName) {
        this.displayName = displayName;
    }

    public String toString() {
        return this.displayName;
    }
}
