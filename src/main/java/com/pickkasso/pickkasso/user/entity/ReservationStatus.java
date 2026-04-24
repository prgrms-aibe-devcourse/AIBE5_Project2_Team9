package com.pickkasso.pickkasso.user.entity;

public enum ReservationStatus {
    PENDING("승인 대기"),
    CONFIRMED("예약 확정"),
    COMPLETED("촬영 완료"),
    REJECTED("작가 거절"),
    CANCELED("사용자 취소");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
