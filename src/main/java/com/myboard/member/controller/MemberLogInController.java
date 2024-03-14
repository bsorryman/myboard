package com.myboard.member.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.myboard.board.util.Message;
import com.myboard.member.domain.Member;
import com.myboard.member.service.MemberService;
import com.myboard.member.service.UserDetailsServiceImpl;

@Controller
@RequestMapping("/login")
public class MemberLogInController {
    
    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    
    public MemberLogInController(
            MemberService memberService,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            UserDetailsServiceImpl userDetailsService ) {
        
        this.memberService = memberService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * 로그인 페이지
     */
    @GetMapping
    public String getLogInPage() {
        
        return "thymeleaf/member/login";
    }
    
    /**
     * 구글 소셜 로그인은 Security에 설정된 formLogin이 아닌 수동으로 로그인을 처리한다.
     * (id, password가 form 파라미터로 전달되지 않기 때문)
     * 구글 로그인 성공 시 전달되는 credential(jwt)를 디코딩하여 수동으로 member에 매핑하고
     * 수동으로 인증 토큰을 생성하여 검증한다. 
     * @param credential
     * @return
     */
    @PostMapping("/google")
    public String getLogInPage(String credential, HttpServletResponse response) {
        try {
            // google 계정 credential(jwt 토큰) 디코딩
            Map<String, Object> payloadMap = new HashMap<String, Object>();

            payloadMap = memberService.decodePayloadInJwt(credential);
                
            String googleEmail = (String)payloadMap.get("email");
            String googleSub = (String)payloadMap.get("sub");
            
            // sub(password로 사용할 필드) 암호화
            String encodedGoogleSub = passwordEncoder.encode(googleSub);

            // member 객체 매핑
            Member member = new Member();

            member.setMemberId(googleEmail);
            member.setMemberPwd(encodedGoogleSub);
            member.setGSocialYn("y");     
            
            // userName(google eamil)로 UserDetails 가져오기
            UserDetails userDetails = userDetailsService.loadUserByUsername(googleEmail);
            
            // 인증 토큰 생성
            UsernamePasswordAuthenticationToken token = 
                    new UsernamePasswordAuthenticationToken(userDetails, encodedGoogleSub, userDetails.getAuthorities());

            // 인증 
            authenticationManager.authenticate(token);
            boolean result = token.isAuthenticated();
            
            // 인증 성공 및 실패 
            if (result) {
                SecurityContextHolder.getContext().setAuthentication(token);
                // 쿠키 설정
                Cookie cookie = new Cookie("userInfo", googleEmail); 
                cookie.setMaxAge(3600); 
                cookie.setDomain("localhost");
                cookie.setPath("/");
                response.addCookie(cookie); 
                
                return "redirect:/thyme-board/list";
            } else {
                /*
                 * 구글에서 이미 로그인된 구글 계정으로 소셜 로그인을 시도하는 것이므로
                 * result가 false(id는 맞지만 password는 틀린)일 경우는 사실상 없다.
                 * 소셜 로그인의 실패는 모두 UsernameNotFountException에서 걸러진다.
                 */
                return "redirect:/login?error=true";
            }            
        } catch (UsernameNotFoundException e) {
            //회원등록되지 않은 구글 계정
            
            return "redirect:/login?error=true";
        } catch (Exception e) {
            e.printStackTrace();
            
            return "redirect:/login?error=true";
        }
        
    }    
    
}
