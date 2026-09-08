package com.campus.blog.util;

import com.campus.blog.common.BlogConstants;
import com.campus.blog.entity.User;
import com.campus.blog.security.BlogUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof BlogUserDetails details) {
            return details.getUser();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user == null ? null : user.getId();
    }

    public static boolean isAdmin(User user) {
        return user != null && Integer.valueOf(BlogConstants.ROLE_ADMIN).equals(user.getRole());
    }

    public static User toSafeUser(User user) {
        if (user == null) {
            return null;
        }
        User safe = new User();
        safe.setId(user.getId());
        safe.setUsername(user.getUsername());
        safe.setAvatar(user.getAvatar());
        safe.setColor(user.getColor());
        safe.setBio(user.getBio());
        safe.setRole(user.getRole());
        safe.setStatus(user.getStatus());
        safe.setCreateTime(user.getCreateTime());
        return safe;
    }

    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}
