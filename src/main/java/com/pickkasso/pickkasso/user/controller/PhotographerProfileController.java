package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileEditRequest;
import com.pickkasso.pickkasso.user.dto.photographer.PhotographerProfileResponse;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer/{photographerId}/profile")
public class PhotographerProfileController {

    private final PhotographerProfileService photographerProfileService;

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