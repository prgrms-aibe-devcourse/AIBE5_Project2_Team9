package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
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

    @GetMapping("/{photographerId}/dashboard")
    public String dashboard(@PathVariable Long photographerId, Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            Photographer photographer = photographerRepository.findByAccountUsername(auth.getName());
            
            // 본인의 대시보드인지 확인
            if (photographer == null || !photographer.getId().equals(photographerId)) {
                // 권한이 없거나 잘못된 접근인 경우 처리 (예: 홈으로 리다이렉트 또는 에러 페이지)
                return "redirect:/";
            }
            
            model.addAttribute("photographer", photographer);
            model.addAttribute("activeTab", "dashboard");
            return "photographer/dashboard";
        }
        return "redirect:/login";
    }
}
