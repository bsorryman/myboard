package com.myboard.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
/**
 * 로컬 계정 로그인 성공시 쿠키를 등록하는 핸들러
 * (구글 소셜 로그인은 수동으로)
 * @author jspark
 *
 */
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); 
        
        // 쿠키 설정
        Cookie cookie = new Cookie("userInfo", username); 
        cookie.setMaxAge(3600); 
        cookie.setDomain("localhost");
        cookie.setPath("/");
        response.addCookie(cookie); 

        // 성공 후 redirect url
        setDefaultTargetUrl("/thyme-board/list");
        super.onAuthenticationSuccess(request, response, authentication);
    }
    
}
