package com.pickkasso.pickkasso.user.cash.service;

import com.pickkasso.pickkasso.user.cash.dto.CashChargeRequest;
import com.pickkasso.pickkasso.user.cash.dto.CashChargeResponse;
import com.pickkasso.pickkasso.user.cash.entity.CashHistory;
import com.pickkasso.pickkasso.user.cash.repository.CashHistoryRepository;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.entity.Member;
import com.pickkasso.pickkasso.user.entity.Photographer;
import com.pickkasso.pickkasso.user.entity.Role;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import com.pickkasso.pickkasso.user.repository.MemberRepository;
import com.pickkasso.pickkasso.user.repository.PhotographerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CashService {

    private static final Set<Integer> PRESETS = Set.of(10_000, 30_000, 50_000, 100_000);

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final PhotographerRepository photographerRepository;
    private final CashHistoryRepository cashHistoryRepository;

    @Transactional
    public CashChargeResponse charge(String username, Integer amount) {
        if (amount == null || !PRESETS.contains(amount)) {
            throw new IllegalArgumentException("허용되지 않은 충전 금액입니다.");
        }

        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new AccessDeniedException("계정을 찾을 수 없습니다.");
        }

        int newBalance;
        if (account.getRole() == Role.MEMBER) {
            Member member = memberRepository.findByAccount(account);
            newBalance = member.addCache(amount);
        } else if (account.getRole() == Role.PHOTOGRAPHER) {
            Photographer photographer = photographerRepository.findByAccount(account);
            newBalance = photographer.addCache(amount);
        } else {
            throw new AccessDeniedException("캐시 충전 권한이 없습니다.");
        }

        CashHistory history = cashHistoryRepository.save(
                CashHistory.ofCharge(account, amount, newBalance));
        return new CashChargeResponse(history.getId(), newBalance);
    }
}
