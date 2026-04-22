package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.dto.photographer.ProfileCompletionDto;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer")
public class PhotographerDashboardController {

    private final PhotographerRepository photographerRepository;
    private final PhotographerProfileService photographerProfileService;

    @GetMapping("/{photographerId}/dashboard")
    public String dashboard(@PathVariable Long photographerId, Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            Photographer photographer = photographerRepository.findByAccountUsername(auth.getName());

            if (photographer == null || !photographer.getId().equals(photographerId)) {
                return "redirect:/";
            }

            ProfileCompletionDto completion = photographerProfileService.getProfileCompletion(photographerId);

            model.addAttribute("photographer", photographer);
            model.addAttribute("completion", completion);
            model.addAttribute("activeTab", "dashboard");
            return "photographer/dashboard";
        }
        return "redirect:/login";
    }
}
