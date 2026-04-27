package com.pickkasso.pickkasso.item.service;

import com.pickkasso.pickkasso.item.dto.PlanRegisterRequest;
import com.pickkasso.pickkasso.item.entity.Item;
import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<Plan> getEnabledPlans(Long itemId) {
        List<Plan> resList =  planRepository.findByItemIdAndEnabled(itemId);
        resList.sort(Comparator.comparing(p -> p.getPlanType().getOrder()));
        return resList;
    }

    public Plan savePlan(Item item, PlanRegisterRequest request) {
        Plan plan = planRepository.findByItemIdAndPlanType(item.getId(), request.getPlanType())
            .orElseGet(() -> {
                Plan p = Plan.createPlan(
                    item,
                    request.getPlanType(),
                    request.getEnabled() != null ? request.getEnabled() : false,
                    request.getPlanName() != null ? request.getPlanName() : request.getPlanType().toString(),
                    request.getPrice() != null ? request.getPrice() : 1_000_000,
                    request.getShootingDuration() != null ? request.getShootingDuration() : 1,
                    request.getOriginalPhotoCount() != null ? request.getOriginalPhotoCount() : 0,
                    request.getEditedPhotoCount() != null ? request.getEditedPhotoCount() : 0,
                    request.getDeliveryDays() != null ? request.getDeliveryDays() : 3
                );
                p.getItem().addPlan(p);
                return p;
            });
        // update
        if (plan.getId() != null) {
            plan.updatePlan(
                request.getEnabled() != null ? request.getEnabled() : false,
                request.getPlanName() != null ? request.getPlanName() : request.getPlanType().toString(),
                request.getPrice() != null ? request.getPrice() : 1_000_000,
                request.getShootingDuration() != null ? request.getShootingDuration() : 1,
                request.getOriginalPhotoCount() != null ? request.getOriginalPhotoCount() : 0,
                request.getEditedPhotoCount() != null ? request.getEditedPhotoCount() : 0,
                request.getDeliveryDays() != null ? request.getDeliveryDays() : 3
            );
            plan = planRepository.save(plan);
        }

        return plan;
    }
}
