package com.pickkasso.pickkasso.review.controller;

import com.pickkasso.pickkasso.review.dto.ReviewCreateRequest;
import com.pickkasso.pickkasso.review.repository.ReviewRepository;
import com.pickkasso.pickkasso.review.service.ReviewService;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import com.pickkasso.pickkasso.user.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private static final int FRAGMENT_PAGE_SIZE = 5;

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/reservations/{reservationId}/review")
    public String reviewForm(@PathVariable Long reservationId, Authentication auth, Model model) {
        Member member = resolveMember(auth);
        if (member == null) return "redirect:/login";

        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!reservation.getMember().getId().equals(member.getId())) {
            return "redirect:/member/mypage/reservations";
        }

        model.addAttribute("member", member);
        model.addAttribute("activeTab", "reservations");
        model.addAttribute("reservation", reservation);
        model.addAttribute("hasReview", reviewRepository.existsByReservationId(reservationId));
        return "user/mypage/review-write";
    }

    @PostMapping("/reservations/{reservationId}/review")
    public String submitReview(@PathVariable Long reservationId,
                               @ModelAttribute ReviewCreateRequest request,
                               Authentication auth,
                               RedirectAttributes redirectAttrs) {
        Member member = resolveMember(auth);
        if (member == null) return "redirect:/login";

        try {
            reviewService.createReview(member.getId(), reservationId, request);
            redirectAttrs.addFlashAttribute("reviewSuccess", true);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("reviewError", e.getMessage());
            return "redirect:/reservations/" + reservationId + "/review";
        }
        return "redirect:/member/mypage/reservations";
    }

    @GetMapping("/review/item/{itemId}/fragment")
    public String itemReviewFragment(
        @PathVariable Long itemId,
        @RequestParam(value = "orderBy", defaultValue = "recent") String orderBy,
        @RequestParam(value = "page", defaultValue = "0") Integer page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), FRAGMENT_PAGE_SIZE, resolveSort(orderBy));
        Page<com.pickkasso.pickkasso.review.dto.spec.ReviewDto> reviewPage = reviewRepository
            .findByReservation_Item_Id(itemId, pageable)
            .map(com.pickkasso.pickkasso.review.dto.spec.ReviewDto::from);

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("reviewCount", (int) reviewPage.getTotalElements());
        model.addAttribute("hasNext", reviewPage.hasNext());
        return "review/item_review_fragment :: reviewList";
    }

    @GetMapping("/review/photographer/{photographerId}/fragment")
    public String photographerReviewFragment(
        @PathVariable Long photographerId,
        @RequestParam(value = "orderBy", defaultValue = "recent") String orderBy,
        @RequestParam(value = "page", defaultValue = "0") Integer page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), FRAGMENT_PAGE_SIZE, resolveSort(orderBy));
        Page<com.pickkasso.pickkasso.review.dto.spec.ReviewDto> reviewPage = reviewRepository
            .findByPhotographer_Id(photographerId, pageable)
            .map(com.pickkasso.pickkasso.review.dto.spec.ReviewDto::from);

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("reviewCount", (int) reviewPage.getTotalElements());
        model.addAttribute("hasNext", reviewPage.hasNext());
        return "review/photographer_review_fragment :: reviewList";
    }

    private Sort resolveSort(String orderBy) {
        if ("score-desc".equalsIgnoreCase(orderBy)) {
            return Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("createdAt"));
        }
        if ("score-asc".equalsIgnoreCase(orderBy)) {
            return Sort.by(Sort.Order.asc("rating"), Sort.Order.desc("createdAt"));
        }
        return Sort.by(Sort.Order.desc("createdAt"));
    }

    private Member resolveMember(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        Account account = accountRepository.findByUsername(auth.getName());
        if (account == null) return null;
        return memberRepository.findByAccount(account);
    }
}
