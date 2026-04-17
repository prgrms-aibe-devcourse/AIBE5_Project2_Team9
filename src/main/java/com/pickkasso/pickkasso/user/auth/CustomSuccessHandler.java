package com.pickkasso.pickkasso.user.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    throws IOException, ServletException{

        Object principal = authentication.getPrincipal();

        //카카오 로그인
        if(principal instanceof OAuth2User){
            OAuth2User oAuth2User = (OAuth2User) principal;

            System.out.println("카카오 로그인 성공");
            System.out.println(oAuth2User.getAttributes());

            response.sendRedirect("/");
            return;
        }


        //일반 로그인 처리
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_MEMBER")) {
            response.sendRedirect("/");
        } else if (role.equals("ROLE_PHOTOGRAPHER")) {
            response.sendRedirect("/");
        } else if (role.equals("ROLE_ADMIN")) {
            response.sendRedirect("/admin");
        } else {
            response.sendRedirect("/");
        }
    }
}
