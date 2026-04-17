package com.pickkasso.pickkasso.item.controller;

import com.pickkasso.pickkasso.item.dto.ItemSearchFormDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class ItemController {

    @GetMapping("/search")
    public String searchItem(@ModelAttribute ItemSearchFormDto itemSearchFormDto) {
        System.out.println(itemSearchFormDto);
        return "search/itemSearchForm";
    }
}
