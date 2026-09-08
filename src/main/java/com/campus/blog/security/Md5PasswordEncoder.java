package com.campus.blog.security;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 演示用 MD5 编码器，与种子数据密码兼容（32 位小写 hex）。
 */
public class Md5PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return DigestUtil.md5Hex(rawPassword == null ? "" : rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        String hashed = encode(rawPassword);
        return hashed.equalsIgnoreCase(encodedPassword);
    }
}
