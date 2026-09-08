package com.campus.blog.controller.front;

import com.campus.blog.common.BusinessException;
import com.campus.blog.entity.User;
import com.campus.blog.security.BlogUserDetails;
import com.campus.blog.service.UserService;
import com.campus.blog.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String tab,
                            @RequestParam(required = false) String redirect,
                            @RequestParam(required = false) String error,
                            Model model) {
        if (SecurityUtil.getCurrentUser() != null) {
            return "redirect:" + (redirect == null || redirect.isBlank() ? "/" : redirect);
        }
        model.addAttribute("tab", tab == null ? "login" : tab);
        model.addAttribute("redirect", redirect == null ? "/" : redirect);
        if ("1".equals(error)) {
            model.addAttribute("errorMsg", "昵称或密码错误");
        }
        return "front/login";
    }

    @PostMapping("/doRegister")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String password2,
                             @RequestParam(required = false, defaultValue = "/") String redirect,
                             HttpServletRequest request,
                             RedirectAttributes ra) {
        try {
            User user = userService.register(username, password, password2);
            BlogUserDetails details = new BlogUserDetails(user);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
            return "redirect:" + (redirect == null || redirect.isBlank() ? "/" : redirect);
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            ra.addFlashAttribute("tab", "register");
            return "redirect:/login?tab=register&redirect="
                    + URLEncoder.encode(redirect, StandardCharsets.UTF_8);
        }
    }
}
