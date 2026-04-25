package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.item.dto.ItemBoxDto;
import com.pickkasso.pickkasso.item.dto.ItemSearchCondition;
import com.pickkasso.pickkasso.item.entity.Item;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ItemRepositoryCustom {
    Page<ItemBoxDto> getSearchItemPage(ItemSearchCondition condition, int pageSize);

    /**
     * AI PICK: 파싱 JSON 조건을 반영한 Item 목록. JOIN FETCH: tag, photographer.
     */
    List<Item> findForAiPick(AiItemQuerySpec spec);
}
