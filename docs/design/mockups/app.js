/* Y-Dimension mockups — طبقة السلوك فقط. لا تُنسخ إلى Compose. */
(function () {
  "use strict";

  const ICONS = {
    home: '<path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1z"/>',
    check: '<path d="m5 12.5 4.2 4.2L19 7"/>',
    "check-square": '<rect x="4" y="4" width="16" height="16" rx="4"/><path d="m8 12.5 2.8 2.8L16.5 9"/>',
    wallet: '<path d="M4 8.5A2.5 2.5 0 0 1 6.5 6H19a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1H6.5A2.5 2.5 0 0 1 4 16.5z"/><path d="M4 9h16"/><path d="M16 13h2.5"/>',
    grid: '<rect x="4" y="4" width="6" height="6" rx="1.6"/><rect x="14" y="4" width="6" height="6" rx="1.6"/><rect x="4" y="14" width="6" height="6" rx="1.6"/><rect x="14" y="14" width="6" height="6" rx="1.6"/>',
    user: '<circle cx="12" cy="8" r="3.4"/><path d="M5 20c.6-3.5 3.3-5.4 7-5.4s6.4 1.9 7 5.4"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    search: '<circle cx="11" cy="11" r="6.5"/><path d="m16 16 4 4"/>',
    bell: '<path d="M6.5 10a5.5 5.5 0 0 1 11 0c0 4 1.5 5.5 1.5 5.5H5S6.5 14 6.5 10z"/><path d="M10 19a2 2 0 0 0 4 0"/>',
    settings: '<circle cx="12" cy="12" r="3"/><path d="M19.4 13.5a7.6 7.6 0 0 0 0-3l2-1.4-2-3.4-2.3.9a7.6 7.6 0 0 0-2.6-1.5L14 2h-4l-.5 2.1a7.6 7.6 0 0 0-2.6 1.5l-2.3-.9-2 3.4 2 1.4a7.6 7.6 0 0 0 0 3l-2 1.4 2 3.4 2.3-.9a7.6 7.6 0 0 0 2.6 1.5L10 22h4l.5-2.1a7.6 7.6 0 0 0 2.6-1.5l2.3.9 2-3.4z"/>',
    "chevron-left": '<path d="m14 6-6 6 6 6"/>',
    "chevron-right": '<path d="m10 6 6 6-6 6"/>',
    "chevron-down": '<path d="m6 10 6 6 6-6"/>',
    moon: '<path d="M20 15.2A8.5 8.5 0 0 1 8.8 4 8.5 8.5 0 1 0 20 15.2z"/>',
    sun: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M19.1 4.9l-1.4 1.4M6.3 17.7l-1.4 1.4"/>',
    droplet: '<path d="M12 3.5S6 10 6 14a6 6 0 0 0 12 0c0-4-6-10.5-6-10.5z"/>',
    bed: '<path d="M4 19v-9M4 14h16v5M8 11h4a3 3 0 0 1 3 3H8z"/><path d="M20 19v-4"/>',
    star: '<path d="m12 3.8 2.5 5.1 5.6.8-4 4 .9 5.6-5-2.7-5 2.7.9-5.6-4-4 5.6-.8z"/>',
    book: '<path d="M5 5.5A2.5 2.5 0 0 1 7.5 3H19v16H7.5A2.5 2.5 0 0 0 5 21.5z"/><path d="M5 6v13"/><path d="M9 7h6"/>',
    trophy: '<path d="M8 4h8v4a4 4 0 0 1-8 0z"/><path d="M8 6H5a3 3 0 0 0 3 4M16 6h3a3 3 0 0 1-3 4"/><path d="M12 12v4M9 20h6M10 16h4"/>',
    chart: '<path d="M4 20V4"/><path d="M4 20h16"/><path d="M8 16v-4M12 16V8M16 16v-2"/>',
    lock: '<rect x="5" y="10" width="14" height="10" rx="2.5"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/><path d="M12 14v2"/>',
    cloud: '<path d="M7 18h10a4 4 0 0 0 .4-8A6 6 0 0 0 6 11.5 3.4 3.4 0 0 0 7 18z"/>',
    download: '<path d="M12 4v10M8 10l4 4 4-4"/><path d="M5 19h14"/>',
    upload: '<path d="M12 15V5M8 9l4-4 4 4"/><path d="M5 19h14"/>',
    calendar: '<rect x="4" y="5" width="16" height="16" rx="3"/><path d="M8 3v4M16 3v4M4 10h16"/>',
    clock: '<circle cx="12" cy="12" r="8.5"/><path d="M12 7v5l3 2"/>',
    more: '<circle cx="6" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="18" cy="12" r="1"/>',
    x: '<path d="m6 6 12 12M18 6 6 18"/>',
    "arrow-left": '<path d="M20 12H5"/><path d="m11 6-6 6 6 6"/>',
    "arrow-right": '<path d="M4 12h15"/><path d="m13 6 6 6-6 6"/>',
    heart: '<path d="M12 20s-7-4.3-7-9.2A4 4 0 0 1 12 8a4 4 0 0 1 7 2.8C19 15.7 12 20 12 20z"/>',
    target: '<circle cx="12" cy="12" r="8.5"/><circle cx="12" cy="12" r="4"/><circle cx="12" cy="12" r="1"/>',
    flame: '<path d="M12 21c4 0 6-2.6 6-6 0-4.5-4-6.5-4-10-3 1.5-4 4-4 6-1.5-1-2-2.5-2-4-2 2-2 4.5-2 6 0 4.5 2.5 8 6 8z"/>',
    coins: '<ellipse cx="9" cy="7" rx="5" ry="2.5"/><path d="M4 7v4c0 1.4 2.2 2.5 5 2.5s5-1.1 5-2.5V7"/><path d="M4 11v4c0 1.4 2.2 2.5 5 2.5"/><ellipse cx="16" cy="16" rx="4" ry="2"/><path d="M12 16v3c0 1.1 1.8 2 4 2s4-.9 4-2v-3"/>',
    bag: '<path d="M5 8h14l-1 12H6z"/><path d="M9 8V6a3 3 0 0 1 6 0v2"/>',
    message: '<path d="M20 12a7 7 0 0 1-7 7H8l-4 3v-7a7 7 0 0 1 7-7h2a7 7 0 0 1 7 4z"/>',
    shield: '<path d="M12 3 20 6v6c0 4.5-3.4 7.8-8 9-4.6-1.2-8-4.5-8-9V6z"/><path d="m9 12 2 2 4-4"/>',
    sparkles: '<path d="m12 3 1.6 4.4L18 9l-4.4 1.6L12 15l-1.6-4.4L6 9l4.4-1.6z"/><path d="m19 15 .8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8z"/>',
    crown: '<path d="m4 8 4 3 4-6 4 6 4-3-2 10H6z"/><path d="M6 21h12"/>',
    play: '<path d="m9 6 9 6-9 6z"/>',
    dumbbell: '<path d="M7 8v8M4 10v4M17 8v8M20 10v4M7 12h10"/>',
    brain: '<path d="M9 5a3 3 0 0 0-3 3 3 3 0 0 0-1 5.8A3 3 0 0 0 9 19h3V5z"/><path d="M15 5a3 3 0 0 1 3 3 3 3 0 0 1 1 5.8A3 3 0 0 1 15 19h-3V5z"/>',
    notebook: '<rect x="5" y="3" width="14" height="18" rx="2.5"/><path d="M9 3v18M12 8h4M12 12h4"/>',
    flag: '<path d="M6 21V4"/><path d="M6 4h11l-2.5 4L17 12H6"/>',
    users: '<circle cx="9" cy="8" r="3"/><path d="M3 20c.5-3.4 3-5.2 6-5.2s5.5 1.8 6 5.2"/><path d="M16 5.5a3 3 0 0 1 0 5.8M18 20c-.2-2.2-.8-3.8-2-4.8"/>',
    refresh: '<path d="M20 11a8 8 0 1 0-2.3 6"/><path d="M20 5v6h-6"/>',
    filter: '<path d="M4 6h16M7 12h10M10 18h4"/>',
    edit: '<path d="M4 20h4l11-11-4-4L4 16z"/><path d="m13 6 4 4"/>',
    trash: '<path d="M5 7h14M10 7V4h4v3M7 7l1 13h8l1-13"/>',
    gem: '<path d="m6 4 12 0 3 5-9 11L3 9z"/><path d="M3 9h18M9 4 7 9l5 11 5-11-2-5"/>',
    gift: '<rect x="4" y="9" width="16" height="11" rx="2"/><path d="M3 9h18v3H3zM12 9v11"/><path d="M12 9C9 9 7 7.8 7 6a2 2 0 0 1 4-.6A2 2 0 0 1 17 6c0 1.8-2 3-5 3z"/>',
    compass: '<circle cx="12" cy="12" r="8.5"/><path d="m15.5 8.5-2 5-5 2 2-5z"/>',
    map: '<path d="m4 6 5-2 6 2 5-2v14l-5 2-6-2-5 2z"/><path d="M9 4v14M15 6v14"/>',
    "book-open": '<path d="M12 6C10 4.5 7 4 4 5v14c3-1 6-.5 8 1 2-1.5 5-2 8-1V5c-3-1-6-.5-8 1z"/><path d="M12 6v14"/>',
    mosque: '<path d="M5 20v-7a7 7 0 0 1 14 0v7"/><path d="M12 6c-2 2-3 3.5-3 5h6c0-1.5-1-3-3-5z"/><path d="M3 20h18M10 20v-4h4v4"/>',
    "cloud-off": '<path d="M7 18h10a4 4 0 0 0 2.3-7.2M6 11.5A3.4 3.4 0 0 0 7 18"/><path d="m4 4 16 16"/>'
  };

  const $ = (sel, root) => (root || document).querySelector(sel);
  const $$ = (sel, root) => Array.from((root || document).querySelectorAll(sel));

  function mountIcons(root) {
    $$("[data-icon]", root).forEach((el) => {
      const name = el.getAttribute("data-icon");
      if (el.firstElementChild && el.firstElementChild.tagName === "svg") return;
      el.innerHTML =
        '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
        (ICONS[name] || ICONS.sparkles) +
        "</svg>";
    });
  }

  const root = document.documentElement;
  const store = {
    get(k, d) { try { return localStorage.getItem(k) || d; } catch (e) { return d; } },
    set(k, v) { try { localStorage.setItem(k, v); } catch (e) {} }
  };

  function applyTheme(theme) {
    root.setAttribute("data-theme", theme);
    $$("[data-set-theme]").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.setTheme === theme)));
    $$("[data-theme-toggle]").forEach((b) => b.setAttribute("aria-label", theme === "dark" ? "الوضع الفاتح" : "الوضع الداكن"));
    store.set("yd-theme", theme);
  }

  function applyMotion(level) {
    root.setAttribute("data-motion", level);
    $$("[data-set-motion]").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.setMotion === level)));
    store.set("yd-motion", level);
  }

  function applyFont(scale) {
    root.style.setProperty("--fs-scale", scale);
    $$("[data-set-font]").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.setFont === scale)));
    store.set("yd-font", scale);
  }

  function toast(text, action) {
    const el = $(".toast");
    if (!el) return;
    $(".toast-text", el).textContent = text;
    const btn = $("button", el);
    if (btn) {
      btn.textContent = action || "تراجع";
      btn.hidden = !action;
    }
    el.classList.add("show");
    clearTimeout(el._t);
    el._t = setTimeout(() => el.classList.remove("show"), 2600);
  }

  function animateCounters() {
    if (root.getAttribute("data-motion") === "none") {
      $$("[data-count]").forEach((el) => { el.textContent = Number(el.dataset.count).toLocaleString("en-US"); });
      return;
    }
    $$("[data-count]").forEach((el) => {
      const target = Number(el.dataset.count);
      if (!Number.isFinite(target)) return;
      const start = performance.now();
      const dur = 900;
      function tick(now) {
        const t = Math.min(1, (now - start) / dur);
        const eased = 1 - Math.pow(1 - t, 3);
        el.textContent = Math.round(target * eased).toLocaleString("en-US");
        if (t < 1) requestAnimationFrame(tick);
      }
      requestAnimationFrame(tick);
    });
  }

  function setupSheet() {
    const scrim = $(".scrim");
    const sheet = $(".sheet");
    if (!scrim || !sheet) return;
    const open = () => { scrim.classList.add("open"); sheet.classList.add("open"); sheet.setAttribute("aria-hidden", "false"); };
    const close = () => { scrim.classList.remove("open"); sheet.classList.remove("open"); sheet.setAttribute("aria-hidden", "true"); };
    $$("[data-open-sheet]").forEach((b) => b.addEventListener("click", open));
    $$("[data-close-sheet]").forEach((b) => b.addEventListener("click", close));
    scrim.addEventListener("click", close);
    document.addEventListener("keydown", (e) => { if (e.key === "Escape") close(); });
    $$("[data-sheet-action]").forEach((b) =>
      b.addEventListener("click", () => {
        close();
        toast(b.dataset.sheetAction + " — أُضيفت");
      })
    );
  }

  function setupTasks() {
    $$(".task-check").forEach((btn) => {
      btn.addEventListener("click", (e) => {
        e.stopPropagation();
        const task = btn.closest(".task");
        if (!task) return;
        task.classList.toggle("done");
        btn.setAttribute("aria-pressed", String(task.classList.contains("done")));
        if (task.classList.contains("done")) toast("أُنجزت المهمة · +15 XP", "تراجع");
      });
    });
  }


  const BOOKS = {
    tasks: {
      title: "كتاب المهام", desc: "تنظيم اليوم، الأولويات، والمصفوفة.",
      start: "#143A2E", end: "#081A14", glow: "#5CD39B", icon: "check-square",
      chapters: [
        { icon: "check-square", label: "المهام اليومية", href: "tasks.html" },
        { icon: "calendar", label: "قادم ومتأخر", href: "tasks.html" },
        { icon: "target", label: "مصفوفة أيزنهاور", href: "states.html" }
      ]
    },
    money: {
      title: "كتاب المال", desc: "الرصيد، الفئات، والحركات.",
      start: "#0E3B38", end: "#061C1B", glow: "#6FE3D5", icon: "wallet",
      chapters: [
        { icon: "wallet", label: "سجل الحركات", href: "money.html" },
        { icon: "chart", label: "الميزانية والتقرير", href: "money.html" },
        { icon: "coins", label: "المحافظ والعملات", href: "states.html" }
      ]
    },
    health: {
      title: "كتاب الحياة", desc: "الماء، النوم، والتمارين.",
      start: "#4A231A", end: "#26100A", glow: "#F0B08A", icon: "droplet",
      chapters: [
        { icon: "droplet", label: "الماء", href: "tool-water.html" },
        { icon: "bed", label: "النوم", href: "states.html" },
        { icon: "dumbbell", label: "التمارين", href: "stats.html" }
      ]
    },
    faith: {
      title: "كتاب الإيمان", desc: "الأذكار، الصلوات، والقبلة.",
      start: "#3A2C80", end: "#1C123F", glow: "#E8C06A", icon: "mosque",
      chapters: [
        { icon: "mosque", label: "صلوات اليوم", href: "states.html" },
        { icon: "book-open", label: "حصن المسلم", href: "states.html" },
        { icon: "compass", label: "القبلة", href: "states.html" }
      ]
    },
    library: {
      title: "كتاب المكتبة", desc: "٢٢ كتاباً في الحياة والتنمية.",
      start: "#1B2350", end: "#0A0F26", glow: "#A8B4FF", icon: "book-open",
      chapters: [
        { icon: "book", label: "كتبي", href: "states.html" },
        { icon: "plus", label: "إضافة كتاب", href: "states.html" },
        { icon: "notebook", label: "ملاحظات القراءة", href: "states.html" }
      ]
    },
    learn: {
      title: "كتاب التعلم", desc: "مهارات، خطط، ومسارات.",
      start: "#32204F", end: "#170E2A", glow: "#C9B0F2", icon: "brain",
      chapters: [
        { icon: "brain", label: "خطة التعلم", href: "states.html" },
        { icon: "target", label: "الأهداف", href: "states.html" },
        { icon: "chart", label: "التقدم", href: "stats.html" }
      ]
    },
    journal: {
      title: "كتاب المذكرات", desc: "يومياتك ومزاجك.",
      start: "#3B2A18", end: "#20140A", glow: "#E8C06A", icon: "notebook",
      chapters: [
        { icon: "notebook", label: "مذكرات اليوم", href: "states.html" },
        { icon: "heart", label: "المزاج", href: "states.html" }
      ]
    },
    forum: {
      title: "كتاب المنتدى", desc: "مجتمع السحرة وأسئلتهم.",
      start: "#123836", end: "#071D1C", glow: "#6FE3D5", icon: "users",
      chapters: [
        { icon: "message", label: "المواضيع", href: "forum.html" },
        { icon: "plus", label: "موضوع جديد", href: "forum.html" }
      ]
    },
    device: {
      title: "كتاب الحاجب", desc: "التطبيقات، الحدود، والصلاحيات.",
      start: "#1E2A45", end: "#0D1422", glow: "#A8B4FF", icon: "shield",
      chapters: [
        { icon: "shield", label: "حاجب التطبيقات", href: "states.html" },
        { icon: "clock", label: "حدود الوقت", href: "states.html" },
        { icon: "lock", label: "الخصوصية", href: "states.html" }
      ]
    },
    data: {
      title: "كتاب البيانات", desc: "الاستهلاك والسرعة والذاكرة.",
      start: "#26303B", end: "#10161C", glow: "#9FB4C9", icon: "chart",
      chapters: [
        { icon: "chart", label: "استهلاك البيانات", href: "stats.html" },
        { icon: "refresh", label: "اختبار السرعة", href: "states.html" }
      ]
    },
    privacy: {
      title: "كتاب الخصوصية", desc: "الصلاحيات وما يُشارك.",
      start: "#2A1A33", end: "#120A18", glow: "#C9B0F2", icon: "lock",
      chapters: [
        { icon: "lock", label: "الصلاحيات", href: "states.html" },
        { icon: "shield", label: "المتجر الآمن", href: "store.html" }
      ]
    },
    cleaner: {
      title: "كتاب التنظيف", desc: "الملفات المؤقتة والذاكرة.",
      start: "#23303A", end: "#0F171C", glow: "#9FB4C9", icon: "trash",
      chapters: [
        { icon: "trash", label: "تنظيف سريع", href: "states.html" },
        { icon: "chart", label: "تفاصيل التخزين", href: "stats.html" }
      ]
    },
    path: {
      title: "كتاب المسار", desc: "الرتب، الحملات، ورحلتك.",
      start: "#3A2C1A", end: "#1C1409", glow: "#E8C06A", icon: "compass",
      chapters: [
        { icon: "compass", label: "خريطة الرتب", href: "achievements.html" },
        { icon: "target", label: "الحملات", href: "achievements.html" }
      ]
    },
    achievements: {
      title: "كتاب الإنجازات", desc: "الأوسمة، XP، والاحتفال.",
      start: "#33235F", end: "#180F32", glow: "#E8C06A", icon: "trophy",
      chapters: [
        { icon: "trophy", label: "الأوسمة", href: "achievements.html" },
        { icon: "star", label: "XP والرتبة", href: "achievements.html" }
      ]
    },
    store: {
      title: "كتاب المتجر", desc: "سكنات وعناصر تُفتح بالعملات.",
      start: "#143A2E", end: "#081A14", glow: "#E8C06A", icon: "bag",
      chapters: [
        { icon: "bag", label: "المتجر", href: "store.html" },
        { icon: "sparkles", label: "السكنات", href: "store.html" }
      ]
    },
    raids: {
      title: "كتاب الغارات", desc: "تحديات ثابتة لا يكتبها المستخدم.",
      start: "#3F1A22", end: "#200A0F", glow: "#FF8A92", icon: "target",
      chapters: [
        { icon: "target", label: "الغارة الحالية", href: "achievements.html" },
        { icon: "trophy", label: "سجل الغارات", href: "achievements.html" }
      ]
    }
  };

  function setupGrimoires() {
    const sheet = $("#grimoire-sheet");
    if (!sheet) return;
    const scrim = $(".scrim");
    const title = $("[data-book-title]", sheet);
    const desc = $("[data-book-desc]", sheet);
    const cover = $("[data-book-cover]", sheet);
    const list = $("[data-book-chapters]", sheet);
    function open(key) {
      const book = BOOKS[key];
      if (!book) return;
      title.textContent = book.title;
      desc.textContent = book.desc;
      cover.style.setProperty("--book-start", book.start);
      cover.style.setProperty("--book-end", book.end);
      cover.style.setProperty("--book-glow", book.glow);
      cover.innerHTML = '<span class="icon" data-icon="' + book.icon + '"></span>';
      list.innerHTML = book.chapters.map((c) =>
        '<a class="chapter" href="' + c.href + '"><span class="icon sm" data-icon="' + c.icon + '"></span>' +
        '<span>' + c.label + '</span><span class="icon sm chev" data-icon="chevron-left"></span></a>'
      ).join("");
      mountIcons(sheet);
      scrim.classList.add("open");
      sheet.classList.add("open");
      sheet.setAttribute("aria-hidden", "false");
    }
    $$("[data-grimoire]").forEach((b) => b.addEventListener("click", () => open(b.dataset.grimoire)));
  }

  function setupFilters() {
    $$("[data-filter-input]").forEach((input) => {
      input.addEventListener("input", () => {
        const q = input.value.trim().toLowerCase();
        $$("[data-search]").forEach((el) => {
          el.hidden = q.length > 0 && !el.textContent.toLowerCase().includes(q);
        });
      });
    });
    $$(".chiprow").forEach((row) => {
      row.addEventListener("click", (e) => {
        const chip = e.target.closest(".chip");
        if (!chip) return;
        $$(".chip", row).forEach((c) => c.setAttribute("aria-pressed", "false"));
        chip.setAttribute("aria-pressed", "true");
      });
    });
  }

  function setupAudio() {
    let audio = null;
    $$("[data-audio]").forEach((b) => {
      b.addEventListener("click", () => {
        if (!audio) audio = new Audio();
        audio.pause();
        audio.src = b.dataset.audio;
        audio.currentTime = 0;
        audio.play().catch(() => toast("تعذّر تشغيل الصوت"));
        toast("صوت نور يعمل");
      });
    });
  }

  function setupConfetti() {
    const box = $(".confetti");
    if (!box || root.getAttribute("data-motion") === "none") return;
    const colors = ["#E0A92E", "#B8A6FF", "#6C4CF1", "#5CD39B", "#FFA8C5"];
    for (let i = 0; i < 22; i++) {
      const p = document.createElement("i");
      p.style.setProperty("--l", Math.random() * 100 + "%");
      p.style.setProperty("--x", (Math.random() * 120 - 60) + "px");
      p.style.setProperty("--r", Math.round(Math.random() * 540) + "deg");
      p.style.setProperty("--d", (Math.random() * 0.5).toFixed(2) + "s");
      p.style.background = colors[i % colors.length];
      box.appendChild(p);
    }
    setTimeout(() => { box.innerHTML = ""; }, 2600);
  }

  function setupReveal() {
    if (!("IntersectionObserver" in window)) { $$(".reveal").forEach((el) => el.classList.add("in")); return; }
    const io = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) { entry.target.classList.add("in"); io.unobserve(entry.target); }
      });
    }, { threshold: 0.15 });
    $$(".reveal").forEach((el) => io.observe(el));
  }

  function setupMisc() {
    $$("[data-toast]").forEach((b) => b.addEventListener("click", () => toast(b.dataset.toast)));
    $$("[data-toast-undo]").forEach((b) => b.addEventListener("click", () => toast(b.dataset.toastUndo, "تراجع")));
    $$("[data-set-theme]").forEach((b) => b.addEventListener("click", () => applyTheme(b.dataset.setTheme)));
    $$("[data-theme-toggle]").forEach((b) =>
      b.addEventListener("click", () => applyTheme(root.getAttribute("data-theme") === "dark" ? "light" : "dark"))
    );
    $$("[data-set-motion]").forEach((b) => b.addEventListener("click", () => applyMotion(b.dataset.setMotion)));
    $$("[data-set-font]").forEach((b) => b.addEventListener("click", () => applyFont(b.dataset.setFont)));
    $$("[data-undo]").forEach((b) => b.addEventListener("click", () => toast("تم التراجع")));
  }

  function contrast(a, b) {
    function lum(hex) {
      const c = hex.replace("#", "");
      const rgb = [0, 2, 4].map((i) => parseInt(c.slice(i, i + 2), 16) / 255).map((v) => (v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4)));
      return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
    }
    const x = Math.max(lum(a), lum(b));
    const y = Math.min(lum(a), lum(b));
    return (x + 0.05) / (y + 0.05);
  }

  function setupSwatches() {
    $$("[data-ratio]").forEach((el) => {
      const fg = el.dataset.fg;
      const bg = el.dataset.bg;
      if (!fg || !bg) return;
      const r = contrast(fg, bg);
      el.textContent = r.toFixed(1) + ":1";
      el.classList.toggle("fail", r < 4.5);
    });
  }

  function setupLab() {
    const lab = $("[data-lab]");
    if (!lab) return;
    const title = document.body.dataset.title || "Y-Dimension";
    const sub = document.body.dataset.sub || "معاينة واجهة · 412×915 · RTL";
    lab.innerHTML =
      '<div class="lab-head">' +
      '<div><h1 class="lab-title">' + title + '</h1><p class="lab-sub">' + sub + "</p></div>" +
      '<div class="lab-spacer"></div>' +
      '<div class="lab-tools">' +
      '<div class="seg" role="group" aria-label="الوضع"><button data-set-theme="light">فاتح</button><button data-set-theme="dark">داكن</button></div>' +
      '<div class="seg" role="group" aria-label="مستوى الحركة"><button data-set-motion="full">حركة كاملة</button><button data-set-motion="light">خفيف</button><button data-set-motion="none">بدون</button></div>' +
      '<div class="seg" role="group" aria-label="حجم الخط"><button data-set-font="1">100%</button><button data-set-font="1.3">130%</button></div>' +
      '<a class="lab-link" href="index.html">الفهرس</a>' +
      "</div></div>";
  }

  function init() {
    setupLab();
    mountIcons();
    const savedTheme = store.get("yd-theme", "");
    applyTheme(savedTheme || (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light"));
    applyMotion(store.get("yd-motion", "full"));
    applyFont(store.get("yd-font", "1"));
    setupMisc();
    setupSheet();
    setupGrimoires();
    setupTasks();
    setupAudio();
    setupFilters();
    setupReveal();
    setupSwatches();
    animateCounters();
    if (document.body.dataset.confetti === "true") setupConfetti();
  }

  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", init);
  else init();
})();
