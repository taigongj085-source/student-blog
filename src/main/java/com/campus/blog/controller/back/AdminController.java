package com.campus.blog.controller.back;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.blog.common.BusinessException;
import com.campus.blog.entity.Comment;
import com.campus.blog.entity.Post;
import com.campus.blog.entity.User;
import com.campus.blog.mapper.UserMapper;
import com.campus.blog.security.BlogUserDetails;
import com.campus.blog.service.CommentService;
import com.campus.blog.service.PostService;
import com.campus.blog.service.StatsCacheService;
import com.campus.blog.service.UserService;
import com.campus.blog.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PostService postService;
    private final CommentService commentService;
    private final StatsCacheService statsCacheService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String loginPage() {
        User user = SecurityUtil.getCurrentUser();
        if (SecurityUtil.isAdmin(user)) {
            return "redirect:/admin";
        }
        return "back/login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletRequest request,
                          RedirectAttributes ra) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof BlogUserDetails details) || !SecurityUtil.isAdmin(details.getUser())) {
                throw new BusinessException("非管理员账号，无法登录后台");
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
            return "redirect:/admin";
        } catch (BusinessException e) {
            SecurityContextHolder.clearContext();
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/login";
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            ra.addFlashAttribute("errorMsg", "昵称或密码错误");
            return "redirect:/admin/login";
        }
    }

    @GetMapping({"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("stats", statsCacheService.getStats());
        model.addAttribute("menu", "dashboard");
        return "back/index";
    }

    @GetMapping("/posts")
    public String posts(@RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "1") long page,
                        Model model) {
        Page<Post> postPage = postService.adminPage(q, page, 15);
        model.addAttribute("page", postPage);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("menu", "posts");
        return "back/post-list";
    }

    @PostMapping("/posts/{id}/top")
    public String top(@PathVariable Long id, @RequestParam Integer isTop) {
        postService.setTop(id, isTop);
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/status")
    public String status(@PathVariable Long id, @RequestParam Integer status) {
        postService.setStatus(id, status);
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id, SecurityUtil.getCurrentUser());
        return "redirect:/admin/posts";
    }

    @GetMapping("/comments")
    public String comments(@RequestParam(defaultValue = "1") long page, Model model) {
        Page<Comment> commentPage = commentService.adminPage(page, 20);
        model.addAttribute("page", commentPage);
        model.addAttribute("menu", "comments");
        return "back/comment-list";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return "redirect:/admin/comments";
    }

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "1") long page, Model model) {
        Page<User> userPage = userMapper.selectPage(new Page<>(page, 20),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));
        model.addAttribute("page", userPage);
        model.addAttribute("menu", "users");
        return "back/user-list";
    }

    @PostMapping("/users/{id}/status")
    public String userStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return "redirect:/admin/users";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return "redirect:/admin/login";
    }
}
