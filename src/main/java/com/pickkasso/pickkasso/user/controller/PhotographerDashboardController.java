package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.photographer.ProfileCompletionDto;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import com.pickkasso.pickkasso.user.service.PhotographerReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer")
public class PhotographerDashboardController {

    private final PhotographerRepository photographerRepository;
    private final PhotographerProfileService photographerProfileService;
    private final PhotographerReservationService reservationService;

    @GetMapping("/{photographerId}/dashboard")
    public String dashboard(@PathVariable Long photographerId, Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        Photographer photographer = photographerRepository.findByAccountUsername(auth.getName());
        if (photographer == null || !photographer.getId().equals(photographerId)) {
            return "redirect:/";
        }

        ProfileCompletionDto completion = photographerProfileService.getProfileCompletion(photographerId);

        model.addAttribute("photographer", photographer);
        model.addAttribute("completion", completion);
        model.addAttribute("activeTab", "dashboard");
        model.addAttribute("summary", reservationService.getSummary(photographerId));
        model.addAttribute("pendingList", reservationService.getPendingList(photographerId));
        model.addAttribute("todaySchedule", reservationService.getTodaySchedule(photographerId));
        model.addAttribute("weekly", reservationService.getWeeklyCalendar(photographerId));

        return "photographer/dashboard";
    }

    @PostMapping("/{photographerId}/reservations/{reservationId}/accept")
    public String accept(@PathVariable Long photographerId,
                         @PathVariable Long reservationId,
                         Authentication auth,
                         RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";
        Photographer me = photographerRepository.findByAccountUsername(auth.getName());
        if (me == null || !me.getId().equals(photographerId)) return "redirect:/";
        try {
            reservationService.approve(photographerId, reservationId);
            ra.addFlashAttribute("toast", "예약을 수락했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("toastError", e.getMessage());
        }
        return "redirect:/photographer/" + photographerId + "/dashboard";
    }

    @PostMapping("/{photographerId}/reservations/{reservationId}/reject")
    public String reject(@PathVariable Long photographerId,
                         @PathVariable Long reservationId,
                         Authentication auth,
                         RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";
        Photographer me = photographerRepository.findByAccountUsername(auth.getName());
        if (me == null || !me.getId().equals(photographerId)) return "redirect:/";
        try {
            reservationService.reject(photographerId, reservationId);
            ra.addFlashAttribute("toast", "예약을 거절했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("toastError", e.getMessage());
        }
        return "redirect:/photographer/" + photographerId + "/dashboard";
    }
}
