package com.campus.blog.util;

import cn.hutool.core.util.StrUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class BlogUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CN_DAY = DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日");

    private BlogUtil() {
    }

    public static String timeAgo(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        Duration d = Duration.between(time, LocalDateTime.now());
        long minutes = d.toMinutes();
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + " 分钟前";
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours + " 小时前";
        }
        long days = d.toDays();
        if (days < 30) {
            return days + " 天前";
        }
        return time.format(DAY);
    }

    public static String formatDate(LocalDateTime time) {
        return time == null ? "" : time.format(FMT);
    }

    public static String formatDateCn(LocalDateTime time) {
        return time == null ? "" : time.format(CN_DAY);
    }

    public static List<String> splitTags(String tags) {
        if (StrUtil.isBlank(tags)) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split("[,，\\s]+"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .limit(8)
                .collect(Collectors.toList());
    }

    public static String joinTags(String tags) {
        return String.join(",", splitTags(tags));
    }

    /**
     * 从 HTML 正文生成纯文本摘要。
     */
    public static String plainExcerpt(String html, int maxLen) {
        if (StrUtil.isBlank(html)) {
            return "";
        }
        String plain = html
                .replaceAll("(?is)<script[^>]*>.*?</script>", " ")
                .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
        if (maxLen <= 0 || plain.length() <= maxLen) {
            return plain;
        }
        return plain.substring(0, maxLen) + "…";
    }

    public static String plainExcerpt(String html) {
        return plainExcerpt(html, 120);
    }
}
