package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.service.ItemService;
import com.pickkasso.pickkasso.user.dto.ReservationCreateRequest;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import com.pickkasso.pickkasso.user.repository.ReservationRepository;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import com.pickkasso.pickkasso.user.service.UserReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final ItemService itemService;
    private final PhotographerProfileService photographerProfileService;
    private final UserReservationService userReservationService;
    private final ReservationRepository reservationRepository;

    @GetMapping("/items/{itemId}/reserve")
    public String reservationPage(
            @PathVariable Long itemId,
            @RequestParam(required = false) Long planId,
            Authentication auth,
            Model model) {

        Item item = itemService.getItemById(itemId);

        Plan selectedPlan = null;
        if (planId != null) {
            selectedPlan = item.getPlanList().stream()
                    .filter(p -> p.getId().equals(planId))
                    .findFirst().orElse(null);
        }
        if (selectedPlan == null && !item.getPlanList().isEmpty()) {
            selectedPlan = item.getPlanList().get(0);
        }

        model.addAttribute("item", item);
        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("member", resolveMember(auth));
        model.addAttribute("profile", photographerProfileService.getProfileForm(item.getPhotographer().getId()));
        return "user/reservation";
    }

    @PostMapping("/items/{itemId}/reserve")
    public String createReservation(
            @PathVariable Long itemId,
            @ModelAttribute ReservationCreateRequest request,
            Authentication auth,
            RedirectAttributes redirectAttrs) {

        Member member = resolveMember(auth);
        if (member == null) {
            return "redirect:/login";
        }

        try {
            Long reservationId = userReservationService.createReservation(member.getId(), request);
            return "redirect:/reservations/" + reservationId + "/complete";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/items/" + itemId + "/reserve?planId=" + request.getPlanId();
        }
    }

    @GetMapping("/reservations/{reservationId}/complete")
    public String reservationComplete(
            @PathVariable Long reservationId,
            Model model) {

        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        model.addAttribute("reservation", reservation);
        return "user/reservation-complete";
    }

    private Member resolveMember(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        Account account = accountRepository.findByUsername(auth.getName());
        if (account == null) return null;
        return memberRepository.findByAccount(account);
    }
}
