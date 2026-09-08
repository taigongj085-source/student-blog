package com.campus.blog.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.blog.common.BlogConstants;
import com.campus.blog.common.BusinessException;
import com.campus.blog.entity.Favorite;
import com.campus.blog.entity.Post;
import com.campus.blog.entity.PostLike;
import com.campus.blog.entity.User;
import com.campus.blog.mapper.FavoriteMapper;
import com.campus.blog.mapper.PostLikeMapper;
import com.campus.blog.mapper.PostMapper;
import com.campus.blog.mapper.UserMapper;
import com.campus.blog.util.BlogUtil;
import com.campus.blog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper postLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final StatsCacheService statsCacheService;

    public Page<Post> pagePosts(String category, String keyword, String sort, long pageNum, long pageSize) {
        LambdaQueryWrapper<Post> qw = new LambdaQueryWrapper<>();
        qw.eq(Post::getStatus, 1);
        if (StrUtil.isNotBlank(category) && !"全部".equals(category)) {
            qw.eq(Post::getCategory, category);
        }
        if (StrUtil.isNotBlank(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(Post::getTitle, kw)
                    .or().like(Post::getContent, kw)
                    .or().like(Post::getTags, kw)
                    .or().like(Post::getExcerpt, kw));
        }
        qw.orderByDesc(Post::getIsTop);
        if ("hot".equals(sort)) {
            qw.orderByDesc(Post::getLikeCount).orderByDesc(Post::getCommentCount).orderByDesc(Post::getCreateTime);
        } else if ("views".equals(sort)) {
            qw.orderByDesc(Post::getViewCount).orderByDesc(Post::getCreateTime);
        } else {
            qw.orderByDesc(Post::getCreateTime);
        }
        Page<Post> page = postMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        fillAuthors(page.getRecords());
        return page;
    }

    public Post getDetail(Long id, Long currentUserId) {
        Post post = postMapper.selectById(id);
        if (post == null || Objects.equals(post.getStatus(), 0)) {
            return null;
        }
        fillAuthors(Collections.singletonList(post));
        post.setTagList(BlogUtil.splitTags(post.getTags()));
        if (currentUserId != null) {
            post.setLiked(postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getPostId, id).eq(PostLike::getUserId, currentUserId)) > 0);
            post.setFavorited(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getPostId, id).eq(Favorite::getUserId, currentUserId)) > 0);
        } else {
            post.setLiked(false);
            post.setFavorited(false);
        }
        return post;
    }

    public void increaseView(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return;
        }
        Post upd = new Post();
        upd.setId(id);
        upd.setViewCount(post.getViewCount() == null ? 1 : post.getViewCount() + 1);
        postMapper.updateById(upd);
        statsCacheService.clearCache();
    }

    @Transactional
    public Post create(User user, String title, String category, String tags, String content,
                       String excerpt, String coverEmoji, String coverColor) {
        title = StrUtil.trim(title);
        content = content == null ? "" : content.trim();
        if (StrUtil.isBlank(title)) {
            throw new BusinessException("请输入标题");
        }
        if (title.length() > 120) {
            throw new BusinessException("标题不能超过 120 字");
        }
        if (StrUtil.isBlank(content) || content.length() < 5) {
            throw new BusinessException("正文至少 5 个字");
        }
        if (StrUtil.isBlank(category)) {
            category = BlogConstants.CATEGORIES.get(0);
        }
        if (!BlogConstants.CATEGORIES.contains(category)) {
            throw new BusinessException("分类不存在");
        }
        if (StrUtil.isBlank(excerpt)) {
            excerpt = BlogUtil.plainExcerpt(content, 160);
        }
        if (StrUtil.isBlank(coverEmoji)) {
            coverEmoji = BlogConstants.COVER_EMOJIS[ThreadLocalRandom.current().nextInt(BlogConstants.COVER_EMOJIS.length)];
        }
        if (StrUtil.isBlank(coverColor)) {
            coverColor = BlogConstants.COVER_COLORS[ThreadLocalRandom.current().nextInt(BlogConstants.COVER_COLORS.length)];
        }
        Post post = new Post();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setCategory(category);
        post.setTags(BlogUtil.joinTags(tags));
        post.setExcerpt(excerpt);
        post.setContent(content);
        post.setCoverEmoji(coverEmoji);
        post.setCoverColor(coverColor);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setIsTop(0);
        post.setStatus(1);
        postMapper.insert(post);
        statsCacheService.clearCache();
        return post;
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        boolean admin = SecurityUtil.isAdmin(user);
        if (!Objects.equals(post.getUserId(), user.getId()) && !admin) {
            throw new BusinessException("无权删除该文章");
        }
        postMapper.deleteById(postId);
        statsCacheService.clearCache();
    }

    public List<Post> listByUser(Long userId) {
        List<Post> list = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreateTime));
        fillAuthors(list);
        return list;
    }

    public List<Post> listLikedByUser(Long userId) {
        List<PostLike> likes = postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, userId)
                .orderByDesc(PostLike::getCreateTime));
        if (CollUtil.isEmpty(likes)) {
            return Collections.emptyList();
        }
        List<Long> ids = likes.stream().map(PostLike::getPostId).collect(Collectors.toList());
        List<Post> posts = postMapper.selectByIds(ids);
        Map<Long, Post> map = posts.stream().collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
        List<Post> ordered = ids.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
        fillAuthors(ordered);
        return ordered;
    }

    public List<Post> listFavoriteByUser(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));
        if (CollUtil.isEmpty(favorites)) {
            return Collections.emptyList();
        }
        List<Long> ids = favorites.stream().map(Favorite::getPostId).collect(Collectors.toList());
        List<Post> posts = postMapper.selectByIds(ids);
        Map<Long, Post> map = posts.stream().collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
        List<Post> ordered = ids.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
        fillAuthors(ordered);
        return ordered;
    }

    public List<Post> listByTag(String tag) {
        if (StrUtil.isBlank(tag)) {
            return Collections.emptyList();
        }
        String t = tag.trim();
        List<Post> list = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .and(w -> w.like(Post::getTags, t)
                        .or().like(Post::getTags, t + ",")
                        .or().like(Post::getTags, "," + t))
                .orderByDesc(Post::getCreateTime));
        // 精确匹配标签（避免子串误命中）
        list = list.stream()
                .filter(p -> BlogUtil.splitTags(p.getTags()).stream().anyMatch(x -> x.equalsIgnoreCase(t)))
                .collect(Collectors.toList());
        fillAuthors(list);
        return list;
    }

    public List<Post> listByCategory(String category) {
        if (StrUtil.isBlank(category)) {
            return Collections.emptyList();
        }
        List<Post> list = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .eq(Post::getCategory, category.trim())
                .orderByDesc(Post::getCreateTime));
        fillAuthors(list);
        return list;
    }

    /**
     * 按年-月归档，保持时间倒序。
     */
    public Map<String, List<Post>> listArchives() {
        List<Post> list = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreateTime));
        fillAuthors(list);
        Map<String, List<Post>> map = new LinkedHashMap<>();
        for (Post post : list) {
            if (post.getCreateTime() == null) {
                continue;
            }
            String key = post.getCreateTime().format(YEAR_MONTH);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(post);
        }
        return map;
    }

    public Map<String, Long> categoryCounts() {
        List<Post> list = postMapper.selectList(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 1));
        Map<String, Long> map = new LinkedHashMap<>();
        for (String cat : BlogConstants.CATEGORIES) {
            map.put(cat, 0L);
        }
        for (Post post : list) {
            if (StrUtil.isBlank(post.getCategory())) {
                continue;
            }
            map.merge(post.getCategory(), 1L, Long::sum);
        }
        return map;
    }

    public Map<String, Long> tagCounts() {
        List<Post> list = postMapper.selectList(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 1));
        Map<String, Long> map = new HashMap<>();
        for (Post post : list) {
            for (String tag : BlogUtil.splitTags(post.getTags())) {
                map.merge(tag, 1L, Long::sum);
            }
        }
        return map.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public List<Post> hotPosts(int limit) {
        List<Post> all = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getViewCount)
                .last("LIMIT 50"));
        fillAuthors(all);
        return all.stream()
                .peek(p -> {
                    int likes = p.getLikeCount() == null ? 0 : p.getLikeCount();
                    int comments = p.getCommentCount() == null ? 0 : p.getCommentCount();
                    int views = p.getViewCount() == null ? 0 : p.getViewCount();
                    p.setHeat(views + likes * 8 + comments * 12);
                })
                .sorted((a, b) -> Integer.compare(b.getHeat(), a.getHeat()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void setTop(Long id, Integer isTop) {
        Post upd = new Post();
        upd.setId(id);
        upd.setIsTop(isTop);
        postMapper.updateById(upd);
        statsCacheService.clearCache();
    }

    public void setStatus(Long id, Integer status) {
        Post upd = new Post();
        upd.setId(id);
        upd.setStatus(status);
        postMapper.updateById(upd);
        statsCacheService.clearCache();
    }

    public Page<Post> adminPage(String keyword, long pageNum, long pageSize) {
        LambdaQueryWrapper<Post> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            qw.like(Post::getTitle, keyword.trim());
        }
        qw.orderByDesc(Post::getIsTop).orderByDesc(Post::getCreateTime);
        Page<Post> page = postMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        fillAuthors(page.getRecords());
        return page;
    }

    private void fillAuthors(List<Post> posts) {
        if (CollUtil.isEmpty(posts)) {
            return;
        }
        Set<Long> ids = posts.stream().map(Post::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, User> map = new HashMap<>();
        userMapper.selectByIds(ids).forEach(u -> map.put(u.getId(), u));
        for (Post post : posts) {
            User author = map.get(post.getUserId());
            if (author == null) {
                author = new User();
                author.setUsername("已注销用户");
                author.setAvatar("👤");
                author.setColor("#94a3b8");
            }
            post.setAuthor(author);
            post.setTagList(BlogUtil.splitTags(post.getTags()));
        }
    }
}
