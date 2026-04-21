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
        String memberType = request.getParameter("memberType"); // user or photographer
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 일반회원 로그인 눌렀는데 작가 계정이면 차단
        if ("user".equals(memberType) && role.equals("ROLE_PHOTOGRAPHER")) {
            response.sendRedirect("/login?error=true");
            return;
        }

        // 작가 로그인 눌렀는데 일반회원이면 차단
        if ("photographer".equals(memberType) && role.equals("ROLE_MEMBER")) {
            response.sendRedirect("/login?error=true");
            return;
        }

        // 정상 로그인
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
