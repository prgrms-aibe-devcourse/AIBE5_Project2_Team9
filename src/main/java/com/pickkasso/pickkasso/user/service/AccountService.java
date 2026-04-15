package com.pickkasso.pickkasso.user.service;

import com.pickkasso.pickkasso.user.dto.AccountDto;
import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private Account saveAccount(Account account) {
        validateDuplicateAccount(account);
        return accountRepository.save(account);
    }

    public Account saveAccount(AccountDto dto) {
        String encoded = passwordEncoder.encode(dto.getPassword());
        return saveAccount(dto.createAccount(encoded));
    }

    private void validateDuplicateAccount(Account account) {
        Account findAccount = accountRepository.findByUsername(account.getUsername());
        if (findAccount != null) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
    }
}
