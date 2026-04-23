package com.pickkasso.pickkasso.user.dto.photographer;

import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record ReservationManagementItemDto(
        Long reservationId,
        ReservationStatus status,
        String statusLabel,
        String statusClassName,
        String statusHint,
        String statusHintColorClass,
        String memberName,
        String memberInitial,
        String itemName,
        String scheduleLabel,
        String address,
        String priceLabel,
        String memo,
        boolean actionable
) {
    private static final String[] WEEKDAY_KOR = {"월", "화", "수", "목", "금", "토", "일"};
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final NumberFormat PRICE_FMT = NumberFormat.getNumberInstance(Locale.KOREA);

    public static ReservationManagementItemDto of(Reservation reservation) {
        LocalDateTime start = reservation.getScheduledAt();
        LocalDateTime end = start.plusMinutes(reservation.getDurationMinutes());
        String weekday = WEEKDAY_KOR[start.getDayOfWeek().getValue() - 1];
        String scheduleLabel = start.getMonthValue() + "월 " + start.getDayOfMonth() + "일 (" + weekday + ") "
                + start.format(TIME_FMT) + " - " + end.format(TIME_FMT);
        String memberName = reservation.getMember().getName();
        String initial = memberName.isEmpty() ? "?" : String.valueOf(memberName.charAt(0));
        String memo = reservation.getMemo() != null && !reservation.getMemo().isBlank()
                ? reservation.getMemo()
                : defaultMemo(reservation.getStatus(), reservation.getItem().getName());

        return new ReservationManagementItemDto(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getStatus().label(),
                toStatusClassName(reservation.getStatus()),
                toStatusHint(reservation),
                toStatusHintColorClass(reservation.getStatus()),
                memberName,
                initial,
                reservation.getItem().getName(),
                scheduleLabel,
                reservation.getAddress(),
                PRICE_FMT.format(reservation.getTotalPrice()) + "C",
                memo,
                reservation.getStatus() == ReservationStatus.PENDING
        );
    }

    private static String toStatusClassName(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "pending";
            case CONFIRMED -> "confirmed";
            case COMPLETED -> "completed";
            case REJECTED, CANCELED -> "rejected";
        };
    }

    private static String toStatusHint(Reservation reservation) {
        LocalDate today = LocalDate.now();
        return switch (reservation.getStatus()) {
            case PENDING -> reservation.getRequestedAt().toLocalDate().isBefore(today)
                    ? "빠른 응답 필요"
                    : "승인 검토 중";
            case CONFIRMED -> reservation.getScheduledAt().toLocalDate().equals(today.plusDays(1))
                    ? "D-1 리마인드 예정"
                    : "촬영 준비 진행 중";
            case COMPLETED -> "후속 작업 진행 중";
            case REJECTED -> "응답 완료";
            case CANCELED -> "사용자 취소";
        };
    }

    private static String toStatusHintColorClass(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "text-[#FF6B2B]";
            case CONFIRMED -> "text-[#1D3CFF]";
            case COMPLETED -> "text-[#1A8E40]";
            case REJECTED, CANCELED -> "text-[#888]";
        };
    }

    private static String defaultMemo(ReservationStatus status, String itemName) {
        return switch (status) {
            case PENDING -> itemName + " 예약 요청이 접수되었습니다. 고객 요청사항을 확인하고 승인 여부를 결정해 주세요.";
            case CONFIRMED -> itemName + " 예약이 확정되었습니다. 촬영 전 안내 메시지와 준비 사항을 점검해 주세요.";
            case COMPLETED -> itemName + " 촬영이 완료되었습니다. 후속 전달 일정과 정산 상태를 확인해 주세요.";
            case REJECTED -> itemName + " 예약을 거절한 건입니다. 필요 시 고객 안내 문구를 다시 확인해 주세요.";
            case CANCELED -> itemName + " 예약이 취소된 건입니다. 환불 및 일정 비움 처리를 함께 점검해 주세요.";
        };
    }
}
