package com.campus.blog.config;

import com.campus.blog.common.BlogConstants;
import com.campus.blog.entity.User;
import com.campus.blog.util.SecurityUtil;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("loginUser")
    public User loginUser() {
        return SecurityUtil.toSafeUser(SecurityUtil.getCurrentUser());
    }

    @ModelAttribute("categories")
    public Object categories() {
        return BlogConstants.CATEGORIES;
    }

    @ModelAttribute("categoryMeta")
    public Object categoryMeta() {
        return BlogConstants.CATEGORY_META_LIST;
    }
}
