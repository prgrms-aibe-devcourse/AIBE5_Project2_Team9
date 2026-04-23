package com.pickkasso.pickkasso.user.entity;

import com.pickkasso.pickkasso.item.entity.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private Photographer photographer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "memo", length = 500)
    private String memo;

    private Reservation(Member member, Photographer photographer, Item item,
                        LocalDateTime scheduledAt, int durationMinutes,
                        String address, int totalPrice, String memo) {
        this.member = member;
        this.photographer = photographer;
        this.item = item;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.address = address;
        this.totalPrice = totalPrice;
        this.memo = memo;
        this.status = ReservationStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public static Reservation create(Member member, Photographer photographer, Item item,
                                     LocalDateTime scheduledAt, int durationMinutes,
                                     String address, int totalPrice, String memo) {
        return new Reservation(member, photographer, item,
                scheduledAt, durationMinutes, address, totalPrice, memo);
    }

    public void approve() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("승인 가능한 상태가 아닙니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
        this.decidedAt = LocalDateTime.now();
    }

    public void reject() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("거절 가능한 상태가 아닙니다.");
        }
        this.status = ReservationStatus.REJECTED;
        this.decidedAt = LocalDateTime.now();
        // TODO(다음 PR): 캐시 환불 → member.refundCache(totalPrice)
    }

    public void markCompleted() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("완료 처리 가능한 상태가 아닙니다.");
        }
        this.status = ReservationStatus.COMPLETED;
    }
}
