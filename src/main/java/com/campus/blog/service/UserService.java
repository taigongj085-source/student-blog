package com.campus.blog.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.blog.common.BlogConstants;
import com.campus.blog.common.BusinessException;
import com.campus.blog.entity.User;
import com.campus.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    public User findByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username.trim()));
    }

    public User register(String username, String password, String password2) {
        username = StrUtil.trim(username);
        if (StrUtil.isBlank(username)) {
            throw new BusinessException("请输入昵称");
        }
        if (username.length() < 2 || username.length() > 12) {
            throw new BusinessException("昵称长度需为 2-12 个字符");
        }
        if (StrUtil.isBlank(password) || password.length() < 6) {
            throw new BusinessException("密码至少 6 位");
        }
        if (!password.equals(password2)) {
            throw new BusinessException("两次输入的密码不一致");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (count != null && count > 0) {
            throw new BusinessException("该昵称已被占用，换一个吧");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(DigestUtil.md5Hex(password));
        user.setAvatar(BlogConstants.AVATARS[ThreadLocalRandom.current().nextInt(BlogConstants.AVATARS.length)]);
        user.setColor(BlogConstants.COLORS[ThreadLocalRandom.current().nextInt(BlogConstants.COLORS.length)]);
        user.setBio("这个人很懒，什么都没写～");
        user.setRole(BlogConstants.ROLE_USER);
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }

    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (Integer.valueOf(BlogConstants.ROLE_ADMIN).equals(user.getRole())) {
            throw new BusinessException("不能禁用管理员账号");
        }
        User upd = new User();
        upd.setId(id);
        upd.setStatus(status);
        userMapper.updateById(upd);
    }
}
