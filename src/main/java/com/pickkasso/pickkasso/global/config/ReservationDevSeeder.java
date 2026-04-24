package com.pickkasso.pickkasso.global.config;

import com.pickkasso.pickkasso.global.tag.Tag;
import com.pickkasso.pickkasso.global.tag.TagRepository;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.ItemType;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.repository.ItemRepository;
import com.pickkasso.pickkasso.user.entity.*;
import com.pickkasso.pickkasso.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class ReservationDevSeeder implements ApplicationRunner {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final PhotographerRepository photographerRepository;
    private final ItemRepository itemRepository;
    private final TagRepository tagRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Photographer photographer = ensurePhotographer();
        if (reservationRepository.countByPhotographerId(photographer.getId()) > 0) {
            log.info("[seed] reservations already exist for dev photographer={} — skip", photographer.getId());
            return;
        }

        Member m1 = ensureMember("dev_member1", "이수빈", "subin@test.com");
        Member m2 = ensureMember("dev_member2", "정우진", "woojin@test.com");
        Member m3 = ensureMember("dev_member3", "강지우", "jiwoo@test.com");

        Map<String, Item> itemsByName = ensureItems(photographer);

        LocalDate today = LocalDate.now();
        int shift = today.getDayOfWeek().getValue() % 7; // 일=0, 월=1, ..., 토=6
        LocalDate sunday = today.minusDays(shift);
        LocalDate nextFriday = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.FRIDAY));
        LocalDate nextSaturday = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY));

        // 오늘 10:00 CONFIRMED (정우진)
        save(Reservation.create(m2, photographer, itemsByName.get("프로필 촬영"), today.atTime(10, 0),
                60, "서울 스튜디오", 80000, "프로필 촬영 요청"), ReservationStatus.CONFIRMED);

        // 오늘 13:00 CONFIRMED 2시간 (이수빈)
        save(Reservation.create(m1, photographer, itemsByName.get("데이트 스냅"), today.atTime(13, 0),
                120, "홍대 / 연남동", 150000, "데이트 스냅 요청"), ReservationStatus.CONFIRMED);

        // 이번 주 월요일 10:00 CONFIRMED 2시간 (이수빈)
        save(Reservation.create(m1, photographer, itemsByName.get("졸업사진"), sunday.plusDays(1).atTime(10, 0),
                120, "광화문", 180000, "졸업사진 촬영"), ReservationStatus.CONFIRMED);

        // 이번 주 토요일 11:00 CONFIRMED (강지우)
        save(Reservation.create(m3, photographer, itemsByName.get("가족사진"), sunday.plusDays(6).atTime(11, 0),
                60, "분당 율동공원", 130000, "가족사진 촬영"), ReservationStatus.CONFIRMED);

        // 다가오는 금요일 09:00 PENDING (정우진)
        save(Reservation.create(m2, photographer, itemsByName.get("증명사진"), nextFriday.atTime(9, 0),
                60, "강남 스튜디오", 50000, "증명사진 요청"), ReservationStatus.PENDING);

        // 다가오는 토요일 14:00 PENDING (강지우)
        save(Reservation.create(m3, photographer, itemsByName.get("웨딩 스냅"), nextSaturday.atTime(14, 0),
                60, "한강공원", 200000, "웨딩 스냅 요청"), ReservationStatus.PENDING);

        // 지난주 COMPLETED (정우진)
        save(Reservation.create(m2, photographer, itemsByName.get("프로필 촬영"), sunday.minusDays(5).atTime(11, 0),
                60, "부산 해운대", 120000, "지난주 촬영"), ReservationStatus.COMPLETED);

        // 지난주 REJECTED (강지우)
        save(Reservation.create(m3, photographer, itemsByName.get("웨딩 스냅"), sunday.minusDays(2).atTime(15, 0),
                60, "성수동", 100000, "거절 케이스"), ReservationStatus.REJECTED);

        log.info("[seed] reservation dev seeding complete (7 records)");
    }

    private Map<String, Item> ensureItems(Photographer photographer) {
        Map<String, Item> itemsByName = new LinkedHashMap<>();
        for (Item item : itemRepository.findByPhotographerId(photographer.getId())) {
            itemsByName.putIfAbsent(item.getName(), item);
        }

        ensureItem(itemsByName, photographer, "프로필", "프로필 촬영",
                "전문 프로필 사진 촬영", "보정 3컷 포함", "의상 및 메이크업 별도",
                ItemType.IN, 24, "촬영 3일 전부터 환불 불가",
                "서울특별시 강남구 테헤란로 1", 37.5000, 127.0370,
                "1인 프로필", 80000, 60, 120, 3, 3);
        ensureItem(itemsByName, photographer, "데이트", "데이트 스냅",
                "자연스러운 커플 스냅 촬영", "원본 전체 제공", "입장료 별도",
                ItemType.OUT, 48, "촬영 5일 전부터 일정 변경만 가능",
                "서울특별시 마포구 양화로 188", 37.5570, 126.9245,
                "커플 2시간", 150000, 120, 250, 10, 5);
        ensureItem(itemsByName, photographer, "증명", "증명사진",
                "빠르게 완성하는 증명사진 촬영", "기본 보정 포함", "인화 배송 별도",
                ItemType.IN, 12, "촬영 전날까지 취소 가능",
                "서울특별시 강남구 봉은사로 112", 37.5045, 127.0280,
                "기본 증명", 50000, 30, 20, 2, 2);
        ensureItem(itemsByName, photographer, "웨딩", "웨딩 스냅",
                "본식 전후 웨딩 스냅 촬영", "보정본 20컷 포함", "드레스 및 부케 별도",
                ItemType.OUT, 72, "촬영 7일 전부터 환불 불가",
                "서울특별시 용산구 서빙고로 137", 37.5204, 126.9946,
                "웨딩 하프데이", 200000, 180, 400, 20, 7);
        ensureItem(itemsByName, photographer, "졸업", "졸업사진",
                "졸업 시즌 야외/실내 촬영", "보정본 8컷 포함", "학사복 대여 별도",
                ItemType.OUT, 24, "우천 시 일정 조율",
                "서울특별시 종로구 세종대로 175", 37.5720, 126.9769,
                "졸업 2시간", 180000, 120, 220, 8, 4);
        ensureItem(itemsByName, photographer, "가족", "가족사진",
                "자연스러운 가족 야외 촬영", "보정본 6컷 포함", "소품은 개별 준비",
                ItemType.OUT, 24, "촬영 2일 전까지 변경 가능",
                "경기도 성남시 분당구 문정로 145", 37.3786, 127.1492,
                "가족 1시간", 130000, 60, 150, 6, 4);

        return itemsByName;
    }

    private void ensureItem(Map<String, Item> itemsByName,
                            Photographer photographer,
                            String tagName,
                            String itemName,
                            String description,
                            String includes,
                            String excludes,
                            ItemType itemType,
                            int minBookingLeadTime,
                            String cancellationPolicy,
                            String address,
                            double lat,
                            double lng,
                            String planName,
                            int price,
                            int shootingDuration,
                            int originalPhotoCount,
                            int editedPhotoCount,
                            int deliveryDays) {
        if (itemsByName.containsKey(itemName)) {
            return;
        }

        Tag tag = ensureTag(tagName);
        Item item = Item.createItem(
                photographer, tag, itemName, description,
                includes, excludes, itemType, minBookingLeadTime,
                cancellationPolicy, address, lat, lng
        );
        Plan.createPlan(item, planName, price, shootingDuration, originalPhotoCount, editedPhotoCount, deliveryDays);
        itemRepository.save(item);
        itemsByName.put(itemName, item);
        log.info("[seed] created dev item: {}", itemName);
    }

    private Tag ensureTag(String name) {
        return tagRepository.findTagByName(name)
                .orElseGet(() -> tagRepository.save(Tag.createTag(name)));
    }

    private void save(Reservation r, ReservationStatus targetStatus) {
        switch (targetStatus) {
            case CONFIRMED -> r.approve();
            case REJECTED -> r.reject();
            case COMPLETED -> {
                r.approve();
                r.markCompleted();
            }
            case PENDING, CANCELED -> {
            }
        }
        reservationRepository.save(r);
    }

    private Photographer ensurePhotographer() {
        Account acc = accountRepository.findByUsername("dev_photo");
        if (acc == null) {
            acc = Account.createAccount("dev_photo",
                    passwordEncoder.encode("password1a"), Role.PHOTOGRAPHER);
            accountRepository.save(acc);
        }

        Photographer photographer = photographerRepository.findByAccountUsername("dev_photo");
        if (photographer == null) {
            Photographer p = Photographer.createMember(acc, "devphoto@test.com",
                    "테스트작가", Gender.MALE, "010-0000-0001", 0);
            photographerRepository.save(p);
            log.info("[seed] created dev photographer account");
            photographer = p;
        }
        return photographer;
    }

    private Member ensureMember(String username, String name, String email) {
        Account acc = accountRepository.findByUsername(username);
        if (acc == null) {
            acc = Account.createAccount(username,
                    passwordEncoder.encode("password1a"), Role.MEMBER);
            accountRepository.save(acc);
        }

        Member member = memberRepository.findByAccount(acc);
        if (member == null) {
            Member m = Member.createMember(acc, email, name, Gender.FEMALE, "010-1111-1111", 500000);
            memberRepository.save(m);
            log.info("[seed] created dev member: {}", username);
            member = m;
        }
        return member;
    }
}
