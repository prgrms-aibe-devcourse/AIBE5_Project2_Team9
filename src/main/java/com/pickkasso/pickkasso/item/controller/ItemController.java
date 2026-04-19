package com.pickkasso.pickkasso.item.controller;

import com.pickkasso.pickkasso.global.tag.TagService;
import com.pickkasso.pickkasso.item.dto.ItemSearchFormDto;
import com.pickkasso.pickkasso.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final TagService tagService;
    private final ItemService itemService;

    @GetMapping("/search")
    public String searchItem(@ModelAttribute ItemSearchFormDto itemSearchFormDto, Model model) {
        model.addAttribute("tags", tagService.findAllTagReference());
        model.addAttribute("items", Collections.emptyList());
        model.addAttribute("totalCount", 0);
        return "search/itemSearchForm";
    }

    @GetMapping("/items/fragment")
    public String scoreGridFragment(
        @RequestParam(required = false) String tagName,
        @RequestParam(defaultValue = "5") int count,
        Model model) {
        model.addAttribute("scoreItemList", itemService.getScoreItemList(tagName, count));
        return "fragments/item_box :: scoreGrid";
    }
}
