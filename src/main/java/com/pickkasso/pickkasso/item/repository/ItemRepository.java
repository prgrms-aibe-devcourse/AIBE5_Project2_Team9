package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
