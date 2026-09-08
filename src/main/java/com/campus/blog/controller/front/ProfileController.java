package com.campus.blog.controller.front;

import com.campus.blog.entity.Comment;
import com.campus.blog.entity.Post;
import com.campus.blog.entity.User;
import com.campus.blog.service.CommentService;
import com.campus.blog.service.PostService;
import com.campus.blog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/profile")
    public String profile(@RequestParam(defaultValue = "posts") String tab, Model model) {
        User user = SecurityUtil.getCurrentUser();
        List<Post> myPosts = postService.listByUser(user.getId());
        List<Comment> myComments = commentService.listByUser(user.getId());
        List<Post> myLikes = postService.listLikedByUser(user.getId());
        List<Post> myFavorites = postService.listFavoriteByUser(user.getId());
        model.addAttribute("tab", tab);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("myComments", myComments);
        model.addAttribute("myLikes", myLikes);
        model.addAttribute("myFavorites", myFavorites);
        model.addAttribute("navActive", "profile");
        return "front/profile";
    }
}
