package com.campus.blog.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.blog.common.BusinessException;
import com.campus.blog.entity.Comment;
import com.campus.blog.entity.Post;
import com.campus.blog.entity.User;
import com.campus.blog.mapper.CommentMapper;
import com.campus.blog.mapper.PostMapper;
import com.campus.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final StatsCacheService statsCacheService;

    public List<Comment> listByPost(Long postId) {
        List<Comment> list = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .orderByAsc(Comment::getFloorNo)
                .orderByAsc(Comment::getCreateTime));
        fillAuthors(list);
        return list;
    }

    public List<Comment> listByUser(Long userId) {
        List<Comment> list = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreateTime));
        fillAuthors(list);
        if (CollUtil.isNotEmpty(list)) {
            Set<Long> postIds = list.stream().map(Comment::getPostId).collect(Collectors.toSet());
            Map<Long, Post> postMap = postMapper.selectByIds(postIds).stream()
                    .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
            for (Comment c : list) {
                Post p = postMap.get(c.getPostId());
                c.setPostTitle(p == null ? "文章已删除" : p.getTitle());
            }
        }
        return list;
    }

    @Transactional
    public Comment add(Long postId, User user, String content) {
        content = StrUtil.trim(content);
        if (StrUtil.isBlank(content)) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException("评论不能超过 1000 字");
        }
        Post post = postMapper.selectById(postId);
        if (post == null || Objects.equals(post.getStatus(), 0)) {
            throw new BusinessException("文章不存在");
        }
        Long count = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
        int floor = count == null ? 1 : count.intValue() + 1;
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(user.getId());
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setFloorNo(floor);
        commentMapper.insert(comment);

        Post upd = new Post();
        upd.setId(postId);
        upd.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
        postMapper.updateById(upd);
        statsCacheService.clearCache();
        return comment;
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return;
        }
        commentMapper.deleteById(id);
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            Post upd = new Post();
            upd.setId(post.getId());
            int c = post.getCommentCount() == null ? 0 : post.getCommentCount();
            upd.setCommentCount(Math.max(0, c - 1));
            postMapper.updateById(upd);
        }
        statsCacheService.clearCache();
    }

    public Page<Comment> adminPage(long pageNum, long pageSize) {
        Page<Comment> page = commentMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreateTime));
        fillAuthors(page.getRecords());
        if (CollUtil.isNotEmpty(page.getRecords())) {
            Set<Long> postIds = page.getRecords().stream().map(Comment::getPostId).collect(Collectors.toSet());
            Map<Long, Post> postMap = postMapper.selectByIds(postIds).stream()
                    .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
            for (Comment c : page.getRecords()) {
                Post p = postMap.get(c.getPostId());
                c.setPostTitle(p == null ? "文章已删除" : p.getTitle());
            }
        }
        return page;
    }

    private void fillAuthors(List<Comment> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Long> ids = list.stream().map(Comment::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, User> map = new HashMap<>();
        userMapper.selectByIds(ids).forEach(u -> map.put(u.getId(), u));
        for (Comment c : list) {
            User author = map.get(c.getUserId());
            if (author == null) {
                author = new User();
                author.setUsername("已注销用户");
                author.setAvatar("👤");
                author.setColor("#94a3b8");
            }
            c.setAuthor(author);
        }
    }
}
