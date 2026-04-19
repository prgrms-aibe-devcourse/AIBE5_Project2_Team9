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
        if (tagName == null) return getScoreItemList(count);
        Pageable pageable = PageRequest.of(0, count);
        List<Item> itemList = itemRepository.findScoreItemListByTagName(tagName, pageable);
        return getItemBoxDtoList(itemList);
    }

    private List<ItemBoxDto> getItemBoxDtoList(List<Item> itemList) {
        List<ItemBoxDto> resList = new ArrayList<>();
        for (Item item : itemList) {
            ItemBoxDto now = ItemBoxDto.from(item);
            resList.add(now);
        }

        return resList;
    }

    // 테스트용 랜덤 item 들고오는 코드
    // 추천 알고리즘 작성 시, 이 항목은 대체됩니다.
    @Transactional(readOnly = true)
    public List<ItemBoxDto> getRandomItemList(Integer count) {
        Pageable pageable = PageRequest.of(0, count);
        List<Item> itemList = itemRepository.findRandomItemList(pageable);
        return getItemBoxDtoList(itemList);
    }
}
