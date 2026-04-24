package com.pickkasso.pickkasso.item.service;

import com.pickkasso.pickkasso.global.city.City;
import com.pickkasso.pickkasso.global.tag.Tag;
import com.pickkasso.pickkasso.global.tag.TagRepository;
import com.pickkasso.pickkasso.item.dto.ItemBoxDto;
import com.pickkasso.pickkasso.item.dto.ItemRegisterRequest;
import com.pickkasso.pickkasso.item.dto.ItemSearchCondition;
import com.pickkasso.pickkasso.item.dto.PlanRegisterRequest;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.repository.ItemRepository;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final PhotographerRepository photographerRepository;
    private final TagRepository tagRepository;

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

    // 선택하는 내용에 거리를 몇Km로 할 건지도 있어야 할 것 같아서 추가 예정
    @Transactional(readOnly = true)
    public Page<ItemBoxDto> getItemList(ItemSearchCondition condition, int pageSize) {
        return itemRepository.getSearchItemPage(condition, pageSize);
    }

    public Long registerItem(Long photographerId, ItemRegisterRequest request) {
        Photographer photographer = photographerRepository.findById(photographerId)
            .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다."));

        Tag tag = tagRepository.findTagByName(request.getTagName())
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + request.getTagName()));

        Item item = Item.createItem(
            photographer,
            tag,
            request.getName(),
            request.getDescription() != null ? request.getDescription() : "",
            request.getIncludes(),
            request.getExcludes(),
            request.getItemType(),
            request.getMinBookingLeadTime() != null ? request.getMinBookingLeadTime() : 1,
            request.getCancellationPolicy(),
            request.getAddress(),
            request.getLat(),
            request.getLng()
        );

        if (request.getPlans() != null) {
            for (PlanRegisterRequest planReq : request.getPlans()) {
                Plan.createPlan(
                    item,
                    planReq.getPlanName(),
                    planReq.getPrice() != null ? planReq.getPrice() : 0,
                    planReq.getShootingDuration() != null ? planReq.getShootingDuration() : 1,
                    planReq.getOriginalPhotoCount() != null ? planReq.getOriginalPhotoCount() : 0,
                    planReq.getEditedPhotoCount() != null ? planReq.getEditedPhotoCount() : 0,
                    planReq.getDeliveryDays() != null ? planReq.getDeliveryDays() : 3
                );
            }
        }

        item.updateDefaultPrice();
        return itemRepository.save(item).getId();
    }

    public Long updateItem(Long photographerId, Long itemId, ItemRegisterRequest request) {
        Item item = itemRepository.findByIdWithDetails(itemId)
            .orElseThrow(() -> new IllegalArgumentException("서비스를 찾을 수 없습니다."));
        if (!item.getPhotographer().getId().equals(photographerId)) {
            throw new IllegalArgumentException("본인 서비스만 수정할 수 있습니다.");
        }

        Tag tag = tagRepository.findTagByName(request.getTagName())
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + request.getTagName()));

        item.updateBasicInfo(
            tag,
            request.getName(),
            request.getDescription(),
            request.getIncludes(),
            request.getExcludes(),
            request.getItemType(),
            request.getMinBookingLeadTime(),
            request.getCancellationPolicy(),
            request.getAddress(),
            request.getLat(),
            request.getLng()
        );

        List<Plan> existingPlans = new ArrayList<>(item.getPlanList());
        for (Plan plan : existingPlans) {
            plan.deletePlan();
        }

        if (request.getPlans() != null) {
            for (PlanRegisterRequest planReq : request.getPlans()) {
                Plan.createPlan(
                    item,
                    planReq.getPlanName(),
                    planReq.getPrice() != null ? planReq.getPrice() : 0,
                    planReq.getShootingDuration() != null ? planReq.getShootingDuration() : 1,
                    planReq.getOriginalPhotoCount() != null ? planReq.getOriginalPhotoCount() : 0,
                    planReq.getEditedPhotoCount() != null ? planReq.getEditedPhotoCount() : 0,
                    planReq.getDeliveryDays() != null ? planReq.getDeliveryDays() : 3
                );
            }
        }
        item.updateDefaultPrice();
        return itemRepository.save(item).getId();
    }

    private List<ItemBoxDto> getItemBoxDtoList(List<Item> itemList) {
        List<ItemBoxDto> resList = new ArrayList<>();
        for (Item item : itemList) {
            ItemBoxDto now = ItemBoxDto.from(item);
            resList.add(now);
        }

        return resList;
    }

    @Transactional(readOnly = true)
    public List<Item> getItemsByPhotographerId(Long photographerId) {
        return itemRepository.findByPhotographerId(photographerId);
    }

    @Transactional(readOnly = true)
    public Item getItemById(Long itemId) {
        return itemRepository.findByIdWithDetails(itemId)
            .orElseThrow(() -> new IllegalArgumentException("서비스를 찾을 수 없습니다."));
    }

    // 테스트용 랜덤 item 들고오는 코드
    // 추천 알고리즘 작성 시, 이 항목은 대체됩니다.
    @Transactional(readOnly = true)
    public List<ItemBoxDto> getRandomItemList(Integer count) {
        Pageable pageable = PageRequest.of(0, count);
        List<Item> itemList = itemRepository.findRandomItemList(pageable);
        return getItemBoxDtoList(itemList);
    }

    // TODO: Item에 purchaseCount가 추가되면, 해당 값도 조회해서 그 값을 더하는 방식 사용
    @Transactional(readOnly = true)
    public List<City> getTop3CityByPhotographerId(Long photographerId) {
        Map<City, Integer> resMap = new HashMap<>();
        List<City> resList = new ArrayList<>();
        List<String> addressList = itemRepository.findAddressByPhotographerId(photographerId);

        for (String address : addressList) {
            resMap.merge(City.fromString(address), 1, Integer::sum);
        }
        resMap.entrySet().stream()
            .sorted(Map.Entry.<City, Integer>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .forEach(resList::add);
        return resList;
    }
}
