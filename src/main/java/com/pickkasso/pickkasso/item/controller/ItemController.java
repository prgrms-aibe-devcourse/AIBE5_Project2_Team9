package com.pickkasso.pickkasso.item.controller;

import com.pickkasso.pickkasso.global.tag.TagService;
import com.pickkasso.pickkasso.item.dto.ItemBoxDto;
import com.pickkasso.pickkasso.item.dto.ItemSearchCondition;
import com.pickkasso.pickkasso.item.dto.ItemSearchFormDto;
import com.pickkasso.pickkasso.item.entity.ItemType;
import com.pickkasso.pickkasso.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final TagService tagService;
    private final ItemService itemService;
    private static final int PAGE_VIEW_COUNT = 20;

    private ItemSearchCondition toCondition(ItemSearchFormDto dto) {
        ItemSearchCondition condition = new ItemSearchCondition();

        condition.setLat(dto.getLat());
        condition.setLng(dto.getLng());
        condition.setDistance(dto.getDistance());
        condition.setTag((dto.getTag() == null) ? null : tagService.findByName(dto.getTag()).orElse(null));
        condition.setDate(dto.getDate());
        condition.setOrderBy(dto.getOrderBy());
        condition.setPage((dto.getPage() == null) ? 1 : dto.getPage());

        ItemType itemType = null;
        if (dto.getItemType() != null) {
            try {
                itemType = ItemType.valueOf(dto.getItemType());
            } catch (IllegalArgumentException e) {
                itemType = null;
            }
        }
        condition.setItemType(itemType);

        return condition;
    }

    @GetMapping("/search")
    public String searchItem(@ModelAttribute ItemSearchFormDto itemSearchFormDto, Model model) {
        model.addAttribute("tagList", tagService.findAllTagReference());
        // List<ItemBoxDto> items = itemService.getScoreItemList(10);
        Page<ItemBoxDto> items = itemService.getItemList(toCondition(itemSearchFormDto), PAGE_VIEW_COUNT);
        model.addAttribute("items", items.getContent());
        model.addAttribute("totalCount", items.getTotalElements());

        int pageGroupSize = 5;
        int pageGroup = (items.getNumber()) / pageGroupSize;
        int startPage = pageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, items.getTotalPages());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", items.getTotalPages());
        model.addAttribute("currentPage", items.getNumber() + 1); // 1 기반
        return "search/itemSearchForm";
    }

    @GetMapping("/items/fragment")
    public String scoreGridFragment(
        @RequestParam(required = false) String tagName,
        @RequestParam(defaultValue = "5") int count,
        Model model) {
        model.addAttribute("scoreItemList", itemService.getScoreItemList(tagName, count));
        return "fragments/score_items :: scoreGrid";
    }
}
