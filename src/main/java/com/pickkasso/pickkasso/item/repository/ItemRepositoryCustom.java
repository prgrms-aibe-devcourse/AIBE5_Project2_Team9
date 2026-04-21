package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.item.dto.ItemBoxDto;
import com.pickkasso.pickkasso.item.dto.ItemSearchCondition;
import org.springframework.data.domain.Page;

public interface ItemRepositoryCustom {
    Page<ItemBoxDto> getSearchItemPage(ItemSearchCondition condition, int pageSize);
}
