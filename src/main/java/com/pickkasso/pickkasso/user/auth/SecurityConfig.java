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
                    .requestMatchers("/", "/home", "/login", "/signup/**", "/find-account", "/403", "/404", "/500", "/400", "/error-default",
                        "/css/**", "/fonts/**", "/js/**", "/images/**",
                        "/find-id/**", "/find-pw/**", "/item/**",
                        "/search/**", "/items/fragment","/api/check-username",
                        "/review/item/**", "/review/photographer/**"
                    ).permitAll()
                    .requestMatchers("/photographer/*/profile", "/photographer/*/portfolio/*").permitAll()
                    .requestMatchers("/photographer/**").hasRole("PHOTOGRAPHER")
                            .anyRequest().authenticated()
            )
                .exceptionHandling(exception -> exception
                    .accessDeniedPage("/403")
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
                                .userService(customOAuth2UserService))
                                .successHandler(customSuccessHandler)
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
