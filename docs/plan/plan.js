/* توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com */
(function () {
  'use strict';

  var KEY = 'yd-plan-feedback-v1';
  var OLD_NOTES = 'yousef-plan-notes-v1';
  var OLD_SUGGESTIONS = 'yousef-plan-suggestions-v1';
  var PLACES = ['الواجهة', 'التطبيق', 'البيانات', 'الخادم والحساب', 'أدوات الجهاز', 'أخرى'];

  var form = document.getElementById('feedbackForm');
  if (!form) return;

  var kind = document.getElementById('kind');
  var suggestionFields = document.getElementById('suggestionFields');
  var noteFields = document.getElementById('noteFields');
  var place = document.getElementById('place');
  var list = document.getElementById('list');
  var status = document.getElementById('status');
  var fallback = document.getElementById('fallback');
  var count = document.getElementById('count');

  function read(key) {
    try {
      var value = JSON.parse(localStorage.getItem(key));
      return Array.isArray(value) ? value : [];
    } catch (error) {
      return [];
    }
  }

  function save(items) {
    try { localStorage.setItem(KEY, JSON.stringify(items)); } catch (error) {}
  }

  function oldPlace(section) {
    if (!section) return 'أخرى';
    if (['stats', 'tools', 'account'].indexOf(section) >= 0) return 'الواجهة';
    if (section === 'device') return 'أدوات الجهاز';
    if (['server', 'updates', 'general'].indexOf(section) >= 0) return 'الخادم والحساب';
    return 'التطبيق';
  }

  function migrate() {
    var items = read(KEY);
    var notes = read(OLD_NOTES);
    var suggestions = read(OLD_SUGGESTIONS);
    var migrated = [];

    notes.forEach(function (note) {
      migrated.push({
        id: 'note-' + (note.id || Date.now() + Math.random()),
        kind: 'note',
        place: oldPlace(note.section),
        text: String(note.text || ''),
        at: Number(note.at) || Date.now()
      });
    });
    suggestions.forEach(function (suggestion) {
      migrated.push({
        id: 'sug-' + (suggestion.id || Date.now() + Math.random()),
        kind: 'suggestion',
        title: String(suggestion.title || 'بدون اسم'),
        text: String(suggestion.text || ''),
        at: Number(suggestion.at) || Date.now()
      });
    });
    if (migrated.length) {
      items = items.concat(migrated);
      save(items);
      try {
        localStorage.removeItem(OLD_NOTES);
        localStorage.removeItem(OLD_SUGGESTIONS);
      } catch (error) {}
    }
    return items;
  }

  var items = migrate().filter(function (item) {
    return item && (item.kind === 'suggestion' || item.kind === 'note');
  });

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function date(value) {
    try { return new Date(value).toLocaleString('ar'); } catch (error) { return ''; }
  }

  function card(item) {
    var isSuggestion = item.kind === 'suggestion';
    var badge = isSuggestion ? 'اقتراح' : 'ملاحظة';
    var title = isSuggestion ? escapeHtml(item.title || 'بدون اسم') : escapeHtml(item.place || 'أخرى');
    var body = escapeHtml(item.text || '');
    var head = isSuggestion ? '<h3>' + title + '</h3>' : '<h3>مكان المشكلة: ' + title + '</h3>';
    return '<article class="item">' +
      '<div class="meta"><span class="pill">' + badge + '</span><time>' + escapeHtml(date(item.at)) + '</time></div>' +
      head + '<p>' + body + '</p>' +
      '<button class="del" type="button" data-del="' + escapeHtml(item.id) + '">حذف</button>' +
      '</article>';
  }

  function render() {
    var sorted = items.slice().sort(function (a, b) { return b.at - a.at; });
    list.innerHTML = sorted.length ? sorted.map(card).join('') : '<div class="empty">لا توجد مدخلات بعد.</div>';
    var suggestions = items.filter(function (item) { return item.kind === 'suggestion'; }).length;
    count.textContent = '(' + items.length + ' كلها · ' + suggestions + ' اقتراح · ' + (items.length - suggestions) + ' ملاحظة)';
  }

  function buildText() {
    var out = ['اقتراحات وملاحظات — ' + new Date().toLocaleDateString('ar'), ''];
    items.slice().sort(function (a, b) { return b.at - a.at; }).forEach(function (item) {
      if (item.kind === 'suggestion') {
        out.push('[اقتراح] ' + (item.title || 'بدون اسم'));
        out.push(item.text || '');
      } else {
        out.push('[ملاحظة] مكان المشكلة: ' + (item.place || 'أخرى'));
        out.push(item.text || '');
      }
      out.push('');
    });
    return out.join('\n').trim() + '\n';
  }

  function setKind() {
    var isSuggestion = kind.value === 'suggestion';
    suggestionFields.hidden = !isSuggestion;
    noteFields.hidden = isSuggestion;
  }

  function setMessage(text) {
    status.textContent = text;
  }

  place.innerHTML = PLACES.map(function (name) {
    return '<option value="' + escapeHtml(name) + '">' + escapeHtml(name) + '</option>';
  }).join('');

  form.addEventListener('submit', function (event) {
    event.preventDefault();
    var isSuggestion = kind.value === 'suggestion';
    var item = {
      id: String(Date.now()) + Math.random().toString(16).slice(2, 7),
      kind: kind.value,
      at: Date.now()
    };
    if (isSuggestion) {
      item.title = document.getElementById('title').value.trim();
      item.text = document.getElementById('description').value.trim();
      if (!item.title || !item.text) { setMessage('اكتب الاسم والوصف.'); return; }
      document.getElementById('title').value = '';
      document.getElementById('description').value = '';
    } else {
      item.place = place.value;
      item.text = document.getElementById('noteText').value.trim();
      if (!item.text) { setMessage('اكتب الملاحظة.'); return; }
      document.getElementById('noteText').value = '';
    }
    items.push(item);
    save(items);
    setMessage(isSuggestion ? 'أُضيف الاقتراح.' : 'أُضيفت الملاحظة.');
    render();
  });

  kind.addEventListener('change', setKind);

  document.getElementById('copyAll').addEventListener('click', function () {
    if (!items.length) { setMessage('لا توجد مدخلات بعد.'); return; }
    var text = buildText();
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(function () {
        setMessage('تم النسخ.');
      }).catch(function () {
        fallback.hidden = false;
        fallback.value = text;
        fallback.focus();
        fallback.select();
        setMessage('حدد النص وانسخه يدوياً.');
      });
    } else {
      fallback.hidden = false;
      fallback.value = text;
      fallback.focus();
      fallback.select();
      setMessage('حدد النص وانسخه يدوياً.');
    }
  });

  list.addEventListener('click', function (event) {
    var button = event.target.closest('[data-del]');
    if (!button) return;
    var id = button.getAttribute('data-del');
    items = items.filter(function (item) { return String(item.id) !== String(id); });
    save(items);
    render();
    setMessage('تم الحذف.');
  });

  setKind();
  render();
})();
/* توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com */
