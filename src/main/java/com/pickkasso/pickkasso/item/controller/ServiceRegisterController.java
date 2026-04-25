package com.pickkasso.pickkasso.item.controller;

import com.pickkasso.pickkasso.global.tag.TagService;
import com.pickkasso.pickkasso.item.dto.ItemRegisterRequest;
import com.pickkasso.pickkasso.item.dto.PlanRegisterRequest;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.entity.PlanType;
import com.pickkasso.pickkasso.item.service.ItemService;
import com.pickkasso.pickkasso.item.service.PlanService;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.service.PhotographerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/photographer/{photographerId}/service")
public class ServiceRegisterController {

    private final ItemService itemService;
    private final TagService tagService;
    private final PhotographerProfileService photographerProfileService;
    private final PlanService planService;
    private final AccountRepository accountRepository;

    @GetMapping("/{itemId}")
    public String serviceDetail(
        @PathVariable Long photographerId,
        @PathVariable Long itemId,
        Model model,
        Authentication authentication) {

        Item item = itemService.getItemById(itemId);
        model.addAttribute("item", item);
        model.addAttribute("profile", photographerProfileService.getProfileForm(photographerId));
        model.addAttribute("enabledPlans", planService.getEnabledPlans(itemId));
        model.addAttribute("itemImgList", itemService.getItemImage(photographerId, itemId));
        model.addAttribute("canEditService", isOwnerPhotographer(item, authentication));
        return "photographer/service-detail";
    }

    @GetMapping("/new")
    public String serviceRegisterForm(@PathVariable Long photographerId, Model model) {
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("tagList", tagService.findAllTagReference());
        model.addAttribute("itemRegisterRequest", new ItemRegisterRequest());
        model.addAttribute("planTypes", PlanType.values());
        model.addAttribute("formAction", "/photographer/" + photographerId + "/service/new");
        return "photographer/service-register";
    }

    @PostMapping("/new")
    public String registerService(
        @PathVariable Long photographerId,
        @ModelAttribute ItemRegisterRequest itemRegisterRequest,
        @RequestParam(required = false) List<String> keptImgUrls,
        @RequestParam(required = false) List<Integer> keptImgOrders,
        @RequestParam(required = false) List<MultipartFile> newFiles,
        @RequestParam(required = false) List<Integer> newFileOrders,
        RedirectAttributes redirectAttributes) {

        try {
            Long itemId = itemService.registerItem(photographerId, itemRegisterRequest, newFiles, newFileOrders);
            redirectAttributes.addFlashAttribute("successMessage", "서비스가 등록되었습니다.");
            return "redirect:/photographer/" + photographerId + "/service/" + itemId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/photographer/" + photographerId + "/service/new";
        }
    }


    @GetMapping("/{itemId}/edit")
    public String serviceEditForm(
        @PathVariable Long photographerId,
        @PathVariable Long itemId,
        Authentication authentication,
        Model model) {
        ensurePhotographerOwner(authentication, photographerId);

        Item item = itemService.getItemById(itemId);
        if (!item.getPhotographer().getId().equals(photographerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 서비스만 수정할 수 있습니다.");
        }

        model.addAttribute("photographerId", photographerId);
        model.addAttribute("tagList", tagService.findAllTagReference());
        model.addAttribute("itemRegisterRequest", itemService.getItemRegisterRequest(photographerId, itemId));
        model.addAttribute("itemImgList", itemService.getItemImage(photographerId, itemId));
        model.addAttribute("planTypes", PlanType.values());
        model.addAttribute("formAction", "/photographer/" + photographerId + "/service/" + itemId + "/edit");
        return "photographer/service-register";
    }

    // 예외처리 부분 반영하지 않음
    @PostMapping("/{itemId}/edit")
    public String updateService(
        @PathVariable Long photographerId,
        @PathVariable Long itemId,
        Authentication authentication,
        @ModelAttribute ItemRegisterRequest itemRegisterRequest,
        @RequestParam(required = false) List<String> keptImgUrls,
        @RequestParam(required = false) List<Integer> keptImgOrders,
        @RequestParam(required = false) List<MultipartFile> newFiles,
        @RequestParam(required = false) List<Integer> newFileOrders,
        RedirectAttributes redirectAttributes) {
        ensurePhotographerOwner(authentication, photographerId);

        try {
            itemService.updateItem(photographerId, itemId, itemRegisterRequest, keptImgUrls, keptImgOrders, newFiles, newFileOrders);
            redirectAttributes.addFlashAttribute("successMessage", "서비스가 수정되었습니다.");
            return "redirect:/photographer/" + photographerId + "/service/" + itemId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/photographer/" + photographerId + "/service/" + itemId + "/edit";
        }
    }

    private void ensurePhotographerOwner(Authentication authentication, Long photographerId) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        Account account = accountRepository.findByUsername(authentication.getName());
        if (account == null || account.getRole() != Role.PHOTOGRAPHER || !photographerId.equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 계정으로만 수정할 수 있습니다.");
        }
    }

    private boolean isOwnerPhotographer(Item item, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }
        if (authentication.getAuthorities().stream().noneMatch(a -> "ROLE_PHOTOGRAPHER".equals(a.getAuthority()))) {
            return false;
        }
        String ownerUsername = item.getPhotographer().getAccount().getUsername();
        return ownerUsername != null && ownerUsername.equals(authentication.getName());
    }

    // @GetMapping("/item/{itemId}/edit")
    // public String serviceEditForm(
    //     @PathVariable Long photographerId,
    //     @PathVariable Long itemId,
    //     Authentication authentication,
    //     Model model) {
    //     ensurePhotographerOwner(authentication, photographerId);
    //
    //     Item item = itemService.getItemById(itemId);
    //     if (!item.getPhotographer().getId().equals(photographerId)) {
    //         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 서비스만 수정할 수 있습니다.");
    //     }
    //
    //     ItemRegisterRequest itemRequest = toItemRegisterRequest(item);
    //     populateEditFormModel(model, photographerId, itemId, itemRequest);
    //     return "photographer/service-edit";
    // }
    //
    // @PostMapping("/item/{itemId}/edit")
    // public String editService(
    //     @PathVariable Long photographerId,
    //     @PathVariable Long itemId,
    //     Authentication authentication,
    //     @ModelAttribute ItemRegisterRequest itemRegisterRequest,
    //     Model model,
    //     RedirectAttributes redirectAttributes) {
    //     ensurePhotographerOwner(authentication, photographerId);
    //
    //     try {
    //         itemService.updateItem(photographerId, itemId, itemRegisterRequest);
    //         redirectAttributes.addFlashAttribute("successMessage", "서비스가 수정되었습니다.");
    //         return "redirect:/item/" + itemId;
    //     } catch (Exception e) {
    //         model.addAttribute("errorMessage", e.getMessage());
    //         populateEditFormModel(model, photographerId, itemId, itemRegisterRequest);
    //         return "photographer/service-edit";
    //     }
    // }
    //
    // private void populateEditFormModel(
    //     Model model,
    //     Long photographerId,
    //     Long itemId,
    //     ItemRegisterRequest itemRequest
    // ) {
    //     model.addAttribute("photographerId", photographerId);
    //     model.addAttribute("tagList", tagService.findAllTagReference());
    //     model.addAttribute("categoryEmojis", CATEGORY_EMOJIS);
    //     model.addAttribute("itemRequest", itemRequest);
    //     model.addAttribute("itemRegisterRequest", itemRequest);
    //     model.addAttribute("formAction", "/photographer/" + photographerId + "/item/" + itemId + "/edit");
    // }
    //
    // private void ensurePhotographerOwner(Authentication authentication, Long photographerId) {
    //     if (authentication == null || authentication.getName() == null) {
    //         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    //     }
    //     Account account = accountRepository.findByUsername(authentication.getName());
    //     if (account == null || account.getRole() != Role.PHOTOGRAPHER || !photographerId.equals(account.getId())) {
    //         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 계정으로만 수정할 수 있습니다.");
    //     }
    // }
    //
    // private boolean isOwnerPhotographer(Item item, Authentication authentication) {
    //     if (authentication == null || authentication.getName() == null) {
    //         return false;
    //     }
    //     if (authentication.getAuthorities().stream().noneMatch(a -> "ROLE_PHOTOGRAPHER".equals(a.getAuthority()))) {
    //         return false;
    //     }
    //     String ownerUsername = item.getPhotographer().getAccount().getUsername();
    //     return ownerUsername != null && ownerUsername.equals(authentication.getName());
    // }
    //
    // private ItemRegisterRequest toItemRegisterRequest(Item item) {
    //     ItemRegisterRequest request = new ItemRegisterRequest();
    //     request.setTagName(item.getTag().getName());
    //     request.setItemType(item.getItemType());
    //     request.setName(item.getName());
    //     request.setDescription(item.getDescription());
    //     request.setIncludes(item.getIncludes());
    //     request.setExcludes(item.getExcludes());
    //     request.setAddress(item.getAddress());
    //     request.setLat(item.getLat());
    //     request.setLng(item.getLng());
    //     request.setMinBookingLeadTime(item.getMinBookingLeadTime());
    //     request.setCancellationPolicy(item.getCancellationPolicy());
    //
    //     List<PlanRegisterRequest> plans = new ArrayList<>();
    //     item.getPlanList().stream()
    //         .sorted(Comparator.comparing(Plan::getId))
    //         .forEach(plan -> {
    //             PlanRegisterRequest planRequest = new PlanRegisterRequest();
    //             planRequest.setPlanName(plan.getName());
    //             planRequest.setPrice(plan.getPrice());
    //             planRequest.setShootingDuration(plan.getShootingDuration());
    //             planRequest.setOriginalPhotoCount(plan.getOriginalPhotoCount());
    //             planRequest.setEditedPhotoCount(plan.getEditedPhotoCount());
    //             planRequest.setDeliveryDays(plan.getDeliveryDays());
    //             plans.add(planRequest);
    //         });
    //     request.setPlans(plans);
    //     return request;
    // }
}
