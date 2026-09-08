package com.campus.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.blog.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
