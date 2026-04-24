package com.pickkasso.pickkasso.user.controller;

import com.pickkasso.pickkasso.global.img.ImageUploadException;
import com.pickkasso.pickkasso.global.tag.TagService;
import com.pickkasso.pickkasso.user.dto.portfolio.PortfolioDto;
import com.pickkasso.pickkasso.user.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer/{photographerId}/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final TagService tagService;

    @GetMapping("/new")
    public String createPortfolioForm(@PathVariable Long photographerId, Model model) {
        model.addAttribute("portfolioDto", portfolioService.getEmptyPortfolioDto(photographerId));
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("tagList", tagService.findAllTagReference());
        model.addAttribute("formAction", "/photographer/" + photographerId + "/portfolio/new");
        return "photographer/editPortfolio";
    }

    @PostMapping("/new")
    public String createPortfolio(
            @PathVariable Long photographerId,
            @ModelAttribute("portfolioDto") PortfolioDto portfolioDto,
            @RequestParam(required = false) List<String> keptImgUrls,
            @RequestParam(required = false) List<Integer> keptImgOrders,
            @RequestParam(required = false) List<MultipartFile> newFiles,
            @RequestParam(required = false) List<Integer> newFileOrders,
            Model model
    ) {
        // System.out.println("===");
        // System.out.println(keptImgUrls);
        // System.out.println(keptImgOrders);
        // System.out.println(newFiles == null ? null : newFiles.size());
        // System.out.println(newFileOrders);
        // System.out.println("===");

        try {
            Long portfolioId = portfolioService.createPortfolio(photographerId, portfolioDto, newFiles, newFileOrders);
            return "redirect:/photographer/" + photographerId + "/portfolio/" + portfolioId;
        } catch (IllegalArgumentException | ImageUploadException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("portfolioDto", portfolioDto);
            model.addAttribute("tagList", tagService.findAllTagReference());
            model.addAttribute("photographerId", photographerId);
            model.addAttribute("formAction", "/photographer/" + photographerId + "/portfolio/new");
            return "photographer/editPortfolio";
        }
    }

    @GetMapping("/{portfolioId}")
    public String portfolioDetail(
            @PathVariable Long photographerId,
            @PathVariable Long portfolioId,
            Model model
    ) {
        PortfolioDto portfolioDto = portfolioService.getPortfolioDto(photographerId, portfolioId);
        model.addAttribute("portfolioName", portfolioDto.name());
        model.addAttribute("portfolioDescription", portfolioDto.description());
        model.addAttribute("tagList", tagService.findAllTagReference());
        model.addAttribute("portfolioTagList", portfolioService.getPortfolioTagReference(photographerId, portfolioId));
        model.addAttribute("portfolioImgList", portfolioService.getPortfolioImage(photographerId, portfolioId));
        model.addAttribute("portfolioProjectType", portfolioDto.projectType());
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("portfolioId", portfolioId);
        model.addAttribute("photographerNickname", "");
        model.addAttribute("photographerImgUrl", null);
        model.addAttribute("photographerRating", 0.0);
        model.addAttribute("photographerReviewCount", 0);
        return "photographer/portfolio-detail";
    }

    @GetMapping("/{portfolioId}/edit")
    public String editPortfolioForm(
            @PathVariable Long photographerId,
            @PathVariable Long portfolioId,
            Model model
    ) {
        PortfolioDto portfolioDto = portfolioService.getPortfolioDto(photographerId, portfolioId);
        model.addAttribute("portfolioDto", portfolioDto);
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("tagList", tagService.findAllTagReference());
        model.addAttribute("portfolioImgList", portfolioService.getPortfolioImage(photographerId, portfolioId));
        model.addAttribute("portfolioId", portfolioId);
        model.addAttribute("formAction", "/photographer/" + photographerId + "/portfolio/" + portfolioId + "/edit");
        return "photographer/editPortfolio";
    }

    @PostMapping("/{portfolioId}/edit")
    public String editPortfolio(
            @PathVariable Long photographerId,
            @PathVariable Long portfolioId,
            @ModelAttribute("portfolioDto") PortfolioDto portfolioDto,
            @RequestParam(required = false) List<String> keptImgUrls,
            @RequestParam(required = false) List<Integer> keptImgOrders,
            @RequestParam(required = false) List<MultipartFile> newFiles,
            @RequestParam(required = false) List<Integer> newFileOrders,
            Model model
    ) {
        try {
            portfolioService.updatePortfolio(photographerId, portfolioId, portfolioDto, keptImgUrls, keptImgOrders, newFiles, newFileOrders);
            return "redirect:/photographer/" + photographerId + "/portfolio/" + portfolioId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("portfolioDto", portfolioDto);
            model.addAttribute("tagList", tagService.findAllTagReference());
            model.addAttribute("portfolioImgList", portfolioService.getPortfolioImage(photographerId, portfolioId));
            model.addAttribute("photographerId", photographerId);
            model.addAttribute("portfolioId", portfolioId);
            model.addAttribute("formAction", "/photographer/" + photographerId + "/portfolio/" + portfolioId + "/edit");
            return "photographer/editPortfolio";
        }
    }

    @PostMapping("/{portfolioId}/delete")
    public String deletePortfolio(
            @PathVariable Long photographerId,
            @PathVariable Long portfolioId
    ) {
        try {
            portfolioService.deletePortfolio(photographerId, portfolioId);
            return "redirect:/photographer/" + photographerId + "/profile";
        } catch (IllegalArgumentException e) {
            return "redirect:/photographer/" + photographerId + "/portfolio/" + portfolioId;
        }
    }
}