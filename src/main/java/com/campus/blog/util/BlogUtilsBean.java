package com.campus.blog.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供 Thymeleaf 调用的工具 Bean：${#blogUtils.timeAgo(...)}
 */
@Component("blogUtils")
public class BlogUtilsBean {

    public String timeAgo(LocalDateTime time) {
        return BlogUtil.timeAgo(time);
    }

    public String formatDate(LocalDateTime time) {
        return BlogUtil.formatDate(time);
    }

    public String formatDateCn(LocalDateTime time) {
        return BlogUtil.formatDateCn(time);
    }

    public List<String> splitTags(String tags) {
        return BlogUtil.splitTags(tags);
    }

    public String plainExcerpt(String html) {
        return BlogUtil.plainExcerpt(html);
    }

    public String plainExcerpt(String html, int maxLen) {
        return BlogUtil.plainExcerpt(html, maxLen);
    }
}
