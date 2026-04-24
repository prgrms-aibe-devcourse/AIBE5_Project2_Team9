package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.item.service.ItemService;
import com.pickkasso.pickkasso.review.dto.ReviewDto;
import com.pickkasso.pickkasso.review.repository.ReviewRepository;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileEditRequest;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileResponse;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer/{photographerId}/profile")
public class PhotographerProfileController {

    private final PhotographerProfileService photographerProfileService;
    private final ItemService itemService;
    private final AccountRepository accountRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping
    public String profilePage(@PathVariable Long photographerId, Model model, Authentication authentication) {
        PhotographerProfileResponse response = photographerProfileService.getProfileForm(photographerId);
        model.addAttribute("profile", response);
        model.addAttribute("items", itemService.getItemsByPhotographerId(photographerId));

        List<ReviewDto> reviews = reviewRepository.findByPhotographerIdWithDetails(photographerId)
                .stream().map(ReviewDto::new).toList();
        double avgRating = reviews.stream().mapToInt(ReviewDto::getRating).average().orElse(0.0);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("avgRating", String.format("%.1f", avgRating));

        boolean isOwner = false;
        if (authentication != null && authentication.isAuthenticated()) {
            Account account = accountRepository.findByUsername(authentication.getName());
            isOwner = account != null && account.getId().equals(photographerId);
        }
        model.addAttribute("isOwner", isOwner);

        return "photographer/profile";
    }

    @GetMapping("/edit")
    public String editProfilePage(@PathVariable Long photographerId, Model model) {
        PhotographerProfileResponse response = photographerProfileService.getProfileForm(photographerId);

        model.addAttribute("profile", response);
        return "photographer/editProfileForm";
    }

    @PostMapping("/edit")
    public String editProfile(
            @PathVariable Long photographerId,
            @ModelAttribute("profile") PhotographerProfileEditRequest request,
            Model model
    ) {
        try {
            photographerProfileService.createOrUpdateProfile(photographerId, request);
            return "redirect:/photographer/" + photographerId + "/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("profile", request);
            return "photographer/editProfileForm";
        }
    }
}