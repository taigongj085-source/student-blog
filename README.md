# 个人博客（小白的博客）

基于 **Spring Boot 4** 的单体个人博客，参考 `my-blog/` 原型页面实现。

## 技术栈

- Spring Boot 4.0.8
- Spring Security（表单登录 + 角色授权）
- MySQL 8
- MyBatis-Plus 3.5.17（`mybatis-plus-spring-boot4-starter`）
- Redis（站点统计 / 热门文章缓存，不可用时自动回退数据库）
- Thymeleaf
- Hutool（密码 MD5、字符串等工具）

## 功能

- 用户注册、登录、退出（Spring Security）
- 文章发布、列表（分类 / 排序 / 搜索）、详情
- 分类、标签、归档浏览
- 评论盖楼
- 文章点赞、评论点赞、收藏
- 个人中心（我的文章 / 评论 / 点赞 / 收藏）
- 管理后台（文章置顶/隐藏/删除、评论删除、用户启停）

## 目录说明

```
blog-back/
├── docs/schema.sql              # 建库建表及种子数据
├── my-blog/                     # 纯前端原型（参考）
└── src/main/resources/
    ├── static/                  # 静态资源
    └── templates/
        ├── front/               # 前台页面
        └── back/                # 管理后台
```

## 快速启动

1. 准备 MySQL 8，执行：

```bash
mysql -uroot -p < docs/schema.sql
```

2. 修改 `src/main/resources/application.yml` 中的数据库账号密码、端口，以及 Redis 地址。

3. 启动：

```bash
mvn spring-boot:run
```

4. 访问：

- 前台：http://localhost:8086/
- 后台：http://localhost:8086/admin/login

## 演示账号

| 昵称 | 密码 | 说明 |
|------|------|------|
| 管理员 | 123456 | 后台管理员 |
| 小白 / 阿兰 / 大熊 / 莉莉 | 123456 | 普通用户 |
