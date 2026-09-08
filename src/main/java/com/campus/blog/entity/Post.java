package com.campus.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("blog_post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String category;
    private String tags;
    private String excerpt;
    private String content;
    private String coverEmoji;
    private String coverColor;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer isTop;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private User author;
    @TableField(exist = false)
    private List<String> tagList;
    @TableField(exist = false)
    private Boolean liked;
    @TableField(exist = false)
    private Boolean favorited;
    @TableField(exist = false)
    private Integer heat;
}
