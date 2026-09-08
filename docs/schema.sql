-- 个人博客数据库脚本
-- MySQL 8

CREATE DATABASE IF NOT EXISTS personal_blog
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE personal_blog;

-- ----------------------------
-- 清理旧表
-- ----------------------------
DROP TABLE IF EXISTS blog_favorite;
DROP TABLE IF EXISTS blog_comment_like;
DROP TABLE IF EXISTS blog_post_like;
DROP TABLE IF EXISTS blog_comment;
DROP TABLE IF EXISTS blog_post;
DROP TABLE IF EXISTS blog_user;

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE blog_user (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username      VARCHAR(32)  NOT NULL COMMENT '昵称/登录名',
  password      VARCHAR(64)  NOT NULL COMMENT '密码(MD5)',
  avatar        VARCHAR(16)  DEFAULT '😀' COMMENT '头像表情',
  color         VARCHAR(16)  DEFAULT '#4f7cff' COMMENT '头像背景色',
  bio           VARCHAR(255) DEFAULT '这个人很懒，什么都没写～' COMMENT '个人简介',
  role          TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0普通用户 1管理员',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客用户';

-- ----------------------------
-- 文章表
-- ----------------------------
CREATE TABLE blog_post (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
  user_id        BIGINT       NOT NULL COMMENT '作者ID',
  title          VARCHAR(120) NOT NULL COMMENT '标题',
  category       VARCHAR(32)  NOT NULL COMMENT '分类',
  tags           VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  excerpt        VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  content        MEDIUMTEXT   NOT NULL COMMENT '正文(支持HTML)',
  cover_emoji    VARCHAR(16)  DEFAULT '📝' COMMENT '封面表情',
  cover_color    VARCHAR(120) DEFAULT 'linear-gradient(135deg,#4f7cff,#7c5cff)' COMMENT '封面渐变',
  view_count     INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
  like_count     INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  comment_count  INT          NOT NULL DEFAULT 0 COMMENT '回复数',
  favorite_count INT          NOT NULL DEFAULT 0 COMMENT '收藏数',
  is_top         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶',
  status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0隐藏',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_user_id (user_id),
  KEY idx_category (category),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章';

-- ----------------------------
-- 回复/评论表
-- ----------------------------
CREATE TABLE blog_comment (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
  post_id       BIGINT        NOT NULL COMMENT '文章ID',
  user_id       BIGINT        NOT NULL COMMENT '作者ID',
  content       VARCHAR(1000) NOT NULL COMMENT '评论内容',
  like_count    INT           NOT NULL DEFAULT 0 COMMENT '点赞数',
  floor_no      INT           NOT NULL DEFAULT 1 COMMENT '楼层号',
  deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_post_id (post_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论';

-- ----------------------------
-- 文章点赞
-- ----------------------------
CREATE TABLE blog_post_like (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id       BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,
  create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_user (post_id, user_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞';

-- ----------------------------
-- 评论点赞
-- ----------------------------
CREATE TABLE blog_comment_like (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  comment_id    BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,
  create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_comment_user (comment_id, user_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞';

-- ----------------------------
-- 收藏
-- ----------------------------
CREATE TABLE blog_favorite (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id       BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,
  create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_user (post_id, user_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章收藏';

-- ----------------------------
-- 种子数据（密码均为 123456 的 MD5：e10adc3949ba59abbe56e057f20f883e）
-- ----------------------------
INSERT INTO blog_user (id, username, password, avatar, color, bio, role, create_time) VALUES
(1, '管理员', 'e10adc3949ba59abbe56e057f20f883e', '🛠️', '#64748b', '博客站长，负责内容维护。', 1, DATE_SUB(NOW(), INTERVAL 120 DAY)),
(2, '小白',   'e10adc3949ba59abbe56e057f20f883e', '🧑‍💻', '#4f7cff', '前端爱好者，用最朴素的技术写认真的文章。', 0, DATE_SUB(NOW(), INTERVAL 100 DAY)),
(3, '阿兰',   'e10adc3949ba59abbe56e057f20f883e', '🐱', '#ec4899', '产品经理 / 猫奴 / 咖啡爱好者。', 0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(4, '大熊',   'e10adc3949ba59abbe56e057f20f883e', '🐻', '#f59e0b', '后端开发，喜欢折腾部署。', 0, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(5, '莉莉',   'e10adc3949ba59abbe56e057f20f883e', '🌸', '#10b981', '设计师，关注体验细节。', 0, DATE_SUB(NOW(), INTERVAL 30 DAY));

INSERT INTO blog_post (id, user_id, title, category, tags, excerpt, content, cover_emoji, cover_color, view_count, like_count, comment_count, favorite_count, is_top, create_time) VALUES
(1, 2, '用 HTML/CSS/JS 从零搭建多页面博客', '前端开发', 'HTML,CSS,JavaScript',
'不依赖任何框架，只用最基础的 Web 技术，也能搭建一个功能完整、界面美观的多页面博客站点。',
'<p>很多人以为做网站必须用 React、Vue 这类框架，其实对于内容型站点，原生 HTML + CSS + 少量 JavaScript 就足够了，而且更轻量、加载更快。</p>
<h2>一、规划页面结构</h2>
<p>先把站点拆成独立页面：首页、文章列表、文章详情、分类、标签、归档、关于、联系。每个页面职责单一，导航互相链接。</p>
<h2>二、用 CSS 变量实现主题</h2>
<p>通过 CSS 自定义属性定义颜色体系，切换 <code>data-theme</code> 属性即可实现深色模式。</p>
<blockquote>简单不等于简陋。用最朴素的技术，也能做出认真的产品。</blockquote>',
'🚀', 'linear-gradient(135deg,#4f7cff,#7c5cff)', 1280, 5, 2, 2, 1, '2026-08-01 10:00:00'),

(2, 2, 'CSS Grid 与 Flexbox 该怎么选？', '前端开发', 'CSS,布局',
'一维布局用 Flexbox，二维布局用 Grid。本文通过多个真实案例帮你理清两者的适用场景。',
'<p>Flexbox 和 Grid 不是竞争关系，而是互补关系。理解它们的维度差异是用好它们的关键。</p>
<h2>Flexbox：一维排列</h2>
<p>导航栏、按钮组、标签栏这类沿一条轴排列的元素，用 Flexbox 最顺手。</p>
<h2>Grid：二维布局</h2>
<p>文章卡片墙、相册、仪表盘这类行列都要对齐的布局，Grid 一行代码搞定。</p>',
'🧩', 'linear-gradient(135deg,#ff9966,#ff5e62)', 860, 3, 1, 1, 0, '2026-07-22 14:30:00'),

(3, 2, 'JavaScript 异步编程完全指南', 'JavaScript', 'JavaScript,异步',
'从回调到 Promise 再到 async/await，理解 JS 异步演进背后的动机，才能真正写出优雅的异步代码。',
'<p>JavaScript 是单线程语言，异步是它的宿命。好在语法一路在进化，让异步代码越来越像同步代码。</p>
<h2>回调地狱</h2>
<p>最早的异步靠回调函数层层嵌套，可读性极差。</p>
<h2>Promise 与 async/await</h2>
<p>记住：await 只能在 async 函数里用；并行请求用 Promise.all，别串行等待。</p>',
'⚡', 'linear-gradient(135deg,#36d1dc,#5b86e5)', 1024, 4, 2, 2, 0, '2026-07-10 09:20:00'),

(4, 2, '程序员的写作课：技术博客怎么写', '随笔', '写作,成长',
'写博客不只是分享知识，更是最好的学习方式。本文聊聊选题、结构与坚持写作的方法。',
'<p>费曼说过，讲给别人听是最好的学习方式。写技术博客正是如此。</p>
<h2>选题从问题出发</h2>
<p>最好的选题来自你刚解决的难题：踩过的坑、对比过的方案、总结过的经验。</p>
<blockquote>完成比完美更重要。先写出来，再慢慢改好。</blockquote>',
'✍️', 'linear-gradient(135deg,#a8e063,#56ab2f)', 654, 2, 1, 1, 0, '2026-06-28 16:00:00'),

(5, 2, 'Git 工作流实践：让协作不再混乱', '工程实践', 'Git,团队协作',
'分支策略、提交规范、Code Review，一套适合中小团队的 Git 工作流实践总结。',
'<p>团队协作中最容易出乱子的就是代码合并。一个好的 Git 工作流能让协作效率倍增。</p>
<h2>分支策略</h2>
<p>主干开发、分支发布：main 永远可发布，feature 分支开发，hotfix 分支紧急修复。</p>
<h2>提交信息规范</h2>
<pre>feat: 新增文章搜索功能
fix: 修复深色模式闪烁
docs: 更新部署文档</pre>',
'🌿', 'linear-gradient(135deg,#f7971e,#ffd200)', 732, 3, 1, 1, 0, '2026-06-15 11:10:00'),

(6, 5, '深色模式设计的 8 个细节', '设计', 'UI设计,CSS',
'深色模式不是简单地把白底换成黑底。阴影、对比度、色彩饱和度都有讲究。',
'<p>直接把亮色主题反色得到的深色模式往往很难看。好的深色模式需要重新设计。</p>
<h2>不要用纯黑</h2>
<p>纯黑背景配白字对比过强，容易疲劳。用深灰（如 #14181f）更柔和。</p>
<h2>降低色彩饱和度</h2>
<p>高饱和色在深色背景上会刺眼，主色可以适当提亮降饱和。</p>',
'🌙', 'linear-gradient(135deg,#232526,#414345)', 510, 2, 1, 0, 0, '2026-05-30 20:00:00'),

(7, 2, '我的 2026 上半年技术总结', '随笔', '总结,成长',
'读过的书、做过的项目、踩过的坑——半年一度的复盘，回顾与展望。',
'<p>时间过得飞快，转眼 2026 已过半。按惯例做一次复盘。</p>
<h2>做过的事</h2>
<p>上线了两个小工具站点，重写了个人博客，读完 6 本技术书，写了 20 篇笔记。</p>
<h2>下半年计划</h2>
<p>深入学习一门后端语言，把博客加上评论系统和搜索功能，继续保持每周更新。</p>',
'📊', 'linear-gradient(135deg,#667eea,#764ba2)', 445, 1, 0, 1, 0, '2026-07-01 08:00:00'),

(8, 4, 'Node.js 静态服务器十分钟上手', '后端', 'Node.js,后端',
'不装任何框架，用 Node.js 内置模块写一个能托管本博客的静态文件服务器。',
'<p>本博客早期是纯静态站点，部署非常简单。这里教你用 Node.js 原生模块写一个迷你静态服务器。</p>
<h2>核心思路</h2>
<p>用 http 模块创建服务，根据 URL 读取本地文件并返回。生产环境建议加上 MIME 类型与缓存处理。</p>',
'🖥️', 'linear-gradient(135deg,#11998e,#38ef7d)', 388, 2, 1, 0, 0, '2026-05-12 15:40:00');

INSERT INTO blog_comment (id, post_id, user_id, content, like_count, floor_no, create_time) VALUES
(1, 1, 3, '写得很清楚，学到了很多！', 1, 1, '2026-08-02 10:24:00'),
(2, 1, 4, '期待后续更新 👍', 0, 2, '2026-08-03 21:05:00'),
(3, 2, 5, 'Grid 那段例子很实用，收藏了。', 1, 1, '2026-07-23 09:12:00'),
(4, 3, 3, 'async/await 终于理顺了，感谢分享。', 1, 1, '2026-07-11 18:30:00'),
(5, 3, 4, '补充一句：记得处理 Promise.allSettled 的失败场景。', 0, 2, '2026-07-12 10:00:00'),
(6, 4, 5, '坚持写作真的能倒逼输入，同感！', 0, 1, '2026-06-29 12:00:00'),
(7, 5, 3, '提交规范我们团队也在用，强烈推荐。', 1, 1, '2026-06-16 14:20:00'),
(8, 6, 2, '深色模式细节讲得很到位。', 0, 1, '2026-05-31 08:40:00'),
(9, 8, 2, '十分钟真能跑起来，适合练手。', 1, 1, '2026-05-13 11:00:00');

INSERT INTO blog_post_like (post_id, user_id) VALUES
(1,3),(1,4),(1,5),(1,1),(1,2),
(2,3),(2,5),(2,4),
(3,2),(3,3),(3,4),(3,5),
(4,3),(4,5),
(5,2),(5,3),(5,4),
(6,2),(6,3),
(7,3),
(8,2),(8,5);

INSERT INTO blog_comment_like (comment_id, user_id) VALUES
(1,2),(3,2),(4,5),(7,2),(9,3);

INSERT INTO blog_favorite (post_id, user_id) VALUES
(1,3),(1,4),(2,5),(3,3),(3,4),(4,2),(5,3),(7,5);
