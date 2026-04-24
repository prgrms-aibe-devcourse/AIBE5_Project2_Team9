package com.pickkasso.pickkasso.review.entity;

import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_review",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_reservation", columnNames = "order_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private Photographer photographer;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Review(Reservation reservation, Member member, Photographer photographer,
                   int rating, String content) {
        this.reservation = reservation;
        this.member = member;
        this.photographer = photographer;
        this.rating = rating;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public static Review create(Reservation reservation, Member member, Photographer photographer,
                                int rating, String content) {
        return new Review(reservation, member, photographer, rating, content);
    }
}
