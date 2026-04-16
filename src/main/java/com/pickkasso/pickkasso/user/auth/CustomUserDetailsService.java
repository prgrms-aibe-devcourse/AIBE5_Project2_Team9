package com.pickkasso.pickkasso.user.auth;

import com.pickkasso.pickkasso.user.entity.Account;
import com.pickkasso.pickkasso.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username){
        Account account = accountRepository.findByUsername(username);

        if(account == null){
            throw new UsernameNotFoundException("유저 없음");
        }

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole().name())
                .build();

    }
}
