/* 博客公共脚本：主题、移动菜单、返回顶部、CSRF 请求 */

(function () {
  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
    var btn = document.getElementById('themeToggle');
    if (btn) {
      btn.textContent = theme === 'dark' ? '☀️ 浅色' : '🌙 深色';
    }
  }

  function initTheme() {
    var saved = localStorage.getItem('theme') || 'light';
    applyTheme(saved);
    var btn = document.getElementById('themeToggle');
    if (btn) {
      btn.addEventListener('click', function () {
        var cur = document.documentElement.getAttribute('data-theme');
        applyTheme(cur === 'dark' ? 'light' : 'dark');
      });
    }
  }

  function initMobileMenu() {
    var menuBtn = document.getElementById('menuBtn');
    var navLinks = document.getElementById('navLinks');
    if (menuBtn && navLinks) {
      menuBtn.addEventListener('click', function () {
        navLinks.classList.toggle('open');
      });
    }
  }

  function initUserChip() {
    var btn = document.getElementById('userChipBtn');
    var dropdown = document.getElementById('userDropdown');
    if (!btn || !dropdown) return;
    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      dropdown.classList.toggle('open');
    });
    document.addEventListener('click', function () {
      dropdown.classList.remove('open');
    });
  }

  function initBackTop() {
    var btn = document.getElementById('backTop');
    if (!btn) return;
    window.addEventListener('scroll', function () {
      btn.style.display = window.scrollY > 400 ? 'block' : 'none';
    });
    btn.addEventListener('click', function () {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }

  function initLoginTabs() {
    var tabs = document.querySelectorAll('.login-tabs [data-tab]');
    if (!tabs.length) return;
    tabs.forEach(function (tab) {
      tab.addEventListener('click', function () {
        var name = tab.getAttribute('data-tab');
        document.querySelectorAll('.login-tabs [data-tab]').forEach(function (t) {
          t.classList.toggle('active', t.getAttribute('data-tab') === name);
        });
        document.querySelectorAll('.login-pane').forEach(function (pane) {
          pane.classList.toggle('active', pane.getAttribute('data-pane') === name);
        });
      });
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    initTheme();
    initMobileMenu();
    initUserChip();
    initBackTop();
    initLoginTabs();
  });
})();

/** 从 meta 读取 CSRF */
function getCsrf() {
  var tokenMeta = document.querySelector('meta[name="_csrf"]');
  var headerMeta = document.querySelector('meta[name="_csrf_header"]');
  return {
    token: tokenMeta ? tokenMeta.getAttribute('content') : '',
    header: headerMeta ? headerMeta.getAttribute('content') : 'X-CSRF-TOKEN'
  };
}

/** 带 CSRF 的 JSON POST */
async function postJson(url, body) {
  var csrf = getCsrf();
  var headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };
  if (csrf.token) {
    headers[csrf.header] = csrf.token;
  }
  var res = await fetch(url, {
    method: 'POST',
    headers: headers,
    credentials: 'same-origin',
    body: body == null ? null : JSON.stringify(body)
  });
  if (res.status === 401) {
    window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
    return null;
  }
  return res.json();
}

/** 带 CSRF 的空 body POST（点赞/收藏） */
async function postAction(url) {
  var csrf = getCsrf();
  var headers = { 'Accept': 'application/json' };
  if (csrf.token) {
    headers[csrf.header] = csrf.token;
  }
  var res = await fetch(url, {
    method: 'POST',
    headers: headers,
    credentials: 'same-origin'
  });
  if (res.status === 401) {
    window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname + window.location.hash);
    return null;
  }
  return res.json();
}
