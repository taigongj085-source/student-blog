/* ============ 博客共享数据与逻辑 ============ */

/* ---- 文章数据（模拟数据库） ---- */
const POSTS = [
  {
    id: 1,
    title: "用 HTML/CSS/JS 从零搭建多页面博客",
    date: "2026-08-01",
    category: "前端开发",
    tags: ["HTML", "CSS", "JavaScript"],
    emoji: "🚀",
    color: "linear-gradient(135deg,#4f7cff,#7c5cff)",
    excerpt: "不依赖任何框架，只用最基础的 Web 技术，也能搭建一个功能完整、界面美观的多页面博客站点。",
    content: `
      <p>很多人以为做网站必须用 React、Vue 这类框架，其实对于内容型站点，原生 HTML + CSS + 少量 JavaScript 就足够了，而且更轻量、加载更快。</p>
      <h2>一、规划页面结构</h2>
      <p>先把站点拆成独立页面：首页、文章列表、文章详情、分类、标签、归档、关于、联系。每个页面职责单一，导航互相链接。</p>
      <h2>二、用 CSS 变量实现主题</h2>
      <p>通过 CSS 自定义属性定义颜色体系，切换 <code>data-theme</code> 属性即可实现深色模式：</p>
      <pre>:root { --bg:#f5f7fa; --text:#2c3e50; }
[data-theme="dark"] { --bg:#14181f; --text:#e8ecf2; }</pre>
      <h2>三、数据驱动渲染</h2>
      <p>把文章信息放在一个 JS 数组里，列表页、详情页、分类页都从这份数据渲染，避免重复维护内容。</p>
      <blockquote>简单不等于简陋。用最朴素的技术，也能做出认真的产品。</blockquote>
      <p>动手试试吧，完整代码就在本站点的源码里。</p>`
  },
  {
    id: 2,
    title: "CSS Grid 与 Flexbox 该怎么选？",
    date: "2026-07-22",
    category: "前端开发",
    tags: ["CSS", "布局"],
    emoji: "🧩",
    color: "linear-gradient(135deg,#ff9966,#ff5e62)",
    excerpt: "一维布局用 Flexbox，二维布局用 Grid。本文通过多个真实案例帮你理清两者的适用场景。",
    content: `
      <p>Flexbox 和 Grid 不是竞争关系，而是互补关系。理解它们的维度差异是用好它们的关键。</p>
      <h2>Flexbox：一维排列</h2>
      <p>导航栏、按钮组、标签栏这类沿一条轴排列的元素，用 Flexbox 最顺手。</p>
      <pre>.nav { display:flex; justify-content:space-between; }</pre>
      <h2>Grid：二维布局</h2>
      <p>文章卡片墙、相册、仪表盘这类行列都要对齐的布局，Grid 一行代码搞定：</p>
      <pre>.grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:20px; }</pre>
      <p>实际项目里两者经常混用：页面整体用 Grid 划分区域，区域内部用 Flexbox 排列内容。</p>`
  },
  {
    id: 3,
    title: "JavaScript 异步编程完全指南",
    date: "2026-07-10",
    category: "JavaScript",
    tags: ["JavaScript", "异步"],
    emoji: "⚡",
    color: "linear-gradient(135deg,#36d1dc,#5b86e5)",
    excerpt: "从回调到 Promise 再到 async/await，理解 JS 异步演进背后的动机，才能真正写出优雅的异步代码。",
    content: `
      <p>JavaScript 是单线程语言，异步是它的宿命。好在语法一路在进化，让异步代码越来越像同步代码。</p>
      <h2>回调地狱</h2>
      <p>最早的异步靠回调函数层层嵌套，可读性极差。</p>
      <h2>Promise 链式调用</h2>
      <pre>fetch(url)
  .then(res => res.json())
  .then(data => console.log(data))
  .catch(err => console.error(err));</pre>
      <h2>async/await</h2>
      <pre>async function load() {
  try {
    const res = await fetch(url);
    const data = await res.json();
    return data;
  } catch (err) {
    console.error(err);
  }
}</pre>
      <p>记住：await 只能在 async 函数里用；并行请求用 Promise.all，别串行等待。</p>`
  },
  {
    id: 4,
    title: "程序员的写作课：技术博客怎么写",
    date: "2026-06-28",
    category: "随笔",
    tags: ["写作", "成长"],
    emoji: "✍️",
    color: "linear-gradient(135deg,#a8e063,#56ab2f)",
    excerpt: "写博客不只是分享知识，更是最好的学习方式。本文聊聊选题、结构与坚持写作的方法。",
    content: `
      <p>费曼说过，讲给别人听是最好的学习方式。写技术博客正是如此——当你试图把一个概念写清楚时，你会发现自己哪里还没真正理解。</p>
      <h2>选题从问题出发</h2>
      <p>最好的选题来自你刚解决的难题：踩过的坑、对比过的方案、总结过的经验。读者需要的正是这些。</p>
      <h2>结构先于文字</h2>
      <p>动笔前先列大纲：问题背景 → 解决思路 → 具体步骤 → 总结。有骨架的文章不会跑偏。</p>
      <h2>坚持比完美重要</h2>
      <blockquote>完成比完美更重要。先写出来，再慢慢改好。</blockquote>
      <p>每周一篇，坚持三个月，你会惊讶于自己的进步。</p>`
  },
  {
    id: 5,
    title: "Git 工作流实践：让协作不再混乱",
    date: "2026-06-15",
    category: "工程实践",
    tags: ["Git", "团队协作"],
    emoji: "🌿",
    color: "linear-gradient(135deg,#f7971e,#ffd200)",
    excerpt: "分支策略、提交规范、Code Review，一套适合中小团队的 Git 工作流实践总结。",
    content: `
      <p>团队协作中最容易出乱子的就是代码合并。一个好的 Git 工作流能让协作效率倍增。</p>
      <h2>分支策略</h2>
      <p>主干开发、分支发布：main 永远可发布，feature 分支开发，hotfix 分支紧急修复。</p>
      <h2>提交信息规范</h2>
      <pre>feat: 新增文章搜索功能
fix: 修复深色模式闪烁
docs: 更新部署文档</pre>
      <h2>Code Review 不可少</h2>
      <p>所有改动通过 Pull Request 合入，至少一人 Review。这不仅是找 bug，更是知识共享的过程。</p>`
  },
  {
    id: 6,
    title: "深色模式设计的 8 个细节",
    date: "2026-05-30",
    category: "设计",
    tags: ["UI设计", "CSS"],
    emoji: "🌙",
    color: "linear-gradient(135deg,#232526,#414345)",
    excerpt: "深色模式不是简单地把白底换成黑底。阴影、对比度、色彩饱和度都有讲究。",
    content: `
      <p>直接把亮色主题反色得到的深色模式往往很难看。好的深色模式需要重新设计。</p>
      <h2>不要用纯黑</h2>
      <p>纯黑背景配白字对比过强，容易疲劳。用深灰（如 #14181f）更柔和。</p>
      <h2>用层级表达深度</h2>
      <p>亮色模式靠阴影区分层级，深色模式则靠背景色明度：越靠上的卡片越亮。</p>
      <h2>降低色彩饱和度</h2>
      <p>高饱和色在深色背景上会刺眼，主色可以适当提亮降饱和。</p>
      <blockquote>本站点右上角的按钮就可以切换深浅主题，源码里正是按这些原则实现的。</blockquote>`
  },
  {
    id: 7,
    title: "我的 2026 上半年技术总结",
    date: "2026-07-01",
    category: "随笔",
    tags: ["总结", "成长"],
    emoji: "📊",
    color: "linear-gradient(135deg,#667eea,#764ba2)",
    excerpt: "读过的书、做过的项目、踩过的坑——半年一度的复盘，回顾与展望。",
    content: `
      <p>时间过得飞快，转眼 2026 已过半。按惯例做一次复盘。</p>
      <h2>做过的事</h2>
      <p>上线了两个小工具站点，重写了个人博客（就是你现在看到的这个），读完 6 本技术书，写了 20 篇笔记。</p>
      <h2>学到的道理</h2>
      <p>最大的体会是：输出倒逼输入。坚持写博客让我读书更认真、思考更深入。</p>
      <h2>下半年计划</h2>
      <p>深入学习一门后端语言，把博客加上评论系统和搜索功能，继续保持每周更新。</p>`
  },
  {
    id: 8,
    title: "Node.js 静态服务器十分钟上手",
    date: "2026-05-12",
    category: "后端",
    tags: ["Node.js", "后端"],
    emoji: "🖥️",
    color: "linear-gradient(135deg,#11998e,#38ef7d)",
    excerpt: "不装任何框架，用 Node.js 内置模块写一个能托管本博客的静态文件服务器。",
    content: `
      <p>本博客是纯静态站点，部署非常简单。这里教你用 Node.js 原生模块写一个迷你静态服务器。</p>
      <h2>核心代码</h2>
      <pre>const http = require('http');
const fs = require('fs');
const path = require('path');

http.createServer((req, res) => {
  const file = path.join(__dirname, req.url === '/' ? 'index.html' : req.url);
  fs.readFile(file, (err, data) => {
    if (err) { res.statusCode = 404; res.end('Not Found'); return; }
    res.end(data);
  });
}).listen(3000);</pre>
      <p>运行后访问 localhost:3000 即可看到站点。生产环境建议加上 MIME 类型与缓存处理。</p>`
  }
];

/* ---- 分类信息 ---- */
const CATEGORIES = [
  { name: "前端开发", icon: "💻", desc: "HTML / CSS / 浏览器相关" },
  { name: "JavaScript", icon: "⚡", desc: "语言特性与编程技巧" },
  { name: "后端", icon: "🖥️", desc: "服务端与部署" },
  { name: "工程实践", icon: "🛠️", desc: "工具链与团队协作" },
  { name: "设计", icon: "🎨", desc: "UI 与视觉设计" },
  { name: "随笔", icon: "📝", desc: "思考、总结与生活" }
];

/* ---- 工具函数 ---- */
function getAllTags() {
  const map = {};
  POSTS.forEach(p => p.tags.forEach(t => { map[t] = (map[t] || 0) + 1; }));
  return map;
}

function getPostsByCategory(cat) {
  return POSTS.filter(p => p.category === cat);
}

function getPostsByTag(tag) {
  return POSTS.filter(p => p.tags.includes(tag));
}

function getPostById(id) {
  return POSTS.find(p => p.id === Number(id));
}

function formatDate(d) {
  const [y, m, day] = d.split("-");
  return `${y} 年 ${Number(m)} 月 ${Number(day)} 日`;
}

/* ---- 渲染文章卡片 ---- */
function postCardHTML(p) {
  return `
  <article class="post-card">
    <a href="post.html?id=${p.id}"><div class="post-cover" style="background:${p.color}">${p.emoji}</div></a>
    <div class="post-body">
      <div class="post-meta">
        <span>📅 ${p.date}</span>
        <span>📁 <a href="categories.html#${encodeURIComponent(p.category)}">${p.category}</a></span>
      </div>
      <h3><a href="post.html?id=${p.id}">${p.title}</a></h3>
      <p class="post-excerpt">${p.excerpt}</p>
      <div class="post-tags">
        ${p.tags.map(t => `<a class="tag" href="tags.html#${encodeURIComponent(t)}">#${t}</a>`).join("")}
      </div>
    </div>
  </article>`;
}

/* ---- 渲染顶部导航 ---- */
function renderNav(active) {
  const links = [
    { href: "index.html", label: "首页", key: "index" },
    { href: "posts.html", label: "文章", key: "posts" },
    { href: "categories.html", label: "分类", key: "categories" },
    { href: "tags.html", label: "标签", key: "tags" },
    { href: "archives.html", label: "归档", key: "archives" },
    { href: "about.html", label: "关于", key: "about" },
    { href: "contact.html", label: "联系", key: "contact" }
  ];
  document.getElementById("navbar").innerHTML = `
  <div class="container nav-inner">
    <a class="logo" href="index.html">小白的<span>博客</span></a>
    <button class="menu-btn" id="menuBtn" aria-label="菜单">☰</button>
    <ul class="nav-links" id="navLinks">
      ${links.map(l => `<li><a href="${l.href}" class="${l.key === active ? "active" : ""}">${l.label}</a></li>`).join("")}
      <li><button class="theme-toggle" id="themeToggle">🌙 深色</button></li>
    </ul>
  </div>`;

  // 移动端菜单
  document.getElementById("menuBtn").onclick = () =>
    document.getElementById("navLinks").classList.toggle("open");

  // 主题切换
  const btn = document.getElementById("themeToggle");
  const applyTheme = t => {
    document.documentElement.setAttribute("data-theme", t);
    localStorage.setItem("theme", t);
    btn.textContent = t === "dark" ? "☀️ 浅色" : "🌙 深色";
  };
  applyTheme(localStorage.getItem("theme") || "light");
  btn.onclick = () =>
    applyTheme(document.documentElement.getAttribute("data-theme") === "dark" ? "light" : "dark");
}

/* ---- 渲染页脚 ---- */
function renderFooter() {
  document.getElementById("footer").innerHTML = `
  <div class="container">
    <div class="links">
      <a href="index.html">首页</a><a href="posts.html">文章</a>
      <a href="categories.html">分类</a><a href="tags.html">标签</a>
      <a href="about.html">关于</a><a href="contact.html">联系</a>
    </div>
    <p>© 2026 小白的博客 · 用原生 HTML/CSS/JS 构建 · 仅供学习交流</p>
  </div>`;
}

/* ---- 返回顶部 ---- */
function initBackTop() {
  const btn = document.getElementById("backTop");
  window.addEventListener("scroll", () => {
    btn.style.display = window.scrollY > 400 ? "block" : "none";
  });
  btn.onclick = () => window.scrollTo({ top: 0, behavior: "smooth" });
}

/* ---- 页面初始化通用入口 ---- */
function initPage(activeNav) {
  renderNav(activeNav);
  renderFooter();
  initBackTop();
}
