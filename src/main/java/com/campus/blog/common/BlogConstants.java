package com.campus.blog.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BlogConstants {

    public static final int ROLE_USER = 0;
    public static final int ROLE_ADMIN = 1;

    public static final List<String> CATEGORIES = Arrays.asList(
            "前端开发", "JavaScript", "后端", "工程实践", "设计", "随笔"
    );

    public record CategoryMeta(String name, String icon, String desc) {
    }

    public static final List<CategoryMeta> CATEGORY_META_LIST = List.of(
            new CategoryMeta("前端开发", "💻", "HTML / CSS / 浏览器相关"),
            new CategoryMeta("JavaScript", "⚡", "语言特性与编程技巧"),
            new CategoryMeta("后端", "🖥️", "服务端与部署"),
            new CategoryMeta("工程实践", "🛠️", "工具链与团队协作"),
            new CategoryMeta("设计", "🎨", "UI 与视觉设计"),
            new CategoryMeta("随笔", "📝", "思考、总结与生活")
    );

    public static final Map<String, CategoryMeta> CATEGORY_META = CATEGORY_META_LIST.stream()
            .collect(Collectors.toMap(CategoryMeta::name, m -> m, (a, b) -> a));

    public static final String[] AVATARS = {
            "😀", "😎", "🦊", "🐼", "🐸", "🦄", "🐯", "🐰", "🐨", "🌟", "🍀", "🎈", "🧑‍💻", "🌸"
    };

    public static final String[] COLORS = {
            "#4f7cff", "#ec4899", "#f59e0b", "#10b981", "#06b6d4", "#8b5cf6", "#ef4444", "#64748b"
    };

    public static final String[] COVER_EMOJIS = {
            "📝", "🚀", "⚡", "🧩", "🌿", "🌙", "📊", "🖥️", "✍️", "💡"
    };

    public static final String[] COVER_COLORS = {
            "linear-gradient(135deg,#4f7cff,#7c5cff)",
            "linear-gradient(135deg,#ff9966,#ff5e62)",
            "linear-gradient(135deg,#36d1dc,#5b86e5)",
            "linear-gradient(135deg,#a8e063,#56ab2f)",
            "linear-gradient(135deg,#f7971e,#ffd200)",
            "linear-gradient(135deg,#232526,#414345)",
            "linear-gradient(135deg,#667eea,#764ba2)",
            "linear-gradient(135deg,#11998e,#38ef7d)"
    };

    private BlogConstants() {
    }
}
