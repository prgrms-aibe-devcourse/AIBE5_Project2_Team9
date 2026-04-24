package com.pickkasso.pickkasso.review.service;

import com.pickkasso.pickkasso.item.repository.ItemRepository;
import com.pickkasso.pickkasso.review.dto.ReviewCreateRequest;
import com.pickkasso.pickkasso.review.entity.Review;
import com.pickkasso.pickkasso.review.repository.ReviewRepository;
import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import com.pickkasso.pickkasso.user.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void createReview(Long memberId, Long reservationId, ReviewCreateRequest request) {
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!reservation.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("본인의 예약에만 후기를 작성할 수 있습니다.");
        }
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new IllegalStateException("촬영 완료된 예약에만 후기를 작성할 수 있습니다.");
        }
        if (reviewRepository.existsByReservationId(reservationId)) {
            throw new IllegalStateException("이미 후기가 작성된 예약입니다.");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("별점은 1~5점 사이여야 합니다.");
        }

        Review review = Review.create(
                reservation,
                reservation.getMember(),
                reservation.getItem().getPhotographer(),
                request.getRating(),
                request.getContent()
        );
        reviewRepository.save(review);

        reservation.getItem().addReview(request.getRating());
        itemRepository.save(reservation.getItem());
    }
}
