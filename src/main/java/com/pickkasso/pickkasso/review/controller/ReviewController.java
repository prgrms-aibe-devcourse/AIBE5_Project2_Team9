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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {

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

    private Member resolveMember(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        Account account = accountRepository.findByUsername(auth.getName());
        if (account == null) return null;
        return memberRepository.findByAccount(account);
    }
}
