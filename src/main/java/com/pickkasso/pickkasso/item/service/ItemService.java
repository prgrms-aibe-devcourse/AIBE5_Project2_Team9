package com.pickkasso.pickkasso.item.service;

import com.pickkasso.pickkasso.global.city.City;
import com.pickkasso.pickkasso.global.img.DefaultImgDto;
import com.pickkasso.pickkasso.global.region.RegionDto;
import com.pickkasso.pickkasso.global.service.DefaultImgService;
import com.pickkasso.pickkasso.global.tag.Tag;
import com.pickkasso.pickkasso.global.tag.TagReference;
import com.pickkasso.pickkasso.global.tag.TagRepository;
import com.pickkasso.pickkasso.item.dto.ItemBoxDto;
import com.pickkasso.pickkasso.item.dto.ItemRegisterRequest;
import com.pickkasso.pickkasso.item.dto.ItemSearchCondition;
import com.pickkasso.pickkasso.item.dto.PlanRegisterRequest;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.ItemImg;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.repository.ItemRepository;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final PhotographerRepository photographerRepository;
    private final TagRepository tagRepository;
    private final DefaultImgService defaultImgService;
    private final PlanService planService;

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

    private List<ItemImg> toItemImgList(Item item, List<DefaultImgDto> imgDtoList) {
        if (imgDtoList == null || imgDtoList.isEmpty()) return new ArrayList<>();
        return imgDtoList.stream()
            .map(dto -> ItemImg.createItemImg(
                item,
                dto.getImgUrl(),
                dto.getImgOrder()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DefaultImgDto> getItemImage(Long photographerId, Long itemId) {
        Item item = itemRepository.findByIdWithImgList(itemId)
            .orElseThrow(() -> new IllegalArgumentException("해당 작가의 서비스가 아닙니다."));
        return item.getItemImgList().stream()
            .map(DefaultImgDto::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ItemRegisterRequest getItemRegisterRequest(Long photographerId, Long itemId) {
        Item item = itemRepository.findByIdAndPhotographerId(itemId, photographerId)
            .orElseThrow(() -> new IllegalArgumentException("해당 작가의 서비스가 아닙니다."));
        List<PlanRegisterRequest> planRegisterRequestList = new ArrayList<>();
        for (Plan plan : item.getPlanList()) {
            planRegisterRequestList.add(new PlanRegisterRequest(
                plan.getName(),
                plan.getPlanType(),
                plan.getEnabled(),
                plan.getShootingDuration(),
                plan.getOriginalPhotoCount(),
                plan.getEditedPhotoCount(),
                plan.getDeliveryDays(),
                plan.getPrice()
            ));
        }

        ItemRegisterRequest request = new ItemRegisterRequest(
            item.getTag().getName(),
            item.getItemType(),
            item.getName(),
            item.getDescription(),
            item.getIncludes(),
            item.getExcludes(),
            item.getAddress(),
            item.getLat(),
            item.getLng(),
            item.getMinBookingLeadTime(),
            item.getCancellationPolicy(),
            planRegisterRequestList
        );
        return request;
    }

    public Long registerItem(Long photographerId, ItemRegisterRequest request, List<MultipartFile> newFiles, List<Integer> newFileOrders) {
        Photographer photographer = photographerRepository.findById(photographerId)
            .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다."));

        Tag tag = tagRepository.findTagByName(request.getTagName())
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + request.getTagName()));

        // image upload + save
        String dirName = "photographer/user_" + photographerId + "/service";
        List<DefaultImgDto> imgDtoList = defaultImgService.uploadImages(newFiles, newFileOrders, dirName);


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

        System.out.println("complete create item");
        System.out.println();
        if (request.getPlans() != null) {
            for (PlanRegisterRequest planReq : request.getPlans()) {
                Plan plan = planService.savePlan(item, planReq);
                System.out.println("complete create plan " + plan.getPlanType());
            }
        }

        item.updateItemImgList(toItemImgList(item, imgDtoList));
        item.updateDefaultPrice();
        return itemRepository.save(item).getId();
    }

    public void updateItem(Long photographerId, Long itemId, ItemRegisterRequest request, List<String> keptImgUrls, List<Integer> keptImgOrders, List<MultipartFile> newFiles, List<Integer> newFileOrders) {
        Photographer photographer = photographerRepository.findById(photographerId)
            .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다."));

        Tag tag = tagRepository.findTagByName(request.getTagName())
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + request.getTagName()));

        Item item = itemRepository.findByIdAndPhotographerId(itemId, photographerId)
            .orElseThrow(() -> new IllegalArgumentException("해당 작가의 서비스가 아닙니다."));
        itemRepository.findByIdWithImgList(itemId);

        // image upload + save
        String dirName = "photographer/user_" + photographerId + "/service";
        List<DefaultImgDto> imgDtoList = defaultImgService.updateImages(item.getItemImgList(), keptImgUrls, keptImgOrders, newFiles, newFileOrders, dirName);

        item.updateItem(
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

        for (PlanRegisterRequest plan : request.getPlans()) {
            planService.savePlan(item, plan);
        }

        item.updateItemImgList(toItemImgList(item, imgDtoList));
        item.updateDefaultPrice();
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
            try {
                resMap.merge(City.fromString(address), 1, Integer::sum);
            } catch (Exception ignored) {

            }
        }
        resMap.entrySet().stream()
            .sorted(Map.Entry.<City, Integer>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .forEach(resList::add);
        return resList;
    }

    public List<ItemBoxDto> getItemBoxDtoById(Long photographerId) {
        List<Item> items = itemRepository.findItemByPhotographerId(photographerId);
        tagRepository.findAllTagReference();
        List<ItemBoxDto> resList = new ArrayList<>();
        for (Item item : items) {
            ItemBoxDto dto = ItemBoxDto.builder()
                .id(item.getId())
                .name(item.getName())
                .imgUrl(item.getThumbnailImgUrl())
                .tag(TagReference.from(item.getTag()))
                .region(RegionDto.from(item))
                .defaultPrice(item.getDefaultPrice())
                .itemType(item.getItemType())
                .reviewScore(item.getReviewScore())
                .reviewCount(item.getReviewCount())
                .build();
            resList.add(dto);
        }
        return resList;
    }

    public List<ItemBoxDto> getCustomItemList(int count) {
        List<Item> itemList = itemRepository.findCustomItem(PageRequest.of(0, count));
        return getItemBoxDtoList(itemList);
    }
}
