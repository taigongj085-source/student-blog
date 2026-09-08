package com.campus.blog.security;

import com.campus.blog.entity.User;
import com.campus.blog.util.SecurityUtil;

/**
 * 登录用户同步访问入口：认证身份来自 SecurityContext 中的 BlogUserDetails。
 */
public final class LoginUserHolder {

    private LoginUserHolder() {
    }

    public static User get() {
        return SecurityUtil.getCurrentUser();
    }

    public static Long getUserId() {
        return SecurityUtil.getCurrentUserId();
    }

    public static boolean isAdmin() {
        return SecurityUtil.isAdmin(get());
    }
}
