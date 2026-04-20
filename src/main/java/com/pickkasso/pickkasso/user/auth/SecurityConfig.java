package com.pickkasso.pickkasso.user.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/login", "/signup/**", "/find-account","/css/**", "/js/**", "/images/**","/find-id/**", "/find-pw/**").permitAll()
                    .requestMatchers("/photographer/**").hasRole("PHOTOGRAPHER")
                    .requestMatchers("/member/**").hasRole("MEMBER")
                    .anyRequest().authenticated()
            )
                .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customSuccessHandler)
                .failureUrl("/login?error=true")
                )

                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(user -> user
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("/", true)
                )
               .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
           );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
