package com.campus.blog.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.blog.entity.Post;
import com.campus.blog.entity.User;
import com.campus.blog.mapper.CommentMapper;
import com.campus.blog.mapper.PostMapper;
import com.campus.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsCacheService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${blog.redis.stats-key:blog:stats}")
    private String statsKey;

    @Value("${blog.redis.hot-posts-key:blog:hot:posts}")
    private String hotPostsKey;

    @Value("${blog.redis.hot-posts-ttl-seconds:60}")
    private long hotTtl;

    public Map<String, Object> getStats() {
        try {
            String cached = stringRedisTemplate.opsForValue().get(statsKey);
            Map<String, Object> parsed = parseStats(cached);
            if (parsed != null) {
                return parsed;
            }
        } catch (Exception e) {
            log.warn("读取 Redis 统计缓存失败，回退数据库: {}", e.getMessage());
        }
        Map<String, Object> stats = loadStatsFromDb();
        try {
            stringRedisTemplate.opsForValue().set(statsKey, formatStats(stats), hotTtl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入 Redis 统计缓存失败: {}", e.getMessage());
        }
        return stats;
    }

    public List<Post> getHotPosts(int limit) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(hotPostsKey);
            if (StrUtil.isNotBlank(cached)) {
                List<Long> ids = Arrays.stream(cached.split(","))
                        .map(String::trim)
                        .filter(StrUtil::isNotBlank)
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                if (!ids.isEmpty()) {
                    List<Post> posts = postMapper.selectByIds(ids);
                    Map<Long, Post> map = posts.stream()
                            .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
                    List<Post> ordered = ids.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
                    fillHotMeta(ordered);
                    if (!ordered.isEmpty()) {
                        return ordered.size() > limit ? ordered.subList(0, limit) : ordered;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("读取 Redis 热文缓存失败，回退数据库: {}", e.getMessage());
        }
        List<Post> hot = loadHotFromDb(limit);
        try {
            String ids = hot.stream().map(p -> String.valueOf(p.getId())).collect(Collectors.joining(","));
            if (StrUtil.isNotBlank(ids)) {
                stringRedisTemplate.opsForValue().set(hotPostsKey, ids, hotTtl, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("写入 Redis 热文缓存失败: {}", e.getMessage());
        }
        return hot;
    }

    public void clearCache() {
        try {
            stringRedisTemplate.delete(statsKey);
            stringRedisTemplate.delete(hotPostsKey);
        } catch (Exception e) {
            log.warn("清理 Redis 缓存失败: {}", e.getMessage());
        }
    }

    private Map<String, Object> loadStatsFromDb() {
        Map<String, Object> stats = new HashMap<>();
        Long posts = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 1));
        Long comments = commentMapper.selectCount(null);
        Long users = userMapper.selectCount(null);
        Long todayPosts = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .ge(Post::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay()));
        stats.put("posts", posts == null ? 0 : posts);
        stats.put("comments", comments == null ? 0 : comments);
        stats.put("users", users == null ? 0 : users);
        stats.put("todayPosts", todayPosts == null ? 0 : todayPosts);
        return stats;
    }

    private List<Post> loadHotFromDb(int limit) {
        List<Post> all = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getViewCount)
                .last("LIMIT 50"));
        fillHotMeta(all);
        all.sort((a, b) -> Integer.compare(b.getHeat(), a.getHeat()));
        if (all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    private void fillHotMeta(List<Post> posts) {
        Map<Long, User> authors = new HashMap<>();
        for (Post p : posts) {
            authors.computeIfAbsent(p.getUserId(), id -> userMapper.selectById(id));
            p.setAuthor(authors.get(p.getUserId()));
            int likes = p.getLikeCount() == null ? 0 : p.getLikeCount();
            int comments = p.getCommentCount() == null ? 0 : p.getCommentCount();
            int views = p.getViewCount() == null ? 0 : p.getViewCount();
            p.setHeat(views + likes * 8 + comments * 12);
        }
    }

    private String formatStats(Map<String, Object> stats) {
        return "posts=" + stats.get("posts")
                + ";comments=" + stats.get("comments")
                + ";users=" + stats.get("users")
                + ";todayPosts=" + stats.get("todayPosts");
    }

    private Map<String, Object> parseStats(String cached) {
        if (StrUtil.isBlank(cached)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        for (String part : cached.split(";")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(kv[0], Long.parseLong(kv[1]));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        if (!map.containsKey("posts") || !map.containsKey("comments")
                || !map.containsKey("users") || !map.containsKey("todayPosts")) {
            return null;
        }
        return map;
    }
}
