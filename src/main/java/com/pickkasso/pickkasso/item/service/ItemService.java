package com.pickkasso.pickkasso.item.service;

import com.pickkasso.pickkasso.item.dto.ItemBoxDto;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<ItemBoxDto> getScoreItemList(Integer count) {
        Pageable pageable = PageRequest.of(0, count);
        List<Item> itemList = itemRepository.findScoreItemList(pageable);
        return getItemBoxDtoList(itemList);
    }

    @Transactional(readOnly = true)
    public List<ItemBoxDto> getScoreItemList(String tagName, Integer count) {
        Pageable pageable = PageRequest.of(0, count);
        List<Item> itemList = itemRepository.findScoreItemListByTagName(tagName, pageable);
        return getItemBoxDtoList(itemList);
    }

    private List<ItemBoxDto> getItemBoxDtoList(List<Item> itemList) {
        List<ItemBoxDto> resList = new ArrayList<>();
        for (Item item : itemList) {
            ItemBoxDto now = ItemBoxDto.from(item);
        }

        return resList;
    }
}
