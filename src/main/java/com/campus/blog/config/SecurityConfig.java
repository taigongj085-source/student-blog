package com.campus.blog.config;

import cn.hutool.json.JSONUtil;
import com.campus.blog.common.Result;
import com.campus.blog.security.BlogUserDetails;
import com.campus.blog.security.Md5PasswordEncoder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Md5PasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/login", "/admin/doLogin").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/post/new",
                                "/post/create",
                                "/post/delete/**",
                                "/comment/add",
                                "/api/like/**",
                                "/api/favorite/**",
                                "/profile/**"
                        ).authenticated()
                        .requestMatchers("/doRegister").permitAll()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/doLogin")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler())
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/logout"))
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();
                            if (uri != null && uri.startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setCharacterEncoding("UTF-8");
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.getWriter().write(JSONUtil.toJsonStr(Result.fail("请先登录")));
                                return;
                            }
                            String redirect = uri;
                            String query = request.getQueryString();
                            if (query != null && !query.isBlank()) {
                                redirect = uri + "?" + query;
                            }
                            String encoded = URLEncoder.encode(redirect == null ? "/" : redirect, StandardCharsets.UTF_8);
                            response.sendRedirect("/login?redirect=" + encoded);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendRedirect("/"))
                );
        return http.build();
    }

    private AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            Object principal = authentication.getPrincipal();
            if (principal instanceof BlogUserDetails) {
                // BlogUserDetails 已持有 User，无需额外 Session 身份字段
            }
            String redirect = request.getParameter("redirect");
            if (redirect == null || redirect.isBlank() || !redirect.startsWith("/")) {
                redirect = "/";
            }
            response.sendRedirect(redirect);
        };
    }

    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String redirect = request.getParameter("redirect");
            if (redirect == null || redirect.isBlank()) {
                redirect = "/";
            }
            String encoded = URLEncoder.encode(redirect, StandardCharsets.UTF_8);
            response.sendRedirect("/login?error=1&redirect=" + encoded);
        };
    }
}
