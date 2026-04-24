package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.review.dto.ReviewDto;
import com.pickkasso.pickkasso.review.dto.StarDistributionEntry;
import com.pickkasso.pickkasso.review.entity.Review;
import com.pickkasso.pickkasso.review.repository.ReviewRepository;
import com.pickkasso.pickkasso.user.dto.photographer.ProfileCompletionDto;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import com.pickkasso.pickkasso.user.service.PhotographerReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer")
public class PhotographerDashboardController {

    private final PhotographerRepository photographerRepository;
    private final PhotographerProfileService photographerProfileService;
    private final PhotographerReservationService reservationService;
    private final ReviewRepository reviewRepository;

    @GetMapping("/{photographerId}/dashboard")
    public String dashboard(@PathVariable Long photographerId,
                            @RequestParam(required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                            Authentication auth,
                            Model model) {
        Photographer photographer = resolveAuthorizedPhotographer(photographerId, auth);
        if (photographer == null) return auth == null || !auth.isAuthenticated() ? "redirect:/login" : "redirect:/";

        populateDashboardModel(model, photographer, weekStart);

        return "photographer/dashboard";
    }

    @GetMapping("/{photographerId}/reservations")
    public String reservations(@PathVariable Long photographerId,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String keyword,
                               Authentication auth,
                               Model model) {
        Photographer photographer = resolveAuthorizedPhotographer(photographerId, auth);
        if (photographer == null) return auth == null || !auth.isAuthenticated() ? "redirect:/login" : "redirect:/";

        model.addAttribute("photographer", photographer);
        model.addAttribute("userId", photographerId);
        model.addAttribute("activeTab", "reservations");
        model.addAttribute("summary", reservationService.getSummary(photographerId));
        model.addAttribute("reservationManagementSummary", reservationService.getManagementSummary(photographerId));
        model.addAttribute("reservationItems", reservationService.getManagementReservations(photographerId, status, keyword));
        model.addAttribute("selectedStatus", reservationService.normalizeStatusFilter(status));
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());

        return "photographer/reservations";
    }

    @GetMapping("/{photographerId}/dashboard/weekly-calendar")
    public String weeklyCalendar(@PathVariable Long photographerId,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                                 Authentication auth,
                                 Model model) {
        Photographer photographer = resolveAuthorizedPhotographer(photographerId, auth);
        if (photographer == null) return auth == null || !auth.isAuthenticated() ? "redirect:/login" : "redirect:/";

        model.addAttribute("photographer", photographer);
        model.addAttribute("weekly", reservationService.getWeeklyCalendar(
                photographerId, reservationService.normalizeWeekStart(weekStart)
        ));
        return "photographer/dashboard :: weeklyCalendarCard";
    }

    @GetMapping("/{photographerId}/reviews")
    public String reviews(@PathVariable Long photographerId,
                          Authentication auth,
                          Model model) {
        Photographer photographer = resolveAuthorizedPhotographer(photographerId, auth);
        if (photographer == null) return auth == null || !auth.isAuthenticated() ? "redirect:/login" : "redirect:/";

        List<Review> rawReviews = reviewRepository.findByPhotographerIdWithDetails(photographerId);
        List<ReviewDto> reviews = rawReviews.stream().map(ReviewDto::new).toList();

        int total = rawReviews.size();
        double avg = rawReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        YearMonth now = YearMonth.now();
        long thisMonthCount = rawReviews.stream()
                .filter(r -> YearMonth.from(r.getCreatedAt()).equals(now))
                .count();

        List<StarDistributionEntry> starDistribution = new ArrayList<>();
        for (int i = 5; i >= 1; i--) {
            final int star = i;
            int c = (int) rawReviews.stream().filter(r -> r.getRating() == star).count();
            int percent = total > 0 ? c * 100 / total : 0;
            starDistribution.add(new StarDistributionEntry(star, c, percent));
        }

        model.addAttribute("photographer", photographer);
        model.addAttribute("userId", photographerId);
        model.addAttribute("activeTab", "reviews");
        model.addAttribute("summary", reservationService.getSummary(photographerId));
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", total);
        model.addAttribute("avgRating", String.format("%.1f", avg));
        model.addAttribute("thisMonthCount", thisMonthCount);
        model.addAttribute("starDistribution", starDistribution);

        return "photographer/reviews";
    }

    @GetMapping("/{photographerId}/settlement")
    public String settlement(@PathVariable Long photographerId,
                             Authentication auth,
                             Model model) {
        Photographer photographer = resolveAuthorizedPhotographer(photographerId, auth);
        if (photographer == null) return auth == null || !auth.isAuthenticated() ? "redirect:/login" : "redirect:/";

        model.addAttribute("photographer", photographer);
        model.addAttribute("userId", photographerId);
        model.addAttribute("activeTab", "settlement");
        model.addAttribute("summary", reservationService.getSummary(photographerId));

        return "photographer/settlement";
    }

    @PostMapping("/{photographerId}/reservations/{reservationId}/accept")
    public String accept(@PathVariable Long photographerId,
                         @PathVariable Long reservationId,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                         @RequestParam(required = false) String redirectTo,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String keyword,
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
        return redirectAfterReservationAction(photographerId, redirectTo, weekStart, status, keyword);
    }

    @PostMapping("/{photographerId}/reservations/{reservationId}/reject")
    public String reject(@PathVariable Long photographerId,
                         @PathVariable Long reservationId,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                         @RequestParam(required = false) String redirectTo,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String keyword,
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
        return redirectAfterReservationAction(photographerId, redirectTo, weekStart, status, keyword);
    }

    private String redirectToDashboard(Long photographerId, LocalDate weekStart) {
        if (weekStart == null) {
            return "redirect:/photographer/" + photographerId + "/dashboard";
        }
        return "redirect:/photographer/" + photographerId + "/dashboard?weekStart=" + weekStart;
    }

    private String redirectAfterReservationAction(Long photographerId,
                                                  String redirectTo,
                                                  LocalDate weekStart,
                                                  String status,
                                                  String keyword) {
        if ("reservations".equalsIgnoreCase(redirectTo)) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/photographer/{photographerId}/reservations")
                    .queryParamIfPresent("status", java.util.Optional.ofNullable(status).filter(s -> !s.isBlank()))
                    .queryParamIfPresent("keyword", java.util.Optional.ofNullable(keyword).filter(s -> !s.isBlank()));
            return "redirect:" + builder.buildAndExpand(photographerId).encode().toUriString();
        }
        return redirectToDashboard(photographerId, weekStart);
    }

    private Photographer resolveAuthorizedPhotographer(Long photographerId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Photographer photographer = photographerRepository.findByAccountUsername(auth.getName());
        if (photographer == null || !photographer.getId().equals(photographerId)) {
            return null;
        }
        return photographer;
    }

    private void populateDashboardModel(Model model, Photographer photographer, LocalDate weekStart) {
        Long photographerId = photographer.getId();
        LocalDate normalizedWeekStart = reservationService.normalizeWeekStart(weekStart);
        ProfileCompletionDto completion = photographerProfileService.getProfileCompletion(photographerId);

        model.addAttribute("photographer", photographer);
        model.addAttribute("userId", photographerId);
        model.addAttribute("completion", completion);
        model.addAttribute("activeTab", "dashboard");
        model.addAttribute("summary", reservationService.getSummary(photographerId));
        model.addAttribute("pendingList", reservationService.getPendingList(photographerId));
        model.addAttribute("todaySchedule", reservationService.getTodaySchedule(photographerId));
        model.addAttribute("weekly", reservationService.getWeeklyCalendar(photographerId, normalizedWeekStart));
    }
}
