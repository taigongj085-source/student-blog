package com.campus.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.blog.common.BusinessException;
import com.campus.blog.entity.Comment;
import com.campus.blog.entity.CommentLike;
import com.campus.blog.entity.Favorite;
import com.campus.blog.entity.Post;
import com.campus.blog.entity.PostLike;
import com.campus.blog.mapper.CommentLikeMapper;
import com.campus.blog.mapper.CommentMapper;
import com.campus.blog.mapper.FavoriteMapper;
import com.campus.blog.mapper.PostLikeMapper;
import com.campus.blog.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteractService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final StatsCacheService statsCacheService;

    @Transactional
    public Map<String, Object> togglePostLike(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        PostLike exist = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId));
        boolean liked;
        int count = post.getLikeCount() == null ? 0 : post.getLikeCount();
        if (exist != null) {
            postLikeMapper.deleteById(exist.getId());
            count = Math.max(0, count - 1);
            liked = false;
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            count = count + 1;
            liked = true;
        }
        Post upd = new Post();
        upd.setId(postId);
        upd.setLikeCount(count);
        postMapper.updateById(upd);
        statsCacheService.clearCache();
        Map<String, Object> map = new HashMap<>();
        map.put("liked", liked);
        map.put("count", count);
        return map;
    }

    @Transactional
    public Map<String, Object> toggleCommentLike(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        CommentLike exist = commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId).eq(CommentLike::getUserId, userId));
        boolean liked;
        int count = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
        if (exist != null) {
            commentLikeMapper.deleteById(exist.getId());
            count = Math.max(0, count - 1);
            liked = false;
        } else {
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            count = count + 1;
            liked = true;
        }
        Comment upd = new Comment();
        upd.setId(commentId);
        upd.setLikeCount(count);
        commentMapper.updateById(upd);
        Map<String, Object> map = new HashMap<>();
        map.put("liked", liked);
        map.put("count", count);
        return map;
    }

    @Transactional
    public Map<String, Object> toggleFavorite(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getPostId, postId).eq(Favorite::getUserId, userId));
        boolean favorited;
        int count = post.getFavoriteCount() == null ? 0 : post.getFavoriteCount();
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            count = Math.max(0, count - 1);
            favorited = false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setPostId(postId);
            favorite.setUserId(userId);
            favoriteMapper.insert(favorite);
            count = count + 1;
            favorited = true;
        }
        Post upd = new Post();
        upd.setId(postId);
        upd.setFavoriteCount(count);
        postMapper.updateById(upd);
        Map<String, Object> map = new HashMap<>();
        map.put("favorited", favorited);
        map.put("count", count);
        return map;
    }
}
