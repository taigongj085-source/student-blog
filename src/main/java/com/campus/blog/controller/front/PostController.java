package com.campus.blog.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.blog.common.BlogConstants;
import com.campus.blog.common.Result;
import com.campus.blog.entity.Comment;
import com.campus.blog.entity.CommentLike;
import com.campus.blog.entity.Post;
import com.campus.blog.entity.User;
import com.campus.blog.mapper.CommentLikeMapper;
import com.campus.blog.service.CommentService;
import com.campus.blog.service.InteractService;
import com.campus.blog.service.PostService;
import com.campus.blog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final InteractService interactService;
    private final CommentLikeMapper commentLikeMapper;

    @GetMapping("/post/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User loginUser = SecurityUtil.getCurrentUser();
        Post post = postService.getDetail(id, loginUser == null ? null : loginUser.getId());
        if (post == null) {
            model.addAttribute("notFound", true);
            model.addAttribute("navActive", "posts");
            return "front/detail";
        }
        postService.increaseView(id);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        List<Comment> comments = commentService.listByPost(id);
        if (loginUser != null && !comments.isEmpty()) {
            List<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
            Set<Long> likedIds = commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                            .eq(CommentLike::getUserId, loginUser.getId())
                            .in(CommentLike::getCommentId, commentIds))
                    .stream().map(CommentLike::getCommentId).collect(Collectors.toCollection(HashSet::new));
            for (Comment c : comments) {
                c.setLiked(likedIds.contains(c.getId()));
            }
        }
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("canDelete", loginUser != null
                && (loginUser.getId().equals(post.getUserId()) || SecurityUtil.isAdmin(loginUser)));
        model.addAttribute("navActive", "posts");
        return "front/detail";
    }

    @GetMapping("/post/new")
    public String newPost(Model model) {
        model.addAttribute("categories", BlogConstants.CATEGORIES);
        model.addAttribute("categoryMeta", BlogConstants.CATEGORY_META_LIST);
        model.addAttribute("navActive", "posts");
        return "front/new";
    }

    @PostMapping("/post/create")
    public String create(@RequestParam String title,
                         @RequestParam String category,
                         @RequestParam(required = false) String tags,
                         @RequestParam String content,
                         @RequestParam(required = false) String excerpt,
                         @RequestParam(required = false) String coverEmoji,
                         @RequestParam(required = false) String coverColor,
                         RedirectAttributes ra) {
        User user = SecurityUtil.getCurrentUser();
        Post post = postService.create(user, title, category, tags, content, excerpt, coverEmoji, coverColor);
        ra.addFlashAttribute("okMsg", "发布成功");
        return "redirect:/post/" + post.getId();
    }

    @PostMapping("/post/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        User user = SecurityUtil.getCurrentUser();
        postService.deletePost(id, user);
        ra.addFlashAttribute("okMsg", "文章已删除");
        return "redirect:/posts";
    }

    @PostMapping("/comment/add")
    public String addComment(@RequestParam Long postId,
                             @RequestParam String content) {
        User user = SecurityUtil.getCurrentUser();
        commentService.add(postId, user, content);
        return "redirect:/post/" + postId + "#comments";
    }

    @PostMapping("/api/like/post/{id}")
    @ResponseBody
    public Result<Map<String, Object>> likePost(@PathVariable Long id) {
        User user = SecurityUtil.getCurrentUser();
        return Result.ok(interactService.togglePostLike(id, user.getId()));
    }

    @PostMapping("/api/like/comment/{id}")
    @ResponseBody
    public Result<Map<String, Object>> likeComment(@PathVariable Long id) {
        User user = SecurityUtil.getCurrentUser();
        return Result.ok(interactService.toggleCommentLike(id, user.getId()));
    }

    @PostMapping("/api/favorite/{id}")
    @ResponseBody
    public Result<Map<String, Object>> favorite(@PathVariable Long id) {
        User user = SecurityUtil.getCurrentUser();
        return Result.ok(interactService.toggleFavorite(id, user.getId()));
    }
}
