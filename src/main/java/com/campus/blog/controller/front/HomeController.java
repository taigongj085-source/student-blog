package com.campus.blog.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.blog.common.BlogConstants;
import com.campus.blog.entity.Post;
import com.campus.blog.service.PostService;
import com.campus.blog.service.StatsCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final StatsCacheService statsCacheService;

    @GetMapping({"/", "/index"})
    public String index(@RequestParam(required = false) String cat,
                        @RequestParam(required = false, name = "q") String keyword,
                        @RequestParam(required = false, defaultValue = "latest") String sort,
                        @RequestParam(defaultValue = "1") long page,
                        Model model) {
        String category = (cat == null || cat.isBlank()) ? "全部" : cat;
        Page<Post> postPage = postService.pagePosts(category, keyword, sort, page, 8);
        model.addAttribute("posts", postPage.getRecords());
        model.addAttribute("page", postPage);
        model.addAttribute("curCat", category);
        model.addAttribute("curSort", sort);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("stats", statsCacheService.getStats());
        model.addAttribute("hotPosts", statsCacheService.getHotPosts(6));
        model.addAttribute("allCategories", BlogConstants.CATEGORIES);
        model.addAttribute("categoryMeta", BlogConstants.CATEGORY_META_LIST);
        model.addAttribute("navActive", "home");
        return "front/index";
    }

    @GetMapping("/posts")
    public String posts(@RequestParam(required = false) String cat,
                        @RequestParam(required = false, name = "q") String keyword,
                        @RequestParam(required = false, defaultValue = "latest") String sort,
                        @RequestParam(defaultValue = "1") long page,
                        Model model) {
        String category = (cat == null || cat.isBlank()) ? "全部" : cat;
        Page<Post> postPage = postService.pagePosts(category, keyword, sort, page, 12);
        model.addAttribute("posts", postPage.getRecords());
        model.addAttribute("page", postPage);
        model.addAttribute("curCat", category);
        model.addAttribute("curSort", sort);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("allCategories", BlogConstants.CATEGORIES);
        model.addAttribute("navActive", "posts");
        return "front/posts";
    }

    @GetMapping("/categories")
    public String categories(@RequestParam(required = false) String name, Model model) {
        Map<String, Long> counts = postService.categoryCounts();
        model.addAttribute("categoryCounts", counts);
        model.addAttribute("categoryMeta", BlogConstants.CATEGORY_META_LIST);
        if (name != null && !name.isBlank()) {
            List<Post> posts = postService.listByCategory(name);
            model.addAttribute("curCategory", name);
            model.addAttribute("posts", posts);
        }
        model.addAttribute("navActive", "categories");
        return "front/categories";
    }

    @GetMapping("/tags")
    public String tags(@RequestParam(required = false) String name, Model model) {
        Map<String, Long> tagCounts = postService.tagCounts();
        model.addAttribute("tagCounts", tagCounts);
        if (name != null && !name.isBlank()) {
            List<Post> posts = postService.listByTag(name);
            model.addAttribute("curTag", name);
            model.addAttribute("posts", posts);
        }
        model.addAttribute("navActive", "tags");
        return "front/tags";
    }

    @GetMapping("/archives")
    public String archives(Model model) {
        model.addAttribute("archives", postService.listArchives());
        model.addAttribute("navActive", "archives");
        return "front/archives";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("stats", statsCacheService.getStats());
        model.addAttribute("navActive", "about");
        return "front/about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("navActive", "contact");
        return "front/contact";
    }
}
